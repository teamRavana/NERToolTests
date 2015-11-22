/**
 *  Copyright (c) 2015 Raavana
 */
package Stanford;

import edu.stanford.nlp.ie.crf.CRFClassifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StanfordClassifierHandler {
	public static final String RESOURCES = "resources";
	public static final String CLASSIFIERS = "classifiers";

	public static void main(String[] args) throws IOException {
		StanfordClassifierHandler demo = new StanfordClassifierHandler();
		demo.train("SinhalaTest.prop", "train-cleaned.tsv");
	}

	/**
	 * @param propFile  : Name of the Properties file (should be located inside resources directory)
	 * @param trainFile : Name of the Training data file (should be located inside resources directory)
	 *                  Output file will be created on classifiers directory
	 */
	public void train(String propFile, String trainFile) {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(RESOURCES + "/" + propFile);
			if (input == null) {
				System.out.println("Sorry, unable to find " + propFile);
				return;
			}

			//load a properties file from class path, inside static method
			prop.load(input);

			CRFClassifier classifier = new CRFClassifier(prop);
			classifier.train(RESOURCES + "/" + trainFile);
			classifier.serializeClassifier(CLASSIFIERS + "/" + "Stanford-crf-serialized.ser.gz");

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
}
