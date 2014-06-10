package at.ecrit.github.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import jxl.write.WriteException;

import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import at.ecrit.evaluation.ApplicationModelReference;
import at.ecrit.evaluation.Evaluation;
import at.ecrit.evaluation.EvaluationFactory;
import at.ecrit.evaluation.Localization;
import at.ecrit.github.evaluation.persistency.AMRPersistencyManager;
import at.ecrit.github.evaluation.persistency.ExcelFileWriter;

import com.google.common.io.Files;

public class DownloadManager {
	/**
	 * fragment identification via '<fragment:ModelFragments'
	 */
	private static final String FRAGMENT_TAG = "<fragment:ModelFragments";
	
	private Evaluation evaluation;
	private List<ApplicationModelReference> appModelRefs;
	private List<ApplicationModelReference> ignored;
	private static String destDirectory = "";
	private static File contentFile;
	
	private static ResourceSet resourceSet = new ResourceSetImpl();
	
	public DownloadManager(){
		DirectoryDialog dirDialog = new DirectoryDialog(new Shell());
		dirDialog.setText("Select a direcotry to save the ApplicationModel files...");
		destDirectory = dirDialog.open();
		
		evaluation = AMRPersistencyManager.getEvaluations();
		appModelRefs = evaluation.getAppModelReferences();
		ignored = new ArrayList<ApplicationModelReference>();
	}
	
	public void evaluate(boolean ignoreFragments, double minFileSize, int minCommands,
		int minWindowElements){
		System.out.println("START EVALUATION...");
		
		if (ignoreFragments)
			ignoreFragments();
		ignoreFilesSmallerThan(minFileSize);
		ignoreNotEnoughContent(minCommands, minWindowElements);
		
		List<ApplicationModelReference> relevant = new ArrayList<ApplicationModelReference>();
		for (ApplicationModelReference amr : appModelRefs) {
			if (!ignored.contains(amr))
				relevant.add(amr);
		}
		saveRelevantFilesInSeparateDirectory(relevant);
	}
	
	public void downloadFiles(){
		for (ApplicationModelReference amr : appModelRefs) {
			downloadFile(amr);
		}
		AMRPersistencyManager.save();
	}
	
