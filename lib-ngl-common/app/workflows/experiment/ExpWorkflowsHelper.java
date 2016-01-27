package workflows.experiment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.Level.CODE;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.tree.From;
import models.laboratory.container.instance.tree.ParentContainers;
import models.laboratory.container.instance.tree.TreeOfLifeNode;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.instance.ContainerHelper;
import models.utils.instance.ExperimentHelper;

import org.apache.commons.collections.CollectionUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;



import play.Play;
import play.libs.Akka;
import rules.services.RulesActor6;
import rules.services.RulesMessage;
import validation.ContextValidation;
import workflows.container.ContSupportWorkflows;
import workflows.container.ContWorkflows;
import workflows.process.ProcWorkflows;
import akka.actor.ActorRef;
import akka.actor.Props;
import fr.cea.ig.MongoDBDAO;

public class ExpWorkflowsHelper {
	
	private static final String NEW_PROCESS_CODES = "NEW_PROCESS_CODES";
	private static ContWorkflows containerWorkflows = ContWorkflows.instance;
	private static ContSupportWorkflows containerSupportWorkflows = ContSupportWorkflows.instance;
	private static ProcWorkflows processWorkflows = ProcWorkflows.instance;
	
	public static void updateXCodes(Experiment exp) {
		Set<String> sampleCodes = new HashSet<String>();
		Set<String> projectCodes  = new HashSet<String>();
		Set<String> inputContainerSupportCodes = new HashSet<String>();
		Set<String> inputContainerCodes = new HashSet<String>();
		Set<String> inputProcessCodes  = new HashSet<String>();
		Set<String> inputFromTransformationTypeCodes = new HashSet<String>();
		Set<String> inputProcessTypeCodes = new HashSet<String>();
			
		exp.atomicTransfertMethods.stream().map(atm -> atm.inputContainerUseds)
			.flatMap(List::stream)
			.forEach(inputContainer -> {
				inputContainerCodes.add(inputContainer.code);
				projectCodes.addAll(inputContainer.projectCodes);
				sampleCodes.addAll(inputContainer.sampleCodes);
				inputContainerSupportCodes.add(inputContainer.locationOnContainerSupport.code);
				inputProcessCodes.addAll(inputContainer.inputProcessCodes);
				inputFromTransformationTypeCodes.addAll(inputContainer.fromExperimentTypeCodes);
				inputProcessTypeCodes.addAll(inputContainer.processTypeCodes);
			});
		
		exp.projectCodes = projectCodes;		
		exp.sampleCodes = sampleCodes;
		exp.inputContainerSupportCodes = inputContainerSupportCodes;		
		exp.inputContainerCodes = inputContainerCodes;
		exp.inputProcessCodes = inputProcessCodes;
		exp.inputProcessTypeCodes = inputProcessTypeCodes;
		exp.inputFromTransformationTypeCodes = inputFromTransformationTypeCodes;
		MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", exp.code)
				,DBUpdate.set("projectCodes", exp.projectCodes)
					.set("sampleCodes", exp.sampleCodes)
					.set("inputContainerSupportCodes", exp.inputContainerSupportCodes)
					.set("inputContainerCodes", exp.inputContainerCodes)
					.set("inputProcessCodes", exp.inputProcessCodes)
					.set("inputProcessTypeCodes", exp.inputProcessTypeCodes)
					.set("inputFromTransformationTypeCodes", exp.inputFromTransformationTypeCodes)
						);
	}


	public static void updateOutputContainerCodes(Experiment exp) {
		Set<String> outputContainerSupportCodes = new HashSet<String>();
		Set<String> outputContainerCodes = new HashSet<String>();
		
		exp.atomicTransfertMethods.stream()
			.filter(atm -> (atm.outputContainerUseds != null))
			.map(atm -> atm.outputContainerUseds)
			.flatMap(List::stream)
			.forEach(outputContainer ->{
				outputContainerCodes.add(outputContainer.code);
				outputContainerSupportCodes.add(outputContainer.locationOnContainerSupport.code);
				
			});
		if(CollectionUtils.isNotEmpty(outputContainerCodes)){
			exp.outputContainerCodes = outputContainerCodes;
			exp.outputContainerSupportCodes = outputContainerSupportCodes;
			
			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", exp.code)
					,DBUpdate.set("outputContainerSupportCodes", exp.outputContainerSupportCodes)
							.set("outputContainerCodes", exp.outputContainerCodes));
		}
	}

	
	public static void updateStateOfInputContainers(Experiment exp, State nextState, ContextValidation ctxVal) {
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("code", exp.inputContainerCodes))
			.cursor.forEach(c -> containerWorkflows.setState(ctxVal, c, nextState));						
	}
	
	public static void updateStateOfInputContainerSupports(Experiment exp, State nextState, ContextValidation ctxVal) {
		MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,DBQuery.in("code", exp.inputContainerSupportCodes))
			.cursor.forEach(c -> containerSupportWorkflows.setState(ctxVal, c, nextState));				
	}
	
	public static void updateStateOfProcesses(Experiment exp, State nextState, ContextValidation ctxVal) {
		MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class,DBQuery.in("code", exp.inputProcessCodes))
			.cursor.forEach(c -> processWorkflows.setState(ctxVal, c, nextState));				
	}

	public static void linkExperimentWithProcesses(Experiment exp, ContextValidation validation) {
		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME,Process.class,
				DBQuery.in("code", exp.inputProcessCodes).notEquals("state.code", "F").notIn("experimentCodes", exp.code), 
				DBUpdate.set("currentExperimentTypeCode", exp.typeCode).push("experimentCodes", exp.code),true);
		
	}
	
	public static void updateAddContainersToExperiment(Experiment expFromUser, ContextValidation ctxVal, State nextState) {
		Experiment expFromDB = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, expFromUser.code);
		
		List<String> newContainerCodes = getNewContainerCodes(expFromDB, expFromUser);
		if(newContainerCodes.size() > 0){
			Set<String> newContainerSupportCodes = new TreeSet<String>();
			Set<String> newProcessCodes = new TreeSet<String>();
			
			MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("code", newContainerCodes))
					.cursor.forEach(c -> {				
				newContainerSupportCodes.add(c.support.code);
				newProcessCodes.addAll(c.inputProcessCodes);
				containerWorkflows.setState(ctxVal, c, nextState);
			});
			
			MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,DBQuery.in("code", newContainerSupportCodes))
				.cursor.forEach(c -> {				
					containerSupportWorkflows.setState(ctxVal, c, nextState);
			});
			
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
					DBQuery.in("code", newProcessCodes).notEquals("state.code", "F"), 
					DBUpdate.set("currentExperimentTypeCode", expFromDB.typeCode).push("experimentCodes", expFromDB.code));			
		}
	}
	
	public static void updateRemoveContainersFromExperiment(Experiment expFromUser,	ContextValidation ctxVal, State nextState) {
		Experiment expFromDB = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, expFromUser.code);
		
		List<String> removeContainerCodes = getRemoveContainerCodes(expFromDB, expFromUser);
		if(removeContainerCodes.size() > 0){
			Set<String> removeContainerSupportCodes = new TreeSet<String>();
			Set<String> removeProcessCodes = new TreeSet<String>();
			
			MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", removeContainerCodes)).cursor
					.forEach(c -> {
						removeContainerSupportCodes.add(c.support.code);
						removeProcessCodes.addAll(c.inputProcessCodes);
						containerWorkflows.setState(ctxVal, c, nextState);
					});
			MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.in("code", removeContainerSupportCodes)).cursor
					.forEach(c -> {
						containerSupportWorkflows
								.setState(ctxVal, c, nextState);
					});
			
			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
					DBQuery.in("code", removeProcessCodes).notEquals("state.code", "F"), 
					DBUpdate.unset("currentExperimentTypeCode").pull("experimentCodes", expFromDB.code));
			
		}
	}

	private static List<String> getNewContainerCodes(Experiment expFromDB, Experiment expFromUser) {
		List<String> containerCodesFromDB = ExperimentHelper.getAllInputContainers(expFromDB).stream().map((InputContainerUsed c) -> c.code).collect(Collectors.toList());
		List<String> containerCodesFromUser = ExperimentHelper.getAllInputContainers(expFromUser).stream().map((InputContainerUsed c) -> c.code).collect(Collectors.toList());
		
		List<String> newContainersCodes = new ArrayList<String>();
		for(String codeFromDB:containerCodesFromUser){
			if(!containerCodesFromDB.contains(codeFromDB)){
				newContainersCodes.add(codeFromDB);
			}
		}		
		return newContainersCodes;
	}


	
		

	private static List<String> getRemoveContainerCodes(Experiment expFromDB, Experiment expFromUser) {
		List<String> containerCodesFromDB = ExperimentHelper.getAllInputContainers(expFromDB).stream().map((InputContainerUsed c) -> c.code).collect(Collectors.toList());
		List<String> containerCodesFromUser = ExperimentHelper.getAllInputContainers(expFromUser).stream().map((InputContainerUsed c) -> c.code).collect(Collectors.toList());
		
		List<String> removeContainersCodes = new ArrayList<String>();
		for(String codeFromDB:containerCodesFromDB){
			if(!containerCodesFromUser.contains(codeFromDB)){
				removeContainersCodes.add(codeFromDB);
			}
		}
		
		return removeContainersCodes;
	}
	
	/**
	 * Update OutputContainerUsed :
	 * 		- generate ContainerSupportCode and ContainerCode if needed
	 * 		- populate content, projectCodes, sampleCodes, fromExperimentTypeCodes, processTypeCodes, inputProcessCodes
	 * 		- remove empty volume, quantity, concentration
	 * 
	 * !! missing populate properties on container !!		
	 * 
	 * @param exp
	 * @param validation
	 */
	public static void updateATMs(Experiment exp, boolean justContainerCode) {
		ContainerSupportCategory outputCsc = ContainerSupportCategory.find.findByCode(exp.instrument.outContainerSupportCategoryCode);
		
		if(outputCsc.nbLine.equals(Integer.valueOf(1)) && outputCsc.nbColumn.equals(Integer.valueOf(1))){
			exp.atomicTransfertMethods.forEach((AtomicTransfertMethod atm) -> updateOutputContainerUsed(exp, atm, outputCsc, CodeHelper.getInstance().generateContainerSupportCode(), justContainerCode));
		}else if(!outputCsc.nbLine.equals(Integer.valueOf(1))){
			String supportCode = CodeHelper.getInstance().generateContainerSupportCode();
			exp.atomicTransfertMethods.forEach((AtomicTransfertMethod atm) -> updateOutputContainerUsed(exp, atm, outputCsc, supportCode, justContainerCode));
		}	
		
		MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", exp.code), DBUpdate.set("atomicTransfertMethods", exp.atomicTransfertMethods));
	}


	private static void updateOutputContainerUsed(Experiment exp, AtomicTransfertMethod atm, ContainerSupportCategory outputCsc, String supportCode, boolean justContainerCode) {
		if(atm.outputContainerUseds != null){
			atm.updateOutputCodeIfNeeded(outputCsc, supportCode);
			if(!justContainerCode){
				List<Content> contents = getContents(exp, atm);
							
				atm.outputContainerUseds.forEach((OutputContainerUsed ocu) ->{
					ocu.contents = contents;
					if(ocu.volume != null && ocu.volume.value == null)ocu.volume=null;
					if(ocu.concentration != null &&ocu.concentration.value == null)ocu.concentration=null;
					if(ocu.quantity != null &&ocu.quantity.value == null)ocu.quantity=null;			
				});
			}
		}		
	}


	private static Set<String> getFromExperimentTypeCodes(Experiment exp, AtomicTransfertMethod atm) {
		Set<String> _fromExperimentTypeCodes = new HashSet<String>(0);
		if(ExperimentCategory.CODE.transformation.equals(ExperimentCategory.CODE.valueOf(exp.categoryCode))){
			_fromExperimentTypeCodes.add(exp.typeCode);
		}else{
			_fromExperimentTypeCodes = atm.inputContainerUseds.stream().map((InputContainerUsed icu) -> icu.fromExperimentTypeCodes).flatMap(Set::stream).collect(Collectors.toSet());
		}
		return _fromExperimentTypeCodes;
	}


	private static List<Content> getContents(Experiment exp, AtomicTransfertMethod atm) {
		List<Content> contents =  atm.inputContainerUseds.stream().map((InputContainerUsed icu) -> ContainerHelper.calculPercentageContent(icu.contents, icu.percentage)).flatMap(List::stream).collect(Collectors.toList());
		contents = ContainerHelper.fusionContents(contents);
		Map<String, PropertyValue> newContentProperties = getPropertiesForALevel(exp, atm, CODE.Content);
		contents.forEach((Content c) ->{
			c.properties.putAll(newContentProperties);
		});
		return contents;
	}

	/**
	 * Generate Support and container and save it in MongoDB
	 * Add the support code inside processes
	 * Il only one error during validation process all object are delete from MongoDB
	 * @param exp
	 * @param validation
	 */
	public static void createOutputContainerSupports(Experiment exp, ContextValidation validation) {
		Map<String, List<Container>> containersBySupportCode = exp.atomicTransfertMethods.stream()
				.map(atm -> createOutputContainers(exp, atm, validation))
				.flatMap(List::stream)
				.collect(Collectors.groupingBy(c -> c.support.code));
		
		//soit 1 seul support pour tous les atm
		//soit autant de support que d'atm
		
		//Map<ContainerSupport, List<Container>> containersBySupport = new HashMap<ContainerSupport, List<Container>>(0);
		ContextValidation supportsValidation = new ContextValidation(validation.getUser());
		supportsValidation.setCreationMode();
		
		containersBySupportCode.entrySet().forEach(entry -> {
			List<Container> containers = entry.getValue();
			ContainerSupport support = createContainerSupport(entry.getKey(), containers, validation);
			//TODO GA extract only properties from exp and inst not from atm => must be improve
			support.properties = getPropertiesForALevel(exp, CODE.ContainerSupport); 
			
			support.validate(supportsValidation);
			if(!supportsValidation.hasErrors()){
				MongoDBDAO.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, support);
				containers.forEach(container -> {
					container.validate(supportsValidation);
					if(!supportsValidation.hasErrors()){
						MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, container);
						MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, 
								DBQuery.in("code", container.inputProcessCodes).notIn("newContainerSupportCodes", container.support.code),
								DBUpdate.push("newContainerSupportCodes",container.support.code));
					}
				});
			}				
		});
		
		//delete all supports and containers if only one error
		if(supportsValidation.hasErrors()){
			containersBySupportCode.entrySet().forEach(entry -> {
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
						DBQuery.in("newContainerSupportCodes", entry.getKey()),
						DBUpdate.pull("newContainerSupportCodes",entry.getKey()));
				MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", entry.getKey()));
				MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", entry.getKey()));
			});
			Set<String> newProcessCodes = (Set<String>)validation.getObject(NEW_PROCESS_CODES);
			if(null != newProcessCodes){
				MongoDBDAO.delete(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", newProcessCodes));
			}
			
		}			
		validation.addErrors(supportsValidation.errors);
	}

	public static void deleteOutputContainerSupports(Experiment exp, ContextValidation validation) {
		if(null != exp.outputContainerSupportCodes){
			exp.outputContainerSupportCodes.forEach(code -> {
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
						DBQuery.in("newContainerSupportCodes", code),
						DBUpdate.pull("newContainerSupportCodes",code));
				MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", code));
				MongoDBDAO.delete(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("code", code));
			});
			Set<String> newProcessCodes = (Set<String>)validation.getObject(NEW_PROCESS_CODES);
			if(null != newProcessCodes){
				MongoDBDAO.delete(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", newProcessCodes));
			}
		}
	}

	
	private static ContainerSupport createContainerSupport(String code, List<Container> containers, ContextValidation validation) {
		validation.addKeyToRootKeyName("creation.outputSupport."+code);
		ContainerSupport support = new ContainerSupport();
		support.code = code;
		support.state = new State("N", validation.getUser());
		support.traceInformation  = new TraceInformation(validation.getUser());
		support.categoryCode = getSupportCategoryCode(containers, validation);
		support.storageCode = getSupportStorageCode(containers, validation);
		support.projectCodes = containers.stream().map(c -> c.projectCodes).flatMap(Set::stream).collect(Collectors.toSet());
		support.sampleCodes = containers.stream().map(c -> c.sampleCodes).flatMap(Set::stream).collect(Collectors.toSet());
		support.fromExperimentTypeCodes = containers.stream().map(c -> c.fromExperimentTypeCodes).flatMap(Set::stream).collect(Collectors.toSet());
		validation.removeKeyFromRootKeyName("creation.outputSupport."+code);
		return support;
	}


	private static String getSupportCategoryCode(List<Container> containers, ContextValidation validation) {
		Set<String> categoryCodes = containers.stream().map(c -> c.support.categoryCode).collect(Collectors.toSet());
		if(categoryCodes.size() == 1)
			return categoryCodes.iterator().next();
		else{
			validation.addErrors("categoryCode","different for several containers");
			return null;
		}
	}
	private static String getSupportStorageCode(List<Container> containers, ContextValidation validation) {
		Set<String> storageCodes = containers.stream().map(c -> c.support.storageCode).collect(Collectors.toSet());
		if(storageCodes.size() == 1)
			return storageCodes.iterator().next();
		else{
			validation.addErrors("storageCode","different for several containers");
			return null;
		}
	}

	private static List<Container> createOutputContainers(Experiment exp, AtomicTransfertMethod atm, ContextValidation validation) {
		Set<String> fromExperimentTypeCodes = getFromExperimentTypeCodes(exp, atm);
		Map<String, PropertyValue> containerProperties = getPropertiesForALevel(exp, atm, CODE.Container);
		TreeOfLifeNode tree = getTreeOfLifeNode(exp, atm);
		
		Set<String> projectCodes =new HashSet<String>();
		Set<String> sampleCodes =new HashSet<String>();
		Set<String> processTypeCodes =new HashSet<String>();
		Set<String> inputProcessCodes =new HashSet<String>();
		
		atm.inputContainerUseds.forEach(icu -> {
			projectCodes.addAll(icu.projectCodes);
			sampleCodes.addAll(icu.sampleCodes);
			processTypeCodes.addAll(icu.processTypeCodes);
			inputProcessCodes.addAll(icu.inputProcessCodes);
		});
		
		
		State state = new State("N", validation.getUser());
		TraceInformation traceInformation = new TraceInformation(validation.getUser());
		List<Container> newContainers = new ArrayList<Container>();
		if(atm.outputContainerUseds != null && atm.outputContainerUseds.size() != 0){
			OutputContainerUsed ocu = atm.outputContainerUseds.get(0);
			Container c = new Container();
			c.code = ocu.code;
			c.categoryCode = ocu.categoryCode;
			c.contents = ocu.contents;
			c.support = ocu.locationOnContainerSupport;
			c.properties = containerProperties;
			c.mesuredConcentration = ocu.concentration;
			c.mesuredQuantity = ocu.quantity;
			c.mesuredVolume = ocu.volume;
			c.projectCodes = projectCodes;
			c.sampleCodes = sampleCodes;
			c.fromExperimentTypeCodes = fromExperimentTypeCodes;
			c.processTypeCodes = processTypeCodes;
			c.inputProcessCodes = inputProcessCodes;
			c.state = state;
			c.traceInformation = traceInformation;
			c.treeOfLife=tree;
			newContainers.add(c);
		}
		if(atm.outputContainerUseds != null && atm.outputContainerUseds.size() > 1){
			Set<String> allNewInputProcessCodes = new HashSet<String>();
			List<OutputContainerUsed> outputContainerUseds = atm.outputContainerUseds.subList(1, atm.outputContainerUseds.size());
			outputContainerUseds.forEach((OutputContainerUsed ocu) ->{
				Set<String> newInputProcessCodes = duplicateProcesses(inputProcessCodes);
				allNewInputProcessCodes.addAll(newInputProcessCodes);
				Container c = new Container();
				c.code = ocu.code;
				c.categoryCode = ocu.categoryCode;
				c.contents = ocu.contents;
				c.support = ocu.locationOnContainerSupport;
				c.properties = containerProperties;
				c.mesuredConcentration = ocu.concentration;
				c.mesuredQuantity = ocu.quantity;
				c.mesuredVolume = ocu.volume;
				c.projectCodes = projectCodes;
				c.sampleCodes = sampleCodes;
				c.fromExperimentTypeCodes = fromExperimentTypeCodes;
				c.processTypeCodes = processTypeCodes;
				c.inputProcessCodes = newInputProcessCodes;
				c.state = state;
				c.traceInformation = traceInformation;
				c.treeOfLife=tree;
				newContainers.add(c);
			});
			validation.putObject(NEW_PROCESS_CODES, allNewInputProcessCodes);
		}
		
		return newContainers;
	}

	private static Set<String> duplicateProcesses(Set<String> inputProcessCodes) {
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", inputProcessCodes)).toList();
		Set<String> newInputProcessCodes = new HashSet<String>();
		processes.forEach(p -> {
			p._id = null;
			p.code = CodeHelper.getInstance().generateProcessCode(p);
			MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, p);
			newInputProcessCodes.add(p.code);			
		});		
		return newInputProcessCodes;
	}


	private static TreeOfLifeNode getTreeOfLifeNode(Experiment exp,	AtomicTransfertMethod atm) {
		TreeOfLifeNode treeNode = new TreeOfLifeNode();
		
		treeNode.from = new From();
		treeNode.from.experimentCode = exp.code;
		treeNode.from.experimentTypeCode = exp.typeCode;
		treeNode.from.containers = atm.inputContainerUseds.stream().map(icu -> {
			ParentContainers pc = new ParentContainers();
			pc.code = icu.code;
			pc.supportCode = icu.locationOnContainerSupport.code;
			pc.fromTransformationTypeCodes = icu.fromExperimentTypeCodes;
			pc.processCodes = icu.inputProcessCodes;
			pc.processTypeCodes = icu.processTypeCodes;
			return pc;
			}).collect(Collectors.toList());
		
		treeNode.paths = new ArrayList<String>();
		
		atm.inputContainerUseds.forEach(icu -> {
			Container c = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code", icu.code));
			if(null != c.treeOfLife && null != c.treeOfLife.paths){
				treeNode.paths.addAll(c.treeOfLife.paths.stream().map(s -> s+","+icu.code).collect(Collectors.toList()));
			}else{
				treeNode.paths.add(","+icu.code);
			}
		});
		
		return treeNode;
	}


	private static Set<String> getPropertyDefinitionCodesByLevel(List<PropertyDefinition> propertyDefs, Level.CODE level){
		
		Level l = new Level(level);
		
		return propertyDefs.stream().filter(pd -> pd.levels.contains(l)).map(pd -> pd.code).collect(Collectors.toSet());
	}
	
	private static Map<String, PropertyValue> getPropertiesForALevel(Experiment exp, CODE code) {
		return getPropertiesForALevel(exp, null, code);
	}
	
	private static Map<String, PropertyValue> getPropertiesForALevel(Experiment exp, AtomicTransfertMethod atm, Level.CODE level) {
		Map<String, PropertyValue> propertiesForALevel = new HashMap<String, PropertyValue>();
		
		ExperimentType expType = ExperimentType.find.findByCode(exp.typeCode);
		Set<String> experimentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevel(expType.propertiesDefinitions, level);
		
		//extract experiment content properties
		if(null != exp.experimentProperties && experimentPropertyDefinitionCodes.size() > 0){
			propertiesForALevel.putAll(exp.experimentProperties.entrySet()
					.stream()
					.filter(entry -> experimentPropertyDefinitionCodes.contains(entry.getKey()))
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
		}
				
		
		if(null != atm && experimentPropertyDefinitionCodes.size() > 0){
			//TODO GA bug when several the same properties in input when map create in collectors
			propertiesForALevel.putAll(atm.inputContainerUseds.stream()
					.filter((InputContainerUsed icu) -> icu.experimentProperties != null)
					.map((InputContainerUsed icu) -> icu.experimentProperties.entrySet())
					.flatMap(Set::stream)
					.filter(entry -> experimentPropertyDefinitionCodes.contains(entry.getKey()))					
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
			if(null != atm.outputContainerUseds){
				//TODO GA bug when several the same properties in output when map create in collectors
				propertiesForALevel.putAll(atm.outputContainerUseds.stream()
						.filter((OutputContainerUsed ocu) -> ocu.experimentProperties != null)
						.map((OutputContainerUsed ocu) -> ocu.experimentProperties.entrySet())
						.flatMap(Set::stream)
						.filter(entry -> experimentPropertyDefinitionCodes.contains(entry.getKey()))
						.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
			}			
		}
		
		//extract instrument content properties
		InstrumentUsedType insType = InstrumentUsedType.find.findByCode(exp.instrument.typeCode);
		Set<String> instrumentPropertyDefinitionCodes = getPropertyDefinitionCodesByLevel(insType.propertiesDefinitions, level);
		
		if(null != exp.instrumentProperties && instrumentPropertyDefinitionCodes.size() > 0){
			propertiesForALevel.putAll(exp.instrumentProperties.entrySet()
					.stream()
					.filter(entry -> instrumentPropertyDefinitionCodes.contains(entry.getKey()))
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
		}
		if(null != atm && instrumentPropertyDefinitionCodes.size() > 0){
			propertiesForALevel.putAll(atm.inputContainerUseds.stream()
					.filter((InputContainerUsed icu) -> icu.instrumentProperties != null)
					.map((InputContainerUsed icu) -> icu.instrumentProperties.entrySet())
					.flatMap(Set::stream)
					.filter(entry -> instrumentPropertyDefinitionCodes.contains(entry.getKey()))
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
			if(null != atm.outputContainerUseds){
				propertiesForALevel.putAll(atm.outputContainerUseds.stream()
						.filter((OutputContainerUsed ocu) -> ocu.instrumentProperties != null)
						.map((OutputContainerUsed ocu) -> ocu.instrumentProperties.entrySet())
						.flatMap(Set::stream)
						.filter(entry -> instrumentPropertyDefinitionCodes.contains(entry.getKey()))
						.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
			}					
		}
		/* Do not extract property from process because the risk to have the same property on several process is very big
		 * To put process property in container used rules*/
		if(null != atm){
			//extract process content properties for only the inputContainer of the process
			List<String> processesPropertyDefinitionCodes = atm.inputContainerUseds.stream()
					.map((InputContainerUsed icu) -> getProcessesPropertyDefinitionCodes(icu, level))
					.flatMap(List::stream)
					.collect(Collectors.toList()); 
			if(processesPropertyDefinitionCodes.size() >0){
				propertiesForALevel.putAll(atm.inputContainerUseds.stream()
						.map((InputContainerUsed icu) -> getProcessesProperties(icu))
						.flatMap(List::stream)
						.filter(entry -> processesPropertyDefinitionCodes.contains(entry.getKey()))
						.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));				
			}
		}
		
		return propertiesForALevel;
	}
	
	private static List<String> getProcessesPropertyDefinitionCodes(InputContainerUsed icu, Level.CODE level) {		
		return icu.processTypeCodes.stream()
				.map((String code) ->ProcessType.find.findByCode(code))
				.map((ProcessType p) -> p.getPropertyDefinitionByLevel(level))
				.flatMap(List::stream)
				.map(pd -> pd.code)
				.collect(Collectors.toList());		
	}

	/**
	 * Extract process property for only the first experiment of the process
	 * 
	 */
	private static List<Map.Entry<String,PropertyValue>> getProcessesProperties(InputContainerUsed icu) {
		List<Process> processes=MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code", icu.inputProcessCodes).is("containerInputCode", icu.code)).toList();
		if(null != processes && processes.size() > 0){
			return processes.stream()
				.filter(p -> p.properties != null)
				.map((Process p) -> p.properties.entrySet())
				.flatMap(Set::stream)
				.collect(Collectors.toList());
		}else{
			return new ArrayList<Map.Entry<String, PropertyValue>>();
		}
		
	}


	public static void updateComments(Experiment exp, ContextValidation validation) {
		if(null != exp.comments && exp.comments.size() > 0){
			exp.comments.forEach(comment -> {
				if(comment.createUser == null){
					comment.createUser = validation.getUser();
					comment.creationDate = new Date();
				}else if(comment.creationDate == null){
					comment.creationDate = new Date();
				}
				
				if(comment.code == null){
					comment.code = CodeHelper.getInstance().generateExperimentCommentCode(comment);	
				}
			});
		}		
	}


	public static void updateStatus(Experiment exp, ContextValidation validation) {
		if(!TBoolean.UNSET.equals(exp.status.valid)){
			exp.status.date = new Date();
			exp.status.user = validation.getUser();
		}
		
	}
	private static ActorRef rulesActor = Akka.system().actorOf(Props.create(RulesActor6.class));

	public static void callWorkflowRules(ContextValidation validation, Experiment exp) {
		rulesActor.tell(new RulesMessage(Play.application().configuration().getString("rules.key"), "workflow", exp, validation),null);
	}


	

}
