import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class proov {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		//aks-mbp:dist ak$ java -jar assignment.jar timing.log 10
		// aks-mbp:dist ak$
		// (java -jar)
		// (name of .jar)
		// location of file
		// n = 10 (last argument)
		
		if(args.length > 0){ // we have args!
			System.out.println("we have args");
			System.out.println("First argument is " + args[0]); // if no arg: java.lang.ArrayIndexOutOfBoundsException
			// args[0].equals("h")
			boolean userNeedsHelp = Arrays.asList(args).contains("help") || Arrays.asList(args).contains("h") || Arrays.asList(args).contains("?"); // ? and help should be as well
			if(userNeedsHelp){
				System.out.println("user needs help!!!!!");
				// ant [options] [target [target2 [target3] ...]]
				// Usage: / Options:
				// where options include:
				// -help, -h
				// -projecthelp, -p
				// -logfile <file>       use given file for log
				// -logger <classname>
			
				// if line is too big, explanation to 2nd line but still same distance
				
				//System.out.println("-help, -h              print this message and exit");
				// TEXT, empty space, 16th or 24th letter is text
				System.out.println("Usage: java -jar jarfile <logfile> [args...]"); // logfile location?
				System.out.println("Options:");
				System.out.println("-help, -h, -?,        print this help message and exit");
				System.out.println("<number>              prints top n resources with highest average request duration");
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				System.out.println("Program ran for " + totalTime + " milliseconds.");
				return;
			}else{
				System.out.println("NO HELP NEEDED");
			}	
		}
		
		System.out.println("test");
		System.out.println(getLocalCurrentDate());

		String userDir = System.getProperty("user.dir"); // means C:\Users\karlk\workspace\Java-proov
		//File file = new File("timing.log"); //("file.txt");
		File file = new File(userDir + "/timing.log");

		try {
			Scanner scanner = new Scanner(file);
			System.out.println("Scanner:" + scanner.toString());

			// Reading Scanner class lines
			int lineNr = 1;
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();
				//System.out.println("Line: " + lineNr + " :" + line);
				lineNr++;
				String[] wordsOfLine = line.split(" ");
				System.out.println("Length: " + wordsOfLine.length + " Line: " + lineNr + " " + line);
				// 7 or 9
				// 1) date eg 2015-08-19
				// 2) timestamp eg 00:06:44,560
				// 3) thread-id (in brackets) eg (http--0.0.0.0-28080-187)
				// 4) optional user context (in square brackets) eg [USER:300109921258]
				
				// !!! IF 7
				// 5) URI + query string 

				
				// !!! IF 8
				// 5) requested resource name (one string) eg getBroadbandSubscriptions
				// 6) ??? eg CUS12B1435
				
				// !!! IF 9
				// 5) requested resource name (one string) eg getSubcriptionCampaigns
				// 6) data payload elements for resource (0..n elements) eg 300109921258
				// 7) BOOLEAN SOMETHING!? eg true
				
				// length-2) string "in"
				// length-1) request duration in ms
				String lastWord = wordsOfLine[wordsOfLine.length - 1]; // duration
				//System.out.println("last word is " + lastWord);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Scanner error");
			e.printStackTrace();
		}

		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Program ran for " + totalTime + " milliseconds.");
	} // END MAIN

	private static Date getLocalCurrentDate() {
		return new Date();
	}

}
