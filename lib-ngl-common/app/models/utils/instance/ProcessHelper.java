package models.utils.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import play.Logger;
import play.Play;
import rules.services.RulesServices6;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.container.instance.ContainerSupportValidationHelper;
import validation.container.instance.ContainerValidationHelper;
import fr.cea.ig.MongoDBDAO;

public class ProcessHelper {

	public static void updateContainer(Container container, String typeCode, Set<String> codes,ContextValidation contextValidation){
		if(container.fromExperimentTypeCodes == null || container.fromExperimentTypeCodes.size() == 0){
			container.fromExperimentTypeCodes = new HashSet<String>();
			ProcessType processType;
			try {
				processType = ProcessType.find.findByCode(typeCode);
				container.fromExperimentTypeCodes.add(processType.voidExperimentType.code);

			} catch (DAOException e) {
				throw new RuntimeException();
			}
		}
		container.processTypeCode = typeCode;
		if(container.inputProcessCodes==null){
			container.inputProcessCodes=new HashSet<String>();
		}
		container.inputProcessCodes.addAll(codes);
		
//		contextValidation.putObject(CommonValidationHelper.FIELD_STATE_CODE, container.state.code);
		
//		container.inputProcessCodes=InstanceHelpers.addCodesList(codes, container.inputProcessCodes);
//		ContainerValidationHelper.validateInputProcessCodes(container.inputProcessCodes,contextValidation);
//		ContainerValidationHelper.validateExperimentTypeCodes(container.fromExperimentTypeCodes, contextValidation);
//		ContainerValidationHelper.validateProcessTypeCode(container.processTypeCode, contextValidation);
		if(!contextValidation.hasErrors()){
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					DBQuery.is("code",container.code),
					DBUpdate.set("inputProcessCodes", container.inputProcessCodes)
					.set("processTypeCode", container.processTypeCode)
					.set("fromExperimentTypeCodes",container.fromExperimentTypeCodes));
		}
	}


	public static void updateContainerSupportFromContainer(Container container,ContextValidation contextValidation){
		ContainerSupport containerSupport=MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, container.support.code);
		if(null != containerSupport){
			if(containerSupport.fromExperimentTypeCodes==null){
				containerSupport.fromExperimentTypeCodes=new HashSet<String>();
			}
			containerSupport.fromExperimentTypeCodes.addAll(container.fromExperimentTypeCodes);
			//containerSupport.fromExperimentTypeCodes=InstanceHelpers.addCodesList(container.fromExperimentTypeCodes, containerSupport.fromExperimentTypeCodes);
			contextValidation.putObject(CommonValidationHelper.FIELD_STATE_CODE, container.state.code);
			ContainerSupportValidationHelper.validateExperimentTypeCodes(containerSupport.fromExperimentTypeCodes, contextValidation);
			if(!contextValidation.hasErrors()){
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,ContainerSupport.class,
						DBQuery.is("code", container.support.code)
						,DBUpdate.set("fromExperimentTypeCodes",container.fromExperimentTypeCodes));
			}
		}else{
			Logger.error("Support container not exist = "+container.support.code);
		}
	}


	public static void updateNewContainerSupportCodes(OutputContainerUsed outputContainerUsed,
			List<InputContainerUsed> inputContainerUseds,Experiment experiment) {
		List<Query> queryOr = new ArrayList<Query>();
		queryOr.add(DBQuery.in("containerInputCode",ContainerUsedHelper.getContainerCodes(inputContainerUseds)));
		queryOr.add(DBQuery.in("newContainerSupportCodes",ContainerUsedHelper.getContainerSupportCodes(inputContainerUseds)));
		Query query=null;
		query=DBQuery.and(DBQuery.in("experimentCodes",experiment.code));
		if(queryOr.size()!=0){
			query=query.and(DBQuery.or(queryOr.toArray(new Query[queryOr.size()])));
		}

		MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,query,
				DBUpdate.push("newContainerSupportCodes",outputContainerUsed.locationOnContainerSupport.code),true);


	}
	
	public static Process applyRules(Process proc, ContextValidation ctx ,String rulesName){
		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(proc);
		facts.add(ctx);
		List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(Play.application().configuration().getString("rules.key"), rulesName, facts);
		return proc;

	}
	
	public static List<Process>  applyRules(List<Process> processes, ContextValidation ctx ,String rulesName){
		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(ctx);
		for(Process proc:processes){
		facts.add(proc);
		}
		

		List<Object> factsAfterRules = RulesServices6.getInstance().callRulesWithGettingFacts(Play.application().configuration().getString("rules.key"), rulesName, facts);
		

		return processes;

	}	
	
	public static HashMap<String, PropertyValue> cloneProcessProperties(Process process){		
		HashMap<String, PropertyValue> hmap = null;
		if(process.properties!=null && !process.properties.isEmpty()){
			hmap = new HashMap<String, PropertyValue>(process.properties);
		}
		return hmap;

	}

}
