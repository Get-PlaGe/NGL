package models.laboratory.container.instance;

import org.codehaus.jackson.annotate.JsonIgnore;

import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.ObjectMongoDBReference;
import models.utils.ObjectSGBDReference;

public class SampleUsed {
	// Reference Sample code
	public String sampleCode;
	// Reference SampleType code
	public String typeCode;
	// Reference SampleCategory code
	public String categoryCode;
	
	public SampleUsed(){
		
		
	}

	public SampleUsed(String sampleCode,String typeCode,String categoryCode){
		this.sampleCode=sampleCode;
		this.typeCode=typeCode;
		this.categoryCode=categoryCode;
		
	}

	@JsonIgnore
	public Sample getSample(){

		try {
			return new ObjectMongoDBReference<Sample>(Sample.class,sampleCode).getObject();
		} catch (Exception e) {
			//TODO
		}
		return null;

	}
	
	@JsonIgnore
	public SampleType getSampleType(){

		try {
			//return new ObjectSGBDReference<SampleType>(SampleType.class,typeCode).getObject();
		} catch (Exception e) {
			//TODO
		}
		return null;

	}
	
	@JsonIgnore
	public SampleCategory getSampleCategory(){

		try {
			//return new ObjectSGBDReference<SampleCategory>(SampleCategory.class,categoryCode).getObject();
		} catch (Exception e) {
			// TODO
		}
		return null;
	}

}
