package models.laboratory.common.instance.property;

import java.util.Collection;
import java.util.List;

import validation.ContextValidation;
import validation.utils.ValidationHelper;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;


/**
 *
 */
public class PropertyListValue extends PropertyValue<List<? extends Object>>{
	
	public String unit;
	
	public PropertyListValue() {
		super(PropertyValue.listType);
	}
	public PropertyListValue(List<? extends Object> value) {
		super(PropertyValue.listType, value);
	}
	public PropertyListValue(List<? extends Object> value, String unit) {
		super(PropertyValue.listType, value);
		this.unit = unit;
	}

	@Override
	public String toString() {
		return "PropertyListValue [value=" + value + ", unit=" + unit + ", class="+value.getClass().getName()+"]";
	}
	@Override
	public void validate(ContextValidation contextValidation) {
		super.validate(contextValidation);
		PropertyDefinition propertyDefinition = (PropertyDefinition) ((Collection<PropertyDefinition>)contextValidation.getObject("propertyDefinitions")).toArray()[0];
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
