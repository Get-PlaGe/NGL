package services.description.run;

import java.io.File;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.run.description.TreatmentCategory;
import models.laboratory.run.description.TreatmentContext;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.run.description.TreatmentTypeContext;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.common.LevelService;

public class TreatmentService {

	public static void main(Map<String, List<ValidationError>> errors)  throws DAOException{		
		DAOHelpers.removeAll(TreatmentContext.class, TreatmentContext.find);
		DAOHelpers.removeAll(TreatmentType.class, TreatmentType.find);
		DAOHelpers.removeAll(TreatmentCategory.class, TreatmentCategory.find);		
		saveTreatmentCategory(errors);
		saveTreatmentContext(errors);
		saveTreatmentType(errors);	
	}
		
	public static void saveTreatmentCategory(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentCategory> l = new ArrayList<TreatmentCategory>();
		for (TreatmentCategory.CODE code : TreatmentCategory.CODE.values()) {
			l.add(DescriptionFactory.newSimpleCategory(TreatmentCategory.class, code.name(), code.name()));
		}
		DAOHelpers.saveModels(TreatmentCategory.class, l, errors);
	}
	
	public static void saveTreatmentContext(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentContext> l = new ArrayList<TreatmentContext>();
		l.add(DescriptionFactory.newTreatmentContext("Default","default"));
		l.add(DescriptionFactory.newTreatmentContext("Read1","read1"));
		l.add(DescriptionFactory.newTreatmentContext("Read2","read2"));
		l.add(DescriptionFactory.newTreatmentContext("Pairs","pairs"));
		l.add(DescriptionFactory.newTreatmentContext("Single","single"));
		
		DAOHelpers.saveModels(TreatmentContext.class, l, errors);
	}
	
	public static void saveTreatmentType(Map<String, List<ValidationError>> errors) throws DAOException {
		List<TreatmentType> l = new ArrayList<TreatmentType>();
		// common CNS - CNG
		l.add(DescriptionFactory.newTreatmentType("SAV","sav", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.sequencing.name()), "sav", 
				getSAVPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1", Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG, Institute.CODE.CNS), "10"));
		
		l.add(DescriptionFactory.newTreatmentType("NGSRG","ngsrg-illumina", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ngsrg.name()), "ngsrg", 
				getNGSRGPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG, Institute.CODE.CNS), "20"));		
		
