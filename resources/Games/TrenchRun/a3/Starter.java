package a3;

import java.util.Scanner;

import a3.TrenchRun;

public class Starter {
	
	public static void main(String [] args) {

		if(args.length > 0){
			int port = Integer.parseInt(args[2]);
			new TrenchRun("no", args[1], port).start();
		}
		else{
			System.out.println("Would you like to play in Full Screen Mode? yes/no");
			Scanner scanIn = new Scanner(System.in);
			String fullScreen = scanIn.nextLine();

			System.out.println("Enter Server IP Address: ");
			scanIn = new Scanner(System.in);
			String ip = scanIn.nextLine();
			scanIn.close();

			new TrenchRun(fullScreen, ip, 6666).start();
		}

	}
}
