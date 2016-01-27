package controllers.commons.api;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.Value;
import models.laboratory.parameter.Index;
import models.utils.InstanceConstants;
import models.utils.ListObject;
import models.utils.dao.DAOException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;
import controllers.CommonController;
import controllers.readsets.api.ReadSetsSearchForm;
import fr.cea.ig.MongoDBDAO;

public class Parameters extends CommonController {
	
	// GA 24/07/2015 implementaton de la form +  params list et datatablecd SOL
	final static Form<ParametersSearchForm> form = form(ParametersSearchForm.class);

	public static Result list(String typeCode) throws DAOException {
	    Form<ParametersSearchForm> filledForm = filledFormQueryString(
				form, ParametersSearchForm.class);
		ParametersSearchForm parametersSearch = filledForm.get();
		parametersSearch.typeCode=typeCode;
		Query query = getQuery(parametersSearch);		
		
		List<Index> values=MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Index.class, query).toList();
		
		if (parametersSearch.datatable) {
		    return ok(Json.toJson(new DatatableResponse<Index>(values, values.size())));
		} else if (parametersSearch.list) {
		    List<ListObject> valuesListObject = new ArrayList<ListObject>();
		    for (Index s : values) {
		    	valuesListObject.add(new ListObject(s.code, s.name));
		    }
		    return ok(Json.toJson(valuesListObject));
		} else {
			return ok(Json.toJson(values));
		}
				
    }
 
	public static Result get(String typeCode, String code) throws DAOException {
		Index index=MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Index.class, DBQuery.is("typeCode", typeCode).is("code", code));
		if(index != null){
			return ok(Json.toJson(index));
		}
		else { return notFound(); }

    }  
	
	private static Query getQuery(ParametersSearchForm form) {
		List<Query> queries = new ArrayList<Query>();
		Query query = null;

		queries.add(DBQuery.is("typeCode", form.typeCode));
		
		if (StringUtils.isNotBlank(form.sequence)) { 
			queries.add(DBQuery.is("sequence", form.sequence));
		}
		if(CollectionUtils.isNotEmpty(form.categoryCodes)){
			queries.add(DBQuery.in("categoryCode", form.categoryCodes));
		}
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}
}
