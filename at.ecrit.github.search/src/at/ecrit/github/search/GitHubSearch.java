package at.ecrit.github.search;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import at.ecrit.github.evaluation.GitHubConnector;

public class GitHubSearch {
	private static String GIT_URL =
		"https://github.com/search?o=desc&p=1&q=in%3Apath+*.e4xmi+extension%3Ae4xmi&type=Code&ref=searchresults&s=indexed";
	private static int START_PAGE = 1;
	private static int END_PAGE = 70;
	
	public static void main(String[] args) throws IOException, InterruptedException{
		// init some basic settings to read result from github
		Scanner scanner = new Scanner(new InputStreamReader(System.in));
		System.out.println("GitHub Search Result Collector ");
		
		// set url of first result page
		System.out
			.println("URL of first result page (make sure after search? ther is something like p=1");
		GIT_URL = scanner.nextLine();
		
		// Start and end page
		System.out.println("Number of pages from search result: ");
		END_PAGE = Integer.parseInt(scanner.nextLine());
		
		// set username and password
		System.out.println("Please enter your git username: ");
		String user = scanner.nextLine();
		System.out.println("...and your git password: ");
		String password = scanner.nextLine();
		GitHubConnector gitConnector = new GitHubConnector(user, password);
		
		// starting this search session
		System.out.println("At which search result page would you like to start: ");
		int startPage = Integer.parseInt(scanner.nextLine());
		
		String linksFilePath = System.getProperty("user.dir") + File.separator + "links.txt";
		GitHubResultCollector.findApplicationModelLinks(collectResultPages(startPage),
			gitConnector, linksFilePath);
	}
	
	private static List<String> collectResultPages(int ignoreTill){
		List<String> urlList = new ArrayList<String>();
		String url = GIT_URL;
		int prev = START_PAGE;
		
		for (int pageNr = START_PAGE; pageNr <= END_PAGE; pageNr++) {
			if (pageNr >= ignoreTill) {
				url = url.replaceFirst(Integer.toString(prev), Integer.toString(pageNr));
				urlList.add(url);
				prev = pageNr;
			}
		}
		System.out.println("#ResultPages: " + urlList.size());
		return urlList;
	}
}
