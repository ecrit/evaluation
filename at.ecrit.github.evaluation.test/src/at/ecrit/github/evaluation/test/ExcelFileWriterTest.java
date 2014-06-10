package at.ecrit.github.evaluation.test;

import java.io.IOException;
import java.util.List;

import jxl.write.WriteException;

import org.junit.Before;
import org.junit.Test;

import at.ecrit.evaluation.ApplicationModelReference;
import at.ecrit.github.evaluation.persistency.AMRPersistencyManager;
import at.ecrit.github.evaluation.persistency.ExcelFileWriter;

public class ExcelFileWriterTest {
	private List<ApplicationModelReference> amrList;
	private List<String> headers;
	
	@Before
	public void initBasicLists(){
		amrList = AMRPersistencyManager.getEvaluations().getAppModelReferences();
	}
	
	@Test
	public void writeTest() throws WriteException, IOException{
		ExcelFileWriter test = new ExcelFileWriter();
		
		test.setOutputFile("C:/Users/lucia/Desktop/AppModelTests/content.xls");
		test.write(amrList, null, null);
		test.close();
		
		System.out
			.println("Please check the result file under C:/Users/lucia/Desktop/AppModelTests/content.xls");
	}
	
}
