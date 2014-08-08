package at.ecrit.github.analyser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import at.ecrit.github.core.model.ApplicationModelReference;

public class ExcelFileWriter {
	private CreationHelper createHelper;
	
	public void write(List<ApplicationModelReference> amrList, File outputFile) throws IOException{
		Workbook wb = new HSSFWorkbook();
		createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("Content");
		
		Row row = sheet.createRow((short) 0);
		createTableHeaders(row);
		createTableContent(amrList, sheet);
		
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(outputFile);
		wb.write(fileOut);
		fileOut.close();
		
	}
	
	private void createTableHeaders(Row row){
		List<String> headers = getDefaultHeaders();
		for (int i = 0; i < headers.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headers.get(i));
		}
	}
	
	private void createTableContent(List<ApplicationModelReference> amrList, Sheet sheet){
		for (int i = 1; i <= amrList.size(); i++) {
			ApplicationModelReference amr = amrList.get(i - 1);
			
			Row row = sheet.createRow((short) i);
			// Repository Name
			createCell(row, 0, amr.getGitBaseLocation() + "/" + amr.getGitRepository());
			// Path GitHub
			createHyperlinkCell(row, 1, amr.getUrl());
			// FileSize
			createCell(row, 2, String.format("%1$,.2f", amr.getFileSize()));
			// description
			createCell(row, 3, amr.getDescription());
			// readme
			String readMe = amr.getReadmeUrl();
			if (readMe == null || readMe.isEmpty()) {
				createCell(row, 4, "");
			} else {
				createHyperlinkCell(row, 4, readMe);
			}
			// isFragment
			createCell(row, 5, amr.getContext().isFragment() + "");
			// # parts
			createCell(row, 6, amr.getContext().getNrParts() + "");
			// # perspectives
			createCell(row, 7, amr.getContext().getNrPerspectives() + "");
			// # commands
			createCell(row, 8, amr.getContext().getNrCommands() + "");
			// # direct menu items
			createCell(row, 9, amr.getContext().getNrDirectMenus() + "");
			// # handled menu items
			createCell(row, 10, amr.getContext().getNrHandledMenus() + "");
			// # mainMenu
			createCell(row, 11, amr.getContext().isMainMenu() + "");
			// # toolbar
			createCell(row, 12, amr.getContext().isToolBar() + "");
		}
	}
	
	private void createHyperlinkCell(Row row, int col, String url){
		Cell cell = row.createCell(col);
		cell.setCellValue(url);
		Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
		link.setAddress(url);
		cell.setHyperlink(link);
	}
	
	private void createCell(Row row, int col, String string){
		Cell cell = row.createCell(col);
		cell.setCellValue(string);
	}
	
	private List<String> getDefaultHeaders(){
		List<String> defaultHeaders = new ArrayList<String>();
		defaultHeaders.add("RepositoryName");
		defaultHeaders.add("Path (GitHub)");
		defaultHeaders.add("FileSize (KB)");
		defaultHeaders.add("Description");
		defaultHeaders.add("ReadMe");
		defaultHeaders.add("IsFragment");
		defaultHeaders.add("# Parts");
		defaultHeaders.add("# Perspectives");
		defaultHeaders.add("# Commands");
		defaultHeaders.add("# DirectMenus");
		defaultHeaders.add("# HandledMenus");
		defaultHeaders.add("Has Main Menu");
		defaultHeaders.add("Has Toolbar");
		defaultHeaders.add("Category");
		defaultHeaders.add("Relevant?");
		
		return defaultHeaders;
	}
}
