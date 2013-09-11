package validation.utils;

import static validation.utils.ValidationHelper.addErrors;
import static validation.utils.ValidationHelper.required;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.utils.Model.Finder;
import models.utils.dao.DAOException;
import play.data.validation.ValidationError;
import validation.ContextValidation;
import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;

/**
 * Helper to validate MongoDB Object Used before insert or update a MongoDB
 * object
 * 
 * @author galbini
 * 
 */
public class BusinessValidationHelper {
	private static final String ERROR_NOTUNIQUE = "error.codenotunique";
	private static final String ERROR_NOTEXIST = "error.codenotexist";
	public static final String FIELD_TRACE_INFORMATION = "traceInformation";
	public static final String FIELD_CODE = "code";
	public static final String FIELD_TYPE_CODE = "typeCode";
	public static final String FIELD_SUPPORT_CODE = "containerSupportCode";


	/**
	 * Validate if code is unique in MongoDB collection
	 * Unique code is validate if key "_id" not in map contextObjects or if value of key "_id" is null else no code validation
	 * @param contextValidatin
	 * @param code
	 * @param type
	 * @return collctionName
	 */

	public static <T extends DBObject> boolean validateUniqueInstanceCode(ContextValidation contextValidation,
			String code, Class<T> type, String collectionName){
	
		// Unique code is validate is key "_id" not in map contextObjects or if value of key "_id" is null
		if(contextValidation.getObject("_id") == null){
			if(null!=code && MongoDBDAO.checkObjectExistByCode(collectionName, type, code)){
				contextValidation.addErrors( FIELD_CODE, ERROR_NOTUNIQUE, code);
				//contextValidation.removeKeyFromRootKeyName(FIELD_CODE);
				return false;
			}else if (code!=null){
				return false;
			}	

		}
		
		return true;
	}
	
	
	/**
	 * Validate if field value is unique in MongoDB collection
	 * @param errors
	 * @param key : field name
	 * @param keyValue : field value
	 * @param type : type DBObject
	 * @param collectionName : Mongo collection name
	 * @param returnObject
	 * @return boolean
	 */
	
	public static <T extends DBObject> boolean validateUniqueFieldValue(ContextValidation contextValidation,
			String key, String keyValue, Class<T> type, String collectionName){
		
		if(null!=keyValue && MongoDBDAO.checkObjectExist(collectionName, type, key, keyValue)){
			contextValidation.addErrors(key, ValidationConstants.ERROR_NOTUNIQUE_MSG,keyValue);
			return false;
		}else if (keyValue!=null){
			return false;
		}	
		
		return true;
	}
	
	
	public static <T> void validateRequiredDescriptionCode(ContextValidation contextValidation, String code, String key,
			Finder<T> find) {
		 validateRequiredDescriptionCode(contextValidation, code, key, find,false);
	}

	/**
	 * Validate i a description code is not null and exist in description DB
	 * @param errors
	 * @param code
	 * @param key
	 * @param find
	 * @param returnObject
	 * @return object de T or null if returnObject is false
	 */
	public static <T> T validateRequiredDescriptionCode(ContextValidation contextValidation, String code, String key,
			Finder<T> find, boolean returnObject) {
		T o = null;
		if(required(contextValidation, code, key)){
			o = validateExistDescriptionCode(contextValidation, code, key, find, returnObject);
		}
		return o;		
	}


	/***
	 * Validate if a code in a description table exist
	 * @param errors
	 * @param code
	 * @param key
	 * @param find
	 * @param returnObject
	 * @return void
	 */
	public static <T> void validateExistDescriptionCode(
			ContextValidation contextValidation, String code, String key,
			Finder<T> find) {
		 validateExistDescriptionCode(contextValidation, code, key, find, false);
	}

