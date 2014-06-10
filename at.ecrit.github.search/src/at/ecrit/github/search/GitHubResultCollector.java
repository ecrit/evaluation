package at.ecrit.github.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import at.ecrit.github.evaluation.GitHubConnector;

public class GitHubResultCollector {
	private static final String GIT_BASE = "https://github.com";
	private static File linkSaver;
	private static List<String> appModelLinks;
	private static List<String> processed;
	
	public static void findApplicationModelLinks(List<String> resultPages,
		GitHubConnector gitConnector, String linkFilePath) throws InterruptedException{
		initLinkSaverFile(linkFilePath);
		appModelLinks = loadExistingLinks();
		processed = new ArrayList<String>();
		
		for (String page : resultPages) {
			loadLinks(page, gitConnector);
		}
		System.out.println("DONE!");
	}
	
	private static void loadLinks(String page, GitHubConnector gitConnector){
		if (processed.contains(page))
			return;
		
		int counter = 0;
		try {
			Document doc = Jsoup.parse(gitConnector.getGETPageContent(page));
			// Document doc = Jsoup.connect(page).get();
			
			Elements links = doc.select("a[href]");
			processed.add(page);
			
			for (Element link : links) {
				String linkType = link.text();
				if (linkType.endsWith("e4xmi")) {
					String repoUrl = GIT_BASE + link.parent().select("a[href]").attr("href");
					String appModelUrl = GIT_BASE + link.attr("href");
					
					if (appModelLinks.contains(appModelUrl)) {
						System.out.println("Already added: " + appModelUrl);
						continue;
					}
					
					appModelLinks.add(appModelUrl);
					writeToFile(repoUrl, appModelUrl);
					counter++;
// System.out.println("+ RepoURL: " + repoUrl + " AppModelURL: " + appModelUrl);
				}
			}
		} catch (IOException | URISyntaxException hse) {
			int startIdx = page.indexOf("p=") + 2;
			int endIdx = page.indexOf("&");
			System.out.println("Please restart with page: " + page.substring(startIdx, endIdx));
			System.out.println("Status: " + ((HttpStatusException) hse).getStatusCode() + hse);
			
		}
		if (counter < 10)
			System.out.println("Links added: " + counter + "\nPage: " + page);
	}
	
	private static void writeToFile(String repoUrl, String appModelUrl){
		try {
			FileWriter fw = new FileWriter(linkSaver.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(repoUrl + ";" + appModelUrl + "\n");
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void initLinkSaverFile(String linkFilePath){
		try {
			linkSaver = new File(linkFilePath);
			if (linkSaver.exists())
				linkSaver.createNewFile();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static List<String> loadExistingLinks(){
		List<String> linkList = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(linkSaver));
			String line;
			
			while ((line = br.readLine()) != null) {
				String[] split = line.split(";");
				linkList.add(split[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return linkList;
	}
	
}
