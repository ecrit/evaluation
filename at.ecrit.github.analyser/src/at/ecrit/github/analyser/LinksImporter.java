package at.ecrit.github.analyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import at.ecrit.github.core.AMRPersistencyManager;
import at.ecrit.github.core.model.ApplicationModelReference;
import at.ecrit.github.core.model.ModelFactory;

public class LinksImporter {
	private static final String GIT_BASE = "https://github.com";
	
	private File linksFile;
	private File xmiFile;
	private GitHubClient client;
	private List<String> repos;
	private List<String> e4xmiLinks;
	
	public LinksImporter(String linksFilePath, String xmiFilePath, String gitUser,
		String gitPassword) throws FileNotFoundException{
		new LinksImporter(new File(linksFilePath), new File(xmiFilePath), gitPassword, gitPassword);
	}
	
	public LinksImporter(File linksFile, File xmiFile, String gitUser, String gitPassword){
		this.linksFile = linksFile;
		this.xmiFile = xmiFile;
		client = new GitHubClient();
		client.setCredentials(gitUser, gitPassword);
	}
	
	public void populateApplicationModelReferenceXMI() throws IOException{
		List<ApplicationModelReference> amrList =
			AMRPersistencyManager.getEvaluations(xmiFile.getAbsolutePath()).getAppModelReferences();
		initRepoAndE4XMILists();
		
		int counter = 0;
		for (int i = 0; i < repos.size(); i++) {
			try {
				String repo = repos.get(i);
				
				String tmp = repo.replace(GIT_BASE + "/", "");
				String[] ownerName = tmp.split("/");
				RepositoryService service = new RepositoryService(client);
				Repository r = service.getRepository(ownerName[0], ownerName[1]);
				System.out.println("RepoName: " + r.getName() + "\n" + r.getDescription());
				
				ApplicationModelReference amr =
					ModelFactory.eINSTANCE.createApplicationModelReference();
				amr.setDescription(r.getDescription());
				
				Document doc = Jsoup.connect(repo).get();
				Elements links = doc.select("a[href]");
				for (Element link : links) {
					if (link.text().endsWith("README.md")) {
						amr.setReadmeUrl(GIT_BASE + link.attr("href"));
						continue;
					}
				}
				
				String appModelUrl = e4xmiLinks.get(i);
				if (alreadyAdded(appModelUrl, amrList)) {
					continue;
				}
				amr.setUrl(appModelUrl);
				amr.setRawUrl(appModelUrl.replace("blob", "raw"));
				amr.setGitBaseLocation(ownerName[0]);
				amr.setGitRepository(ownerName[1]);
				amr.setContext(ModelFactory.eINSTANCE.createContextInfo());
				
				amrList.add(amr);
				AMRPersistencyManager.save();
				System.out.println("Done " + amrList.size() + "\n");
			} catch (RequestException re) {
				counter++;
				re.printStackTrace();
			}
		}
		System.out.println("Skipped: " + counter);
		System.out.println("Finished importing links");
	}
	
	/**
	 * checks whether this url is already part of the list
	 * 
	 * @param appModelUrl
	 * @param amrList
	 * @return
	 */
	private boolean alreadyAdded(String appModelUrl, List<ApplicationModelReference> amrList){
		for (ApplicationModelReference amr : amrList) {
			if (amr.getUrl().equals(amrList)) {
				return true;
			}
		}
		return false;
	}
	
	private void initRepoAndE4XMILists(){
		repos = new ArrayList<String>();
		e4xmiLinks = new ArrayList<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(linksFile));
			
			String line;
			int counter = 0;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(";");
				repos.add(counter, splitted[0]);
				e4xmiLinks.add(counter, splitted[1]);
				
				System.out.println(counter++);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
