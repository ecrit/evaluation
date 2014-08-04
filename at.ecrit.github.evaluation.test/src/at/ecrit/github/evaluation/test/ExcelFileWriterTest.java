package at.ecrit.github.evaluation.test;

import static org.junit.Assert.assertTrue;

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
		File xmiFile = new File("rsc/evaluation.xmi");
		amrList =
			AMRPersistencyManager.getEvaluations(xmiFile.getAbsolutePath()).getAppModelReferences();
	}
	
	@Test
	public void writeTest() throws IOException{
		ExcelFileWriter test = new ExcelFileWriter();
		File f = new File("rsc/toc.xls");
		test.write(amrList, f);
		
		assertTrue(f.length() > 0);
		
		System.out.println("Please check the result file under " + f.getAbsolutePath());
	}
	
}
