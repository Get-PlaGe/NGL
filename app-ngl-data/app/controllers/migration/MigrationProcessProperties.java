package controllers.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;
import models.utils.instance.SampleHelper;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import validation.ContextValidation;
import validation.processes.instance.ProcessValidationHelper;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.MongoDBResult.Sort;
import models.laboratory.processes.instance.Process;

public class MigrationProcessProperties extends CommonController {
	
	private static final String PROCESS_COLL_NAME_BCK = InstanceConstants.PROCESS_COLL_NAME+"201702XX_BCK";
	private static final String TAG_PROPERTY_NAME = "tag";
	
	static ALogger logger=Logger.of("MigrationProcessProperties");

	public static Result migration(){

		Logger.info("Start point of Migration Process");

		JacksonDBCollection<Process, String> containersCollBck = MongoDBDAO.getCollection(PROCESS_COLL_NAME_BCK, Process.class);
		if(containersCollBck.count() == 0){
		
			Logger.info("Migration Process start");
			
			//backupContainerCollection();
			
			MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("outputContainerSupportCodes").notExists("outputContainerCodes"))
					.sort("code",Sort.DESC).getCursor().forEach(p -> migrationProcess(p));
			
			Logger.info("Migration process end");

		}else{
			Logger.info("Migration Process already execute !");
		}
		Logger.info("Migration Process finish");
		return ok("Migration Process Finish");
	}

	

	private static void migrationProcess(Process process) {
		//1 Get Tag
		String tag = getTagAssignFromProcessContainers(process);
		process.outputContainerCodes = new TreeSet<String>();
		
		List<String> containerSupportCodes = new ArrayList<String>();
		containerSupportCodes.add(process.inputContainerSupportCode);
		if(null != process.outputContainerSupportCodes){
			containerSupportCodes.addAll(process.outputContainerSupportCodes);
		}
		
		MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,  
				DBQuery.in("support.code", containerSupportCodes).elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes).in("projectCode", process.projectCodes)))
		.cursor.forEach(container -> {
			
			
			
			List<Content> contentFound = container.contents.stream()
				.filter(content -> ((process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && !content.properties.containsKey(TAG_PROPERTY_NAME))
					|| (null != tag && process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && content.properties.containsKey(TAG_PROPERTY_NAME) 
							&&  tag.equals(content.properties.get(TAG_PROPERTY_NAME).value))))
				.collect(Collectors.toList());
			
			if(contentFound.size() == 1){
				Integer nbContentChange = contentFound.stream()
						.filter(content -> ((process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && !content.properties.containsKey(TAG_PROPERTY_NAME))
								|| (null != tag && process.sampleCodes.contains(content.sampleCode) && process.projectCodes.contains(content.projectCode) && content.properties.containsKey(TAG_PROPERTY_NAME) 
										&&  tag.equals(content.properties.get(TAG_PROPERTY_NAME).value))))
						.mapToInt(content -> {
							if(!"UA".equals(container.state.code) 
									&& !"IS".equals(container.state.code)
									&& !"IW-P".equals(container.state.code)
									&& !"F".equals(container.state.code)){
								content.processProperties = process.properties;	
								return 1;	
							}	
							return 0;
						}).sum();
					
					if(!container.code.equals(process.inputContainerCode)){
						/*
						Logger.debug("test "+container.code);
						if(null != container.treeOfLife){
							long nbFound = container.treeOfLife.from.containers.stream().filter(pc -> pc.processCodes.contains(process.code)).count();
							if(nbFound == 1)process.outputContainerCodes.add(container.code);
							else if(nbFound > 1)Logger.warn("strange found several parent container for same process "+container.code);
							else if(container.categoryCode.equals("tube"))process.outputContainerCodes.add(container.code);
						}else{
							//Logger.debug("add container to process "+container.code);
							process.outputContainerCodes.add(container.code);
						}
						*/
						process.outputContainerCodes.add(container.code);
						
					}
					
					if(nbContentChange > 0){
						//Logger.debug("update container "+container.code);
						container.traceInformation.setTraceInformation("ngl");
						//MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);	
					}
			}else if(contentFound.size() > 1){
				Logger.error("found several contents "+container.code);
			}
			
			
		});
		if(process.outputContainerCodes.size() == process.outputContainerSupportCodes.size()){
			//Logger.debug("save process "+process.code+" / "+process.state.code);			
			process.traceInformation.setTraceInformation("ngl");
			//MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME,process);
		}else{
			Logger.debug("process size cont !="+process.code+" / "+process.state.code+" "+process.outputContainerCodes.size()+" != "+process.outputContainerSupportCodes.size());
		}
		
	}


	private static String getTagAssignFromProcessContainers(Process process) {
		
		if(process.sampleOnInputContainer.properties.containsKey(TAG_PROPERTY_NAME)){
			return process.sampleOnInputContainer.properties.get(TAG_PROPERTY_NAME).value.toString();
		}else{
			
			DBQuery.Query query = DBQuery.in("support.code",process.outputContainerSupportCodes)
						.size("contents", 1)
						.elemMatch("contents", DBQuery.in("sampleCode", process.sampleCodes)
													.in("projectCode",  process.projectCodes)
													.exists("properties.tag"));
			
			Set<String> tags = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,query)
					.toList()
					.stream()
					.map(c -> c.contents)
					.flatMap(List::stream)
					.map(c -> c.properties.get(TAG_PROPERTY_NAME).value.toString())
					.collect(Collectors.toSet());
			
			
			if(tags.size() == 1){
				return tags.iterator().next();
			}else if(tags.size() > 1){
				Logger.warn("Found lot of tags for process "+process.code);
				return null;
			} else{
				return null;
			}
		}
	}
	private static void backupContainerCollection() {
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" start");
		MongoDBDAO.save(PROCESS_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class).toList());
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" end");
	}

}
