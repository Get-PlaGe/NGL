package controllers.archives.tpl;

import play.Routes;
import play.mvc.Result;
import views.html.archives.home;
import views.html.archives.search;
import controllers.CommonController;
/**
 * Controller around archive readset object
 * @author galbini
 *
 */
public class ReadSets extends CommonController {
	
	public static Result home(String homecode) {
		return ok(home.render(homecode));
	}
	
	public static Result get(String code) {
		return ok(home.render(code));
	}
	
	public static Result search() {		
		return ok(search.render());
	}
	
	public static Result javascriptRoutes() {
  	    response().setContentType("text/javascript");
  	    return ok(  	    		
  	      Routes.javascriptRouter("jsRoutes",
  	        // Routes
  	    	controllers.archives.tpl.routes.javascript.ReadSets.home(),  
  	    	controllers.archives.tpl.routes.javascript.ReadSets.get(),  
  	    	controllers.archives.api.routes.javascript.ReadSets.list()  	    		
  	      )	  	      
  	    );
  	  }
	
}
