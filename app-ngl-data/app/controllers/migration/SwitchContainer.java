package controllers.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mongojack.DBQuery;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.processes.instance.Process;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.instance.SampleHelper;
import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;

public class SwitchContainer extends CommonController{
	
	
	protected static ALogger logger=Logger.of("SwitchContainer");

	private static final String TAG_PROPERTY_NAME = "tag";
	
	public static Result migration() {
		String oldParentContainerName = "221D4N4IK_C3";
		String newParentContainerName = "221D4N4IK_E3";
		
		//String oldParentContainerName = "21KC2XTKY";
		//String newParentContainerName = "21KC2XTL9";
		
		
		//String oldParentContainerName = "21KC2XTL9";
		//String newParentContainerName = "21KC2XTKY";
		
		
		//21KC2XTKY 21KC2XTL9
		//221D4N4IK_C3 221D4N4IK_E3
		Container oldParentContainer = getContainer(oldParentContainerName);
		Container newParentContainer = getContainer(newParentContainerName);
		
		List<Container> updatedContainers = updateContainers(oldParentContainer,newParentContainer);
		
		List<Experiment> updatedExperiments = new ArrayList<Experiment>(); 
		updatedExperiments.addAll(updateExperimentWhereSwitchError(oldParentContainer, newParentContainer, updatedContainers));
		updatedExperiments.addAll(updateNextExperiments(oldParentContainer, updatedContainers));
		
		
		List<Process> updatedProcesses = new ArrayList<Process>(); 
		updatedProcesses.addAll(updateProcessWhereChild(oldParentContainer,newParentContainer,updatedContainers, updatedExperiments));
		updatedProcesses.addAll(updateProcessWhereParent(updatedContainers, updatedExperiments));
		
		return ok("SwitchContainer End");
	}

	private static  List<Experiment> updateNextExperiments(Container oldParentContainer,
			List<Container> updatedContainers) {
		Logger.info("");
		Logger.info("start update next experiments");	
		Map<String, Container> updatedContainerByCode = updatedContainers.stream().collect(Collectors.toMap(c -> c.code, c -> c));
		
		List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.notIn("inputContainerCodes", oldParentContainer.code)
				.or(DBQuery.in("inputContainerCodes", updatedContainerByCode.keySet()),DBQuery.in("outputContainerCodes", updatedContainerByCode.keySet()))).toList();
		
		experiments.forEach(exp -> {
			Logger.info(exp.code+" : start");
			
			updateXCodes(exp);
			Logger.info(exp.code+" : end");
		});
		
