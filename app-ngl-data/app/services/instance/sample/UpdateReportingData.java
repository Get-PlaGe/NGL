package services.instance.sample;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;

import controllers.migration.OneToVoidContainer;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.sample.instance.Sample;
import models.laboratory.sample.instance.reporting.SampleExperiment;
import models.laboratory.sample.instance.reporting.SampleProcess;
import models.laboratory.sample.instance.reporting.SampleProcessesStatistics;
import models.laboratory.sample.instance.reporting.SampleReadSet;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.Logger;
import play.api.modules.spring.Spring;
import rules.services.RulesException;
import scala.concurrent.duration.FiniteDuration;

import services.instance.AbstractImportData;
import workflows.process.ProcWorkflowHelper;
import static workflows.process.ProcWorkflowHelper.TAG_PROPERTY_NAME;

public class UpdateReportingData extends AbstractImportData {
	private ProcWorkflowHelper procWorkflowHelper;
	
	public UpdateReportingData(FiniteDuration durationFromStart,FiniteDuration durationFromNextIteration) {
			super("UpdateReportingData", durationFromStart, durationFromNextIteration);
			procWorkflowHelper = Spring.getBeanOfType(ProcWorkflowHelper.class);
	}

	@Override
	public void runImport() throws SQLException, DAOException, MongoException, RulesException {
		Logger.debug("Start reporting synchro");
		Date date = new Date();
		try{
			MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class)
			.sort("traceInformation.creationDate", Sort.DESC)//.limit(5000)
				.cursor.forEach(sample -> {
					try{
						updateProcesses(sample);
						if(sample.processes != null && sample.processes.size() > 0){
							Logger.debug("update sample "+sample.code);
							MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sample.code), 
									DBUpdate.set("processes", sample.processes).set("processesStatistics", sample.processesStatistics).set("processesUpdatedDate", date));
						}	
					}catch(Throwable e){
						logger.error("Sample : "+sample.code+" - "+e,e);
					}
				});
		}catch(Throwable e){
			logger.error("Error date : "+e,e);
		}
		
	}

	private void updateProcesses(Sample sample) {
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("sampleCodes", sample.code))
				.toList();
		
		sample.processes = processes.parallelStream()
					.map(process -> convertToSampleProcess(sample, process))
					.collect(Collectors.toList());	
		computeStatistics(sample);
	}

	private void computeStatistics(Sample sample) {
		if(null != sample.processes){
			
			sample.processesStatistics = new SampleProcessesStatistics();	
			sample.processesStatistics.processTypeCodes = sample.processes.stream().collect(Collectors.groupingBy(p -> p.typeCode, Collectors.counting()));
			sample.processesStatistics.processCategoryCodes = sample.processes.stream().collect(Collectors.groupingBy(p -> p.categoryCode, Collectors.counting()));
		
			sample.processesStatistics.readSetTypeCodes = sample.processes.stream().filter(p -> p.readsets != null).map(p -> p.readsets).flatMap(List::stream).collect(Collectors.groupingBy(r -> r.typeCode, Collectors.counting()));
		}
	}

	private SampleProcess convertToSampleProcess(Sample sample, Process process) {
		SampleProcess sampleProcess = new SampleProcess();
		sampleProcess.code= process.code;
		sampleProcess.typeCode= process.typeCode;
		sampleProcess.categoryCode= process.categoryCode;
		sampleProcess.state= process.state;
		sampleProcess.state.historical=null;
		sampleProcess.traceInformation= process.traceInformation;
		if(process.properties != null && process.properties.size() > 0){
			sampleProcess.properties= process.properties;			
		}
		sampleProcess.currentExperimentTypeCode = process.currentExperimentTypeCode;
		if(process.experimentCodes != null && process.experimentCodes.size() > 0){
			List<SampleExperiment> experiments  = updateExperiments(process);
			if(experiments != null && experiments.size() > 0){
				sampleProcess.experiments = experiments;
			}
		}
		
		if(process.outputContainerCodes != null && process.outputContainerCodes.size() > 0){
			List<SampleReadSet> readsets = updateReadSets(sample, process);
			if(readsets != null && readsets.size() > 0){
				sampleProcess.readsets = readsets;
			}
		}
		List<String> transformationCodes = getTransformationCodesForProcessTypeCode(process);
		//extract only transformation
		
		Integer nbExp = 0;
		if(process.experimentCodes != null && process.experimentCodes.size() > 0 && transformationCodes != null && transformationCodes.size() > 0){
			nbExp = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", process.experimentCodes)
					.in("typeCode", transformationCodes)
					.in("state.code", Arrays.asList("IP","F"))).count();
			
			sampleProcess.progressInPercent = (new BigDecimal((nbExp.floatValue() / Integer.valueOf(nbExpPositionInProcessType.get(process.typeCode)).floatValue())*100.00)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			//Logger.debug("progressInPercent : "+(nbExp.floatValue() / Integer.valueOf(transformationCodes.size()).floatValue()));
		}else{
			sampleProcess.progressInPercent = null;			
		}
		
		return sampleProcess;
	}
	private Map<String, List<String>> transformationCodesByProcessTypeCode = new HashMap<String, List<String>>();
	private Map<String, Integer> nbExpPositionInProcessType = new HashMap<String, Integer>(); 
			
	private List<String> getTransformationCodesForProcessTypeCode(Process process) {
		List<String> transformationCodes;
		if(transformationCodesByProcessTypeCode.containsKey(process.typeCode)){
			transformationCodes = transformationCodesByProcessTypeCode.get(process.typeCode);
		}else{
			transformationCodes =ExperimentType.find.findByProcessTypeCode(process.typeCode,true).stream().map(e -> e.code).collect(Collectors.toList());
			transformationCodesByProcessTypeCode.put(process.typeCode, transformationCodes);
			
			Integer nbPos = ExperimentType.find.countDistinctExperimentPositionInProcessType(process.typeCode);
			nbExpPositionInProcessType.put(process.typeCode, nbPos);
		}
		
		return transformationCodes;
	}


	

	private List<SampleExperiment> updateExperiments(Process process) {		
		List<SampleExperiment> sampleExperiments = new ArrayList<SampleExperiment>();
		MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", process.experimentCodes))
			.cursor.forEach(experiment -> {
				sampleExperiments.addAll(convertToSampleExperiments(process, experiment));
			});
		return sampleExperiments;
		//List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", process.experimentCodes)).toList();
		//return experiments.parallelStream().map(exp -> convertToSampleExperiments(process, exp)).flatMap(List::stream).collect(Collectors.toList());		
	}

	
	//map key = expCode-processCode
	private List<SampleExperiment> convertToSampleExperiments(Process process, Experiment experiment) {
		List<SampleExperiment> sampleExperiments = new ArrayList<SampleExperiment>();
		Set<String> containerCodes = new TreeSet<String>();
		containerCodes.add(process.inputContainerCode);
		
		if(null != process.outputContainerCodes){
			containerCodes.addAll(process.outputContainerCodes);
		}
		experiment.atomicTransfertMethods.parallelStream().forEach(atm -> {
			if(OneToVoidContainer.class.isInstance(atm)){
				atm.inputContainerUseds.forEach(icu -> {
					if(containerCodes.contains(icu.code)){
						SampleExperiment sampleExperiment = new SampleExperiment();
						sampleExperiment.code = experiment.code;
						sampleExperiment.typeCode= experiment.typeCode;
						sampleExperiment.categoryCode= experiment.categoryCode;
						sampleExperiment.state= experiment.state;
						sampleExperiment.state.historical=null;
						sampleExperiment.status= experiment.status;
						
						sampleExperiment.traceInformation= experiment.traceInformation;
						sampleExperiment.protocolCode = experiment.protocolCode;
						sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
						sampleExperiments.add(sampleExperiment);
					}					
				});
			}else{
				atm.inputContainerUseds.forEach(icu -> {
					if(null != atm.outputContainerUseds){
						atm.outputContainerUseds.forEach(ocu ->{
							if(ocu.code != null && containerCodes.containsAll(Arrays.asList(icu.code, ocu.code))){
								SampleExperiment sampleExperiment = new SampleExperiment();
								sampleExperiment.code = experiment.code;
								sampleExperiment.typeCode= experiment.typeCode;
								sampleExperiment.categoryCode= experiment.categoryCode;
								sampleExperiment.state= experiment.state;
								sampleExperiment.state.historical=null;
								sampleExperiment.status= experiment.status;
								
								sampleExperiment.traceInformation= experiment.traceInformation;
								sampleExperiment.protocolCode = experiment.protocolCode;
								sampleExperiment.properties = computeExperimentProperties(experiment, icu, ocu);
								sampleExperiments.add(sampleExperiment);
							}else if(containerCodes.contains(icu.code)){
								SampleExperiment sampleExperiment = new SampleExperiment();
								sampleExperiment.code = experiment.code;
								sampleExperiment.typeCode= experiment.typeCode;
								sampleExperiment.categoryCode= experiment.categoryCode;
								sampleExperiment.state= experiment.state;
								sampleExperiment.state.historical=null;
								sampleExperiment.status= experiment.status;
								sampleExperiment.traceInformation= experiment.traceInformation;
								sampleExperiment.protocolCode = experiment.protocolCode;
								sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
								sampleExperiments.add(sampleExperiment);
							}
						});
					}else if(containerCodes.contains(icu.code)){
						SampleExperiment sampleExperiment = new SampleExperiment();
						sampleExperiment.code = experiment.code;
						sampleExperiment.typeCode= experiment.typeCode;
						sampleExperiment.categoryCode= experiment.categoryCode;
						sampleExperiment.state= experiment.state;
						sampleExperiment.state.historical=null;
						sampleExperiment.status= experiment.status;
						
						sampleExperiment.traceInformation= experiment.traceInformation;
						sampleExperiment.protocolCode = experiment.protocolCode;
						sampleExperiment.properties = computeExperimentProperties(experiment, icu, null);
						sampleExperiments.add(sampleExperiment);
					}
				});	
			}
		});
		//one-to-void
		//one-to-one
		//many-to-one
		//one-to-many ???
		
		return sampleExperiments;
	}

	private Map<String, PropertyValue> computeExperimentProperties(Experiment experiment, InputContainerUsed icu,
			OutputContainerUsed ocu) {
		Map<String, PropertyValue> finalProperties = new HashMap<String, PropertyValue>();
		if(null != experiment.experimentProperties)finalProperties.putAll(filterProperties(experiment.experimentProperties));
		if(null != experiment.instrumentProperties)finalProperties.putAll(filterProperties(experiment.instrumentProperties));
		if(null != icu.experimentProperties)finalProperties.putAll(filterProperties(icu.experimentProperties));
		if(null != icu.instrumentProperties)finalProperties.putAll(filterProperties(icu.instrumentProperties));
		if(null != ocu){
			if(null != ocu.experimentProperties)finalProperties.putAll(filterProperties(ocu.experimentProperties));
			if(null != ocu.instrumentProperties)finalProperties.putAll(filterProperties(ocu.instrumentProperties));
		}
		
		return finalProperties;
	}

	private Map<String, PropertyValue> filterProperties(
			Map<String, PropertyValue> properties) {
		return properties.entrySet().parallelStream()
				.filter(entry -> entry.getValue() != null && !entry.getValue()._type.equals(PropertyValue.imgType)
						&& !entry.getValue()._type.equals(PropertyValue.fileType))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}
	

	private List<SampleReadSet> updateReadSets(Sample sample, Process process) {
		List<SampleReadSet> sampleReadSets = new ArrayList<SampleReadSet>();
		String tag = procWorkflowHelper.getTagAssignFromProcessContainers(process);
		
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		
		MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,	
				DBQuery.in("sampleOnContainer.containerCode", process.outputContainerCodes).in("sampleCode", process.sampleCodes).in("projectCode", process.projectCodes),
				keys)
		.cursor
		.forEach(readset -> {
			if(!readset.sampleOnContainer.properties.containsKey(TAG_PROPERTY_NAME)
					|| (null != tag && readset.sampleOnContainer.properties.containsKey(TAG_PROPERTY_NAME) 
					&&  tag.equals(readset.sampleOnContainer.properties.get(TAG_PROPERTY_NAME).value))){
				sampleReadSets.add(convertToSampleReadSet(readset));				
			}
		});
		return sampleReadSets;
	}

	private SampleReadSet convertToSampleReadSet(ReadSet readset) {
		SampleReadSet sampleReadSet = new SampleReadSet();
		sampleReadSet.code = readset.code;
		sampleReadSet.typeCode = readset.typeCode;
		sampleReadSet.state = readset.state;
		sampleReadSet.state.historical = null;
		sampleReadSet.runCode = readset.runCode;
		sampleReadSet.runTypeCode = readset.runTypeCode;
		sampleReadSet.runSequencingStartDate = readset.runSequencingStartDate;
		
		sampleReadSet.productionValuation = readset.productionValuation;   
		sampleReadSet.bioinformaticValuation = readset.bioinformaticValuation; 
		
		if(!readset.typeCode.equals("rsnanopore")){
			BasicDBObject keys = new BasicDBObject();
			keys.put("code", 1);
			keys.put("treatments.ngsrg", 1);
			
			ReadSet rs = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class, readset.code, keys);
			sampleReadSet.treatments = rs.treatments;
		}
		
		
		//sampleReadSet.treatments = filterTreaments(readset.treatments);
		return sampleReadSet;
	}

	private Map<String, Treatment> filterTreaments(Map<String, Treatment> treatments) {
		treatments.values()
			.parallelStream()
			.forEach(treament ->{
				treament.results.entrySet().forEach(entry -> {
					entry.setValue(filterProperties(entry.getValue()));
				});
			});
		return treatments;
	}
	
}
