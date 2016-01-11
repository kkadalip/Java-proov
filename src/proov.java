import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Scanner;

public class proov {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
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
			}
		} catch (FileNotFoundException e) {
			System.out.println("Scanner error");
			e.printStackTrace();

		}

	} // END MAIN

	private static Date getLocalCurrentDate() {
		return new Date();
	}

}
