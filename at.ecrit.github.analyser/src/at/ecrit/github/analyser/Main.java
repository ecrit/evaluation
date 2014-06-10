package at.ecrit.github.analyser;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) throws URISyntaxException, IOException{
		File f = new File(System.getProperty("user.dir") + File.separator + "links.txt");
		
		// set username and password
		Scanner scanner = new Scanner(new InputStreamReader(System.in));
		System.out.println("GitHub Analyser");
		System.out.println("Please enter your git username: ");
		String user = scanner.nextLine();
		System.out.println("...and your git password: ");
		String password = scanner.nextLine();
		
		Analyser analyser = new Analyser(f, user, password);
		analyser.populateApplicationModelReferenceXMI();
	}
	
}