		l.add(DescriptionFactory.newTreatmentType("Global","global", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.global.name()), "global", 
				getReadSetPropertyDefinitions(), 
				getTreatmentTypeContexts("default"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG, Institute.CODE.CNS), "0"));
		
		l.add(DescriptionFactory.newTreatmentType("Read Quality","read-quality", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "readQualityRaw,readQualityClean", 
				getReadQualityPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG, Institute.CODE.CNS), "30,83"));
		
		l.add(DescriptionFactory.newTreatmentType("Duplicates","duplicates", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "duplicatesRaw,duplicatesClean", 
				getDuplicatesPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG, Institute.CODE.CNS), "32,86"));
		
		l.add(DescriptionFactory.newTreatmentType("Mapping","mapping", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "mapping", 
				getMappingPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs", "default"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG, Institute.CODE.CNS), "90"));
		
		// specific CNS
		l.add(DescriptionFactory.newTreatmentType("Trimming","trimming", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "trimmingStd,trimmingVector", 
				getTrimmingPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), 
				getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "33,50"));
		
		l.add(DescriptionFactory.newTreatmentType("Contamination","contamination", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "contaminationColi,contaminationVector,contaminationPhiX", 
				getContaminationPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), 
				getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "35,36,60"));
		
		l.add(DescriptionFactory.newTreatmentType("Taxonomy","taxonomy", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "taxonomy", 
				getTaxonomyPropertyDefinitions(), 
				getTreatmentTypeContexts("read1"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "70"));
		
		l.add(DescriptionFactory.newTreatmentType("Sorting Ribo","sorting-ribo", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "sortingRibo", 
				getSortingRiboPropertyDefinitions(), 
				Arrays.asList(getTreatmentTypeContext("read1",Boolean.TRUE), getTreatmentTypeContext("read2", Boolean.FALSE), getTreatmentTypeContext("pairs", Boolean.FALSE), getTreatmentTypeContext("single", Boolean.FALSE)), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "80"));
		
		l.add(DescriptionFactory.newTreatmentType("Merging","merging", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "merging", 
				getMergingPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "100"));
		
		l.add(DescriptionFactory.newTreatmentType("Merging BA","merging-ba", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "mergingBA", 
				getMergingBAPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "110"));
		
		l.add(DescriptionFactory.newTreatmentType("Assembly","assembly", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "assembly", 
				getAssemblyPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "120"));
		
		l.add(DescriptionFactory.newTreatmentType("Scaffolding","scaffolding", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "scaffolding", 
				getScaffoldingPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "130"));

		l.add(DescriptionFactory.newTreatmentType("Gap Closing","gapClosing", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.ba.name()), "gapClosing", 
				getGapClosingPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNS), "140"));

		//specific CNG 
		l.add(DescriptionFactory.newTreatmentType("alignSingleRead BLAT","alignsingleread-blat", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "alignSingleReadBLATRaw", 
				getASRBPropertyDefinitions(), 
				getTreatmentTypeContexts("read1", "read2"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "50"));
		
		l.add(DescriptionFactory.newTreatmentType("alignSingleRead SOAP2","alignsingleread-soap2", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "alignSingleReadSOAP2Raw", 
				getASRSPropertyDefinitions(), 
				getTreatmentTypeContexts("read1", "read2"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "60"));
		
		l.add(DescriptionFactory.newTreatmentType("Exome","exome", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "exome", 
				getExomeTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "100"));
		
		l.add(DescriptionFactory.newTreatmentType("Whole Genome","whole-genome", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "wholeGenome", 
				getWholeExomeTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "110"));
		
		l.add(DescriptionFactory.newTreatmentType("RNAseq","rna-seq", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "RNAseq", 
				getRnaSeqTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "120"));
		
		l.add(DescriptionFactory.newTreatmentType("ChiPseq","chip-seq", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "ChiPseq", 
				getChiPSeqTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("read1"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "130"));
		
		l.add(DescriptionFactory.newTreatmentType("ChiPseq-PE","chipseq-pe", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "ChiPseqPE", 
				getChiPSeqPETreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "130"));
		
		l.add(DescriptionFactory.newTreatmentType("FAIREseq","faire-seq", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "FAIREseq", 
				getFaireSeqTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("read1"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "150"));
		
		//TODO : level parameter must be verified
		l.add(DescriptionFactory.newTreatmentType("Sample Control","sample-control", TreatmentCategory.find.findByCode(TreatmentCategory.CODE.quality.name()), "sampleControl", 
				getSampleControlTreatmentPropertyDefinitions(), 
				getTreatmentTypeContexts("pairs"), 
				DescriptionFactory.getInstitutes(Institute.CODE.CNG), "160"));
		DAOHelpers.saveModels(TreatmentType.class, l, errors);
	}
	
	private static List<TreatmentTypeContext> getTreatmentTypeContexts(String...codes) throws DAOException {
		List<TreatmentTypeContext> contexts = new ArrayList<TreatmentTypeContext>();
		for(String code : codes){
			contexts.add(getTreatmentTypeContext(code, Boolean.TRUE));
		}		
		return contexts;
	}

	private static TreatmentTypeContext getTreatmentTypeContext(String code, Boolean required) throws DAOException {
		TreatmentContext tc = DAOHelpers.getModelByCode(TreatmentContext.class, TreatmentContext.find, code);
		TreatmentTypeContext ttc = new TreatmentTypeContext(tc, required);
		return ttc;	
	}
	
	
	
	
	private static List<PropertyDefinition> getASRBPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mappedReads","mappedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mappedReadsPercent","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("reads100pcMatchBases","reads100pcMatchBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("reads100pcMatchBasesPerc","reads100pcMatchBasesPerc", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("reads90pcMatchBases","reads90pcMatchBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("reads90pcMatchBasesPerc","reads90pcMatchBasesPerc", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("alignedBases","alignedBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbErrors","nbErrors", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbInsertions","nbInsertions", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbDeletions","nbDeletions", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbMismatches","nbMismatches", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbPerfectReads","nbPerfectReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("coverageDistribution","coverageDistribution", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorPosition","errorPosition", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualityValueDistribution","qualityValueDistribution", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "single"));
		return propertyDefinitions;
	}
	

	private static List<PropertyDefinition> getASRSPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mappedReads","mappedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mappedReadsPercent","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("alignedBases","alignedBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbMismatches","nbMismatches", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mismatchesPercent","mismatchesPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorPosition","errorPosition", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getExomeTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbReads","nbReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mappedReads","mappedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mappedReadsPercent","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("pairingPercent","pairingPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("duplicatedReads","duplicatedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("duplicatedReadsPercent","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sizeRegionsTiled","sizeRegionsTiled", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("targetPercent","targetPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentNT30X","percentNT30X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentNT20X","percentNT20X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentNT10X","percentNT10X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentNT5X","percentNT5X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbRegions0X","nbRegions0X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("regions0XPercent","regions0XPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbRegionsFullCover","nbRegionsFullCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("regionsFullCoverPercent","regionsFullCoverPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("meanCover","meanCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("medianCover","medianCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sdCover","sdCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getWholeExomeTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbReads","nbReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mappedReads","mappedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mappedReadsPercent","mappedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("pairingPercent","pairingPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("duplicatedReads","duplicatedReads", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("duplicatedReadsPercent","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentNT30X","percentNT30X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentNT20X","percentNT20X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentNT10X","percentNT10X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentNT5X","percentNT5X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbRegions0X","nbRegions0X", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("regions0XPercent","regions0XPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("meanCover","meanCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("medianCover","medianCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sdCover","sdCover", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getRnaSeqTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedPairsPercent","storedPairsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedForwardReadPercent","storedForwardReadPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedReverseReadPercent","storedReverseReadPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rejectedPairsPercent","rejectedPairsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("alignedReadsPercent","alignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("uniqueReadsPercent","uniqueReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("uniqueAlignedReadsPercent","uniqueAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mismatchPercent","mismatchPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("duplicatedReadsPercent","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("insertLengthMean","insertLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("insertLengthMeanSd","insertLengthMeanSd", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("1SenseAlignedReadsPercent","1SenseAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("2SenseAlignedReadsPercent","2SenseAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNAPercent","rRNAPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("intragenicReadsPercent","intragenicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("exonicReadsPercent","exonicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("proteinCodingGenes","proteinCodingGenes", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("proteinCodingTranscripts","proteinCodingTranscripts", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("lncRNAGenes","lncRNAGenes", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("lncRNATranscripts","lncRNATranscripts", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getChiPSeqTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedReadsPercent","storedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rejectedReadsPercent","rejectedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("alignedReadsPercent","alignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("uniqueReadsPercent","uniqueReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("uniqueAlignedReadsPercent","uniqueAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mismatchPercent","mismatchPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("duplicatedReadsPercent","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("insertLengthMean","insertLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("insertLengthMeanSd","insertLengthMeanSd", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNAPercent","rRNAPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		return propertyDefinitions;
	}
		
	private static List<PropertyDefinition> getChiPSeqPETreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedReadsPercent","storedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedForwardReadPercent", "storedForwardReadPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedReverseReadPercent", "storedReverseReadPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rejectedReadsPercent", "rejectedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("alignedReadsPercent", "alignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("uniqueReadsPercent", "uniqueReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("uniqueAlignedReadsPercent", "uniqueAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mismatchPercent", "mismatchPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("duplicatedReadsPercent", "duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("insertLengthMean", "insertLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("insertLengthMeanSd", "insertLengthMeanSd", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("1SenseAlignedReadsPercent", "1SenseAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("2SenseAlignedReadsPercent", "2SenseAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNAPercent","rRNAPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("intragenicReadsPercent", "intragenicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("exonicReadsPercent", "exonicReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getFaireSeqTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedReadsPercent","storedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rejectedReadsPercent","rejectedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("alignedReadsPercent","alignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("uniqueReadsPercent","uniqueReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("uniqueAlignedReadsPercent","uniqueAlignedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mismatchPercent","mismatchPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("duplicatedReadsPercent","duplicatedReadsPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("insertLengthMean","insertLengthMean", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("insertLengthMeanSd","insertLengthMeanSd", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNAPercent","rRNAPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getSampleControlTreatmentPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//TODO : Level "pairs" must be confirmed 
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleSexe","sampleSexe", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("samplesComparison","samplesComparison", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getNGSRGPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        //Run level
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("flowcellPosition","flowcellPosition", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbCycle","nbCycle", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("flowcellVersion","flowcellVersion", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbClusterIlluminaFilter","nbClusterIlluminaFilter", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentClusterIlluminaFilter","percentClusterIlluminaFilter", LevelService.getLevels(Level.CODE.Run, Level.CODE.Lane, Level.CODE.Default), Double.class, false, "single"));        
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbBase","nbBase", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mismatch","mismatch", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Boolean.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("controlLane","controlLane", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Integer.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rtaVersion","rtaVersion", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbClusterTotal","nbClusterTotal", LevelService.getLevels(Level.CODE.Run, Level.CODE.Default), Long.class, true, "single"));
        //Lane & ReadSet level
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbCluster","nbCluster", LevelService.getLevels(Level.CODE.Lane, Level.CODE.ReadSet, Level.CODE.Default), Long.class, true, "single"));
        // Lane level
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("prephasing","prephasing", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("percentClusterInternalAndIlluminaFilter","percentClusterInternalAndIlluminaFilter", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Double.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("phasing","phasing", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), String.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbCycleReadIndex2","nbCycleReadIndex2", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Integer.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbCycleRead2","nbCycleRead2", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Integer.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbCycleRead1","nbCycleRead1", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Integer.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbCycleReadIndex1","nbCycleReadIndex1", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Integer.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbBaseInternalAndIlluminaFilter","nbBaseInternalAndIlluminaFilter", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbClusterInternalAndIlluminaFilter","nbClusterInternalAndIlluminaFilter", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("seqLossPercent","seqLossPercent", LevelService.getLevels(Level.CODE.Lane, Level.CODE.Default), Float.class, false, "single"));
        // ReadSet level
        //nbCluster define in the lane level for the 2 levels
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Q30","Q30", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Double.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbBases","nbBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("fraction","fraction", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Double.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualityScore","qualityScore", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Double.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nbReadIllumina","nbReadIllumina", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Integer.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("validSeqPercent","validSeqPercent", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Float.class, false, "single"));
                 
        return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getReadSetPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        // just readset level
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("usefulSequences","usefulSequences", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Long.class, true, "single"));
        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("usefulBases","usefulBases", LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), Long.class, true, "single"));
        return propertyDefinitions;
	}
	
	
	private static List<PropertyDefinition> getSAVPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("clusterDensity","clusterDensity",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("clusterDensityStd","clusterDensityStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("clusterPFPerc","clusterPFPerc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("clusterPFPercStd","clusterPFPercStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("phasing","phasing",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("prephasing","prephasing",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("reads","reads",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsPF","readsPF",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("greaterQ30Perc","greaterQ30Perc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("cyclesErrRated","cyclesErrRated",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("alignedPerc","alignedPerc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("alignedPercStd","alignedPercStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorRatePerc","errorRatePerc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorRatePercStd","errorRatePercStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorRatePercCycle35","errorRatePercCycle35",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1,Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorRatePercCycle35Std","errorRatePercCycle35Std",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorRatePercCycle75","errorRatePercCycle75",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorRatePercCycle75Std","errorRatePercCycle75Std",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorRatePercCycle100","errorRatePercCycle100",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("errorRatePercCycle100Std","errorRatePercCycle100Std",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("intensityCycle1","intensityCycle1",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("intensityCycle1Std","intensityCycle1Std",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1,Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("intensityCycle20Perc","intensityCycle20Perc",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("intensityCycle20PercStd","intensityCycle20PercStd",LevelService.getLevels(Level.CODE.Lane, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		return propertyDefinitions;
	}
		
	public static List<PropertyDefinition> getReadQualityPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("qualScore","qualScore",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nuclDistribution","nuclDistribution",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readWithNpercent","readWithNpercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readSizeDistribution","readSizeDistribution",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("adapterContamination","adapterContamination",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("adapters","adapters",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, false, "list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("GCDistribution","GCDistribution",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("positionN","positionN",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Image.class, true, "img"));				
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("suspectedKmers.Kmer","suspectedKmers.Kmer",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, false, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("suspectedKmers.nbOccurences","suspectedKmers.nbOccurences",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, false, "object_list"));		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("suspectedPrimers","suspectedPrimers",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, false, "list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("maxSizeReads","maxSizeReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("maxSizeReadsPercent","maxSizeReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Float.class, false, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getDuplicatesPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimateDuplicatedReads","estimateDuplicatedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimateDuplicatedReadsPercent","estimateDuplicatedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimateDuplicatedReadsNTimes.times","estimateDuplicatedReadsNTimes.times",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimateDuplicatedReadsNTimes.percent","estimateDuplicatedReadsNTimes.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimateUniqueReads","estimateUniqueReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimateUniqueReadsPercent","estimateUniqueReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Pairs), Float.class, true, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getTrimmingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sizeRange","sizeRange",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsInput","readsInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsOutput","readsOutput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsNoTrim","readsNoTrim",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsNoTrimPercent","readsNoTrimPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Float.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsTrim","readsTrim",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsTrimPercent","readsTrimPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nucleotidesTrim","nucleotidesTrim",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("trimRejectedShort ","trimRejectedShort",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("trimRejectedLength0","trimRejectedLength0",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("trimStored","trimStored",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedPairs","storedPairs",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rejectedPairs","rejectedPairs",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("storedSingleton","storedSingleton",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Single), Long.class, true, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getContaminationPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsInput","readsInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("removedReads","removedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("remainingReads","remainingReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("removedReadsPercent","removedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs, Level.CODE.Single), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("remainingNucleotides","remainingNucleotides",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Pairs), Long.class, false, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getTaxonomyPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("organism","organism",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("taxonomy","taxonomy",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("taxonBilan.taxon","taxonBilan.taxon",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("taxonBilan.nbSeq","taxonBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("taxonBilan.percent","taxonBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "single"));	
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("divisionBilan.division","divisionBilan.division",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true,
				DescriptionFactory.newValues("eukaryota","bacteria","cellular organisms","archaea","viruses"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("divisionBilan.nbSeq","divisionBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Long.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("divisionBilan.percent","divisionBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("keywordBilan.keyword","keywordBilan.keyword",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), String.class, true, 
				DescriptionFactory.newValues("mitochondri","virus","chloroplast","transposase",	"BAC"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("keywordBilan.nbSeq","keywordBilan.nbSeq",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Long.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("keywordBilan.percent","keywordBilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Float.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("krona","krona",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), File.class, true, "file"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("phylogeneticTree","phylogeneticTree",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1), Image.class, true, "img"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getSortingRiboPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("readsInput","readsInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("no_rRNA","no_rRNA",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNA","rRNA",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNAPercent","rRNAPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNABilan.type","rRNABilan.type",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), String.class, true,
				DescriptionFactory.newValues("PhiX", "Eukaryotic 18S", "Eukaryotic 28S", "Bacteria 16S", "Bacteria 23S", "Archeae 16S", "Archeae 23S", "Rfam 5.8S", "Rfam 5S"), "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("rRNABilan.percent","rRNABilan.percent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Read1, Level.CODE.Read2, Level.CODE.Single), Float.class, true, "object_list"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("usefulSequences","usefulSequences",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("usefulBases","usefulBases",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, false, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getMappingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nonChimericAlignedReads","nonChimericAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("FRAlignedReads","FRAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("RFAlignedReads","RFAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("FFAlignedReads","FFAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("RRAlignedReads","RRAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("transAlignedReads","transAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("singleAlignedReads","singleAlignedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("nonChimericAlignedReadsPercent","nonChimericAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("FRAlignedReadsPercent","FRAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("RFAlignedReadsPercent","RFAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("FFAlignedReadsPercent","FFAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("RRAlignedReadsPercent","RRAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("transAlignedReadsPercent","transAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("singleAlignedReadsPercent","singleAlignedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("MPReadDistanceSeparation","MPReadDistanceSeparation",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Image.class, true, "img"));		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimatedMPInsertSize","estimatedMPInsertSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("PEReadDistanceSeparation","PEReadDistanceSeparation",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Image.class, true, "img"));	
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("estimatedPEInsertSize","estimatedPEInsertSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Integer.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("reference","reference",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Default), String.class, true, "single"));
		return propertyDefinitions;		
	}
	
	public static List<PropertyDefinition> getMergingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("sampleInput","sampleInput",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mergedReads","mergedReads",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mergedReadsPercent","mergedReadsPercent",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("medianeSize","medianeSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("avgSize","avgSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("minSize","minSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("maxSize","maxSize",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("mergedReadsDistrib","mergedReadsDistrib",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Image.class, false, "img"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("overlapDistrib","overlapDistrib",LevelService.getLevels(Level.CODE.ReadSet, Level.CODE.Pairs), Image.class, true, "img"));
		return propertyDefinitions;		
	}
	
	
	public static List<PropertyDefinition> getMergingBAPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Reads input","readsInput", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Merged Reads","mergedReads", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% merged reads","mergedReadsPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Mediane size (bases)","medianeSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Average size (bases)","avgSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Min size (bases)","minSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Max size (bases)","maxSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Overlap distribution","overlapDistrib", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Image.class, true, "img"));
		return propertyDefinitions;		
	}
	
	
	public static List<PropertyDefinition> getAssemblyPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		
		//for further development
		/*
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N50 contig size","N50ContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of contigs","N50ContigNb", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N80 contig size","N80ContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of contigs","N80ContigNb", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N90 contig size","N90ContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of contigs","N90ContigNb", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Assembly size","assemblyContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of contigs","assemblyContigNb", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Smallest contig size","minContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Largest contig size","maxContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Average contig size","averageContigSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Float.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Size","contigSizeRepartition.size", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number","contigSizeRepartition.contigNumber", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent","contigSizeRepartition.contigPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cumulative size","contigSizeRepartition.cumulativeSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));	 
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% cumulative size","contigSizeRepartition.cumulativeSizePercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Float.class, true, "single"));
		*/
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Assembly statistics","assemblyStatistics",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percentage of assembled reads","readsAssembledPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Float.class, true, "single"));

		return propertyDefinitions;		
	}
	
	
	public static List<PropertyDefinition> getScaffoldingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();

		//for further development
		/*
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Path","path",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), String.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N50 scaffold size","N50ScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of scaffolds","N50ScaffoldNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N80 scaffold size","N80ScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of scaffolds","N80ScaffoldNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("N90 scaffold size","N90ScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of scaffolds","N90ScaffoldNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Assembly size","assemblyScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Long.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number of scaffolds","assemblyScaffoldNb",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Smallest scaffold size","minScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Largest scaffold size","maxScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Average scaffold size","averageScaffoldSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Float.class, true, "single"));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Size","scaffoldSizeRepartition.size", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Number","scaffoldSizeRepartition.scaffoldNumber", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Percent","scaffoldSizeRepartition.scaffoldPercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cumulative size","scaffoldSizeRepartition.cumulativeSize", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Integer.class, true, "single"));	 
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("% cumulative size","scaffoldSizeRepartition.cumulativeSizePercent", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Default), Float.class, true, "single"));
		*/
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Scaffolding statistics","scaffoldingStatistics",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), String.class, true, "single"));

		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nombre de paires satisfaisantes","nbPairedSatisfied",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nombre de paires non satisfaisantes","nbPairedUnsatisfied",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb paires mappées","nbMappedPairs",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb séquences mappées","nbMappedSequences",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Float.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille d'insert médiane","medianInsertSize",LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Float.class, true, "single"));
		
		return propertyDefinitions;		
	}
	
	
	public static List<PropertyDefinition> getGapClosingPropertyDefinitions() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Actual gap sum","actualGapSum", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Extended gap sum","extendGapSum", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Actual gap count","actualGapCount", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Extended gap count","extendGapCount", LevelService.getLevels(Level.CODE.Analysis, Level.CODE.Pairs), Long.class, true, "single"));

		return propertyDefinitions;		
	}
		
		
	
	
	
}

