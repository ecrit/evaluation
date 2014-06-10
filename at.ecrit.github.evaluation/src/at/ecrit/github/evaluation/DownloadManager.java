package at.ecrit.github.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import at.ecrit.evaluation.ApplicationModelReference;
import at.ecrit.evaluation.Evaluation;
import at.ecrit.github.evaluation.persistency.AMRPersistencyManager;
import at.ecrit.github.evaluation.persistency.ExcelFileWriter;

public class DownloadManager {
	/**
	 * fragment identification via '<fragment:ModelFragments'
	 */
	private static final String FRAGMENT_TAG = "<fragment:ModelFragments";
	private static final String PART_TAG = "xsi:type=\"basic:Part\"";
	private static final String PERSPECTIVE_TAG = "xsi:type=\"advanced:Perspective\"";
	private static final String COMMAND_TAG = "<commands xmi:id";
	private static final String DIRECT_MENU_TAG = "xsi:type=\"menu:DirectMenuItem";
	private static final String HANDLED_MENU_TAG = "xsi:type=\"menu:HandledMenuItem";
	private static final String MAIN_MENU_TAG = "<mainMenu";
	private static final String TOOLBAR_TAG = "xsi:type=\"menu:ToolBar";
	
	private Evaluation evaluation;
	private List<ApplicationModelReference> appModelRefs;
	private static File contentFile;
	
	public DownloadManager(){
		evaluation = AMRPersistencyManager.getEvaluations();
		appModelRefs = evaluation.getAppModelReferences();
	}
	
	public void evaluate(){
		System.out.println("START EVALUATION...");
		
		for (ApplicationModelReference amr : appModelRefs) {
			try {
				System.out.println("evaluating ... " + amr.getUrl());
				// get URL content
				URL url = new URL(amr.getRawUrl());
				URLConnection conn = url.openConnection();
				populateContext(amr, conn.getInputStream());
				
				// set file size
				double size = conn.getContentLength();
				if (size != -1)
					size = size / 1024;
				amr.setFileSize(size);
				
				// save readme information if existing
				String readMe = "";
				if (amr.getReadmeUrl() != null && !amr.getReadmeUrl().isEmpty()) {
					URL readmeUrl = new URL(amr.getReadmeUrl());
					URLConnection readmeConn = readmeUrl.openConnection();
					readMe = read(readmeConn.getInputStream());
				}
				amr.setReadMe(readMe);
				
				AMRPersistencyManager.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		createEvaluationOverviewFile();
	}
	
	private void populateContext(ApplicationModelReference amr, InputStream inputStream)
		throws IOException{
		int nrParts = 0;
		int nrPerspectives = 0;
		int nrCommands = 0;
		int nrDirectMenus = 0;
		int nrHandledMenus = 0;
		boolean fragment = false;
		boolean mainMenu = false;
		boolean toolBar = false;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String inputLine;
		int lineCounter = 1;
		
		while ((inputLine = br.readLine()) != null) {
			if (lineCounter < 4 && inputLine.startsWith(FRAGMENT_TAG)) {
				fragment = true;
				break;
			} else if (inputLine.contains(PART_TAG)) {
				nrParts++;
			} else if (inputLine.contains(PERSPECTIVE_TAG)) {
				nrPerspectives++;
			} else if (inputLine.contains(COMMAND_TAG)) {
				nrCommands++;
			} else if (inputLine.contains(DIRECT_MENU_TAG)) {
				nrDirectMenus++;
			} else if (inputLine.contains(HANDLED_MENU_TAG)) {
				nrHandledMenus++;
			} else if (inputLine.contains(MAIN_MENU_TAG)) {
				mainMenu = true;
			} else if (inputLine.contains(TOOLBAR_TAG)) {
				toolBar = true;
			}
			lineCounter++;
		}
		br.close();
		
		amr.getContext().setNrParts(nrParts);
		amr.getContext().setNrPerspectives(nrPerspectives);
		amr.getContext().setNrCommands(nrCommands);
		amr.getContext().setNrDirectMenus(nrDirectMenus);
		amr.getContext().setNrHandledMenus(nrHandledMenus);
		amr.getContext().setFragment(fragment);
		amr.getContext().setMainMenu(mainMenu);
		amr.getContext().setToolBar(toolBar);
	}
	
	private String read(InputStream inputStream) throws IOException{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		
		String inputLine;
		while ((inputLine = br.readLine()) != null) {
			sb.append(inputLine);
			sb.append("\n");
		}
		br.close();
		
		return sb.toString();
	}
	
	private void createEvaluationOverviewFile(){
		try {
			URL url = DownloadManager.class.getResource("/result");
			contentFile = new File(url.getPath() + File.separator + "toc.xls");
			
			// make sure we start with an empty file
			if (contentFile.exists())
				contentFile.delete();
			
			contentFile.createNewFile();
			
			ExcelFileWriter fwExcel = new ExcelFileWriter();
			System.out.println("Writing output to ... " + contentFile.getAbsolutePath());
			fwExcel.write(appModelRefs, contentFile);
			
			System.out.println("Evaluation FINISHED!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
