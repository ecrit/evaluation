package at.ecrit.github.search;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import at.ecrit.github.evaluation.GitHubConnector;

public class SearchManager {
	// @formatter:off
	/**https://github.com/search?o=desc&p=1&q=in%3Apath+*.e4xmi+extension%3Ae4xmi&type=Code&ref=searchresults&s=indexed
	 */
	// @formatter:on
	private static String gitURL;
	private static int startPage;
	private static int endPage;
	
	// git user credentials
	private static String username;
	private static String password;
	
	private static GitHubConnector gitConnector;
	private static File linkSaverFile;
	
	public static void main(String[] args) throws IOException, InterruptedException{
		// init some basic settings to read result from github
		Scanner scanner = new Scanner(new InputStreamReader(System.in));
		System.out.println("GitHub Search Result Collector ");
		
		// set url of first result page
		System.out
			.println("URL of first result page (make sure after search? there is something like p=1");
		gitURL = scanner.nextLine();
		
		// Start and end page
		startPage = 1;
		System.out.println("Number of pages from search result: ");
		endPage = Integer.parseInt(scanner.nextLine());
		
		// get username and password for gitConnector
		System.out.println("Please enter your git username: ");
		username = scanner.nextLine();
		System.out.println("...and your git password: ");
		password = scanner.nextLine();
		
		startSearch();
	}
	
	/**
	 * starts the git search result collecting process. it will run until all pagination pages are
	 * visited
	 * 
	 * @throws InterruptedException
	 */
	private static void startSearch() throws InterruptedException{
		createFreshLinksDirecoryAndFile();
		GitHubResultCollector gitResultCollector = new GitHubResultCollector(linkSaverFile);
		gitConnector = new GitHubConnector(username, password);
		
		// run till all pages are processed
		while (startPage <= endPage && startPage != 0) {
			startPage =
				gitResultCollector.findApplicationModelLinks(collectResultPages(startPage),
					gitConnector);
			System.out.println(".... restarting with page " + startPage);
		}
		System.out.println("DONE - collected all results");
	}
	
	/**
	 * stores all result pages from the given value till the end
	 * 
	 * @param ignoreTill
	 *            page where to start collecting the pages from
	 * @return a list of all relevant page URL's
	 */
	private static List<String> collectResultPages(int ignoreTill){
		List<String> urlList = new ArrayList<String>();
		String url = gitURL;
		int prev = 1;
		
		for (int pageNr = startPage; pageNr <= endPage; pageNr++) {
			if (pageNr >= ignoreTill) {
				url = url.replaceFirst(Integer.toString(prev), Integer.toString(pageNr));
				urlList.add(url);
				prev = pageNr;
			}
		}
		System.out.println("#ResultPages: " + urlList.size());
		return urlList;
	}
	
	/**
	 * creates a unique directory with the current date&time to store the links.txt in
	 */
	private static void createFreshLinksDirecoryAndFile(){
		DateFormat dFormater = new SimpleDateFormat("ddMMyyyyHHmmss");
		Date date = Calendar.getInstance().getTime();
		
		try {
			// create directory with current datetime
			File directory =
				new File(System.getProperty("user.dir") + File.separator + "links_"
					+ dFormater.format(date));
			directory.mkdir();
			
			// put links file in the created directory
			linkSaverFile = new File(directory.getAbsolutePath() + File.separator + "links.txt");
			linkSaverFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
