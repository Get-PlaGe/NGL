package models.laboratory.sample.description;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.sample.description.dao.ImportTypeDAO;

/**
 * Additional information collaborator
 * @author ejacoby
 *
 */
public class ImportType extends CommonInfoType{

	public ImportCategory importCategory;
	
	public static Finder<ImportType> find = new Finder<ImportType>(ImportTypeDAO.class.getName());
	
	public ImportType()
	{
		super.classNameDAO=ImportTypeDAO.class.getName();
	}
}
