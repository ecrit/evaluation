package at.ecrit.github.evaluation.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import at.ecrit.evaluation.ApplicationModelReference;
import at.ecrit.github.evaluation.persistency.AMRPersistencyManager;
import at.ecrit.github.evaluation.persistency.ExcelFileWriter;

public class ExcelFileWriterTest {
	private List<ApplicationModelReference> amrList;
	
	@Before
	public void initBasicLists(){
		amrList = AMRPersistencyManager.getEvaluations().getAppModelReferences();
	}
	
	@Test
	public void writeTest() throws IOException{
		ExcelFileWriter test = new ExcelFileWriter();
		File f = new File("C:/Users/lucia/Desktop/AppModelTests/content.xls");
		test.write(amrList, f);
		
		System.out
			.println("Please check the result file under C:/Users/lucia/Desktop/AppModelTests/content.xls");
	}
	
}
