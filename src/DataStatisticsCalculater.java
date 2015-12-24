import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

	public class DataStatisticsCalculater {
		private static final String LOCATION = "resources/";
		private static final String FILENAME = "train_21_Nov.tsv"; 

		public static void main(String[] args) {
			
			(new DataStatisticsCalculater()).countAvgLength();

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
	}
