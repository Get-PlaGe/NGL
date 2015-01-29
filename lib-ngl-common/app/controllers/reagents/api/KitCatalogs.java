package controllers.reagents.api;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.reagent.description.KitCatalog;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.ListObject;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

import com.mongodb.BasicDBObject;

import controllers.DocumentController;
import fr.cea.ig.MongoDBResult;

public class KitCatalogs extends DocumentController<KitCatalog>{
	public KitCatalogs() {
		super(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class);
	}
	
	final static Form<KitCatalogSearchForm> kitCatalogSearchForm = form(KitCatalogSearchForm.class);
	
	public Result save(){
		Form<KitCatalog> kitCatalogFilledForm = getMainFilledForm();
		if(!mainForm.hasErrors()){
			KitCatalog kitCatalog = kitCatalogFilledForm.get();
			kitCatalog.code = kitCatalog.name.toLowerCase().replaceAll("\\s", "");
			
			ContextValidation contextValidation = new ContextValidation(getCurrentUser(), mainForm.errors());
			contextValidation.setCreationMode();
			
			InstanceHelpers.save(InstanceConstants.REAGENT_CATALOG_COLL_NAME, kitCatalog, contextValidation);
			if(!contextValidation.hasErrors()){
				return ok();
			}
		}
		return badRequest(mainForm.errorsAsJson());
	}
	
	public Result list(){
		Form<KitCatalogSearchForm> kitCatalogFilledForm = filledFormQueryString(kitCatalogSearchForm,KitCatalogSearchForm.class);
		KitCatalogSearchForm kitCatalogSearch = kitCatalogFilledForm.get();
		BasicDBObject keys = getKeys(kitCatalogSearch);
		DBQuery.Query query = getQuery(kitCatalogSearch);

		if(kitCatalogSearch.datatable){
			MongoDBResult<KitCatalog> results =  mongoDBFinder(kitCatalogSearch, query);
			List<KitCatalog> kitCatalogs = results.toList();
			
			return ok(Json.toJson(new DatatableResponse<KitCatalog>(kitCatalogs, results.count())));
		}else if (kitCatalogSearch.list){
			keys = new BasicDBObject();
			keys.put("_id", 0);//Don't need the _id field
			keys.put("code", 1);
			keys.put("name", 1);
			keys.put("category", 1);
			
			if(null == kitCatalogSearch.orderBy)kitCatalogSearch.orderBy = "code";
			if(null == kitCatalogSearch.orderSense)kitCatalogSearch.orderSense = 0;				
			
			MongoDBResult<KitCatalog> results = mongoDBFinder(kitCatalogSearch, query, keys);
			List<KitCatalog> kitCatalogs = results.toList();
			List<ListObject> los = new ArrayList<ListObject>();
			for(KitCatalog p: kitCatalogs){					
					los.add(new ListObject(p.code, p.name));								
			}
			
			return Results.ok(Json.toJson(los));
		}else{
			if(null == kitCatalogSearch.orderBy)kitCatalogSearch.orderBy = "code";
			if(null == kitCatalogSearch.orderSense)kitCatalogSearch.orderSense = 0;
			
			MongoDBResult<KitCatalog> results = mongoDBFinder(kitCatalogSearch, query);
			List<KitCatalog> kitCatalogs = results.toList();
			
			return ok(Json.toJson(kitCatalogs));
		}
	}
	
	private static Query getQuery(KitCatalogSearchForm kitCatalogSearch){
		List<DBQuery.Query> queryElts = new ArrayList<DBQuery.Query>();
		Query query = null;
		queryElts.add(DBQuery.is("category", "Kit"));
		
		if(queryElts.size() > 0){
			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		}

		return query;
	}
}