		Logger.info("end update next experiments");	
		return experiments;		
	}

	private static List<Experiment> updateExperimentWhereSwitchError(Container oldParentContainer, Container newParentContainer, List<Container> updatedContainers) {
		Logger.info("");
		Logger.info("start update experiment where old in input");	
		List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
				DBQuery.notEquals("categoryCode","qualitycontrol").in("inputContainerCodes", oldParentContainer.code)).toList();
		Map<String, Container> updatedContainerByCode = updatedContainers.stream().collect(Collectors.toMap(c -> c.code, c -> c));
		
		if(experiments.size() == 1){
			Experiment exp = experiments.get(0);
			Logger.info(exp.code+" : start");
			exp.atomicTransfertMethods.stream()
				.map(atm -> atm.inputContainerUseds)
				.flatMap(List::stream)
				.filter(icu -> icu.code.equals(oldParentContainer.code))
				.forEach(icu -> updateICUWithNewContainer(icu, newParentContainer));
			
			exp.atomicTransfertMethods.stream()
				.map(atm -> atm.outputContainerUseds)
				.flatMap(List::stream)
				.filter(ocu -> updatedContainerByCode.keySet().contains(ocu.code))
				.forEach(ocu -> updateOCUWithNewContainer(ocu, updatedContainerByCode.get(ocu.code)));
		
			updateXCodes(exp);
			Logger.info(exp.code+" : end");
		}else{
			throw new RuntimeException("several experiment where "+oldParentContainer.code+" in input");
		}
		Logger.info("end update experiment where old in input");	
		return experiments;
	}

	
	public static void updateXCodes(Experiment exp) {
		Set<String> sampleCodes = new HashSet<String>();
		Set<String> projectCodes  = new HashSet<String>();
		Set<String> inputContainerSupportCodes = new HashSet<String>();
		Set<String> inputContainerCodes = new HashSet<String>();
		Set<String> inputFromTransformationTypeCodes = new HashSet<String>();
		//Set<String> inputProcessTypeCodes = new HashSet<String>();
		//Set<String> inputProcessCodes  = new HashSet<String>();
		
		exp.atomicTransfertMethods.stream().map(atm -> atm.inputContainerUseds)
		.flatMap(List::stream)
		.forEach(inputContainer -> {
			inputContainerCodes.add(inputContainer.code);
			projectCodes.addAll(inputContainer.projectCodes);
			sampleCodes.addAll(inputContainer.sampleCodes);
			inputContainerSupportCodes.add(inputContainer.locationOnContainerSupport.code);			
			if(null != inputContainer.fromTransformationTypeCodes)
				inputFromTransformationTypeCodes.addAll(inputContainer.fromTransformationTypeCodes);			
		});

		ExperimentType experimentType=ExperimentType.find.findByCode(exp.typeCode);
		if(experimentType.newSample){
			exp.atomicTransfertMethods.stream().map(atm -> atm.outputContainerUseds)
			.flatMap(List::stream)
			.forEach(ocu -> {
				Map<String,PropertyValue> experimentProperties = ocu.experimentProperties;
				if(experimentProperties.containsKey("projectCode")){
					projectCodes.add(experimentProperties.get("projectCode").value.toString());
				}
				
				if(experimentProperties.containsKey("sampleCode")){
					sampleCodes.add(experimentProperties.get("sampleCode").value.toString());
				}						
			});
		}
		
		
		exp.projectCodes = projectCodes;		
		exp.sampleCodes = sampleCodes;
		exp.inputContainerSupportCodes = inputContainerSupportCodes;		
		exp.inputContainerCodes = inputContainerCodes;
		exp.inputFromTransformationTypeCodes = inputFromTransformationTypeCodes;
		
	}
	
	private static void updateOCUWithNewContainer(OutputContainerUsed ocu, Container updatedContainer) {
		Logger.info("update OCU "+ocu.code+" with "+updatedContainer.code);
		ocu.contents = updatedContainer.contents;
	}

	private static void updateICUWithNewContainer(InputContainerUsed icu, Container newParentContainer) {
		Logger.info("update ICU "+icu.code+" with "+newParentContainer.code);
		icu.code = newParentContainer.code;
		icu.categoryCode = newParentContainer.categoryCode;
		icu.contents= newParentContainer.contents;
		icu.locationOnContainerSupport= newParentContainer.support;
		
		///TODO Quiz volume, conc, quantity and size ????
		icu.volume= newParentContainer.volume;        
		icu.concentration= newParentContainer.concentration; 
		icu.quantity= newParentContainer.quantity; 	
		icu.size= newParentContainer.size; 	
		
		icu.projectCodes= newParentContainer.projectCodes; 
		icu.sampleCodes= newParentContainer.sampleCodes; 
		icu.fromTransformationTypeCodes= newParentContainer.fromTransformationTypeCodes;
		icu.fromTransformationCodes= newParentContainer.fromTransformationCodes;		
	}

	

	private static List<Process> updateProcessWhereParent(List<Container> updatedContainers, List<Experiment> updatedExperiments) {
		Logger.info("");
		Logger.info("start update process where parent");	
		List<Process> updatedProcesses = new ArrayList<Process>();
		
		updatedContainers.forEach(container ->{
			List<Process> processes = getProcessesWhereInput(container.code);
			Logger.info(container.code+" : "+container.contents.size()+" / "+processes.size());	
			
			if(processes.size() > 0){
				Set<String> contentID = container.contents.stream().map(content -> content.projectCode+"-"+content.sampleCode+"-"+getTagValue(content.properties)).collect(Collectors.toSet());
				
				Map<String, List<Process>> processesByKey = processes.stream().collect(Collectors.groupingBy(p -> p.typeCode+"-"+p.traceInformation.creationDate.toString()));
				Set<String> keySet = processesByKey.keySet();
				Logger.info(container.code+" : nb process type "+keySet.size());
				//Treat each process type separately
				Iterator<String> itiKey = keySet.iterator();
				while(itiKey.hasNext()){
					String key = itiKey.next();
					Logger.info(container.code+" : treat process type "+key);
					List<Process> processNotMatchContents = processesByKey.get(key).stream()
							.filter(p -> !contentID.contains(p.sampleOnInputContainer.projectCode+"-"+p.sampleOnInputContainer.sampleCode+"-"+getTagValue(p.sampleOnInputContainer.properties)))
							.collect(Collectors.toList());
					Logger.info(container.code+" : "+processNotMatchContents.size()+" process(es) must be update ");
					
					Set<String> processID =  processesByKey.get(key).stream().map(p -> p.sampleOnInputContainer.projectCode+"-"+p.sampleOnInputContainer.sampleCode+"-"+getTagValue(p.sampleOnInputContainer.properties)).collect(Collectors.toSet());
					List<Content> contentNotMatchProcesses = container.contents.stream()
							.filter(c -> !processID.contains(c.projectCode+"-"+c.sampleCode+"-"+getTagValue(c.properties)))
							.collect(Collectors.toList());
					Logger.info(container.code+" : "+contentNotMatchProcesses.size()+" content(es) must be processed ");
					
					if(processNotMatchContents.size() == contentNotMatchProcesses.size()){
						
						Set<String> oldProcessCodes = new TreeSet<String>(); 
						Set<String> newProcessCodes = new TreeSet<String>(); 
						
						IntStream.range(0, processNotMatchContents.size()).forEach(i -> {
							Process processNeedUpdate = processNotMatchContents.get(i);
							oldProcessCodes.add(processNeedUpdate.code);
							
							Content contentUsedToUpdate = contentNotMatchProcesses.get(i);
							//TODO not managed new sample code
							processNeedUpdate.sampleCodes = SampleHelper.getSampleParent(contentUsedToUpdate.sampleCode);
							processNeedUpdate.projectCodes = SampleHelper.getProjectParent(processNeedUpdate.sampleCodes);
							processNeedUpdate.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(contentUsedToUpdate, container);
							//need sampleOnInputContainer to generate code
							processNeedUpdate.code = CodeHelper.getInstance().generateProcessCode(processNeedUpdate);
							newProcessCodes.add(processNeedUpdate.code);
							
							updatedProcesses.add(processNeedUpdate);
						});
						
						updatedContainers.forEach(c -> {
							c.treeOfLife.from.containers.stream().forEach(cParents -> {
								if(cParents.processCodes.containsAll(oldProcessCodes)){
									cParents.processCodes.removeAll(oldProcessCodes);
									cParents.processCodes.addAll(newProcessCodes);
									Logger.info(container.code+" -> "+c.code +" : update treeOfLife.from.containers.processCodes for parent code : "+cParents.code);
								}
							});							
						});
						
						updatedExperiments.forEach(exp ->{
							if(exp.inputProcessCodes.containsAll(oldProcessCodes)){
								exp.inputProcessCodes.removeAll(oldProcessCodes);
								exp.inputProcessCodes.addAll(newProcessCodes);
								Logger.info(exp.code +" : update inputProcessCodes");
							}
							
							exp.atomicTransfertMethods.stream()
							.map(atm -> atm.inputContainerUseds)
							.flatMap(List::stream)
							.forEach(icu -> {
								if(icu.processCodes.containsAll(oldProcessCodes)){
									icu.processCodes.removeAll(oldProcessCodes);
									icu.processCodes.addAll(newProcessCodes);
									Logger.info(exp.code +" - "+icu.code +" : update inputProcessCodes");
								}
							});
							
						});
					}else{
						throw new RuntimeException("not managed a nbprocess != nbcontents");
					}					
				}				
			}						
		});
		
		Logger.info("end update process where parent");	
		return updatedProcesses;
	}

	private static String getTagValue(Map<String,PropertyValue> properties) {
		return properties.containsKey(TAG_PROPERTY_NAME)?properties.get(TAG_PROPERTY_NAME).value.toString():"NONE";
		
	}

	private static List<Process> updateProcessWhereChild(Container oldParentContainer, Container newParentContainer, List<Container> updatedContainers, List<Experiment> updatedExperiments) {
		Logger.info("");
		Logger.info("start update process where child");
		
		List<Process> oldProcesses = getProcessesWhereChild(oldParentContainer.code);
		List<Process> newProcesses = getProcessesWhereChild(newParentContainer.code);
		
		Set<String> oldProcessCodes = oldProcesses.stream().map(p -> p.code).collect(Collectors.toSet());
		Set<String> newProcessCodes = newProcesses.stream().map(p -> p.code).collect(Collectors.toSet());
		
		
		List<Container> containers = getNextContainersForProcesses(oldParentContainer.code, oldProcesses.stream().map(p -> p.code).collect(Collectors.toList()));
		
		Set<String> containerCodes = new TreeSet<String>();
		Set<String> containerSupportCodes = new TreeSet<String>();
		Set<String> experimentCodes = new TreeSet<String>();
		
		Iterator<Container> iti = containers.iterator();
		while(iti.hasNext()){
			Container c = iti.next();
			containerCodes.add(c.code);
			containerSupportCodes.add(c.support.code);
			if(null != c.treeOfLife.from.experimentCode)
				experimentCodes.add(c.treeOfLife.from.experimentCode);
			if(null != c.qualityControlResults)
				experimentCodes.addAll(c.qualityControlResults.stream().map(qcr -> qcr.code).collect(Collectors.toSet()));			
		}
		
		Logger.info("containerCodes  : "+containerCodes);
		Logger.info("containerSupportCodes  : "+containerSupportCodes);
		Logger.info("experimentCodes  : "+experimentCodes);
		
		oldProcesses.forEach(p -> {
			Logger.info("update old process  : "+p.code);
			p.experimentCodes.removeAll(experimentCodes);
			p.outputContainerCodes.removeAll(containerCodes);
			p.outputContainerSupportCodes.removeAll(containerSupportCodes);	
			p.currentExperimentTypeCode = getLastExperiment(p.experimentCodes);
			Logger.info("currentExperimentTypeCode  : "+p.currentExperimentTypeCode);
			//TODO update projectCode and sampleCode but need to retrieve the content
		});
		
		newProcesses.forEach(p -> {
			Logger.info("update new process  : "+p.code);
			p.experimentCodes.addAll(experimentCodes);
			p.outputContainerCodes.addAll(containerCodes);
			p.outputContainerSupportCodes.addAll(containerSupportCodes);
			p.currentExperimentTypeCode = getLastExperiment(p.experimentCodes);
			Logger.info("currentExperimentTypeCode  : "+p.currentExperimentTypeCode);
			//TODO update projectCode and sampleCode but need to retrieve the content
		});
		
		updatedContainers.forEach(c -> {
			c.treeOfLife.from.containers.stream().forEach(cParents -> {
				if(cParents.processCodes.containsAll(oldProcessCodes)){
					cParents.processCodes.removeAll(oldProcessCodes);
					cParents.processCodes.addAll(newProcessCodes);
					Logger.info(c.code +" : update treeOfLife.from.containers.processCodes for parent code : "+cParents.code);
				}
			});							
		});
		
		updatedExperiments.forEach(exp ->{
			if(exp.inputProcessCodes.containsAll(oldProcessCodes)){
				exp.inputProcessCodes.removeAll(oldProcessCodes);
				exp.inputProcessCodes.addAll(newProcessCodes);
				Logger.info(exp.code +" : update inputProcessCodes");
			}
			exp.atomicTransfertMethods.stream()
				.map(atm -> atm.inputContainerUseds)
				.flatMap(List::stream)
				.forEach(icu -> {
					if(icu.processCodes.containsAll(oldProcessCodes)){
						icu.processCodes.removeAll(oldProcessCodes);
						icu.processCodes.addAll(newProcessCodes);
						Logger.info(exp.code +" - "+icu.code +" : update inputProcessCodes");
					}
				});
		});
		
		List<Process> updatedProcesses = new ArrayList<Process>(oldProcesses);
		updatedProcesses.addAll(newProcesses);
		
		Logger.info("end update process where child");
		return updatedProcesses;
	}

	private static String getLastExperiment(Set<String> experimentCodes) {
		List<Experiment> exps =  MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.in("code", experimentCodes)).sort("traceInformation.creationDate", Sort.DESC).limit(1).toList();
		return exps.get(0).code;
	}

	private static List<Container> getNextContainersForProcesses(String code, List<String> processCodes) {
		return MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
				DBQuery.in("treeOfLife.from.containers.processCodes", processCodes).regex("treeOfLife.paths", Pattern.compile(","+code+"$|,"+code+",")))
		.sort("traceInformation.creationDate").toList();		
	}

	/**
	 * Load only process where container are child not on input
	 * @param oldParentContainerName
	 * @return
	 */
	private static List<Process> getProcessesWhereChild(String containerCode) {
		// TODO Auto-generated method stub
		return MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("outputContainerCodes", containerCode)).toList();
	}
	
	private static List<Process> getProcessesWhereInput(String containerCode) {
		// TODO Auto-generated method stub
		return MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("inputContainerCode", containerCode)).toList();
	}


	private static Container getContainer(String code) {
		return MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, code);
	}


	private static List<Container> updateContainers(Container oldParent, Container newParent) {
		Logger.info("");
		Logger.info("start update containers");
		
		Set<String> oldParentProjectCodes = getNotExistInSecondElement(oldParent.projectCodes, newParent.projectCodes);
		Set<String> newParentProjectCodes =getNotExistInSecondElement(newParent.projectCodes, oldParent.projectCodes);
		
		Set<String> oldParentSampleCodes = getNotExistInSecondElement(oldParent.sampleCodes, newParent.sampleCodes);
		Set<String> newParentSampleCodes =getNotExistInSecondElement(newParent.sampleCodes, oldParent.sampleCodes);
		
		List<String> oldParentPaths = getPaths(oldParent.treeOfLife.paths, oldParent.code);
		List<String> newParentPaths = getPaths(newParent.treeOfLife.paths, newParent.code);
		
		Logger.info("old sample codes"+oldParentSampleCodes);
		Logger.info("new sample codes"+newParentSampleCodes);

		
		Map<String, PropertyValue> allContentPropertiesKeep = new HashMap<String, PropertyValue>();
		//1 find all childs that's must be updated
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.regex("treeOfLife.paths", Pattern.compile(","+oldParent.code+"$|,"+oldParent.code+",")))
				.sort("traceInformation.creationDate").toList();
		containers.forEach(container -> {
					Logger.info("");
					Logger.info(container.code+" / "+container.traceInformation.creationDate);
					
					container.projectCodes.removeAll(oldParentProjectCodes);
					container.projectCodes.addAll(newParentProjectCodes);
					
					container.sampleCodes.removeAll(oldParentSampleCodes);
					container.sampleCodes.addAll(newParentSampleCodes);
					
					updatePaths(container, oldParentPaths, newParentPaths);		
					updateContents(container, oldParent, newParent, allContentPropertiesKeep);
			
		});
		
		Logger.info("end update containers");
		
		return containers;
	}

	private static void updatePaths(Container container, List<String> oldParentPaths, List<String> newParentPaths) {
		//PATH
		List<String> oldPaths = container.treeOfLife.paths.parallelStream()
				.filter(path -> oldParentPaths.stream().map(oldPath -> path.startsWith(oldPath)).findFirst().get())
				.collect(Collectors.toList());
		
		List<String> newPaths = IntStream.range(0, oldPaths.size())
				.mapToObj(i -> oldPaths.get(i).replace(oldParentPaths.get(i), newParentPaths.get(i)))
				.collect(Collectors.toList());
		if(container.treeOfLife.paths.removeAll(oldPaths)){
			Logger.info(container.code +" : update treeOfLife.paths");	
			container.treeOfLife.paths.addAll(newPaths);
		}
	}
		

	private static void updateContents(Container container,Container oldParent, Container newParent, Map<String, PropertyValue> allContentPropertiesKeep) {
		Set<String> currentContentPropertyKeys = getContentPropertiesMustBeKeep(container);
		
		List<Content> oldContents = container.contents.stream()
				.filter(content -> oldParent.contents.stream()
							.map(oldContent -> isSameContent(content, oldContent))
							.findFirst().get())
				.collect(Collectors.toList());
		
		Map<String, PropertyValue> currentContentProperties = oldContents.parallelStream()
					.map(content -> content.properties.entrySet())
					.flatMap(Set::stream)
					.filter(entry -> currentContentPropertyKeys.contains(entry.getKey()))
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
		
		allContentPropertiesKeep.putAll(currentContentProperties);
		
		
		//Logger.debug("Nb container contents "+container.contents.size());
		
		List<Content> newContents =  newContents(newParent.contents, allContentPropertiesKeep);
		
		
		if(container.contents.removeAll(oldContents)){
			Logger.debug(container.code+" : update contents" );
			container.contents.addAll(newContents);
		}
	}


	private static List<Content> newContents(List<Content> contents,
			Map<String, PropertyValue> allContentPropertiesKeep) {
		
		return contents.stream().map(c -> c.clone()).map(c -> {c.properties.putAll(allContentPropertiesKeep); return c;}).collect(Collectors.toList());
	}


	private static Set<String> getContentPropertiesMustBeKeep(Container container) {
		List<String> expTypeCodes = new ArrayList<String>();
		if(null!=container.fromTransformationTypeCodes)expTypeCodes.addAll(container.fromTransformationTypeCodes);
		if(null!=container.fromPurificationTypeCode)expTypeCodes.add(container.fromPurificationTypeCode);
		if(null!=container.fromTransfertTypeCode)expTypeCodes.add(container.fromTransfertTypeCode);
		
		if(null != container.qualityControlResults){
			expTypeCodes.addAll(container.qualityControlResults.stream().map(qc -> qc.typeCode).collect(Collectors.toList()));
		}
		
		List<ExperimentType> expTypes = ExperimentType.find.findByCodes(expTypeCodes);
		return expTypes.parallelStream()
			.map(expType -> expType.getPropertyDefinitionByLevel(Level.CODE.Content))
			.flatMap(List::stream)
			.map(pd -> pd.code)
			.collect(Collectors.toSet());		
	}


	private static boolean isSameContent(Content content, Content oldContent) {
		return content.projectCode.equals(oldContent.projectCode) 
							&& content.sampleCode.equals(oldContent.sampleCode)
							&& (!oldContent.properties.containsKey(TAG_PROPERTY_NAME) 
									|| (oldContent.properties.containsKey(TAG_PROPERTY_NAME) 
											&& content.properties.get(TAG_PROPERTY_NAME).value.equals(oldContent.properties.get(TAG_PROPERTY_NAME).value)));
		
		//TODO Manage fromSampleCode and fromProjectCode ???
		
	}


	private static List<String> getPaths(List<String> paths, String code) {
		return paths.stream().map(path -> path+","+code).collect(Collectors.toList());		
	}


	private static Set<String> getNotExistInSecondElement(Set<String> list1, Set<String> list2) {
		Set<String> retainElements = new TreeSet<String>(list1);
		retainElements.removeAll(list2);
		return retainElements;
	}
}
