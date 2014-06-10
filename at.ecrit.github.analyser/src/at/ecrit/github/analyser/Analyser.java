package at.ecrit.github.analyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import at.ecrit.evaluation.ApplicationModelReference;
import at.ecrit.evaluation.EvaluationFactory;
import at.ecrit.github.evaluation.persistency.AMRPersistencyManager;

public class Analyser {
	private static final String GIT_BASE = "https://github.com";
	
	private File linksFile;
	private GitHubClient client;
	
	public Analyser(String linksFilePath, String gitUser, String gitPassword)
		throws FileNotFoundException{
		new Analyser(new File(linksFilePath), gitPassword, gitPassword);
	}
	
	public Analyser(File linksFile, String gitUser, String gitPassword)
		throws FileNotFoundException{
		this.linksFile = linksFile;
		if (!linksFile.exists()) {
			throw new FileNotFoundException("File not found under: " + linksFile.getPath());
		}
		client = new GitHubClient();
		client.setCredentials(gitUser, gitPassword);
	}
	
	public void populateApplicationModelReferenceXMI() throws IOException{
		List<ApplicationModelReference> amrList =
			AMRPersistencyManager.getEvaluations().getAppModelReferences();
		Map<String, String> gitLinkMap = readLinksFromFile();
		
		for (String repo : gitLinkMap.keySet()) {
			String tmp = repo.replace(GIT_BASE + "/", "");
			String[] ownerName = tmp.split("/");
			RepositoryService service = new RepositoryService(client);
			Repository r = service.getRepository(ownerName[0], ownerName[1]);
			System.out.println("RepoName: " + r.getName() + "\n" + r.getDescription());
			
			ApplicationModelReference amr =
				EvaluationFactory.eINSTANCE.createApplicationModelReference();
			amr.setDescription(r.getDescription());
			
			Document doc = Jsoup.connect(repo).get();
			Elements links = doc.select("a[href]");
			for (Element link : links) {
				if (link.text().endsWith("README.md")) {
					String readmeUrl = GIT_BASE + link.attr("href");
					amr.setReadmeUrl(readmeUrl.replace("blob", "raw"));
					continue;
				}
			}
			
			String appModelUrl = gitLinkMap.get(repo);
			if (alreadyAdded(appModelUrl, amrList)) {
				continue;
			}
			amr.setUrl(appModelUrl);
			amr.setRawUrl(appModelUrl.replace("blob", "raw"));
			amr.setGitBaseLocation(ownerName[0]);
			amr.setGitRepository(ownerName[1]);
			amr.setContext(EvaluationFactory.eINSTANCE.createContextInfo());
			
			amrList.add(amr);
			AMRPersistencyManager.save();
			System.out.println("Done " + amrList.size() + "\n");
		}
	}
	
	private boolean alreadyAdded(String appModelUrl, List<ApplicationModelReference> amrList){
		for (ApplicationModelReference amr : amrList) {
			if (amr.getUrl().equals(amrList)) {
				return true;
			}
		}
		return false;
	}
	
	private Map<String, String> readLinksFromFile(){
		// key = repo, value = e4xmi
		Map<String, String> gitMap = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(linksFile));
			
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(";");
				gitMap.put(splitted[0], splitted[1]);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gitMap;
	}
	
}
