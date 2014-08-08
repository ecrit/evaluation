package at.ecrit.github.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.ecrit.github.core.GitHubConnector;

public class GitSearcher {
	private String gitURL;
	private int startPage;
	private int endPage;
	
	private GitHubConnector gitConnector;
	
	public GitSearcher(GitHubConnector gitConnector, String gitURL, int startPage, int endPage){
		this.gitConnector = gitConnector;
		this.gitURL = gitURL;
		this.startPage = startPage;
		this.endPage = endPage;
	}
	
	/**
	 * starts the git search result collecting process. it will run until all pagination pages are
	 * visited
	 * 
	 * @param linkSaverFile
	 *            links of each result page will be stored there
	 * @throws InterruptedException
	 */
	public void search(File linkSaverFile) throws InterruptedException{
		GitHubResultCollector gitResultCollector = new GitHubResultCollector(linkSaverFile);
		
		// run till all pages are processed
		while (startPage <= endPage && startPage != 0) {
			startPage =
				gitResultCollector.findApplicationModelLinks(collectResultPages(startPage),
					gitConnector);
			System.out.println(".... restarting with page " + startPage);
		}
		System.out.println("Collected all results");
	}
	
	/**
	 * stores all result pages from the given value till the end
	 * 
	 * @param ignoreTill
	 *            page where to start collecting the pages from
	 * @return a list of all relevant page URL's
	 */
	private List<String> collectResultPages(int ignoreTill){
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
}
