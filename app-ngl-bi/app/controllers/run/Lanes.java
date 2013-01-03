package controllers.run;

import java.util.List;

import models.instance.run.Lane;
import models.instance.run.Run;
import models.instance.validation.BusinessValidationHelper;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.DataTableForm;
import utils.Mongo;
import fr.cea.ig.MongoDBDAO;

public class Lanes extends Controller{
	
	final static Form<DataTableForm> datatableForm = form(DataTableForm.class);
	final static Form<Lane> laneForm = form(Lane.class);
	
	public static Result createOrUpdate(String code, String format){
		Form<Lane> filledForm;
		filledForm = getFilledForm(format);
		
		if(!filledForm.hasErrors()) {
			Run run = MongoDBDAO.findByCode(Constants.CNG_RUN_ILLUMINA, Run.class, code);
			if(run == null){
				return notFound();
			}
			Lane laneValue = filledForm.get();
			BusinessValidationHelper.validateLane(filledForm.errors(), run,laneValue, Constants.CNG_RUN_ILLUMINA, null);
			if(!filledForm.hasErrors()) {
				System.out.println("insert OK :"+laneValue.number);
				Mongo.createOrUpdateInArray(Constants.CNG_RUN_ILLUMINA,Run.class,"code" , code, "lanes", "number", laneValue.number,laneValue);
				filledForm = filledForm.fill(laneValue);
			}
		}
		
		if (!filledForm.hasErrors()) {
			if ("json".equals(format)) {
				return ok(Json.toJson(filledForm.get()));
			} else {
				//return ok(run.render(filledForm, true));
				return ok(); //TODO must be complete
			}
		} else {
			if ("json".equals(format)) {
				return badRequest(filledForm.errorsAsJson());
				
			} else {
				//return badRequest(run.render(filledForm, true));
				return badRequest();
				
			}
		}		
	}

	public static Result list(){
		Form<DataTableForm> filledForm = datatableForm.bindFromRequest();
	
		List<Run> runs = MongoDBDAO.all(Constants.CNG_RUN_ILLUMINA, Run.class);
		ObjectNode result = Json.newObject();
		result.put("iTotalRecords", runs.size());
		result.put("iTotalDisplayRecords", runs.size());
		result.put("sEcho", filledForm.get().sEcho);
		result.put("aaData", Json.toJson(runs));
		
		return ok(Json.toJson(result));
	}
	
	public static Result show(String code,Integer laneNumber, String format){
		Run runValue = MongoDBDAO.findByCode(Constants.CNG_RUN_ILLUMINA, Run.class, code);
		Lane laneValue = null;
		for(Lane lane:runValue.lanes) {
			if(lane.number.equals(laneNumber)){
				laneValue = lane;
			}
		}
		
		if(laneValue != null){
			if("json".equals(format)){
				return ok(Json.toJson(laneValue));
			}else{
				Form<Lane> filledForm = laneForm.fill(laneValue);				
				//return ok(run.render(filledForm, Boolean.FALSE));
				return ok(); //TODO must be complete
			}			
		}else{
			return notFound();
		}		
	}
	
		//necessite une double gestion avec et sans json pour pouvoir faire fonctionner les 2 ensemble
		//ceci est du à la gestion des Map qui est différente entre json et spring binder			
		private static Form<Lane> getFilledForm(String format) {
			Form<Lane> filledForm;
			if("json".equals(format)){
				JsonNode json = request().body().asJson();			
				Lane laneInput = Json.fromJson(json, Lane.class);
				filledForm = laneForm.fill(laneInput);	//bindJson ne marche pas !			
			}else{
				filledForm = laneForm.bindFromRequest();
			}
			return filledForm;
		}
}
