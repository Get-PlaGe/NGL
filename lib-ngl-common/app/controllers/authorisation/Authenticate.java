package controllers.authorisation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import play.mvc.With;

/**
 * Exemple:
 *	 @Permission(value={"54ki2"},teams={"THETEAM"})
 * 	
 * 
 * 	@author ydeshayes
 */

@With({fr.cea.ig.authentication.Authenticate.class, PermissionAction.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
public @interface Authenticate {
	String[] value() default "";//name/value permission
	String[] teams() default "";//the teams
	String  allPermissions() default "false";//need to have all the permission(true) or just one(false) 
}