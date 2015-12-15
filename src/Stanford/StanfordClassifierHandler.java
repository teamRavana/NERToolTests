/**
 * Copyright (c) 2015 Raavana
 */
package Stanford;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.stats.TwoDimensionalCounter;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;

public class StanfordClassifierHandler extends CRFClassifier<CoreMap> {
	public static final String RESOURCES = "resources";
	public static final String CLASSIFIERS = "classifiers";

	/*
	 * Used in processing the results
	 * Contains the entity tag names of the raws in final output
	 */
	public static String[] labelOrder;

	public static void main(String[] args) throws IOException {
		StanfordClassifierHandler demo = new StanfordClassifierHandler();
		// Training a classifier
		// demo.train("SinhalaTest.prop","train_21_Nov.tsv","Stanford-crf-serialized-new.ser.gz");

		// Testing a classifier
		demo.test("test_21_Nov.tsv", "Stanford-crf-serialized-new.ser.gz");

		// Advanced
		// demo.testAdvanced("test_21_Nov.tsv",
		// "Stanford-crf-serialized-new.ser.gz");

		// Classify
		// demo.classifySentence("Stanford-crf-serialized-new.ser.gz");
	}

	/**
	 * @param propFile
	 *            : Name of the Properties file (should be located inside
	 *            resources directory)
	 * @param trainFile
	 *            : Name of the Training data file (should be located inside
	 *            resources directory)
	 *            Output file will be created on classifiers directory
	 */
	public void train(String propFile, String trainFile, String serializedClassifierName) {
		Properties prop = new Properties();
		InputStream input = null;
		CRFClassifier classifier;

		try {
			input = new FileInputStream(RESOURCES + "/" + propFile);

			// load a properties file from class path, inside static method
			prop.load(input);

			classifier = new CRFClassifier(prop);
			classifier.train(RESOURCES + "/" + trainFile);
			classifier.serializeClassifier(CLASSIFIERS + "/" + serializedClassifierName);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param testFile
	 *            : Name of the Testing data file (should be located inside
	 *            resources directory)
	 * @param classifierName
	 *            : Name of the classifier (should be located inside classifiers
	 *            directory)
	 */
	public void test(String testFile, String classifierName) {

		// output results
		List<Counter<String>> results;
		// final percentages will have each entity in separate raw. Columns will
		// be P,R and F
		double[][] finalPercentages = null;

		Properties props = new Properties();
		props.put("loadClassifier", CLASSIFIERS + "/" + classifierName);
		props.put("testFile", RESOURCES + "/" + testFile);

		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		CRFClassifier<CoreLabel> classifier = new CRFClassifier<CoreLabel>(flags);

		classifier.loadClassifierNoExceptions(CLASSIFIERS + "/" + classifierName, props);
		classifier.loadTagIndex();
		DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.defaultReaderAndWriter();
		ObjectBank<List<CoreLabel>> documents =
				classifier.makeObjectBankFromFile(RESOURCES + "/" +
						testFile,
						readerAndWriter);

		try {
			// classifier.classifyAndWriteAnswers(RESOURCES + "/" + testFile,
			// readerAndWriter, true);
			// classifier.classifyAndWriteAnswers(RESOURCES + "/" + testFile,
			// System.out ,readerAndWriter, true);
			results = classifier.classifyReturnAndWriteAnswers(documents, readerAndWriter, true);
			// results[0] is TP,results[1] is FP,results[2] is FN
			finalPercentages = processResults(results.get(0), results.get(1), results.get(2));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Debug: print the final percentages
		if (finalPercentages != null) {
			int i=0;
			for (double[] finalPercentage : finalPercentages) {
				System.out.println(labelOrder[i++]+"\t: "+finalPercentage[0] + "\t" + finalPercentage[1] + "\t" +
						finalPercentage[2]);
			}
		}
	}

	public void testAdvanced(String testFile, String classifierName) {

		Properties props = new Properties();
		props.put("loadClassifier", CLASSIFIERS + "/" + classifierName);
		props.put("testFile", RESOURCES + "/" + testFile);

		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		CRFClassifier<CoreLabel> classifier = new CRFClassifier<CoreLabel>(flags);

		classifier.loadClassifierNoExceptions(CLASSIFIERS + "/" + classifierName, props);
		classifier.loadTagIndex();
		DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.defaultReaderAndWriter();

		classifier.printProbs(RESOURCES + "/" + testFile, readerAndWriter);
		// probabilities(testFile, classifier.);
	}

	public void probabilities(String testFile, DocumentReaderAndWriter<CoreMap> readerAndWriter) {
		Counter<Integer> calibration = new ClassicCounter<>();
		Counter<Integer> correctByBin = new ClassicCounter<>();
		TwoDimensionalCounter<Integer, String> calibratedTokens = new TwoDimensionalCounter<>();

		for (List<CoreMap> doc : makeObjectBankFromFile(RESOURCES + "/" + testFile, readerAndWriter)) {
			Triple<Counter<Integer>, Counter<Integer>, TwoDimensionalCounter<Integer, String>> triple =
					printProbsDocument(doc);
			if (triple != null) {
				Counters.addInPlace(calibration, triple.first());
				Counters.addInPlace(correctByBin, triple.second());
				calibratedTokens.addAll(triple.third());
			}
			System.out.println();
		}
		if (calibration.size() > 0) {
			// we stored stuff, so print it out
			PrintWriter pw = new PrintWriter(System.err);
			outputCalibrationInfo(pw, calibration, correctByBin, calibratedTokens);
			pw.flush();
		}
	}

	public void classifySentence(String classifierName) {

		Scanner keyboard = new Scanner(System.in);
		String input = null;
		Properties props = new Properties();
		props.put("tokenizerFactory", "edu.stanford.nlp.process.WhitespaceTokenizer");
		props.put("tokenizerOptions", "tokenizeNLs=true");

		String serializedClassifier = "classifiers/" + classifierName;
		AbstractSequenceClassifier classifier = null;
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier, props);
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Give the input sentence: ");
		input = keyboard.nextLine();
		System.out.println(classifier.classifyToString(input, "xml", true));
	}

	// Processing Results
	/**
	 *
	 * @param entityTP
	 * @param entityFP
	 * @param entityFN
	 * @return double 2D array.
	 *         Columns=Precision, Recall, F
	 *         Raws=Each tag
	 */
	private static double[][] processResults(Counter<String> entityTP, Counter<String> entityFP,
	                                         Counter<String> entityFN) {
		Set<String> entities = new TreeSet<String>();
		entities.addAll(entityTP.keySet());
		entities.addAll(entityFP.keySet());
		entities.addAll(entityFN.keySet());
		double[][] output = new double[entities.size() + 1][3];
		labelOrder = new String[entities.size() + 1];
		int i = 0;
		for (String entity : entities) {
			double tp = entityTP.getCount(entity);
			double fp = entityFP.getCount(entity);
			double fn = entityFN.getCount(entity);
			output[i] = processEach(tp, fp, fn);
			labelOrder[i++] = entity;
		}

		double tp = entityTP.totalCount();
		double fp = entityFP.totalCount();
		double fn = entityFN.totalCount();
		output[i] = processEach(tp, fp, fn);
		labelOrder[i] = "Totals";

		return output;
	}

	/**
	 * Process each of the entity type for Precision, Recall and F using TP, FP
	 * and FN
	 */
	private static double[] processEach(double tp, double fp, double fn) {

		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn);
		double f1 =
				precision == 0.0 || recall == 0.0 ? 0.0
				                                  : 2.0 / (1.0 / precision + 1.0 / recall);
		double[] finalValuesEntity = { precision, recall, f1 };
		return finalValuesEntity;
	}
}
