package services.description.process;

import static services.description.DescriptionFactory.newProcessType;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
public class ProcessService {
	public static void main(Map<String, List<ValidationError>> errors)  throws DAOException{
		DAOHelpers.removeAll(ProcessType.class, ProcessType.find);
		DAOHelpers.removeAll(ProcessCategory.class, ProcessCategory.find);
		
		saveProcessCategories(errors);
		saveProcessTypes(errors);
	}

	public static void saveProcessCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessCategory> l = new ArrayList<ProcessCategory>();
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Pre-Banque", "pre-library"));
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Banque", "library"));
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Sequençage", "sequencing"));		
		DAOHelpers.saveModels(ProcessCategory.class, l, errors);
		
	}
	
	private static void saveProcessTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessType> l = new ArrayList<ProcessType>();
		l.add(newProcessType("Banque 300-600", "lib-300-600", ProcessCategory.find.findByCode("library"), getPropertyDefinitionsLib300600(), getExperimentTypes("fragmentation","librairie","amplification"), 
				getExperimentTypes("fragmentation").get(0), getExperimentTypes("amplification").get(0), getExperimentTypes("void-lib-300-600").get(0), getInstitutes(Institute.CODE.CNG, Institute.CODE.CNS)));
		DAOHelpers.saveModels(ProcessType.class, l, errors);
	}
	
	private static List<ExperimentType> getExperimentTypes(String...codes) throws DAOException {
		return DAOHelpers.getModelByCodes(ExperimentType.class,ExperimentType.find, codes);
	}
	
	//TODO
	// Key to validate
	public static List<PropertyDefinition> getPropertyDefinitionsLib300600() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//Valeur par defaut SPRI
        propertyDefinitions.add(newPropertiesDefinition("Robot","robotUsing", LevelService.getLevels(Level.CODE.Process),String.class, true,newValues("SPRI")));
     // //Measure par defaut ng
        propertyDefinitions.add(newPropertiesDefinition("Quantité à engager", "quantityUsing", LevelService.getLevels(Level.CODE.Process),Double.class, true,newValues("250","500")));		
		propertyDefinitions.add(newPropertiesDefinition("Objectif Expérience ", "goalExperiment", LevelService.getLevels(Level.CODE.Process),String.class, true,newValues("PE_400","Fragm_cDNA")));
		//Valeur par defaut 300-600pb
        propertyDefinitions.add(newPropertiesDefinition("Objectif Taille", "goalSize", LevelService.getLevels(Level.CODE.Process),String.class, true));
        propertyDefinitions.add(newPropertiesDefinition("Catégorie imputation", "imputationCategory", LevelService.getLevels(Level.CODE.Process),String.class, true,newValues("PRODUCTION","DEVELOPPEMENT")));
		return propertyDefinitions;
	}
	
	public static List<Institute> getInstitutes(Institute.CODE...codes) throws DAOException {
		List<Institute> institutes = new ArrayList<Institute>();
		for(Institute.CODE code : codes){
			institutes.add(Institute.find.findByCode(code.name()));
		}
		return institutes;
	}
}
