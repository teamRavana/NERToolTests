/**
 * Copyright (c) 2015 Raavana
 */
package Stanford;
import edu.stanford.nlp.ie.NERFeatureFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

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

public class StanfordClassifierHandler extends CRFClassifier<CoreMap>{
	public static final String RESOURCES = "resources";
	public static final String CLASSIFIERS = "classifiers";

	public static void main(String[] args) throws IOException {
		StanfordClassifierHandler demo = new StanfordClassifierHandler();
		// Training a classifier
//		 demo.train("SinhalaTest.prop",
//		 "train_21_Nov.tsv","Stanford-crf-serialized-new.ser.gz");
//
//		//Testing a classifier
//		demo.test("test_21_Nov.tsv", "Stanford-crf-serialized-new.ser.gz");
		
		//Advanced 
		demo.testAdvanced("test_21_Nov.tsv", "Stanford-crf-serialized-new.ser.gz");
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

		Properties props = new Properties();
		props.put("loadClassifier", CLASSIFIERS + "/" + classifierName);
		props.put("testFile", RESOURCES + "/" + testFile);

		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		CRFClassifier<CoreLabel> classifier = new CRFClassifier<CoreLabel>(flags);

		classifier.loadClassifierNoExceptions(CLASSIFIERS + "/" + classifierName, props);
		classifier.loadTagIndex();
		DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.defaultReaderAndWriter();

		try {
			classifier.classifyAndWriteAnswers(RESOURCES + "/" + testFile, readerAndWriter, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	//probabilities(testFile, classifier.);
		
		//================================
		 
		
	}
	
	public void probabilities(String testFile, DocumentReaderAndWriter<CoreMap> readerAndWriter){
		Counter<Integer> calibration = new ClassicCounter<>();
	    Counter<Integer> correctByBin = new ClassicCounter<>();
	    TwoDimensionalCounter<Integer,String> calibratedTokens = new TwoDimensionalCounter<>();

	    for (List<CoreMap> doc : makeObjectBankFromFile(RESOURCES + "/" + testFile, readerAndWriter)) {
	      Triple<Counter<Integer>, Counter<Integer>, TwoDimensionalCounter<Integer,String>> triple = printProbsDocument(doc);
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
}
