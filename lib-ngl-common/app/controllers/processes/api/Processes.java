package controllers.processes.api;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.State;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import controllers.DocumentController;
import controllers.NGLControllerHelper;
import controllers.authorisation.Permission;
import controllers.containers.api.Containers;
import controllers.containers.api.ContainersSearchForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;

public class Processes extends DocumentController<Process> {

	final static Form<State> stateForm = form(State.class);
	final static Form<ProcessesSearchForm> processesSearchForm = form(ProcessesSearchForm.class);
	
	public Processes() {
		super(InstanceConstants.PROCESS_COLL_NAME, Process.class);		
	}

	@Permission(value={"reading"})
	public Result list(){
		ProcessesSearchForm searchForm = filledFormQueryString(ProcessesSearchForm.class);
		DBQuery.Query query = getQuery(searchForm);

		if(searchForm.datatable){
			MongoDBResult<Process> results =  mongoDBFinder(searchForm, query);
			List<Process> processes = results.toList();
			return ok(Json.toJson(new DatatableResponse<Process>(processes, results.count())));
		}else if (searchForm.list){
			BasicDBObject keys = new BasicDBObject();
			keys.put("_id", 0);//Don't need the _id field
			keys.put("code", 1);
			if(null == searchForm.orderBy)searchForm.orderBy = "code";
			if(null == searchForm.orderSense)searchForm.orderSense = 0;				

			MongoDBResult<Process> results = mongoDBFinder(searchForm, query, keys);
			List<Process> processes = results.toList();
			List<ListObject> los = new ArrayList<ListObject>();
			for(Process p: processes){					
				los.add(new ListObject(p.code, p.code));								
			}
			return Results.ok(Json.toJson(los));
		}else{
			if(null == searchForm.orderBy)searchForm.orderBy = "code";
			if(null == searchForm.orderSense)searchForm.orderSense = 0;
			MongoDBResult<Process> results = mongoDBFinder(searchForm, query);
			List<Process> processes = results.toList();
			return ok(Json.toJson(processes));
		}
	}
	
