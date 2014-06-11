package at.ecrit.github.evaluation.persistency;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import at.ecrit.evaluation.Evaluation;
import at.ecrit.evaluation.EvaluationPackage;
import at.ecrit.evaluation.impl.EvaluationPackageImpl;

public class AMRPersistencyManager {
	private static Resource resource = null;
	private static String xmiFilePath = "";
	
	public static Evaluation getEvaluations(String xmiPath){
		if (resource == null) {
			if (xmiPath != null && !xmiPath.isEmpty()) {
				xmiFilePath = xmiPath;
			}
			loadResource();
		}
		return (Evaluation) resource.getContents().get(0);
	}
	
	private static void loadResource(){
		EvaluationPackageImpl.init();
		EvaluationPackage.eINSTANCE.eClass();
		
		Resource.Factory.Registry register = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> map = register.getExtensionToFactoryMap();
		map.put("xmi", new XMIResourceFactoryImpl());
		
		try {
			URL url;
			if (xmiFilePath.isEmpty()) {
				url = AMRPersistencyManager.class.getResource("/model/evaluation.xmi");
			} else {
				File xmiFile = new File(xmiFilePath);
				url = xmiFile.toURI().toURL();
			}
			File xmiFile = new File(url.getPath());
			
			ResourceSet resourceSet = new ResourceSetImpl();
			resource = resourceSet.getResource(URI.createURI(xmiFile.toURI().toString()), true);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void save(){
		if (resource == null) {
			return;
		}
		
		try {
			Evaluation evaluation = (Evaluation) resource.getContents().get(0);
			evaluation.setEvaluationDate(Calendar.getInstance().getTime());
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
