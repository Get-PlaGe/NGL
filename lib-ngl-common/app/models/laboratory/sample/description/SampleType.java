package models.laboratory.sample.description;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.sample.description.dao.SampleTypeDAO;

public class SampleType extends CommonInfoType{

	public SampleCategory sampleCategory;

	public static Finder<SampleType> find = new Finder<SampleType>(SampleTypeDAO.class.getName());
	
	public SampleType() {
		super.classNameDAO=SampleTypeDAO.class.getName();
	}
	
	

}