	private void downloadFile(ApplicationModelReference amr){
		try {
			// get URL content
			URL url = new URL(amr.getRawUrl());
			URLConnection conn = url.openConnection();
			
			// set file size
			double size = conn.getContentLength();
			if (size != -1)
				size = size / 1024;
			amr.setFileSize(size);
			
			File pkgDir = new File(getPackageDirectory(amr));
			if (!pkgDir.exists())
				pkgDir.mkdirs();
			
			// save to this filename
			String modelUrl = amr.getUrl();
			String modelName =
				modelUrl.substring((modelUrl.lastIndexOf("/") + 1), modelUrl.length());
			String fileName =
				destDirectory + File.separator + amr.getGitBaseLocation() + File.separator
					+ amr.getGitRepository() + File.separator + modelName;
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			
			// open the input stream and write lines to file
			readInputAndWriteToFile(conn.getInputStream(), file.getAbsolutePath());
			
			if (amr.getReadmeUrl() != null && !amr.getReadmeUrl().isEmpty()) {
				URL readmeUrl = new URL(amr.getReadmeUrl());
				URLConnection readmeConn = readmeUrl.openConnection();
				String readmeName =
					destDirectory + File.separator + amr.getGitBaseLocation() + File.separator
						+ amr.getGitRepository() + File.separator + "README.md";
				File readme = new File(readmeName);
				if (!readme.exists())
					readme.createNewFile();
				
				readInputAndWriteToFile(readmeConn.getInputStream(), readme.getAbsolutePath());
			}
			
			Localization loc = EvaluationFactory.eINSTANCE.createLocalization();
			loc.setAmr(amr);
			loc.setLocalPath(file.getAbsolutePath());
			evaluation.getLocalStorageLoc().add(loc);
			
			AMRPersistencyManager.save();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readInputAndWriteToFile(InputStream inputStream, String filePath)
		throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
		
		String inputLine;
		while ((inputLine = br.readLine()) != null) {
			bw.write(inputLine);
			bw.write("\n");
		}
		bw.close();
		br.close();
	}
	
	private String getPackageDirectory(ApplicationModelReference amr){
		String packageDirectory =
			destDirectory + File.separator + amr.getGitBaseLocation() + File.separator
				+ amr.getGitRepository();
		System.out.println(packageDirectory);
		return packageDirectory;
	}
	
	private void saveRelevantFilesInSeparateDirectory(List<ApplicationModelReference> relevant){
		File evaluateDir = new File(destDirectory + File.separator + "evaluate");
		evaluateDir.mkdirs();
		
		for (ApplicationModelReference amr : relevant) {
			try {
				String evalPath = evaluateDir + File.separator + amr.getGitBaseLocation();
				File newDir = new File(evalPath);
				File oldDir = new File(destDirectory + File.separator + amr.getGitBaseLocation());
				
				Files.move(oldDir, newDir);
				
				String repoDirPath = evalPath + File.separator + amr.getGitRepository();
				File repoDir = new File(repoDirPath);
				String[] fileList = repoDir.list();
				String appModelPath = "";
				for (String fPath : fileList) {
					if (fPath.endsWith(".e4xmi")) {
						appModelPath = repoDirPath + File.separator + fPath;
					}
				}
				
				replaceLocalStorageLoc(amr, appModelPath);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		createEvaluationOverviewFile(evaluateDir.getAbsolutePath());
	}
	
	private void replaceLocalStorageLoc(ApplicationModelReference amr, String appModelPath){
		List<Localization> localStorageLoc = evaluation.getLocalStorageLoc();
		for (Localization loc : localStorageLoc) {
			if (loc.getAmr().equals(amr)) {
				loc.setLocalPath(appModelPath);
			}
		}
		AMRPersistencyManager.save();
	}
	
	private void createEvaluationOverviewFile(String di){
		try {
			contentFile = new File(destDirectory + File.separator + "toc.xls");
			
			// make sure we start with an empty file
			if (contentFile.exists()) {
				contentFile.delete();
				contentFile.createNewFile();
			}
			
			ExcelFileWriter fwExcel = new ExcelFileWriter();
			fwExcel.setOutputFile(contentFile.getAbsolutePath());
			fwExcel.write(appModelRefs, evaluation.getLocalStorageLoc(), ignored);
			fwExcel.close();
			
			System.out.println("Evaluation FINISHED!");
		} catch (IOException | WriteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ignore files which do not fullfile the minimal filesize
	 * 
	 * @param minSize
	 *            minimal accepted size
	 */
	private void ignoreFilesSmallerThan(double minSize){
		for (ApplicationModelReference amr : appModelRefs) {
			if (amr.getFileSize() < minSize) {
				if (!ignored.contains(amr))
					ignored.add(amr);
			}
		}
		System.out.println("Ignoring files smaller than " + minSize + "\n" + "Ignoring "
			+ ignored.size() + " files");
	}
	
	/**
	 * ignore all fragments files
	 */
	private void ignoreFragments(){
		for (ApplicationModelReference amr : appModelRefs) {
			File file = new File(getLocation(amr));
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String inputLine;
				int lineCounter = 1;
				
				// examine first three lines as there could be an empty line at the start
				while ((inputLine = br.readLine()) != null && lineCounter < 4) {
					if (inputLine.startsWith(FRAGMENT_TAG)) {
						amr.getContext().setFragment(true);
						AMRPersistencyManager.save();
						if (!ignored.contains(amr))
							ignored.add(amr);
					}
					lineCounter++;
				}
				br.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Ignoring all fragments\nIgnoring " + ignored.size() + " files");
	}
	
	private String getLocation(ApplicationModelReference amr){
		for (Localization loc : evaluation.getLocalStorageLoc()) {
			if (loc.getAmr().equals(amr)) {
				return loc.getLocalPath();
			}
		}
		return "";
	}
	
	/**
	 * sets all files with less content to the ignore list
	 * 
	 * @param minCommands
	 *            minimal number of commands
	 * @param minWindowElements
	 *            minimal number of window elements
	 */
	private void ignoreNotEnoughContent(int minCommands, int minWindowElements){
		for (ApplicationModelReference amr : appModelRefs) {
			if (!ignored.contains(amr)) {
				ApplicationPackageImpl.init();
				URI e4xmiUri = URI.createFileURI(getLocation(amr));
				resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("e4xmi", new E4XMIResourceFactory());
				
				Resource appModelResource = resourceSet.getResource(e4xmiUri, true);
				MApplication application = (MApplication) appModelResource.getContents().get(0);
				amr.getContext().setNrCommands(application.getCommands().size());
				amr.getContext().setNrWindowElements(application.getChildren().size());
				
				if (amr.getContext().getNrCommands() < minCommands) {
					ignored.add(amr);
					continue;
				}
				
				if (amr.getContext().getNrWindowElements() < minWindowElements) {
					ignored.add(amr);
				}
			}
			
		}
		AMRPersistencyManager.save();
		System.out.println("Ignoring all ApplicationModels with less than: " + minCommands
			+ " commands & less than: " + minWindowElements + " window element\n" + "Ignoring "
			+ ignored.size() + " files");
	}
}