	/**
	 * Construct the process query
	 * @param processesSearch
	 * @return the query
	 */
	private DBQuery.Query getQuery(ProcessesSearchForm processesSearch) throws DAOException{
		List<Query> queryElts = new ArrayList<Query>();
		Query query = null;

		Logger.info("Process Query : "+processesSearch);

		if (CollectionUtils.isNotEmpty(processesSearch.projectCodes)) { //all
			queryElts.add(DBQuery.in("projectCodes", processesSearch.projectCodes));
		}else if(StringUtils.isNotBlank(processesSearch.projectCode)){
			queryElts.add(DBQuery.is("projectCodes", processesSearch.projectCode));
		}

		if (CollectionUtils.isNotEmpty(processesSearch.sampleCodes)) { //all
			queryElts.add(DBQuery.in("sampleCodes", processesSearch.sampleCodes));
		}else if(StringUtils.isNotBlank(processesSearch.sampleCode)){
			queryElts.add(DBQuery.is("sampleCodes", processesSearch.sampleCode));
		}
		
		if (CollectionUtils.isNotEmpty(processesSearch.sampleTypeCodes)) { //all
			queryElts.add(DBQuery.in("sampleOnInputContainer.sampleTypeCode", processesSearch.sampleTypeCodes));
		}

		if(StringUtils.isNotBlank(processesSearch.code)){
			queryElts.add(DBQuery.is("code", processesSearch.code));
		}else if(CollectionUtils.isNotEmpty(processesSearch.codes)){
			queryElts.add(DBQuery.in("code", processesSearch.codes));
		}else if(StringUtils.isNotBlank(processesSearch.codeRegex)){
			queryElts.add(DBQuery.regex("code", Pattern.compile(processesSearch.codeRegex)));
		}
		
		if(StringUtils.isNotBlank(processesSearch.experimentCode)){
			queryElts.add(DBQuery.in("experimentCodes", processesSearch.experimentCode));
		}else if(CollectionUtils.isNotEmpty(processesSearch.experimentCodes)){
			queryElts.add(DBQuery.in("experimentCodes", processesSearch.experimentCodes));
		}else if(StringUtils.isNotBlank(processesSearch.experimentCodeRegex)){
			queryElts.add(DBQuery.regex("experimentCodes", Pattern.compile(processesSearch.experimentCodeRegex)));
		}

		if(StringUtils.isNotBlank(processesSearch.typeCode)){
			queryElts.add(DBQuery.is("typeCode", processesSearch.typeCode));
		}else if(CollectionUtils.isNotEmpty(processesSearch.typeCodes)){
			queryElts.add(DBQuery.in("typeCode", processesSearch.typeCodes));
		}

		if(StringUtils.isNotBlank(processesSearch.categoryCode)){
			queryElts.add(DBQuery.is("categoryCode", processesSearch.categoryCode));
		}else if(CollectionUtils.isNotEmpty(processesSearch.categoryCodes)){
			queryElts.add(DBQuery.in("categoryCode", processesSearch.categoryCodes));
		}

		if(CollectionUtils.isNotEmpty(processesSearch.stateCodes)){
			queryElts.add(DBQuery.in("state.code", processesSearch.stateCodes));
		}else if(StringUtils.isNotBlank(processesSearch.stateCode)){
			queryElts.add(DBQuery.is("state.code", processesSearch.stateCode));
		}
		if(CollectionUtils.isNotEmpty(processesSearch.users)){
			queryElts.add(DBQuery.in("traceInformation.createUser", processesSearch.users));
		}

		if(StringUtils.isNotBlank(processesSearch.createUser)){   
			queryElts.add(DBQuery.is("traceInformation.createUser", processesSearch.createUser));
		}

		if(null != processesSearch.fromDate){
			queryElts.add(DBQuery.greaterThanEquals("traceInformation.creationDate", processesSearch.fromDate));
		}

		if(null != processesSearch.toDate){
			queryElts.add(DBQuery.lessThanEquals("traceInformation.creationDate", (DateUtils.addDays(processesSearch.toDate, 1))));
		}

		if(StringUtils.isNotBlank(processesSearch.supportCode) || 
				StringUtils.isNotBlank(processesSearch.supportCodeRegex) ||
					CollectionUtils.isNotEmpty(processesSearch.supportCodes) ||	
						StringUtils.isNotBlank(processesSearch.containerSupportCategory) ){
			BasicDBObject keys = new BasicDBObject();
			keys.put("_id", 0);//Don't need the _id field
			keys.put("code", 1);

			ContainersSearchForm cs = new ContainersSearchForm();
			cs.supportCodeRegex = processesSearch.supportCodeRegex;
			cs.supportCode = processesSearch.supportCode;
			cs.supportCodes = processesSearch.supportCodes;
			cs.containerSupportCategory=processesSearch.containerSupportCategory;

			List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, Containers.getQuery(cs), keys).toList();
			//InputContainer
			List<Query> queryContainer = new ArrayList<Query>();
			if(containers.size() > 0){
				queryContainer.add(DBQuery.in("inputContainerCode", containers.stream().map(c -> c.code).collect(Collectors.toList())));
			}
			
			if(StringUtils.isNotBlank(processesSearch.supportCode)){
				queryContainer.add(DBQuery.is("outputContainerSupportCodes",processesSearch.supportCode));
			} else if(StringUtils.isNotBlank(processesSearch.supportCodeRegex)){
				queryContainer.add(DBQuery.regex("outputContainerSupportCodes",Pattern.compile(processesSearch.supportCodeRegex)));
			} else if(CollectionUtils.isNotEmpty(processesSearch.supportCodes)){
				queryContainer.add(DBQuery.in("outputContainerSupportCodes",processesSearch.supportCodes));
			}
			
			if(queryContainer.size()!=0){
				queryElts.add(DBQuery.or(queryContainer.toArray(new Query[queryContainer.size()])));
			}
			else {
				queryElts.add(DBQuery.exists("code"));
			}

			Logger.debug("Nb containers find"+containers.size());
		}

		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(processesSearch.properties, Level.CODE.Process, "properties"));
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(processesSearch.sampleOnInputContainerProperties, Level.CODE.Content, "sampleOnInputContainer.properties"));

		if(queryElts.size() > 0){
			query = DBQuery.and(queryElts.toArray(new Query[queryElts.size()]));
		}

		return query;
	}
}
