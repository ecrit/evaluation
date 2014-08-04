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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import at.ecrit.github.evaluation.GitHubConnector;

public class GitHubResultCollector {
	private static final String GIT_BASE = "https://github.com";
	private File linkSaver;
	private List<String> appModelLinks;
	private List<String> processed;
	
	public GitHubResultCollector(File linkSaver){
		this.linkSaver = linkSaver;
	}
	
	/**
	 * finds all the application model links and manages their storage in a text-file
	 * 
	 * @param resultPages
	 *            list of all pages to evaluate
	 * @param gitConnector
	 *            holds user credentials
	 * @return result page where to start next or 0 if finished
	 * @throws InterruptedException
	 */
	public int findApplicationModelLinks(List<String> resultPages, GitHubConnector gitConnector)
		throws InterruptedException{
		appModelLinks = loadExistingLinks();
		processed = new ArrayList<String>();
		
		for (String page : resultPages) {
			try {
				loadLinks(page, gitConnector);
			} catch (IOException | URISyntaxException e) {
				int startIdx = page.indexOf("p=") + 2;
				int endIdx = page.indexOf("&", startIdx);
				int nextStart = Integer.parseInt(page.substring(startIdx, endIdx));
				
				e.printStackTrace();
				return nextStart;
			}
		}
		return 0;
	}
	
	private void loadLinks(String page, GitHubConnector gitConnector) throws IOException,
		URISyntaxException{
		if (processed.contains(page))
			return;
		
		int counter = 0;
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
		if (counter < 10)
			System.out.println("Links added: " + counter + "\nPage: " + page);
		
	}
	
	private void writeToFile(String repoUrl, String appModelUrl){
		try {
			FileWriter fw = new FileWriter(linkSaver.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(repoUrl + ";" + appModelUrl + "\n");
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	private List<String> loadExistingLinks(){
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
