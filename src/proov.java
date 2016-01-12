import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
			//for(String arg : args){
			System.out.println("We have " + args.length + " optional parameters aka args.");
			for(int i=0; i<args.length; i++){
				int normalNumber = i+1;
				System.out.println("Arg " + normalNumber + " is " + args[i]);
			}
			//System.out.println("First argument is " + args[0]); // if no arg: java.lang.ArrayIndexOutOfBoundsException
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

		// IF WE HAVE LOG LOCATION ARGS AND IT EXISTS (tell user otherwise if it doesn't)
		System.out.println(getLocalCurrentDate()); // DELETE LATER

		String userDir = System.getProperty("user.dir"); // means C:\Users\karlk\workspace\Java-proov
		//File file = new File("timing.log"); //("file.txt");
		File file = new File(userDir + "/timing.log");

		List<String> dates = new ArrayList<String>();
		//int[][] hoursDurations = new int[24][1]; // can't be empty 0 and 23
		int[][] hoursAndRequests = new int[24][1];
		
		List<String> uniqueResources = new ArrayList<>();
		List<String> uniquePaths = new ArrayList<>();
		try {
			Scanner scanner = new Scanner(file);
			//System.out.println("Scanner:" + scanner.toString());

			// Reading Scanner class lines
			int lineNr = 1;

			// HOURS ARRAY (FOR HISTOGRAM), X hours. First element 0th, last 23rd if only one 24h period
			// Pair<Integer, String> myPair = new Pair<>(7, "Seven");
			// Map<String, String>
			// Map<String, String[]>

			//Map<String, String[][]> datesAndHours = new HashMap<String, String[][]>();
			//Map<String, String[][]> datesAndHours = new HashMap<String, String[][]>();


			//List<String> dates = new ArrayList<String>();
			//List<String> hours = new ArrayList<String>();

			//String[] dates; //new String[24];
			//String[] hours;
			// [][] date + hour data later on
			
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();
				//System.out.println("Line: " + lineNr + " :" + line);
				String[] wordsOfLine = line.split(" ");
				System.out.println("("+lineNr+") Length: " + wordsOfLine.length + " Line: " + line);
				lineNr++;				

				// 1) DATE
				String date = wordsOfLine[0];
				//dates.add(date);
				//System.out.println("date is " + date);
				// 2) TIMESTAMP (I need hour)
				//String hour = wordsOfLine[1].split(":")[0];
				int hour = Integer.parseInt(wordsOfLine[1].split(":")[0]);
				//System.out.println("hour is " + hour);
				// 3) THREAD
				// 4) OPTIONAL USER CONTEXT
				// 5) RESOURCE
				String resource = wordsOfLine[4];
				if(!uniqueResources.contains(resource)){
					uniqueResources.add(resource);
				}
				System.out.println("Resource: " + resource);
				

					URI aURI;
					try {
						aURI = new URI(resource);
						//if(aURL.getProtocol() != null)
				        //System.out.println("protocol = " + aURL.getProtocol());
						//if(aURI.getAuthority() != null)
						//System.out.println("!!!! authority = " + aURI.getAuthority());
						//if(aURI.getHost() != null)
						//System.out.println("!!!! host = " + aURI.getHost());
						//if(aURI.getPort() > 0)
						//System.out.println("!!!! port = " + aURI.getPort());
						if(aURI.getPath() != null){
							String path = aURI.getPath();
							System.out.println("!!!! path = " + path);
							if(!uniquePaths.contains(path)){
								uniquePaths.add(path);
							}
						}
						
						if(aURI.getQuery() != null)
						System.out.println("!!!! query = " + aURI.getQuery());
						
						//if(aURL.getFile() != null)
						//System.out.println("filename = " + aURL.getFile());
						//if(aURL.getRef() != null)
				        //System.out.println("ref = " + aURL.getRef());

						
						
					} catch (URISyntaxException e) {
						System.out.println("URI SYNTAX EXCEPTION!");
						e.printStackTrace();
					}
					
				
				// URL = Uniform Resource Locator
				// = separates name from value
				// & or ; separate field=value-s, series of items + + +   space is + or %20
				// # can be used to specify a subsection/fragment of a document
				// Letters A-Z, a-z, numbers 0-9 and characters * - . _ are left as-is.
				
				// last) DURATION
				//String duration = wordsOfLine[wordsOfLine.length - 1]; // duration
				int duration = Integer.parseInt(wordsOfLine[wordsOfLine.length - 1]);
				//System.out.println("duration is " + duration);

				System.out.println("[Date: "+ date +"] [Hour: "+ hour +"] [Duration: "+ duration + "]" );
				
				// row // column // only using 0 for temp KVP
				//System.out.println("adding hour " + hour + " and duration " + duration);
				//hoursDurations[hour][0] += duration;
				hoursAndRequests[hour][0] ++;

				//String[][] hourAndDuration = new String[hour][duration];
				if(!dates.contains(date)){
					dates.add(date);
					//if(datesAndHours.get(date) == null){ // SPECIFIC DATE DOES NOT EXIST
					//datesAndHours.put(date,)
					//datesAndHours.put(date, new String[][]);
				};				
				//datesAndHours.put(arg0, arg1)

				// 7 or 9
				// 1) date eg 2015-08-19 // WHAT IF NOT SAME DATE (histogram)? 24h for each date? or just date + add hours?
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
				
				// TODO REQUESTS HAVE DURATION (last element), NEED TO CALCULATE AVERAGE DURATION FOR EACH RESOURCE, SORT BY HIGHEST AND GIVE OUT n HIGHEST

				// length-2) string "in"
				// length-1) request duration in ms
			} // END WHILE
		} catch (FileNotFoundException e) {
			System.out.println("Scanner error");
			e.printStackTrace();
		}
		
		// PRINTING OUT ALL UNIQUE RESOURCES (130)
		System.out.println("There are " + uniqueResources.size() + " unique resources.");
		for(String resource : uniqueResources){
			System.out.println(resource);
		}
		
		// PRINTING OUT ALL UNIQUE PATHS (42)
		System.out.println("There are " + uniquePaths.size() + " unique paths.");
		for(String path : uniquePaths){
			System.out.println(path);
		}
		
		// for (int i=0; i < array.length; i++) {
		for(String d : dates){
			System.out.println("Date is: " + d);
		}
		
		String hourAndRequestsAmount = "";
		for (int i = 0; i < hoursAndRequests.length; i++) {
			//System.out.println("Hour: " + i);
			hourAndRequestsAmount = "Hour: "; // + i + " ";
			if(i < 10){
				hourAndRequestsAmount += "0";
			}
			hourAndRequestsAmount += i + " ";
		    for (int j = 0; j < hoursAndRequests[i].length; j++) {
		        ////System.out.print(hoursDurations[i][j]);
		    	//System.out.println("Duration: " + hoursDurations[i][0]);
		    	hourAndRequestsAmount += "Requests: " + hoursAndRequests[i][0];
		    	System.out.println(hourAndRequestsAmount);
		    }
		}
		
		// TODO LOOP THROUGH EACH DAY, HOURS AND REQUESTS,
		// HIGHEST NUMBER OF REQUESTS PER HOUR (any day!!) IS 100%,
		// make graphs with xxxxx, scales, legend

		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Program ran for " + totalTime + " milliseconds.");
	} // END MAIN

	private static Date getLocalCurrentDate() {
		return new Date();
	}

}
