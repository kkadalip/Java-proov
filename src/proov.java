import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Scanner;

public class proov {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
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
				System.out.println("Line: " + lineNr + " :" + line);
				lineNr++;
				String[] wordsOfLine = line.split(" ");
				System.out.println("wordsOfLine length: " + wordsOfLine.length);
				String lastWord = wordsOfLine[wordsOfLine.length - 1]; // duration
				System.out.println("last word is " + lastWord);
				// TODO Get request duration of each line (aka request)
				// To do that. 1) Get last element of string (8 or 9 parts of string)
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
