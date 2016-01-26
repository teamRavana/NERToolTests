import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

	public class DataStatisticsCalculater {
		private static String LOCATION = "resources/";
		private static String FILENAME = "all.tsv"; 

		public static void main(String[] args) {
			
			//(new DataStatisticsCalculater()).countAvgLength();
			//(new DataStatisticsCalculater()).countStartingWords();
			//(new DataStatisticsCalculater()).countEndingWords();
//			(new DataStatisticsCalculater()).wordFrequency();
			//(new DataStatisticsCalculater()).wordLengthToType();
			
			//File writes
			(new DataStatisticsCalculater()).writeStartingWordsColumn();
			(new DataStatisticsCalculater()).writeEndingWordsColumn();
			(new DataStatisticsCalculater()).writeWordLengthColumn(7);
//			(new DataStatisticsCalculater()).writeWordFrequencyColumn(10);

		}
		
		public void stringLength(){
			String s= "වරදකාරිය";
			for (int i = 0; i < s.length(); i++) {
	            System.out.println(i+"\t:"+s.substring(0, i));
            }
		}
		
		public void countAvgLength(){
			double[] count = {0,0};

			try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
			{

				String sCurrentLine;
				String[] line;
				int i=0,j=0;

				while ((sCurrentLine = br.readLine()) != null) {
					line = sCurrentLine.split("\\s+");
					System.out.println(sCurrentLine);
					//System.out.println(line[0]+"\n"+line[1]);
					//System.out.println(line[1]);
					if (line[1].equals("O")){
						i++;
						count[0]=(count[0]*(i-1)+line[0].length())/i;
					} else {
						j++;
						count[1]=(count[1]*(j-1)+line[0].length())/j;
					}
				}
				
				System.out.println(count[0]+"\n"+count[1]);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void countEndingWords(){
			int[] count = {0,0};

			try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
			{

				String sCurrentLine;
				String[] line;
				int i=0,j=0;
				String prevWord="start";
				String prevTag="O";
				int lineNo=0;

				while ((sCurrentLine = br.readLine()) != null) {
					line = sCurrentLine.split("\\s+");
					
					if (line[0].equals(".")){
						if (prevTag.equals("O"))	count[1]++;
						else{
							System.out.println(prevWord+"\t"+lineNo);
							count[0]++;
						}
					}
					lineNo++;
					prevTag=line[1];
					prevWord=line[0];
				}
				
				System.out.println(count[0]+"\n"+count[1]);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void countStartingWords(){
			int[] count = {0,0};
			int neCount=0;

			try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
			{

				String sCurrentLine;
				String[] line;
				int i=0,j=0;
				String prevChar=".";

				while ((sCurrentLine = br.readLine()) != null) {
					line = sCurrentLine.split("\\s+");
					System.out.println(sCurrentLine);
					//System.out.println(line[0]+"\n"+line[1]);
					//System.out.println(line[1]);
					if (prevChar.equals(".")){
						if (line[1].equals("O"))	count[1]++;
						else	count[0]++;
					}
					prevChar=line[0];
					if(!line[1].equals("O"))	neCount++;
				}
				
				System.out.println(count[0]+"\n"+count[1]);
				System.out.println("Total NE tags"+neCount);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void wordFrequency(){
			Hashtable<String,Integer> wordCount = new Hashtable<String, Integer>();
			try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
			{
				String currentLine;
				String[] line;
				int count=0;

				while ((currentLine = br.readLine()) != null) {
					line = currentLine.split("\\s+");
					if (wordCount.containsKey(line[0])){
						count=wordCount.get(line[0]);
						wordCount.put(line[0], count+1);
					}
					else {
						wordCount.put(line[0], 1);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//write to a file
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(LOCATION,"Freq_"+FILENAME))))) {
				try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
				{
					String currentLine;
					String[] line;
					while ((currentLine = br.readLine()) != null) {
						line = currentLine.split("\\s+");
						out.println(currentLine+"\t"+wordCount.get(line[0]));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
		}
		
		//calculate no of words NE vs nonNE for length=1 to 20
		public void wordLengthToType(){
			int n=50;
			double[][] count = new double [2][n];
			for (int i = 0; i < 2; i++) {
	            for (int j = 0; j < n; j++) {
	                count[i][j]=0;
                }
            }

			try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
			{

				String sCurrentLine;
				String[] line;
				int temp=0;

				while ((sCurrentLine = br.readLine()) != null) {
					line = sCurrentLine.split("\\s+");
					if (line.length >1){
    					if (line[1].equals("O")){
    						count[0][line[0].length()]++;
    					} else {
    						count[1][line[0].length()]++;
    						if(line[0].length()==1)	System.out.println(line[0]+"\t"+temp);
    					}
					}
					temp++;
				}
				for (int i = 0; i < 2; i++) {
		            for (int j = 0; j < n; j++) {
		                System.out.print(count[i][j]+"\t");
	                }
		            System.out.println();
	            }
				System.out.println(count[0]+"\n"+count[1]);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * File writes
		 */
		public void writeEndingWordsColumn(){

			//write to a file
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(LOCATION,"EndWord_"+FILENAME))))) {
				try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
				{
					//rename FILENAME for next iteration
					FILENAME="EndWord_"+FILENAME;
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
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(LOCATION,"StartWord_"+FILENAME))))) {
				try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
				{
					//rename FILENAME for next iteration
					FILENAME="StartWord_"+FILENAME;
					String sCurrentLine;
					String prevChar=".";
					//String prevLine=br.readLine();
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
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(LOCATION,"WordLength"+maxWordLength+"_"+FILENAME))))) {
				try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
				{

					//rename FILENAME for next iteration
					FILENAME="WordLength"+maxWordLength+"_"+FILENAME;
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
		
		//word frequency, writing extra column
		public void writeWordFrequencyColumn(int cutoff){
			
			//read and get count
			Hashtable<String,Integer> wordCount = new Hashtable<String, Integer>();
			try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
			{
				String currentLine;
				String[] line;
				int count=0;

				while ((currentLine = br.readLine()) != null) {
					line = currentLine.split("\\s+");
					if (wordCount.containsKey(line[0])){
						count=wordCount.get(line[0]);
						wordCount.put(line[0], count+1);
					}
					else {
						wordCount.put(line[0], 1);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//write to a file
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(LOCATION,"Frequency"+cutoff+"_"+FILENAME))))) {
				try (BufferedReader br = new BufferedReader(new FileReader(LOCATION+FILENAME)))
				{
					//rename FILENAME for next iteration
					FILENAME="Frequency"+cutoff+"_"+FILENAME;
					String currentLine;
					String[] line;
					while ((currentLine = br.readLine()) != null) {
						line = currentLine.split("\\s+");
						if (wordCount.get(line[0])<=cutoff){
							out.println(currentLine+"\t"+1);
						} else {
							out.println(currentLine+"\t"+0);
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
