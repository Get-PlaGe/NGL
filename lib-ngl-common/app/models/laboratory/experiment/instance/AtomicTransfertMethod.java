package models.laboratory.experiment.instance;

import java.util.List;

import models.laboratory.common.instance.Comment;
import models.laboratory.container.description.ContainerSupportCategory;
import validation.ContextValidation;
import validation.IValidation;
import validation.experiment.instance.AtomicTransfertMethodValidationHelper;
import validation.utils.ValidationHelper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="class", defaultImpl= models.laboratory.experiment.instance.OneToOneContainer.class)
@JsonSubTypes({
	@JsonSubTypes.Type(value =  models.laboratory.experiment.instance.ManyToOneContainer.class, name = "ManyToOne"),
	@JsonSubTypes.Type(value =  models.laboratory.experiment.instance.OneToManyContainer.class, name = "OneToMany"),
	@JsonSubTypes.Type(value =  models.laboratory.experiment.instance.OneToOneContainer.class, name = "OneToOne"),
	@JsonSubTypes.Type(value =  models.laboratory.experiment.instance.OneToVoidContainer.class, name = "OneToVoid")
})
public abstract class AtomicTransfertMethod implements IValidation {

	public List<InputContainerUsed> inputContainerUseds;
	public List<OutputContainerUsed> outputContainerUseds;
	public String line;
	public String column;
	public Comment comment;
	
	public AtomicTransfertMethod() {
		super();
	}
	
	public abstract void updateOutputCodeIfNeeded(ContainerSupportCategory outputCsc, String supportCode);
	
	
	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.required(contextValidation, line, "line");
		ValidationHelper.required(contextValidation, column, "column");
		AtomicTransfertMethodValidationHelper.validateInputContainers(contextValidation, inputContainerUseds);
		
	}

	
	
	
}