package at.ecrit.github.evaluation.persistency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import at.ecrit.evaluation.ApplicationModelReference;
import at.ecrit.evaluation.Localization;

public class ExcelFileWriter {
	private WritableCellFormat arialBoldUnderline;
	private WritableCellFormat arial;
	
	private WritableWorkbook workbook;
	private WritableSheet sheet;
	
	private String inputFile;
	
	private HashMap<ApplicationModelReference, Integer> rowMap;
	private List<ApplicationModelReference> ignored;
	
	public void setOutputFile(String inputFile){
		this.inputFile = inputFile;
	}
	
	public void write(List<ApplicationModelReference> amrList, List<Localization> localStorageLoc,
		List<ApplicationModelReference> ignored) throws IOException, WriteException{
		File file = new File(inputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();
		
		rowMap = new HashMap<ApplicationModelReference, Integer>();
		this.ignored = ignored;
		
		wbSettings.setLocale(new Locale("en", "EN"));
		
		workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Content", 0);
		sheet = workbook.getSheet(0);
		
		createHeaders(getDefaultHeaders());
		createContent(amrList, localStorageLoc);
		
	}
	
	public void close() throws IOException, WriteException{
		if (workbook != null) {
			workbook.write();
			workbook.close();
		}
	}
	
	private void createHeaders(List<String> headers) throws WriteException{
		// Lets create a times font
		WritableFont arial10pt = new WritableFont(WritableFont.ARIAL, 10);
		// Define the cell format
		arial = new WritableCellFormat(arial10pt);
		// Lets automatically wrap the cells
		arial.setWrap(true);
		
		// create create a bold font with unterlines
		WritableFont arial10ptBoldUnderline =
			new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		arialBoldUnderline = new WritableCellFormat(arial10ptBoldUnderline);
		// Lets automatically wrap the cells
		arialBoldUnderline.setWrap(true);
		
		CellView cv = new CellView();
		cv.setFormat(arial);
		cv.setFormat(arialBoldUnderline);
		cv.setAutosize(true);
		
		// Write the table headers
		for (int i = 0; i < headers.size(); i++) {
			addCaption(i, 0, headers.get(i));
		}
	}
	
	private void createContent(List<ApplicationModelReference> amrList,
		List<Localization> localStorageMap) throws WriteException, RowsExceededException{
		Map<ApplicationModelReference, String> localStorageLoc =
			loadLocalStorageMap(localStorageMap);
		
		// now a bit of text
		for (int i = 1; i <= amrList.size(); i++) {
			ApplicationModelReference amr = amrList.get(i - 1);
			rowMap.put(amr, i);
			// Evaluated?
			addLabel(0, i, toEvaluate(amr));
			// Repository Name
			addLabel(1, i, amr.getGitBaseLocation() + "/" + amr.getGitRepository());
			// Path GitHub
			addLabel(2, i, amr.getUrl());
			// Path Local
			addLabel(3, i, localStorageLoc.get(amr));
			// FileSize
			addLabel(4, i, String.format("%1$,.2f", amr.getFileSize()));
			// isFragment
			addLabel(5, i, amr.getContext().isFragment() + "");
			// # commands
			addLabel(6, i, amr.getContext().getNrCommands() + "");
			// # window element
			addLabel(7, i, amr.getContext().getNrWindowElements() + "");
		}
	}
	
	private Map<ApplicationModelReference, String> loadLocalStorageMap(
		List<Localization> localStorageMap){
		Map<ApplicationModelReference, String> localStorageLoc =
			new HashMap<ApplicationModelReference, String>();
		for (Localization loc : localStorageMap) {
			localStorageLoc.put(loc.getAmr(), loc.getLocalPath());
		}
		return localStorageLoc;
	}
	
	public String toEvaluate(ApplicationModelReference amr){
		String evaluated = "true";
		
		if (ignored.contains(amr))
			evaluated = "false";
		
		return evaluated;
	}
	
	private void addCaption(int column, int row, String s) throws RowsExceededException,
		WriteException{
		Label label;
		label = new Label(column, row, s, arialBoldUnderline);
		sheet.addCell(label);
	}
	
	private void addLabel(int column, int row, String s) throws WriteException,
		RowsExceededException{
		Label label;
		label = new Label(column, row, s, arial);
		sheet.addCell(label);
	}
	
	private List<String> getDefaultHeaders(){
		List<String> defaultHeaders = new ArrayList<String>();
		defaultHeaders.add("Evaluated");
		defaultHeaders.add("RepositoryName");
		defaultHeaders.add("Path (GitHub)");
		defaultHeaders.add("Path (Local)");
		defaultHeaders.add("FileSize (KB)");
		defaultHeaders.add("IsFragment");
		defaultHeaders.add("# Commands");
		defaultHeaders.add("# Window Elements");
		defaultHeaders.add("Other");
		
		return defaultHeaders;
	}
}
