package models.laboratory.common.instance.property;

import java.util.Collection;

import validation.ContextValidation;
import validation.utils.ValidationHelper;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;


/**
 * Object used in Map of key/value to stored value with its unit
 * if unit is null, it will not stored in MongoDB
 * 
 * @author mhaquell
 *
 */
public class PropertySingleValue extends PropertyValue<Object>{
	
	public PropertySingleValue() {
		super();
	}
	public PropertySingleValue(Object value) {
		super(value);	
	}
	public PropertySingleValue(Object value, String unit) {
		super(value);
		this.unit = unit;
	}
	
	public String unit;
	@Override
	public String toString() {
		return "PropertySingleValue[value=" + value + ", unit=" + unit +  ", class="+value.getClass().getName()+"]";
	}
	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
		PropertyDefinition propertyDefinition = (PropertyDefinition) ((Collection<PropertyDefinition>)contextValidation.getObject("propertyDefinitions")).toArray()[0];
		if(ValidationHelper.checkIfActive(contextValidation, propertyDefinition)){
			if(ValidationHelper.required(contextValidation, this, propertyDefinition)){				
				if(ValidationHelper.checkIfExistInTheList(contextValidation, this, propertyDefinition)){
					ValidationHelper.convertPropertyValue(contextValidation, this, propertyDefinition);
					//TODO FORMAT AND UNIT
				}
			}
		}
	}
	

}
