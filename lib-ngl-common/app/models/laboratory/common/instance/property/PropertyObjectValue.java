package models.laboratory.common.instance.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import validation.ContextValidation;
import validation.utils.ValidationHelper;


/**
 * used to stock a complex object
 * an object is stock in Map with key = property and value = value of property
 */
public class PropertyObjectValue extends PropertyValue<Map<String,?>>{
	
	public PropertyObjectValue() {
		super(PropertyValue.objectType);		
	}
	
	public PropertyObjectValue(Map<String, ?> value) {
		super(PropertyValue.objectType, value);		
	}
	
	
	public PropertyObjectValue(Map<String, ?> value, Map<String,String> unit) {
		super(PropertyValue.objectType, value);
		this.unit = unit;
	}
	
	public Map<String,String> unit;
	@Override
	public String toString() {
		return "PropertyObjectValue [value=" + value + ", unit=" + unit + ", class="+value.getClass().getName()+"]";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
		Iterator<PropertyDefinition> propertyDefinitions = ((Collection<PropertyDefinition>)contextValidation.getObject("propertyDefinitions")).iterator();
		while(propertyDefinitions.hasNext()){
			PropertyDefinition propertyDefinition = propertyDefinitions.next();
			if(ValidationHelper.checkIfActive(contextValidation, propertyDefinition)){
				if(ValidationHelper.required(contextValidation, this, propertyDefinition)){				
					if(ValidationHelper.convertPropertyValue(contextValidation, this, propertyDefinition)){
						ValidationHelper.checkIfExistInTheList(contextValidation, this, propertyDefinition);
						//TODO FORMAT AND UNIT
					}
				}
			}
		}
	}

}
