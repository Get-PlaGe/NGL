package models.laboratory.common.description;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.ObjectType.CODE;
import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.common.description.dao.StateDAO;
import models.utils.ListObject;
import models.utils.Model;
import models.utils.Model.Finder;
import models.utils.dao.AbstractDAOCommonInfoType;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

/**
 * Class attributes common types
 * Represented by a table in the database with its own id
 * The subclasses are represented by tables in the database with the same id as the parent class
 * Relations with the protocols and instruments are accessible by the common_info_type table for the experiment subclasses (experimentType, qualityCcontrolType ...)
 * @author ejacoby
 *
 */
public class CommonInfoType extends Model<CommonInfoType>{

	public String name; //used as label

	public List<State> states = new ArrayList<State>();
	public List<Resolution> resolutions = new ArrayList<Resolution>();

	public List<PropertyDefinition> propertiesDefinitions=new ArrayList<PropertyDefinition>();

	public ObjectType objectType;
	
	public List<Institute> institutes = new ArrayList<Institute>();
	
	public List<ValidationCriteria> criterias = new ArrayList<ValidationCriteria>();

	public static CommonInfoTypeFinder find = new CommonInfoTypeFinder(); 

	public CommonInfoType() {
		super(CommonInfoTypeDAO.class.getName());
	}

	protected CommonInfoType(String classNameDAO){
		super(classNameDAO);
	}

	public Map<String, PropertyDefinition> getMapPropertyDefinition()
	{
		Map<String, PropertyDefinition> mapProperties = new HashMap<String, PropertyDefinition>();
		for(PropertyDefinition propertyDefinition : propertiesDefinitions){
			mapProperties.put(propertyDefinition.code, propertyDefinition);
		}
		return mapProperties;
	}

	public void setCommonInfoType(CommonInfoType commonInfoType)
	{
		this.id=commonInfoType.id;
		this.name=commonInfoType.name;
		this.code=commonInfoType.code;
		this.states=commonInfoType.states;
		this.resolutions=commonInfoType.resolutions;
		this.propertiesDefinitions=commonInfoType.propertiesDefinitions;
		this.objectType=commonInfoType.objectType;
		
		this.institutes=commonInfoType.institutes;
		this.criterias=commonInfoType.criterias;
	}

	public List<PropertyDefinition> getPropertyDefinitionByLevel(Level.CODE...levels){

		List<PropertyDefinition> proDefinitions=new ArrayList<PropertyDefinition>();

		for(PropertyDefinition propertyDefinition:this.propertiesDefinitions){
			boolean containsAll = true;
			for(int i=0;i<levels.length;i++){
				Level level = new Level(levels[i]);
				if(!propertyDefinition.levels.contains(level)){
					containsAll = false;
					break;
				}						
			}
			if(containsAll){
				proDefinitions.add(propertyDefinition);
			}
		}	
		
		return proDefinitions;
	}
	
	public static class CommonInfoTypeFinder extends Finder<CommonInfoType>{ 

		public CommonInfoTypeFinder() {
			super(CommonInfoTypeDAO.class.getName());
		}

		public List<CommonInfoType> findByObjectTypeCode(CODE objectTypeCode) throws DAOException {
			return ((CommonInfoTypeDAO)getInstance()).findByObjectTypeCode(objectTypeCode);
		}
		
	}

	public static class AbstractCommonInfoTypeFinder<T> extends Finder<T>{ 
	
		public AbstractCommonInfoTypeFinder(Class<? extends AbstractDAOCommonInfoType> type) {
			super(type.getName());
		}
	
		public List<ListObject> findAllForList() throws DAOException{
			return ((AbstractDAOCommonInfoType) getInstance()).findAllForList();
		}
		
	}

}
