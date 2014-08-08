package at.ecrit.github;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import at.ecrit.github.analyser.LinksImporter;
import at.ecrit.github.analyser.StorageManager;
import at.ecrit.github.core.GitHubConnector;
import at.ecrit.github.search.GitSearcher;

public class Main {
	private static String gitURL;
	private static int startPage;
	private static int endPage;
	
	// git user credentials
	private static String username;
	private static String password;
	
	private static GitHubConnector gitConnector;
	private static File linkSaverFile;
	private static File xmiFile;
	private static File excelFile;
	
	public static void main(String[] args){
		// init some basic settings to read result from github
		Scanner scanner = new Scanner(new InputStreamReader(System.in));
		System.out.println("*************************************");
		System.out.println("****** GitHub e4xmi SearchTool ******");
		System.out.println("*************************************");
		
		// set search url
		System.out
			.println("Search URL [Copy&Paste next line for all .e4xmi-Files]\nhttps://github.com/search?o=desc&p=1&q=in%3Apath+*.e4xmi+extension%3Ae4xmi&type=Code&ref=searchresults&s=indexed");
		gitURL = scanner.nextLine();
		
		// start and end page
		startPage = 1;
		System.out.println("Number of pages from search result: ");
		endPage = Integer.parseInt(scanner.nextLine());
		
		// get username and password for gitConnector
		System.out.println("Please enter your git username: ");
		username = scanner.nextLine();
		System.out.println("...and your git password: ");
		password = scanner.nextLine();
		
		performSearchAnalysation();
	}
	
	private static void performSearchAnalysation(){
		gitConnector = new GitHubConnector(username, password);
		createFreshDirectoryAndResultFiles();
		
		try {
			// store all git result links to .txt-file first
			GitSearcher searchManager = new GitSearcher(gitConnector, gitURL, startPage, endPage);
			searchManager.search(linkSaverFile);
			
			// create ApplicationModelReferences objects for each link and populate the xmi with
			// them
			LinksImporter importer = new LinksImporter(linkSaverFile, xmiFile, username, password);
			importer.populateApplicationModelReferenceXMI();
			
			// add content info to references and store to excel file
			StorageManager storageManager =
				new StorageManager(xmiFile.getAbsolutePath(), excelFile);
			storageManager.addContentInfo();
			storageManager.saveResultsInExcelFile();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a directory with the name 'result_ {@link Date}' to store results of this search
	 * round. New (empty) links.txt, evaluation.xmi and toc.xls files are generated into this
	 * directory.
	 */
	private static void createFreshDirectoryAndResultFiles(){
		DateFormat dFormater = new SimpleDateFormat("ddMMyyyyHHmmss");
		Date date = Calendar.getInstance().getTime();
		
		// create directory with current datetime
		File destDirectory =
			new File(System.getProperty("user.dir") + File.separator + "rsc" + File.separator
				+ "result_" + dFormater.format(date));
		destDirectory.mkdir();
		
		try {
			// put links file in the created directory
			linkSaverFile =
				new File(destDirectory.getAbsolutePath() + File.separator + "links.txt");
			linkSaverFile.createNewFile();
			
			// add xmi file for storing AppModelReference objects
			String xmiHeaderLines =
				"<?xml version=\"1.0\" encoding=\"ASCII\"?>\n"
					+ "<model:Evaluation xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" "
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:model=\"at.ecrit.github.core.model\" "
					+ "xsi:schemaLocation=\"at.ecrit.github.core.model Evaluation.xcore#/EPackage\"/>";
			xmiFile = new File(destDirectory.getAbsolutePath() + File.separator + "evaluation.xmi");
			xmiFile.createNewFile();
			FileWriter fw = new FileWriter(xmiFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(xmiHeaderLines);
			bw.close();
			
			// create excel file to store final output with detailed info
			excelFile = new File(destDirectory.getAbsolutePath() + File.separator + "toc.xls");
			excelFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
