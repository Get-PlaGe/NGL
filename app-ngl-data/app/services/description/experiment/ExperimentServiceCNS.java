package services.description.experiment;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.parameter.index.NanoporeIndex;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;

import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
public class ExperimentServiceCNS extends AbstractExperimentService {

	
	@SuppressWarnings("unchecked")
	public  void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProtocolCategory> l = new ArrayList<ProtocolCategory>();
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Developpement", "development"));
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Production", "production"));
		DAOHelpers.saveModels(ProtocolCategory.class, l, errors);

	}
	

	/**
	 * Save all ExperimentCategory
	 * @param errors
	 * @throws DAOException 
	 */
	public  void saveExperimentCategories(Map<String,List<ValidationError>> errors) throws DAOException{
		List<ExperimentCategory> l = new ArrayList<ExperimentCategory>();
		for (ExperimentCategory.CODE code : ExperimentCategory.CODE.values()) {
			l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, code.name(), code.name()));
		}
		DAOHelpers.saveModels(ExperimentCategory.class, l, errors);
	}


	public void saveExperimentTypes(
			Map<String, List<ValidationError>> errors) throws DAOException {
		List<ExperimentType> l = new ArrayList<ExperimentType>();
		//Depot Opgen
		l.add(newExperimentType("Ext to Run Opgen","ext-to-opgen-run",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), getPropertyDefinitionExtToOpgenDepot(), null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Depot Opgen", "opgen-depot",null,3500,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionOpgenDepot(), 
				getInstrumentUsedTypes("ARGUS"), "ManyToOne", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		//Illumina
		//Prepaflowcell : to finish
		l.add(newExperimentType("Ext to Run Illumina","ext-to-illumina-run",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		/*
		l.add(newExperimentType("Ext to qPCR","ext-to-qpcr",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to Solution-stock","ext-to-solution-stock",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		*/
		
		l.add(newExperimentType("Solution stock","solution-stock","STK",1000,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionSolutionStock(),
				getInstrumentUsedTypes("hand","tecan-evo-100"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	
		
		l.add(newExperimentType("Preparation flowcell", "prepa-flowcell",null,1200, 
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsPrepaflowcellCNS(),
				getInstrumentUsedTypes("cBot-interne","cBot"), "ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Prep. flowcell ordonnée", "prepa-fc-ordered",null,1300, 
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsPrepaflowcellOrdered(),
				getInstrumentUsedTypes("cBot"), "ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		

		
		l.add(newExperimentType("Depot Illumina", "illumina-depot",null, 1400,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),getPropertyDefinitionsIlluminaDepot(),
				getInstrumentUsedTypes("MISEQ","HISEQ2000","HISEQ2500","HISEQ4000"), "OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		//Nanopore
		l.add(newExperimentType("Ext to Frg, Lib ONT, Dépôt","ext-to-nanopore-process-library",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to Lib ONT, Dépôt","ext-to-nanopore-process-library-no-frg",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to Run Nanopore","ext-to-nanopore-run",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Fragmentation-réparation","nanopore-fragmentation","FRG",2100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyFragmentationNanopore(), getInstrumentUsedTypes("eppendorf-mini-spin-plus","hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		

		l.add(newExperimentType("Librairie ONT","nanopore-library","LIB",2200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyLibrairieNanopore(), getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Depot Nanopore","nanopore-depot",null,2300,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDepotNanopore(),
				getInstrumentUsedTypes("minion","mk1", "mk1b"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));

		
		
		l.add(newExperimentType("Aliquot","aliquoting",null,10100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()),
				getPropertyAliquoting(), getInstrumentUsedTypes("hand"),"OneToMany", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		//Bionano
		l.add(newExperimentType("Ext to NLRS, Irys chip, dépôt","ext-to-bionano-nlrs-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to Irys Chip, dépôt","ext-to-bionano-chip-process",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to Redépôt BioNano","ext-to-bionano-run",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
		l.add(newExperimentType("Irys Prep NLRS","irys-nlrs-prep",null,3100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionIrysPrepNLRS(),
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		
		l.add(newExperimentType("Préparation Irys CHIP","irys-chip-preparation",null,3200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionPreparationIrysChip(),
				getInstrumentUsedTypes("irys-hand"),"ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		
		l.add(newExperimentType("Dépôt BioNano","bionano-depot",null,3300,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionDepotBionano(),
				getInstrumentUsedTypes("IRYS"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
	
/*
			//quality control

			//purif
			l.add(newExperimentType("Ampure Non Ampli","ampure-na",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()),
					null, getInstrumentUsedTypes("hand"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			l.add(newExperimentType("Ampure Ampli","ampure-a",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), null,
					getInstrumentUsedTypes("hand"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

			//void
			l.add(newExperimentType("Ext to Banque","ext-to-library",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
			l.add(newExperimentType("Ext to prepa flowcell","ext-to-illumina-run",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			
			l.add(newExperimentType("Migration sur puce (ampli)","chip-migration-post-pcr",null,650,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsChipMigration(), 
					getInstrumentUsedTypes("agilent-2100-bioanalyzer", "labchipGX"),"OneToVoid", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			l.add(newExperimentType("Migration sur puce (non ampli)","chip-migration-pre-pcr",null,250,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsChipMigration(), 
					getInstrumentUsedTypes("agilent-2100-bioanalyzer", "labchipGX"),"OneToVoid", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			
			l.add(newExperimentType("Dosage fluorimétrique","fluo-quantification",null,450,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), null, 
					getInstrumentUsedTypes("qubit"),"OneToVoid", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			l.add(newExperimentType("Quantification qPCR","qPCR-quantification",null,850,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), null, 
					getInstrumentUsedTypes("rocheLightCycler-qPCR","stratagene-qPCR"),"OneToVoid", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 	
		*/	
		
		l.add(newExperimentType("Pool Tube","pool-tube",null,10200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionPoolTube(),
				getInstrumentUsedTypes("hand","tecan-evo-100"),"ManyToOne", false,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		
			l.add(newExperimentType("Pool générique","pool",null,10300,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionPoolTube(),
				getInstrumentUsedTypes("tecan-evo-100", "hand"),"ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
		if(ConfigFactory.load().getString("ngl.env").equals("UAT") ){
			
		}else if(ConfigFactory.load().getString("ngl.env").equals("DEV") ){
			
			
			
			/*
			l.add(newExperimentType("Ext to Banque","ext-to-library",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			*/
			l.add(newExperimentType("Ext to Norm, FC, Depot","ext-to-norm-fc-depot-illumina",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			l.add(newExperimentType("Ext to qPCR-norm, FC, Depot","ext-to-qpcr-norm-fc-depot-illumina",null,-1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			/*
			l.add(newExperimentType("Ampure Non Ampli","ampure-na",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()),
					null, getInstrumentUsedTypes("hand"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			l.add(newExperimentType("Ampure Ampli","ampure-a",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), null,
					getInstrumentUsedTypes("hand"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

			
			l.add(newExperimentType("Dosage fluorimétrique","fluo-quantification",null,450,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), null, 
					getInstrumentUsedTypes("qubit"),"OneToVoid", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			*/
			
			l.add(newExperimentType("Quantification qPCR","qpcr-quantification", null,20100,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsQPCR(), 
					getInstrumentUsedTypes("tecan-evo-100-and-stratagene-qPCR-system"),"OneToVoid", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS))); 
			/*
			l.add(newExperimentType("Migration sur puce (ampli)","chip-migration-post-pcr",null,650,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsChipMigration(), 
					getInstrumentUsedTypes("agilent-2100-bioanalyzer", "labchipGX"),"OneToVoid", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			l.add(newExperimentType("Migration sur puce (non ampli)","chip-migration-pre-pcr",null,250,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsChipMigration(), 
					getInstrumentUsedTypes("agilent-2100-bioanalyzer", "labchipGX"),"OneToVoid", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			
			l.add(newExperimentType("Fragmentation","fragmentation",null,200,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionFragmentation(),
					getInstrumentUsedTypes("hand","covaris-s2","covaris-e210"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
			
			l.add(newExperimentType("Librairie indexée","librairie-indexing",null,400,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsLibIndexing(),
					getInstrumentUsedTypes("hand","spri"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			*/

			l.add(newExperimentType("Amplification","amplification","PCR",800,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), null,
					getInstrumentUsedTypes("hand","thermocycler"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			l.add(newExperimentType("Sizing sur gel","sizing","GEL",900,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), null,
					null,"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
			
			//GA Experiment for plate transfert
			/*
			l.add(newExperimentType("Plate Transfert","plate-transfert",null,1,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), null,
					getInstrumentUsedTypes("tecan-evo-100"),"ManyToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			*/
			

		}
		
		DAOHelpers.saveModels(ExperimentType.class, l, errors);

	}

	public void saveExperimentTypeNodes(Map<String, List<ValidationError>> errors) throws DAOException {

		//newExperimentTypeNode("ext-to-qpcr", getExperimentTypes("ext-to-qpcr").get(0), false, false, false, null, null, null, null).save();	
		//newExperimentTypeNode("ext-to-solution-stock", getExperimentTypes("ext-to-solution-stock").get(0), false, false, false, null, null, null, null).save();
		
		newExperimentTypeNode("ext-to-opgen-run", getExperimentTypes("ext-to-opgen-run").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("opgen-depot",getExperimentTypes("opgen-depot").get(0),false,false, false,getExperimentTypeNodes("ext-to-opgen-run"),null,null,null).save();
		
		
		if(ConfigFactory.load().getString("ngl.env").equals("PROD") || ConfigFactory.load().getString("ngl.env").equals("UAT")){
			newExperimentTypeNode("solution-stock",getExperimentTypes("solution-stock").get(0),false, false,false,null,null,null,getExperimentTypes("pool")).save();
			
			
		}else if(ConfigFactory.load().getString("ngl.env").equals("DEV")){
			newExperimentTypeNode("ext-to-norm-fc-depot-illumina", getExperimentTypes("ext-to-norm-fc-depot-illumina").get(0), false, false, false, null, null, null, null).save();
			newExperimentTypeNode("ext-to-qpcr-norm-fc-depot-illumina", getExperimentTypes("ext-to-qpcr-norm-fc-depot-illumina").get(0), false, false, false, null, null, null, null).save();
			newExperimentTypeNode("amplification", getExperimentTypes("amplification").get(0), false, false, false, null, null, null, null).save();
			newExperimentTypeNode("sizing", getExperimentTypes("sizing").get(0), false, false, false, null, null, null, null).save();
			
			newExperimentTypeNode("qpcr-quantification", getExperimentTypes("qpcr-quantification").get(0), false, false, false, getExperimentTypeNodes("ext-to-qpcr-norm-fc-depot-illumina","sizing","amplification"), null, null, null).save();
			newExperimentTypeNode("solution-stock",getExperimentTypes("solution-stock").get(0),false, false,false,getExperimentTypeNodes("ext-to-qpcr-norm-fc-depot-illumina","ext-to-norm-fc-depot-illumina","sizing","amplification"),null,null,getExperimentTypes("pool", "pool-tube")).save();
			
		}
		
		newExperimentTypeNode("ext-to-illumina-run", getExperimentTypes("ext-to-illumina-run").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("prepa-flowcell",getExperimentTypes("prepa-flowcell").get(0),false, false,false,getExperimentTypeNodes("ext-to-illumina-run","solution-stock"),null,null,null).save();
		newExperimentTypeNode("prepa-fc-ordered",getExperimentTypes("prepa-fc-ordered").get(0),false, false,false,getExperimentTypeNodes("ext-to-illumina-run","solution-stock"),null,null,null).save();
		newExperimentTypeNode("illumina-depot",getExperimentTypes("illumina-depot").get(0),false, false,false,getExperimentTypeNodes("prepa-flowcell","prepa-fc-ordered"),	null,null,null).save();
		
		//Nanopore
		newExperimentTypeNode("ext-to-nanopore-run", getExperimentTypes("ext-to-nanopore-run").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-nanopore-process-library", getExperimentTypes("ext-to-nanopore-process-library").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-nanopore-process-library-no-frg", getExperimentTypes("ext-to-nanopore-process-library-no-frg").get(0), false, false, false, null, null, null, null).save();
		
		if(ConfigFactory.load().getString("ngl.env").equals("PROD") || ConfigFactory.load().getString("ngl.env").equals("UAT") ){
			newExperimentTypeNode("nanopore-fragmentation",getExperimentTypes("nanopore-fragmentation").get(0),false, false,false,getExperimentTypeNodes("ext-to-nanopore-process-library"),null,null,getExperimentTypes("aliquoting")).save();
			
		}else if(ConfigFactory.load().getString("ngl.env").equals("DEV") ){
			newExperimentTypeNode("nanopore-fragmentation",getExperimentTypes("nanopore-fragmentation").get(0),false, false,false,getExperimentTypeNodes("ext-to-nanopore-process-library"),null,getExperimentTypes("qpcr-quantification"),getExperimentTypes("aliquoting")).save();
		}
		newExperimentTypeNode("nanopore-library",getExperimentTypes("nanopore-library").get(0),false, false,false,getExperimentTypeNodes("ext-to-nanopore-process-library-no-frg","nanopore-fragmentation"),null,null,getExperimentTypes("pool-tube")).save();
		newExperimentTypeNode("nanopore-depot",getExperimentTypes("nanopore-depot").get(0),false, false,false,getExperimentTypeNodes("nanopore-library","ext-to-nanopore-run"),null,null,null).save();
		
		//Bionano
		newExperimentTypeNode("ext-to-bionano-nlrs-process", getExperimentTypes("ext-to-bionano-nlrs-process").get(0), false, false, null, null, null).save();	
		newExperimentTypeNode("ext-to-bionano-chip-process", getExperimentTypes("ext-to-bionano-chip-process").get(0), false, false, null, null, null).save();	
		newExperimentTypeNode("ext-to-bionano-run", getExperimentTypes("ext-to-bionano-run").get(0), false, false, null, null, null).save();	

		newExperimentTypeNode("irys-nlrs-prep",getExperimentTypes("irys-nlrs-prep").get(0),false,false,getExperimentTypeNodes("ext-to-bionano-nlrs-process"),null,null).save();
		newExperimentTypeNode("irys-chip-preparation",getExperimentTypes("irys-chip-preparation").get(0),false,false,getExperimentTypeNodes("ext-to-bionano-chip-process","irys-nlrs-prep"),null,null).save();
		newExperimentTypeNode("bionano-depot",getExperimentTypes("bionano-depot").get(0),false,false,getExperimentTypeNodes("ext-to-bionano-run","irys-chip-preparation"),null,null).save();
					

		//	newExperimentTypeNode("solution-stock",getExperimentTypes("solution-stock").get(0),false,false,getExperimentTypeNodes("ext-to-qpcr","amplification"),
			//		null,null).save();
			/*
			if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){
				
				newExperimentTypeNode("ext-to-library", getExperimentTypes("ext-to-library").get(0), false, false, null, null, null).save();
				newExperimentTypeNode("ext-to-qPCR-norm-fc-depot-illumina", getExperimentTypes("ext-to-qpcr-norm-fc-depot-illumina").get(0), false, false, null, null, null).save();
				newExperimentTypeNode("ext-to-norm-fc-depot-illumina", getExperimentTypes("ext-to-norm-fc-depot-illumina").get(0), false, false, null, null, null).save();
				
				//REM : experimentTypes list confirmées par Julie
				newExperimentTypeNode("fragmentation", getExperimentTypes("fragmentation").get(0), false, false, getExperimentTypeNodes("ext-to-library"), 
						getExperimentTypes("ampure-na"),  getExperimentTypes("fluo-quantification","chip-migration-pre-pcr")).save();
				
				newExperimentTypeNode("librairie-indexing", getExperimentTypes("librairie-indexing").get(0), false, false, getExperimentTypeNodes("fragmentation"), 
						getExperimentTypes("ampure-na"), getExperimentTypes("fluo-quantification","chip-migration-pre-pcr")).save();
				
				newExperimentTypeNode("librairie-dualindexing", getExperimentTypes("librairie-dualindexing").get(0), false, false, getExperimentTypeNodes("fragmentation"), 
						getExperimentTypes("ampure-na"), getExperimentTypes("fluo-quantification","chip-migration-pre-pcr")).save();			
				
				newExperimentTypeNode("amplification", getExperimentTypes("amplification").get(0), false, false, getExperimentTypeNodes("librairie-indexing"), 
						getExperimentTypes("ampure-a"), getExperimentTypes("fluo-quantification","chip-migration-post-pcr","qpcr-quantification")).save();
				
				newExperimentTypeNode("sizing", getExperimentTypes("sizing").get(0), false, false, getExperimentTypeNodes("amplification"), 
						null, null).save();
				
				newExperimentTypeNode("qpcr-quantification",getExperimentTypes("qpcr-quantification").get(0),false,false,getExperimentTypeNodes("amplification"),null,null).save();
					
			}
		*/

	}

	private List<PropertyDefinition> getPropertyDefinitionsQPCR() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), MeasureUnit.find.findByCode( "nM"), MeasureUnit.find.findByCode("nM"),
				"single", 11, true, null, "2"));		
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), MeasureUnit.find.findByCode("ng/µl"), MeasureUnit.find.findByCode("ng/µl"),
				"single", 12, true, null, "2"));		
		
		return propertyDefinitions;
	}


	private List<PropertyDefinition> getPropertyDefinitionDepotBionano() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single",100));

		propertyDefinitions.add(newPropertiesDefinition("FC active","flowcellActiveOnChip", LevelService.getLevels(Level.CODE.ContainerIn),Boolean.class, false, null, null, null, null,"single", 8, true,"true", null));
		propertyDefinitions.add(newPropertiesDefinition("Relance optimisation","optimizationRedo", LevelService.getLevels(Level.CODE.ContainerIn),Boolean.class, false, null, null, null, null,"single", 8, true,"false", null));

		propertyDefinitions.add(newPropertiesDefinition("Laser","laser", LevelService.getLevels(Level.CODE.ContainerIn),String.class, false, DescriptionFactory.newValues("vert","rouge","vert et rouge"), null, null, null,"single", 9, true));


		propertyDefinitions.add(newPropertiesDefinition("Temps de concentration recommandé","ptrConcentrationTime", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",10, true));
		propertyDefinitions.add(newPropertiesDefinition("Temps de concentration réel","realConcentrationTime", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, DescriptionFactory.newValues("600","450","400","350","300","250","200","150"),
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",11, true));
		propertyDefinitions.add(newPropertiesDefinition("Nombre de cycles","nbCycles", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null, null, null, null,"single", 12, true));
		//Image 
		propertyDefinitions.add(newPropertiesDefinition("Photo zone pillar","pillarRegionPicture", LevelService.getLevels(Level.CODE.ContainerIn),Image.class, false, null, null, null, null,"img", 13, true));
		propertyDefinitions.add(newPropertiesDefinition("Voltage","voltage", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null, null, null, null,"single", 14, true));
		propertyDefinitions.add(newPropertiesDefinition("Nb d'optimisations","nbOfOptimizations", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, DescriptionFactory.newValues("2","3","4"), null, null, null,"single", 15, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Optimized Bump Time","optimizedBumpTime", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",16, true));
		propertyDefinitions.add(newPropertiesDefinition("Optimized Load Time","optimizedLoadTime", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",17, true)); 
		propertyDefinitions.add(newPropertiesDefinition("Résumé manips","manipSummarize", LevelService.getLevels(Level.CODE.ContainerIn),File.class, false, null, null, null, null,"file", 20, true));
		

		return propertyDefinitions;
	}


	private List<PropertyDefinition> getPropertyDefinitionPreparationIrysChip() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",51,true,"8", null));		
		return propertyDefinitions;
	}


	private List<PropertyDefinition> getPropertyDefinitionIrysPrepNLRS() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",8, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",9, true));

		propertyDefinitions.add(newPropertiesDefinition("Quantité prévue par le protocole","requiredQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",11, true));
		//File
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Tableau sélection enzyme","enzymeLabelDensity",LevelService.getLevels(Level.CODE.ContainerIn), File.class, false, "file", 12));

		propertyDefinitions.add(newPropertiesDefinition("Enzyme de restriction", "restrictionEnzyme", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,DescriptionFactory.newValues("BspQI","Bsm1","BbvCI","BsrD1"), null, "single",13));
		
		propertyDefinitions.add(newPropertiesDefinition("Unités d'enzyme","enzymeUnit", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null, null, null, null,"single", 14, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume Enzyme","restrictionEnzymeVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",15, true));

		propertyDefinitions.add(newPropertiesDefinition("Enzyme de restriction 2", "restrictionEnzyme2", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false,DescriptionFactory.newValues("BspQI","Bsm1","BbvCI","BsrD1"), null, "single",16));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume Enzyme 2","restrictionEnzyme2Volume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",17, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Unités d'enzyme 2","enzyme2Unit", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null, null, null, null,"single", 18, true));
		
		propertyDefinitions.add(newPropertiesDefinition("Concentration 1", "measuredConc1", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",19, true));

		propertyDefinitions.add(newPropertiesDefinition("Concentration 2", "measuredConc2", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",20, true));

		propertyDefinitions.add(newPropertiesDefinition("Concentration 3", "measuredConc3", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",21, true));

		propertyDefinitions.add(newPropertiesDefinition("Concentration arrondie", "nlrsConcentration", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",24, true));

		propertyDefinitions.add(newPropertiesDefinition("CV","variationCoefficient", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null, null, null, null,"single", 25, true));

		return propertyDefinitions;
	}



	
	private static List<PropertyDefinition> getPropertyAliquoting() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µl"),MeasureUnit.find.findByCode( "µl"), "single",10, false));
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyFragmentationNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb fragmentations","fragmentionNumber",LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null
				, null ,null,null, "single",11));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté totale dans frg","inputFrgQuantity",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Double.class, true,  null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode("ng"),MeasureUnit.find.findByCode( "ng"), "single",12));
		
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Conc. finale FRG","postFrgConcentration",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",13));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté finale FRG","postFrgQuantity",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",14));
		/*
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Profil","fragmentionProfile",LevelService.getLevels(Level.CODE.ContainerOut), Image.class, false, null
				,null,null,null, "img",15));
		 */
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille réelle","measuredLibrarySize",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"), "single",16));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb réparations","preCRNumber",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, null
				, null ,null,null, "single",17));
		/*propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Conc. finale preCR","postPreCRConcentration",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",8));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté finale preCR","postPreCRQuantity",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false,null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",9));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Volume final","measuredVolume",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µl"),MeasureUnit.find.findByCode( "µl"), "single",10));
		*/
		return propertyDefinitions;

	}

	
	private static List<PropertyDefinition> getPropertyDepotNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single",300));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("PDF Report","report",LevelService.getLevels(Level.CODE.Experiment), File.class, false, "file", 400));

        // Unite a verifier
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date creation","loadingReport.creationDate",LevelService.getLevels(Level.CODE.ContainerIn), Date.class, false, "object_list",600));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Heure dépot","loadingReport.hour",LevelService.getLevels(Level.CODE.ContainerIn), String.class, false,"object_list",601));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Temps","loadingReport.time",LevelService.getLevels(Level.CODE.ContainerIn), Long.class, false,null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),MeasureUnit.find.findByCode( "h"),MeasureUnit.find.findByCode( "h"), "object_list",602));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Volume","loadingReport.volume",LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "object_list",603));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Groupe","qcFlowcell.group",LevelService.getLevels(Level.CODE.ContainerOut), String.class, false, false, "object_list",700));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb pores actifs à réception","qcFlowcell.preLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, "object_list",701));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb pores actifs lors du dépôt","qcFlowcell.postLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, "object_list",702));
		
		//propertyDefinitions.add(newPropertiesDefinition("Channels with Reads", "minknowChannelsWithReads", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",301));
        //propertyDefinitions.add(newPropertiesDefinition("Events in Reads", "minknowEvents", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, "single",302));
        //propertyDefinitions.add(newPropertiesDefinition("Complete reads", "minknowCompleteReads", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",303));
        //propertyDefinitions.add(newPropertiesDefinition("Read count", "metrichorReadCount", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",304));
        //propertyDefinitions.add(newPropertiesDefinition("Total 2D yield", "metrichor2DReadsYield", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null
		//		, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"), "single",305));
        //propertyDefinitions.add(newPropertiesDefinition("Longest 2D read", "metrichorMax2DRead", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null
		//		, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),"single",306));
        //propertyDefinitions.add(newPropertiesDefinition("Peak 2D quality score", "metrichorMax2DQualityScore", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, "single",307));

		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getPropertyLibrairieNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µl"),MeasureUnit.find.findByCode( "µl"), "single",10));
		propertyDefinitions.add(newPropertiesDefinition("Qté engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",11));
//		propertyDefinitions.add(newPropertiesDefinition("Taille", "librarySize", LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, true, null
	//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "kb"),MeasureUnit.find.findByCode( "kb"), "single",8));
		
		propertyDefinitions.add(newPropertiesDefinition("Tag","tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class, false, getTagNanopore(), "single",13));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class, false, getTagCategoriesNanopore(),"SINGLE-INDEX", "single",14));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Conc. finale End Repair","postEndRepairConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",15));
		propertyDefinitions.add(newPropertiesDefinition("Qté finale End Repair","postEndRepairQuality", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",20));

		propertyDefinitions.add(newPropertiesDefinition("Conc. finale dA tailing","postTailingConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",30));
		propertyDefinitions.add(newPropertiesDefinition("Qté finale dA tailing","postTailingQuality", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",40));

		
/*		propertyDefinitions.add(newPropertiesDefinition("Conc. finale Ligation","measuredConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",50));*/
		// Problème code car doit etre measuredquantity
		
		propertyDefinitions.add(newPropertiesDefinition("Qté finale Ligation","ligationQuantity", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",60)); 

		
		return propertyDefinitions;
	}

	private static List<Value> getTagNanopore() {
		List<NanoporeIndex> indexes = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, NanoporeIndex.class, DBQuery.is("typeCode", "index-nanopore-sequencing")).sort("name").toList();
		List<Value> values = new ArrayList<Value>();
		indexes.forEach(index -> {
			values.add(DescriptionFactory.newValue(index.code, index.name));	
		});
		
		return values;
	}

	private static List<Value> getTagCategoriesNanopore(){
		List<Value> values = new ArrayList<Value>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));		
		return values;	
	}

	private static List<PropertyDefinition> getPropertyDefinitionFragmentation() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
		return propertyDefinitions;
	}
	
	// GA separation getPropertyDefinitionsPrepaflowcellCNS / getPropertyDefinitionsPrepaflowcellCNG pour JIRA 676
	//  ==> feuille de calcul differentes pour la prepaflowcell entre CNS et CNG
	private static List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellCNS() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume sol. stock dans dénat.", "requiredVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",11, false));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null 
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",12,true,"1", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Conc. solution NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,DescriptionFactory.newValues("1N","2N"), "2N", "single",13));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume EB", "EBVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",14, false));

		propertyDefinitions.add(newPropertiesDefinition("Conc. dénat. ", "finalConcentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",15,true,"2", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume dénat.", "finalVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "20"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",16));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume dénat. dans dilution", "requiredVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21, false));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume HT1", "HT1Volume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",22, false));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume Phix", "phixVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",23, false));

		propertyDefinitions.add(newPropertiesDefinition("Conc. sol. mère Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "pM"),MeasureUnit.find.findByCode( "nM"),"single",24, true,"0.02", null));

		propertyDefinitions.add(newPropertiesDefinition("Conc. dilution", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null 
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "pM"),MeasureUnit.find.findByCode( "nM"), "single",25));

		propertyDefinitions.add(newPropertiesDefinition("Volume dilution", "finalVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "1000"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",26));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume dilution sur la piste", "requiredVolume3", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",31, false));		
		
		
		//Outputcontainer
		propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null, null, null, null, "single",51,false,"1", null));		
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",52,false));
		
				
		return propertyDefinitions;
	}

	
	private List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellOrdered() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
						MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",11, false));
		propertyDefinitions.add(newPropertiesDefinition("Vol. PhiX", "phixVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",12, false));
		propertyDefinitions.add(newPropertiesDefinition("Vol. RSB", "rsbVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",13, false));
		//Add list value
		propertyDefinitions.add(newPropertiesDefinition("Conc. Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, getPhixConcentrationCodeValues(), null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",14));
		propertyDefinitions.add(newPropertiesDefinition("Concentration dilution", "finalConcentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",15,true));
		propertyDefinitions.add(newPropertiesDefinition("Volume dilution", "finalVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",16));
		propertyDefinitions.add(newPropertiesDefinition("Vol. dil. ds dénat", "inputVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21));

		propertyDefinitions.add(newPropertiesDefinition("Vol. NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",22));
		
		propertyDefinitions.add(newPropertiesDefinition("Conc. NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,null, null, null, null, "single",23,true,"0.1N", null));
		propertyDefinitions.add(newPropertiesDefinition("Vol. TrisHCL", "trisHCLVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",24));
		propertyDefinitions.add(newPropertiesDefinition("Conc. TrisHCL", "trisHCLConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "200000000" 
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "mM"),MeasureUnit.find.findByCode( "nM"), "single",25));
		propertyDefinitions.add(newPropertiesDefinition("Vol. master EPX", "masterEPXVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "35"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",26));
		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true,  null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "pM"),MeasureUnit.find.findByCode( "nM"),"single",27,false));

		
		//OuputContainer
		//keep order declaration between phixPercent and finalVolume
		propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null, null, null, null, "single",51,false,"1", null));		
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",28,false));
		
		return propertyDefinitions;
		
	}
	
	private static List<Value> getPhixConcentrationCodeValues(){
        List<Value> values = new ArrayList<Value>();
        values.add(DescriptionFactory.newValue("0.1","100"));
        values.add(DescriptionFactory.newValue("0.2","200"));
        values.add(DescriptionFactory.newValue("0.3","300"));
        return values;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsLibIndexing() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//Ajouter la liste des index illumina
		propertyDefinitions.add(newPropertiesDefinition("Tag","tag", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true, getTagCategories(),"single"));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
		return propertyDefinitions;
	}

	

	//TODO
	// Propriete taille en output et non en input ?
	// Valider les keys
	public static List<PropertyDefinition> getPropertyDefinitionsChipMigration() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		// A supprimer une fois le type de support category sera géré
		propertyDefinitions.add(newPropertiesDefinition("Position","position", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));		
		propertyDefinitions.add(newPropertiesDefinition("Taille", "size", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, true,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), MeasureUnit.find.findByCode("kb"), MeasureUnit.find.findByCode("kb"), "single"));
		// Voir avec Guillaume comment gérer les fichiers
		propertyDefinitions.add(newPropertiesDefinition("Profil DNA HS", "fileResult", LevelService.getLevels(Level.CODE.ContainerOut),String.class, true, "single"));
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//Utiliser par import ngl-data CNG de creation des depot-illumina
		//propertyDefinitions.add(newPropertiesDefinition("Code LIMS", "limsCode", LevelService.getLevels(Level.CODE.Experiment), Integer.class, false, "single"));	
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single"));
		return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getPropertyDefinitionExtToOpgenDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, false, "single"));
		return propertyDefinitions;
	}	
	
	private static List<PropertyDefinition> getPropertyDefinitionOpgenDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single"));
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionSolutionStock() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume à engager", "requiredVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, "IP",
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21, false,null, "1"));
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, "IP"
				,null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",22, false,null,"1"));
		
		//Outputcontainer
/*		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode("nM"),"single",7,true,"10.0"));		
		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",8,true)); */
		return propertyDefinitions; 
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionPoolTube() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//InputContainer
		
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 20, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 25, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null, 
				"single", 30, true, null,null));
		
	/*	propertyDefinitions.add(newPropertiesDefinition("Volume tampon à rajouter", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",9, false)); */
		//Outputcontainer
		/*	propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",13,true));
		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",14,true));	*/	
		
		return propertyDefinitions;
	}	

}
