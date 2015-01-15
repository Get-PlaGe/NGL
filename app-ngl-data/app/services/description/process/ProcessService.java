package services.description.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.typesafe.config.ConfigFactory;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.common.LevelService;


public class ProcessService {
	public static void main(Map<String, List<ValidationError>> errors)  throws DAOException{
		DAOHelpers.removeAll(ProcessType.class, ProcessType.find);
		DAOHelpers.removeAll(ProcessCategory.class, ProcessCategory.find);

		saveProcessCategories(errors);
		saveProcessTypes(errors);
	}

	public static void saveProcessCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessCategory> l = new ArrayList<ProcessCategory>();
		if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){
			l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Banque", "library"));
			l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Pre-Sequencage", "pre-sequencing"));
			l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Pre-Banque", "pre-library"));
			l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Sequençage", "sequencing"));
		}
		
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Optical mapping", "mapping"));
		DAOHelpers.saveModels(ProcessCategory.class, l, errors);

	}

	private static void saveProcessTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessType> l = new ArrayList<ProcessType>();
		
		if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){

			l.add(DescriptionFactory.newProcessType("Librairie PE sans sizing", "Lib-PE-NoSizing", ProcessCategory.find.findByCode("library"), getPropertyDefinitionsLib300600(), getExperimentTypes("fragmentation","librairie-indexing","amplification"), 
					getExperimentTypes("fragmentation").get(0), getExperimentTypes("amplification").get(0), getExperimentTypes("ext-to-library").get(0), DescriptionFactory.getInstitutes(Institute.CODE.CNS)));
			l.add(DescriptionFactory.newProcessType("qPCR et normalisation", "qPCR-normalisation", ProcessCategory.find.findByCode("pre-sequencing"), getPropertyDefinitionsIlluminaDepot(), getExperimentTypes("solution-stock"), 
					getExperimentTypes("solution-stock").get(0), getExperimentTypes("solution-stock").get(0), getExperimentTypes("ext-to-qpcr").get(0), DescriptionFactory.getInstitutes(Institute.CODE.CNS)));
			l.add(DescriptionFactory.newProcessType("Run Illumina", "illumina-run", ProcessCategory.find.findByCode("sequencing"),getPropertyDefinitionsIlluminaDepot() , getExperimentTypes("prepa-flowcell","illumina-depot"), getExperimentTypes("prepa-flowcell").get(0), getExperimentTypes("illumina-depot").get(0),getExperimentTypes("ext-to-prepa-flowcell").get(0), DescriptionFactory.getInstitutes(Institute.CODE.CNS)));

//			l.add(DescriptionFactory.newProcessType("Run Illumina", "illumina-run-CNG", ProcessCategory.find.findByCode("sequencing"),getPropertyDefinitionsIlluminaDepot() , getExperimentTypes("prepa-flowcell-CNG","illumina-depot-CNG"), getExperimentTypes("prepa-flowcell-CNG").get(0), getExperimentTypes("illumina-depot-CNG").get(0),getExperimentTypes("ext-to-prepa-flowcell").get(0), DescriptionFactory.getInstitutes(Institute.CODE.CNG)));
				
		}
		
		l.add(DescriptionFactory.newProcessType("Run Opgen", "opgen-run", ProcessCategory.find.findByCode("mapping"),null , getExperimentTypes("opgen-depot"), getExperimentTypes("opgen-depot").get(0), getExperimentTypes("opgen-depot").get(0),getExperimentTypes("ext-to-opgen-depot").get(0), DescriptionFactory.getInstitutes(Institute.CODE.CNS)));
		DAOHelpers.saveModels(ProcessType.class, l, errors);
	}


	/*private static List<PropertyDefinition> getPropertyDefinitionsOpgenDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type MapCard","mapcardType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("standard","HD"), "single"));
		return propertyDefinitions;
	}*/

	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();

		//TO do multi value
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type séquencage","sequencingType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("GAIIx", "Hiseq 2000", "Hiseq 2500 normal" , "Hiseq 2500 rapide" ,"Miseq"), "single"));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type de lecture", "readType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("SR","PE"), "single"));		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Longueur de lecture", "readLength"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("50","100","150","250","300","500","600"), "single"));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type séquenceur", "sequencerType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("GAIIx","HISEQ2000","HISEQ2500","MISEQ"), "single"));

		return propertyDefinitions;
	}

	private static List<ExperimentType> getExperimentTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentType.class,ExperimentType.find, codes);
	}


	public static List<PropertyDefinition> getPropertyDefinitionsLib300600() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Robot à utiliser","autoVsManuel", LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("MAN","ROBOT"), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Quantité à engager", "objInputProcess", LevelService.getLevels(Level.CODE.Process),Double.class, true, DescriptionFactory.newValues("250","500"), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Ojectif Taille de banque finale", "objSize", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("300-600","300-800"),"300-600", "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Catégorie Imputation", "imputationCat", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("PRODUCTION","DEVELOPPEMENT"), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date limite", "dateLimite", LevelService.getLevels(Level.CODE.Process),Date.class,false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Indexing", "indexing", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("Single indexing","Dual indexing","Pas d'indexing"), "single"));
		return propertyDefinitions;
	}



}
