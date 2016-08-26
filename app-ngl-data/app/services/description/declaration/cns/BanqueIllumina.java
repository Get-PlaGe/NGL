package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.declaration.AbstractDeclaration;

public class BanqueIllumina extends AbstractDeclaration {

	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<ExperimentType>();
		
		l.add(newExperimentType("Ext to Bq DNA Illumina (non sizée)","ext-to-dna-illumina-indexed-library-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Bq DNA Illumina sizée","ext-to-dna-illumina-indexed-lib-sizing-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return l;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l = new ArrayList<ProcessType>();
		
		l.add(DescriptionFactory.newProcessType("Bq DNA Illumina à partir de frg", "dna-illumina-indexed-library-process", ProcessCategory.find.findByCode("library"), getPropertyBanqueIlluminaWithoutSizing(),
			Arrays.asList(getPET("ext-to-dna-illumina-indexed-library-process",-1)
					,getPET("fragmentation",-1)
					,getPET("tag-pcr",-1)
					,getPET("dna-illumina-indexed-library",0)
					,getPET("pcr-amplification-and-purification",1)
					,getPET("solution-stock",2)
					,getPET("prepa-flowcell",3)
					,getPET("prepa-fc-ordered",3)
					,getPET("illumina-depot",5)), 
			getExperimentTypes("dna-illumina-indexed-library").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-dna-illumina-indexed-library-process").get(0), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));


			l.add(DescriptionFactory.newProcessType("Bq DNA Illumina sizée à partir de frg", "dna-illumina-indexed-lib-sizing-process", ProcessCategory.find.findByCode("library"), getPropertyBanqueIlluminaSizing(),
					Arrays.asList(getPET("ext-to-dna-illumina-indexed-lib-sizing-process",-1)
							,getPET("fragmentation",-1)
							,getPET("tag-pcr",-1)
							,getPET("dna-illumina-indexed-library",0)
							,getPET("pcr-amplification-and-purification",1)
							,getPET("sizing",2)
							,getPET("solution-stock",3)
							,getPET("prepa-flowcell",4)
							,getPET("prepa-fc-ordered",4)
							,getPET("illumina-depot",5)), 
					getExperimentTypes("dna-illumina-indexed-library").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-dna-illumina-indexed-lib-sizing-process").get(0), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
					
			return l;

	}

	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		return null;
	}

	private List<PropertyDefinition> getPropertyBanqueIlluminaWithoutSizing() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();	
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, getBanqueIlluminaDA(), 
				null,null,null,"single", 13, true, null, null));
		propertyDefinitions.addAll(RunIllumina.getPropertyDefinitionsIlluminaDepotCNS());
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyBanqueIlluminaSizing() {
			List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();	
			propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, getBanqueIlluminaDB(), 
					null,null,null,"single", 13, true, null, null));
			propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 1", "sizingGoal", LevelService.getLevels(Level.CODE.Process), String.class, true, null, DescriptionFactory.newValues("ss0.6/0.53","ss0.7/0.58","500-650"), 
					null,null,null,"single", 17, true, null, null));
			propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 2", "sizingGoal2", LevelService.getLevels(Level.CODE.Process), String.class, false, null, DescriptionFactory.newValues("650-800"), 
					null,null,null,"single", 18, true, null, null));
			propertyDefinitions.addAll(RunIllumina.getPropertyDefinitionsIlluminaDepotCNS());
			return propertyDefinitions;
	}

	
	private List<Value> getBanqueIlluminaDA() {
		List<Value> values = new ArrayList<Value>();
		values.add(DescriptionFactory.newValue("DA", "DA - DNAseq"));
		return values;
	}
	
	private List<Value> getBanqueIlluminaDB(){
		List<Value> values = new ArrayList<Value>();
		values.add(DescriptionFactory.newValue("DB", "DB - DNAseq avec sizing"));
		return values;
	}
	
	
	@Override
	protected List<ProcessType> getProcessTypePROD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void getExperimentTypeNodeCommon() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getExperimentTypeNodeDEV() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getExperimentTypeNodePROD() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
		// TODO Auto-generated method stub
		
	}

}
