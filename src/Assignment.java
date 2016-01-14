import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class Assignment {
	public static void main(String[] args) {
		// Setting starting time for calculating program run duration in milliseconds.
		long startTime = System.currentTimeMillis();
		// Command line "n" parameter. If set then program prints out top n (exact value of n is passed as program argument) resources with highest average request duration.
		int nNumberFromParams = 0;
		String logFileNameFromParams = "";
		// Debug parameter, set true to see debug log in console.
		boolean debug = false;

		// START ------------- CHECKING COMMAND LINE ARGUMENTS ---------------

		// Setting numeric command line argument as n, text argument as file location/name
		if(args.length > 0){
			if(debug)System.out.println("We have "+ args.length +" command line arguments!");
			// If user has correct amount of arguments ie one (log file name or location with name) or two (log file and how many average request durations).
			if(args.length <3){
				// Checking for command line arguments -h, -? and -help, if any of them exists in command line arguments, show help menu and stop program.
				List<String> argsList = Arrays.asList(args);
				boolean userNeedsHelp = argsList.contains("-help") || argsList.contains("-h") || argsList.contains("-?");
				if(userNeedsHelp){
					// Printing help menu:
					if(debug)System.out.println("User needs help!!!!!");
					System.out.println("\nUsage: java -jar jarfile <logfile> [number *optional*]");
					System.out.println("To re-build with ant use command: ant main (deletes dist folder, re-compiles .jar into dist folder)");
					System.out.println("Options:");
					System.out.println("-help, -h, -?,        print this help message and exit");
					System.out.println("jarfile,              location of this .jar file");
					System.out.println("<logfile>,            file name with extension (if log file is in command prompt working directory) or exact location of log file");
					System.out.println("[number],             program prints top n resources with highest average request duration (optional)");
					// End calculating program run duration.
					long endTime   = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					System.out.println();
					System.out.println("Program ran for " + totalTime + " milliseconds.");
					return;
				}else{
					if(debug)System.out.println("User doesn't need help.");
					for(String arg : args){
						// If a numeric argument is found from command line arguments, assign it as n.
						// This is good because if user enters n before log location the program will still run correctly.
						if(isNumeric(arg)){
							if(debug)System.out.println("Command line argument "+ arg +" is numeric, using as n.");
							nNumberFromParams = Integer.parseInt(arg);
						}else{
							if(debug)System.out.println("Command line argument "+ arg +" is not numeric, not using as n. Using as log name.");
							logFileNameFromParams = arg;
						}
					}
				}
			}else{ 
				// User has more than 2 arguments, program stops and warns user.
				System.out.println("You are trying to use more than two command line arguments. Type -h for help.");
				return;
			}
		}else{
			// User doesn't have any command line arguments. Minimum log name (or location) is needed.
			System.out.println("You need to use command line parameters to use this program. Type -h for help.");
			return;
		}

		// END --------------- CHECK COMMAND LINE ARGUMENTS ---------------
		// START ------------- CHECK LOG FILE LOCATION --------------------

		// Check if user is entering exact log file location:
		// eg "java -jar dist/Assignment.jar C:\Users\karlk\Desktop\logfile.log 10" while being in C:\Users\karlk\workspace\Java-proov
		String fileDir = "";
		File file;
		// No "." in log file name from command line parameters, perhaps user forgot to add file extension ".log"
		if(!logFileNameFromParams.contains(".")){
			logFileNameFromParams += ".log";
		}

		// Log file parameter contains slashes, this means that user is trying to give exact location
		if(logFileNameFromParams.contains("\\") || logFileNameFromParams.contains("/")){
			fileDir = logFileNameFromParams;
			file = new File(fileDir); 
		}else{
			// Otherwise assume user is entering log file name that resides in console working directory:
			fileDir = System.getProperty("user.dir"); // The working directory of console (user dir). (eg C:\Users\karlk\workspace\Java-proov)
			file = new File(fileDir + "/" + logFileNameFromParams); // eg working directory with parameter timing.log
		}

		// END ------------- CHECK LOG FILE LOCATION -----------------------------------------
		// START ----------- HISTOGRAM RELATED THINGS (DATES, HOURS, HOUR DATA) ---------------

		// NOTE: If list of unique dates are needed separately, create a list to hold that: eg 
		// NOTE: Make duplicate lists for checking case sensitive items if necessary.

		//List<String> uniqueDates = new ArrayList<String>();  // (DEBUG) All unique dates of request lines.
		//List<String> uniquePaths = new ArrayList<>(); // (DEBUG) All unique paths ie first part of URI. Example: /mobileAppCookie.do or getSubscriptionAuthTokens
		//List<String> uniqueResources = new ArrayList<>(); // (DEBUG) All unique resources ie second part of URI. Example: /customerPromotions.do?load=true&id=6810D9E1D84736F6FF8F039C747C3DD3&contentId=main_subscription

		// <DATE> <[HOUR][DATA ie requests in one hour]> TreeMap so it would be sorted.
		Map<String,int[][]> datesAndHoursDataMap = new TreeMap<String,int[][]>(); 
		// [Hours per day] [Request amount per hour] (NB! First element 0, last 23 for rows)
		int[][] hoursAndRequests = new int[24][1]; 
		// Unique path and resource as a single string, eg /mainContent.do action=TERMINALFINANCE.
		//List<String> uniquePathsWithResources = new ArrayList<>(); // Replaced this with Map, redundant
		Map<String, List<String>> uniquePathsWithResourcesMap = new HashMap<String, List<String>>();

		// Using Scanner to read each line in log (text) file.
		try(Scanner scanner = new Scanner(file)){
			int lineNrCounter = 1;
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();

				// Each line consists of either: (NOTE: log usually has 7 to 9 parts per line)
				// [date] [timestamp] [thread-id (in brackets)] [optional user context (in square brackets)] ||| [URI + query string] [string "in"] [request duration in ms]
				// eg 2015-08-19 00:00:02,814 (http--0.0.0.0-28080-245) [CUST:CUS5T27233] /substypechange.do?msisdn=300501633574 in 17
				// OR
				// [date] [timestamp] [thread-id (in brackets)] [optional user context (in square brackets)] ||| [requested resource name (one string)] [data payload elements for resource (0..n elements)] [string "in"] [request duration in ms]
				// eg 2015-08-19 00:04:45,212 (http--0.0.0.0-28080-405) [] updateSubscriptionFromBackend 300445599231 in 203

				// Splitting each line to smaller components using empty spaces between words.
				String[] wordsOfLine = line.split(" ");
				// If debug is enabled, printing out line number counter, how many components the line consists of (usually 7 to 9), printing out original line.
				if(debug)System.out.println("(Line:"+lineNrCounter+") Length: " + wordsOfLine.length + " Line: " + line);
				lineNrCounter++;

				// 1) DATE eg 2015-08-19
				String date = wordsOfLine[0]; 
				//if(!uniqueDates.contains(date)){uniqueDates.add(date);} // (DEBUG) For debugging to print out all unique dates
				// 2) TIMESTAMP (I need hour). Getting it from the first part of example 00:04:45,212.
				int hour = Integer.parseInt(wordsOfLine[1].split(":")[0]);
				// 3) THREAD-ID (in brackets) - not needed, eg (http--0.0.0.0-28080-187)
				// 4) OPTIONAL USER CONTEXT - not needed (in square brackets), eg [USER:300109921258]
				// 5) (if 7 parts total) URI + query string OR (if 8 or 9 parts total) requested RESOURCE name (one string), eg getBroadbandSubscriptions
				String resource = wordsOfLine[4];
				//if(!uniqueResources.contains(resource)){uniqueResources.add(resource);} // For debugging to print out all unique resources
				// (NOTE: if 8 elements then 6) probably optional user context again eg CUS12B1435)
				// (NOTE: if 9 elements then 6) data payload elements for resource (0..n elements) eg 300109921258)
				// (NOTE: if 9 elements then 7) boolean something eg true)
				// second from last ie length-2) is STRING "in"
				// last ie 7th 8th 9th which means wordsOfLine.length-1) DURATION in milliseconds
				//int duration = Integer.parseInt(wordsOfLine[wordsOfLine.length - 1]);
				String duration = wordsOfLine[wordsOfLine.length - 1]; // duration
				if(debug)System.out.println("[1)Date: " + date + "] [2)Hour: " + hour + "] [5)URI+query/Resource: " + resource + "] [last) Duration: " + duration + "]");

				//NOTE: (URL = Uniform Resource Locator;) URI = Uniform Resource Identifier; (URN = Uniform Resource Name)
				//NOTE: (in my case) ? separates URI (left half) and query (right half)
				//NOTE: = separates field-name from value-name (somefield=somename)
				//NOTE: (in my case) & separates field=value-s (or ; series of items + + + space is + or %20)
				//NOTE: (# can be used to specify a subsection/fragment of a document)
				//NOTE: (Letters A-Z, a-z, numbers 0-9 and characters * - . _ are left as-is.)
				URI aURI;
				try {
					aURI = new URI(resource);
					// 1) First part of URI is Path 2) Queries (field=name) part of URI second half ie query
					// NOTE: /main.do&contentId=undefined is found and wrong because of "&" and won't be split, should have used "?" as by spec (this is a typo by user)
					if(aURI.getPath() != null){
						String path = aURI.getPath();
						if(debug)System.out.println("URI path: " + path); // eg /substypechange.do
						//if(!uniquePaths.contains(path)){uniquePaths.add(path);}
						String extraQueryParts = ""; // Holds all extra parts of URI temporarily IMPORTANT!!!
						// IF URI HAS QUERY (the part after whateverpath?)  eg load=true&id=6810D9E1D84736F6FF8F039C747C3DD3&contentId=main_subscription
						if(aURI.getQuery() != null){
							String query = aURI.getQuery(); // (URI part after ?)
							if(debug)System.out.println("URI query: " + query);
							String[] queryPairs = query.split("&"); // Array of queryPairs eg examplefield=examplename
							//if(debug)System.out.println("queryPairs are " + queryPairs); // (DEBUG)
							String pairFirstHalf = ""; // First half of field=name pair ie examplefield.
							//String pairSecondHalf = ""; // Second half of field=name pair ie examplename. Maybe needed later.
							String[] splittedPair = null;
							// Looping through query pairs ie array of somefield=somename-s
							for (String pair : queryPairs){
								//if(debug)System.out.println("pair is " + pair); // (DEBUG)
								//Splitting pair to first and second half and assigning them to splittedPair String array
								splittedPair = pair.split("=");
								// Checking if the pair is field=name or something different, if it is field=name then it has 2 parts
								if(splittedPair.length == 2){
									pairFirstHalf = splittedPair[0];
									//pairSecondHalf = splittedPair[1]; // Maybe needed later, keeping it just in case.
									//if(debug)System.out.println("Pair is " + pair + " First half: " + pairFirstHalf + " Second half: " + pairSecondHalf);//(DEBUG)
								}else{
									if(debug)System.out.println("NB! field=name pair is not two parts!!!");
								}
								// Cheking if pair first half (somefield=somename somefieldname equals one of the conditions)
								if(pairFirstHalf.equals("action") ||
										pairFirstHalf.equals("contentId") ||
										pairFirstHalf.equals("category")||
										pairFirstHalf.equals("properties") ||
										pairFirstHalf.equals("target")){
									extraQueryParts += (pair + " ");
									// NOTE: Since I'm looping over all queryPairs, extraQueryParts can have multiple parts eg: action=SUBSCRIPTION contentId=main_subscription 
								}
							} // FOR pair:queryPairs LOOP END
						}else{
							// URI HAS NO QUERY
						}
						// if unique URI paths (first half before ?) with resources (eg /mainContent.do with extras action=TERMINALFINANCE contentId=main_subscription)
						// doesn't already contain current path+extras then add path and its extra query parts to String array uniquePathsWithResources
						//if(!uniquePathsWithResources.contains(path + " " + extraQueryParts)){uniquePathsWithResources.add(path + " " + extraQueryParts);}
						// SAME THING WITH MAP AND DURATIONS (Key Value Pairs): (No duration here, only path and extra query parts)
						if(!uniquePathsWithResourcesMap.containsKey(path + " " + extraQueryParts)){
							if(debug)System.out.println("Addig path: " + path + " with extraQueryParts: " + extraQueryParts);
							List<String> durations = new ArrayList<String>(Arrays.asList(duration)); // asList creates ("bla","bla","bla"), in this case only contains a single duration
							// PATH with QUERY PARTS, List<String> of durations into uniquePathsWithResourcesMap
							uniquePathsWithResourcesMap.put(path + " " + extraQueryParts,durations);
						}else{
							// IF path with extra queries EXISTS, get via KEY and add duration to existing durations list as VALUE
							uniquePathsWithResourcesMap.get(path + " " + extraQueryParts).add(duration);
						}
					}// END FINDING URI PATH (aURI.getPath() != null)
				}catch (URISyntaxException e) {
					System.err.println("Caught URI syntax exception: " + e.getMessage());
					if(debug)e.printStackTrace();
				}
				// make the data value (request count) of current hour bigger by one (hours in rows, data in single column). Basic solution for Key Value Pairs.
				hoursAndRequests[hour][0]++; 

				//String[][] hourAndDuration = new String[hour][duration];
				// Putting date and duration holders into datesAndHoursDataMap
				int[][] temp = datesAndHoursDataMap.get(date); // exact date hours and their data (request amount). In other temp is temporary date data (hour and request amount) holder. //(map.get("test")[0][1]);
				// If (hour and hour request) data for date exists (ie temporary holder isn't null)
				if(temp != null){ // Date exists!
					//System.out.println("DATE EXISTS");
					int tempHourVal = temp[hour][0];
					//System.out.println("OLD temp hour "+hour+" val is " + tempHourVal);
					// Taking temp array [x hour][0] first element and putting +1 there, increasing the times it has been accessed at certain date, certain hour
					tempHourVal++;
					temp[hour][0] = tempHourVal; // or ++tempHourVal, needs to be increased before putting it there
					datesAndHoursDataMap.put(date, temp); // so new temp value is +1
					//System.out.println("NEW temp hour "+hour+" val is " + temp[hour][0]); // aka tempHourVal
				}else{ // No such date!
					//System.out.println("DATE DOES NOT EXIST");
					//datesAndHoursDataMap.put(date, new int[24][1]); // Hours, hour data
					temp = new int[24][1]; // NOTE: int[][] hourValues = new int [24][];
					temp[hour][0] = 1; 
					datesAndHoursDataMap.put(date, temp);
					//System.out.println("Currernt date current hour request count: " + datesAndHoursDataMap.get(date)[hour][0]);
				}
			} // END WHILE (scanner has next line) ie FILE READ END
		}catch(FileNotFoundException e){
			System.err.println("File not found exception: " + e.getMessage());
			if(debug)e.printStackTrace();
			return;
		}

		// PRINTING OUT ALL UNIQUE RESOURCES IN LOG FILE (130)
		//		Collections.sort(uniqueResources);
		//		System.out.println("------------------------ There are " + uniqueResources.size() + " unique resources.");
		//		for(String resource : uniqueResources){System.out.println(resource);}

		// PRINTING OUT ALL UNIQUE PATHS IN LOG FILE (42)
		//		Collections.sort(uniquePaths);
		//		System.out.println("------------------------ There are " + uniquePaths.size() + " unique paths.");
		//		for(String path : uniquePaths){System.out.println(path);}

		// PRINTING OUT ALL UNIQUE PATHS WITH IMPORTANT QUERIES (atm only "ACTION=blablablabla") (27)
		//		Collections.sort(uniquePathsWithResources);
		//		System.out.println("------------------------ There are " + uniquePathsWithResources.size() + " uniquePathsWithResources.");
		//		for(String path : uniquePathsWithResources){System.out.println(path);}
		// SAME THING WITH MAP AND KVP-s!!!!
		//		System.out.println("------------------------ There are " + uniquePathsWithResources.size() + " uniquePathsWithResources MAP");
		// Keys are paths + important query parts; Values are lists of durations, now I can calculate average
		//for(String key : uniquePathsWithResourcesMap.keySet()){ // ONLY KEY
		//for (String key : uniquePathsWithResourcesMap.values()) { // ONLY VALUES

		//		List<Integer> uniqueHistogramHours = new ArrayList<Integer>(); // Not used
		List<String> uniqueHistogramDays = new ArrayList<String>();
		Map<Integer, List<Integer>> hourDurations = new TreeMap<Integer,List<Integer>>();

		// Looping over date (String) and ([hour][requests in hour amount]) data map
		for (Map.Entry<String, int[][]> entry : datesAndHoursDataMap.entrySet()){ // KEY AND VALUE
			String date = entry.getKey();
			// FOR HISTOGRAM to count the amount of unique histogram days (ie over how many days log lasted):
			if(!uniqueHistogramDays.contains(date)){
				uniqueHistogramDays.add(date);
			}
			//if(!hourDurations.containsKey(date)){hourDurations.put(date);} // Not using this list of hour durations anymore.
			// -------------------------------------------------------------------------------------------------------------------------------REVIEW AND CLEAN BELOW:
			System.out.println("-----Date:"+date+"-----");
			int[][] hoursAndData = entry.getValue();
			for(int i = 0; i < hoursAndData.length; i++){ // loop over ROWS, i ie row value is hour 0 to 23
				for(int element = 0; element < hoursAndData[i].length; element++){ // loop over COLUMNS:
					//System.out.printf("Row: %d Element: %d Value: %d\n", row, element, container[row][element]);
					int requestsAmount = hoursAndData[i][element];
					if(requestsAmount > 0){
						// FOR HISTOGRAM to get unique hours, however not important in my case since I'm showing a whole 24h period including empty hours:
						//if(!uniqueHistogramHours.contains(row)){uniqueHistogramHours.add(row);}

						// KEY: HOUR, VALUE: DATA (FOR HISTOGRAM)
						//System.out.println("putting " + row + " and " + requestsAmount);
						List<Integer> currentRowInts = hourDurations.get(i);
						if(currentRowInts == null){
							currentRowInts = new ArrayList<Integer>();
						}
						currentRowInts.add(requestsAmount);
						hourDurations.put(i, currentRowInts); //requestsAmount);

						// eg hourValues[hour 10][1000]th slot for data
						//						if(hourValues[row] == null){
						//							//hourValues[row][0] = new int [row][0];
						//							System.out.println("Hourvalues row is null ("+row+")");
						//							System.out.println("hourvalues length is " + hourValues.length);
						//							//hourValues[row] = new int[row];
						//							//int currentHourVals = hourValues[row];
						//							//hourValues[row][0] ++;
						//							hourValues[row][0] = new int[row]; // new int [row][0]
						//							System.out.println("hourvalues length is " + hourValues.length);
						//						}

						//						int slotToPutData = 0;
						//						if(hourValues[row] != null){
						//							slotToPutData = hourValues[row].length;
						//						}else{
						//							hourValues[row][0] = new int[];
						//						}
						//						System.out.println("TRYING TO PUT DATA INTO hourValues["+row+"]["+slotToPutData+"]");
						////						hourValues[row][slotToPutData] = requestsAmount;
						//						//hourValues[row][0] ++;
						//						slotToPutData++;

						if(i < 10){
							//System.out.println("[Hour: 0"+ row + "] [Requests: " + element +"]");
							System.out.println("[Hour: 0"+ i + "] [Requests: " + requestsAmount +"]");
						}else{
							//System.out.println("[Hour: "+ row + "] [Requests: " + element +"]");
							System.out.println("[Hour: "+ i + "] [Requests: " + requestsAmount +"]");
						}
					}
				}
			}
		} // END FOR
		double uniqueHistogramDaysAmount = uniqueHistogramDays.size();
		System.out.println("\nHistogram of hourly number of requests. Average calculated over " + (int)uniqueHistogramDaysAmount + " day(s)");

		//System.out.println("\nHistogram of hourly request DURATIONS: Average over " + (int)uniqueHistogramDaysAmount + " day(s)");
		//		for(int hour : uniqueHistogramHours){
		//			if(hour <10){
		//				System.out.println("[Hour: 0" + hour + "]");
		//			}else{
		//				System.out.println("[Hour: " + hour + "]");
		//			}
		//		}

		double totalRequestsThisHour = 0;
		int totalRequestsOverall = 0;
		//double hoursAndAverageDurations[][] = new double[24][1];
		double averageRequestsPerHour[][] = new double[24][1]; // FOR AVERAGES IN HISTOGRAM - each line
		double totalRequestsPerHour[][] = new double[24][1]; // FOR TOTALS IN HISTOGRAM - each line
		for (Entry<Integer, List<Integer>> entry : hourDurations.entrySet()){ // KEY AND VALUE
			int hour = entry.getKey();
			List<Integer> requestsInHours = entry.getValue();
			if(debug)System.out.println("Hour: "+ hour + " Value: " + requestsInHours);
			totalRequestsThisHour = 0;
			for(int request : requestsInHours){
				totalRequestsThisHour += request;
				totalRequestsOverall += request;
			}
			// REQUESTS AMOUNT
			double averageTotalRequestsThisHour = totalRequestsThisHour / uniqueHistogramDaysAmount;
			averageRequestsPerHour[hour][0]=averageTotalRequestsThisHour;

			totalRequestsPerHour[hour][0] = totalRequestsThisHour;
			// FOR ONLY UNIQUE HOURS DATA
			//			if(hour < 10){
			//				System.out.println("Hour: 0" + hour + " Avg. requests: " + averageTotalRequestsThisHour);
			//			}else{
			//				System.out.println("Hour: " + hour + " Avg. requests: " + averageTotalRequestsThisHour);
			//			}

		}

		// MAX WRONG BECAUSE THIS IS NOT DIVIDED BY INSTANCE AMOUNT?
		double avgRequests = 0;
		double totalRequests = 0; // in one hour
		//double maxRequestsInHour = 0;
		int totalAverageAmountOfRequests = 0; // TODO do not use this anymore, just use total amount divided by days or smth

		for(int i=0; i<averageRequestsPerHour.length; i++){
			double tmp = averageRequestsPerHour[i][0];
			totalAverageAmountOfRequests += tmp;
			//if(tmp > maxRequestsInHour){
			//	maxRequestsInHour = tmp;
			//}
		}

		//		System.out.println("Total amount of requests: " + totalAverageAmountOfRequests); // adds averages together, NOT THE OVERALL TOTAL!!!! USELESS DATA
		System.out.println("Total amount of requests: " + totalRequestsOverall);

		DecimalFormat df = new DecimalFormat("000.00");
		DecimalFormat dfNoDecimals = new DecimalFormat("0000");
		//System.out.println("Maximum average request amount in hour is " + df.format(maxRequestsInHour));
		// Max avg dur is 100%
		String histogramBoxes = "";
		double percentage = 0;
		//double roundedPercentage = 0;
		int howManyBoxesFilled = 0;

		for(int i=0; i<24; i++){
			//System.out.println("hoursAndAverageDurations rows are " + hoursAndAverageDurations[i][0]);
			avgRequests = averageRequestsPerHour[i][0];
			totalRequests = totalRequestsPerHour[i][0];
			//System.out.println("avg dur " + avgDuration + " / " + " max dur " + maxAvgDuration + " * 100");
			//percentage = avgRequests / maxRequestsInHour * 100;
			percentage = avgRequests / totalAverageAmountOfRequests * 100; // ALTERNATIVELY CURRENT REQUESTS / ALL REQUESTS * 100 (not averages)
			// TAKE ALL TOTAL AMOUNT INTO ACCOUNT
			//System.out.println("percentage is " + percentage);
			// eg 78 / 10 is 7.8 and when rounded then 8 boxes
			howManyBoxesFilled = (int) Math.round(percentage / 10);
			//System.out.println("corresponds to boxes " + howManyBoxesFilled);
			if(howManyBoxesFilled <= 0){
				histogramBoxes = "[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]";
			}else{
				histogramBoxes = "";
				for(int j=0; j<10; j++){
					if(j<howManyBoxesFilled){
						histogramBoxes += "[x]";
					}else{
						histogramBoxes += "[ ]";
					}
				}
			}
			if(i < 10){
				System.out.println("Hour: 0"+i+" "+histogramBoxes+"("+df.format(percentage)+"%)"+" Total: "+ dfNoDecimals.format(totalRequests) + " Avg: " + df.format(avgRequests) + " req./hour");
			}else{
				System.out.println("Hour: "+i+" "+histogramBoxes+"("+df.format(percentage)+"%)"+" Total: "+ dfNoDecimals.format(totalRequests) +" Avg: " + df.format(avgRequests) + " req./hour");
			}
		}

		Map<String, Double> pathsWithAverageDuration = new TreeMap<String,Double>();

		double totalCount = 0.0;
		int sum = 0;
		// PRINTING ALL unsorted RESULTS WITH AVERAGE DURATIONS
		if(debug)System.out.println("[Average duration][Request] unsorted");
		for (Map.Entry<String, List<String>> entry : uniquePathsWithResourcesMap.entrySet()){ // KEY AND VALUE
			String path = entry.getKey();
			List<String> durations = entry.getValue();
			totalCount = durations.size();
			//System.out.println("DURATIONS SIZE IS " + totalCount);
			if(totalCount > 0){
				sum = 0;
				for(String duration : durations){
					sum += Double.parseDouble(duration);
					//System.out.println("duration added to sum " + duration);
				} // INNER FOR END
				//System.out.println("sum is " + sum + " totalcount is " + totalCount);
				double average = sum / totalCount;
				NumberFormat formatter = new DecimalFormat("#0000.00");
				// Formatter does the rounding for me!
				if(debug)System.out.println("["+formatter.format(average)+"ms] " + path);
				//System.out.println("["+Math.round(average * 100d) / 100d + "ms]" + path);
				//pathsWithAverageDuration.put(path, Double.toString(average));
				pathsWithAverageDuration.put(path, average);
			}else{
				if(debug)System.out.println(path + " Average duration: " + "??? ms.");
			}
		} // FOR END

		// notTODO CREATE A NEW KVP String String Map to sort the path + duration.
		// notTODO Then use command line optional parameter to display top n amount.

		//printMap(pathsWithAverageDuration);

		// To sort Map by keys, use TreeMap
		// TreeMap is unable to sort the Map values, instead, we should use Comparator
		// Convert Map to List, sort list by Comparator, put list back to Map
		// Map ---> List ---> Sort --> SortedList ---> Map

		Map<String, Double> sortedMap = sortByComparator(pathsWithAverageDuration);

		// PRINTING SORTED MAP AND SHOWING n AMOUNT OF HIGHEST AVERAGE DURATION RESULTS
		String sortedResultsHeader = "\n[Average duration][Request]";
		if(nNumberFromParams>0){
			sortedResultsHeader += "(Showing "+nNumberFromParams+"/"+sortedMap.size()+" results)";
		}else{
			sortedResultsHeader += "(Showing " + sortedMap.size() + " results)";
		}
		System.out.println(sortedResultsHeader);

		//printMap(sortedMap, Optional.empty());
		//printMap(sortedMap, Optional.of(1000)); nNumberFromParams
		printMap(sortedMap, nNumberFromParams);

		// for (int i=0; i < array.length; i++) {
		// DATES LIST NOT NEEDED
		//		if(debug){
		//			for(String d : uniqueDates){
		//				System.out.println("Date is: " + d);
		//			}
		//		}

		String hourAndRequestsAmount = "";
		for (int i = 0; i < hoursAndRequests.length; i++) {
			//System.out.println("Hour: " + i);
			hourAndRequestsAmount = "Hour: "; // + i + " ";
			if(i < 10){
				if(debug)hourAndRequestsAmount += "0";
			}
			hourAndRequestsAmount += i + " ";
			for (int j = 0; j < hoursAndRequests[i].length; j++) {
				////System.out.print(hoursDurations[i][j]);
				//System.out.println("Duration: " + hoursDurations[i][0]);
				hourAndRequestsAmount += "Requests: " + hoursAndRequests[i][0];
				if(debug)System.out.println(hourAndRequestsAmount);
			}
		}
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("\nProgram ran for " + totalTime + " milliseconds.");
	} // END MAIN

	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
				new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	public static void printMap(Map<String, Double> map) {
		printMap(map, 0);
	}
	// Print out top n (exact value of n is passed as program argument) resources with highest average request duration.
	public static void printMap(Map<String, Double> map, int n) {
		//int optional_n = n.isPresent() ? n.get() : 0;

		//		for (Map.Entry<String, Double> entry : map.entrySet()) {
		//			//System.out.println("Key : " + entry.getKey() 
		//			//+ " Value : " + entry.getValue());
		//			NumberFormat formatter = new DecimalFormat("#0000.00");
		//			// Value is average duration, Key is path with selected queries
		//			System.out.println("["+formatter.format(entry.getValue())+"ms] "+entry.getKey());
		//		}
		NumberFormat formatter = new DecimalFormat("#0000.00");
		int counter = 0;
		if(n > 0){
			//int counter = 0;
			if(n > map.entrySet().size()){
				n = map.entrySet().size();
			}
			for (Map.Entry<String, Double> entry : map.entrySet()) {
				if(counter<n){
					// Value is average duration, Key is path with selected queries					
					counter++;
					System.out.println(counter + ". ["+formatter.format(entry.getValue())+"ms] "+entry.getKey());
				}else{
					break;
				}
			}
		}else{
			for (Map.Entry<String, Double> entry : map.entrySet()) {
				// Value is average duration, Key is path with selected queries
				counter++;
				System.out.println(counter + ". ["+formatter.format(entry.getValue())+"ms] "+entry.getKey());
			}
		}
	}

	// TO CHECK IF command line ARGUMENT IS NUMBER OR NOT (for n amount of highest)
	// Checks if NumberFormar formatter is able to parse the whole length of given string or not, better than using double d = Double.parseDouble(str); and catching NumberFormatException
	public static boolean isNumeric(String str)  
	{  
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}
} // END CLASS
