import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.stats.Counter;

/**
 * Give classifier name to CLASSIFIER_NAME
 * Put the input string to example_input
 * @author Raavana
 *
 */
public class InputProcessor {
	public static final String CLASSIFIERS = "classifiers";
	public static String TEMP_DIR = "output_temp_bin/";
	public static final String CLASSIFIER_NAME = "all_corpus_model.ser.gz";
	public static int NUM_OF_COLUMNS = 3;   //token, gold annotation, Answer
	public static String TEMP_FILE="temp_input.tsv";

	public static void main(String[] args) {
		InputProcessor engine = new InputProcessor();
		/************************** Input File Read ********************************************/
		StringBuilder input = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(TEMP_DIR+"example_input")))
		{
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				input.append(inputLine+"\n");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		/************************************************************************************/
		
		// Prepare the test file
		engine.prepairTestFile(input.toString());
		
		//Process the test file
		String s = engine.processInput(TEMP_DIR+TEMP_FILE,CLASSIFIER_NAME);
		
	}
	
	public void prepairTestFile(String inputText){
		String[] input = inputText.split("\\s+");
		
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(TEMP_DIR+TEMP_FILE),false)))) {
			for (int i=0;i<input.length;i++){
				out.println(input[i]+"\tO");
			}
		}catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
		/******************** Add Required Columns ****************************************/

		writeStartingWordsColumn();
		writeEndingWordsColumn();
		writeWordLengthColumn(6);
		/**********************************************************************************/
	}
	
	public String processInput(String testFilePath, String classifierName) {

		Properties props = new Properties();
		props.put("loadClassifier", CLASSIFIERS + "/" + classifierName);
		props.put("testFile", testFilePath);

		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		CRFClassifier<CoreLabel> classifier = new CRFClassifier<CoreLabel>(flags);

		classifier.loadClassifierNoExceptions(CLASSIFIERS + "/" + classifierName, props);
		classifier.loadTagIndex();
		DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.defaultReaderAndWriter();
		ObjectBank<List<CoreLabel>> documents =
				classifier.makeObjectBankFromFile(testFilePath, readerAndWriter);
//		OutputStream outputWrite = null;
//        try {
//	        outputWrite = new FileOutputStream("/home/eranda/JavaLab/NERTestTools/classifiers/"+"output-text.txt");
//        } catch (FileNotFoundException e1) {
//	        // TODO Auto-generated catch block
//	        e1.printStackTrace();
//        }

        OutputStream output = new ByteArrayOutputStream();
		try {
			classifier.classifyAndWriteAnswers(testFilePath, output, readerAndWriter, true);
			//classifier.classifyAndWriteAnswers(testFilePath, outputWrite, readerAndWriter, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String stanfordOut = output.toString();
		String[] line = stanfordOut.split("\\s+");
		StringBuilder finalOutput = new StringBuilder();
		for (int i=0;i<line.length;i+=NUM_OF_COLUMNS){
			if(!"O".equals(line[i+2])){
				finalOutput.append("<"+line[i+2]+">"+line[i]+"</"+line[i+2]+"> ");
			}
			else {
				finalOutput.append(line[i]+" ");
			}
			
			if(".".equals(line[i]))	finalOutput.append("\n");
		}
		System.out.println(finalOutput.toString());
		return finalOutput.toString();
	}
	
	/**
	 * Adding Extra Tags
	 */
	public void writeEndingWordsColumn(){

		//write to a file
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(TEMP_DIR,"EndWord_"+TEMP_FILE))))) {
			try (BufferedReader br = new BufferedReader(new FileReader(TEMP_DIR+TEMP_FILE)))
			{
				//rename FILENAME for next iteration
				TEMP_FILE="EndWord_"+TEMP_FILE;
				String sCurrentLine;
				String prevLine=br.readLine();
				String[] line;

				while ((sCurrentLine = br.readLine()) != null) {
					line = sCurrentLine.split("\\s+");

					if (line[0].equals(".")){
						out.println(prevLine+"\t"+"1");
					} else{
						out.println(prevLine+"\t"+"0");
					}
					prevLine = sCurrentLine;
				}
				out.println(prevLine+"\t"+"0");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}
	
	public void writeStartingWordsColumn(){

		//write to a file
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(TEMP_DIR,"StartWord_"+TEMP_FILE))))) {
			try (BufferedReader br = new BufferedReader(new FileReader(TEMP_DIR+TEMP_FILE)))
			{
				//rename FILENAME for next iteration
				TEMP_FILE="StartWord_"+TEMP_FILE;
				String sCurrentLine;
				String prevChar=".";
				String[] line;

				while ((sCurrentLine = br.readLine()) != null) {
					line = sCurrentLine.split("\\s+");
					//System.out.println(sCurrentLine);
					//System.out.println(line[0]+"\n"+line[1]);
					//System.out.println(line[1]);

					if (prevChar.equals(".")){
						out.println(sCurrentLine+"\t"+"1");
					} else{
						out.println(sCurrentLine+"\t"+"0");
					}
					prevChar=line[0];
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}
	
	public void writeWordLengthColumn(int maxWordLength){

		//write to a file
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(TEMP_DIR,"WordLength"+maxWordLength+"_"+TEMP_FILE))))) {
			try (BufferedReader br = new BufferedReader(new FileReader(TEMP_DIR+TEMP_FILE)))
			{

				//rename FILENAME for next iteration
				TEMP_FILE="WordLength"+maxWordLength+"_"+TEMP_FILE;
				String sCurrentLine;
				String[] line;

				while ((sCurrentLine = br.readLine()) != null) {
					line = sCurrentLine.split("\\s+");

					if (line[0].length()<=maxWordLength){
						out.println(sCurrentLine+"\t"+"0");
					} else{
						out.println(sCurrentLine+"\t"+"1");
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}

}