	/***
	 * Validate if a code in a description table exist
	 * @param errors
	 * @param code
	 * @param key
	 * @param find
	 * @param returnObject
	 * @return object de T or null if returnObject is false
	 */
	public static <T> T validateExistDescriptionCode(
			ContextValidation contextValidation, String code, String key,
			Finder<T> find, boolean returnObject) {
		T o = null;
		try {
			if(code != "" && null != code && returnObject){
				o = find.findByCode(code);
				if(o == null){

					contextValidation.addErrors(key, ERROR_NOTEXIST, code);
				}
			}else if(code != "" && null != code ){
				if( !find.isCodeExist(code))
				contextValidation.addErrors(key, ERROR_NOTEXIST, code);
			}
		} catch (DAOException e) {
			throw new RuntimeException(e);
		}
		return o;
	}

	public static <T extends DBObject> void validateRequiredInstanceCode(ContextValidation contextValidation,
			String code, String key, Class<T> type, String collectionName) {
		if(required(contextValidation, code, key)){
			validateExistInstanceCode(contextValidation, code, key, type,collectionName);
		}
	}

	/**
	 * Validate if code is not null and exist
	 * @param errors
	 * @param code
	 * @param key
	 * @param type
	 * @param collectionName
	 * @param returnObject
	 * @return
	 */
	public static <T extends DBObject> T validateRequiredInstanceCode(ContextValidation contextValidation,
			String code, String key, Class<T> type, String collectionName, boolean returnObject) {
		T o = null;
		if(required(contextValidation, code, key)){
			o = validateExistInstanceCode(contextValidation, code, key, type,collectionName, returnObject);
		}
		return o;	
	}


	/**
	 * Validate if list is not null and code exist
	 * @param errors
	 * @param code
	 * @param key
	 * @param type
	 * @param collectionName
	 * @param returnObject
	 * @return
	 */
	public static <T extends DBObject> List<T> validateRequiredInstanceCodes(ContextValidation contextValidation,
			List<String> codes, String key, Class<T> type, String collectionName, boolean returnObject) {

		List<T> l = null;
		
		if(required(contextValidation, codes, key)){
			l=validateExistInstanceCodes(contextValidation, codes, key, type, collectionName, returnObject);
		}
		return l;
		
	}
	
	
	
	/**
	 * Validate a code of a MongoDB Collection
	 * @param errors
	 * @param code
	 * @param key
	 * @param type
	 * @param collectionName
	 * @param returnObject
	 * @return
	 */
	public static <T extends DBObject> List<T> validateExistInstanceCodes(ContextValidation contextValidation,
			List<String> codes, String key, Class<T> type, String collectionName, boolean returnObject) {
		List<T> l = null;
		if(null != codes && codes.size() > 0){
			l = (returnObject)?new ArrayList<T>():null;

			for(String code: codes){
				T o =validateExistInstanceCode(contextValidation, code, key, type, collectionName, returnObject) ;
				if(returnObject){
					l.add(o);
				}
			}			
		}
		return l;
	}

	
	
	public static <T extends DBObject> void validateExistInstanceCode(ContextValidation contextValidation,
			String code, String key, Class<T> type, String collectionName) {
		validateExistInstanceCode(contextValidation, code, key, type, collectionName, false);
	}
	/**
	 * Validate a code of a MongoDB Collection
	 * @param errors
	 * @param code
	 * @param key
	 * @param type
	 * @param collectionName
	 * @param returnObject
	 * @return
	 */
	public static <T extends DBObject> T validateExistInstanceCode(ContextValidation contextValidation,
			String code, String key, Class<T> type, String collectionName, boolean returnObject) {
		T o = null;

		if(null != code && returnObject){
			o =  MongoDBDAO.findByCode(collectionName, type, code);
			if(o == null){
				contextValidation.addErrors(key, ERROR_NOTEXIST, code);
			}
		}else if(null != code && !MongoDBDAO.checkObjectExistByCode(collectionName, type, code)){
			contextValidation.addErrors( key, ERROR_NOTEXIST, code);
		}

		return o;
	}	
}
