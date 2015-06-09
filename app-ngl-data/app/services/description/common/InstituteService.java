package services.description.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import static services.description.DescriptionFactory.*;

public class InstituteService {
	public static void main(Map<String, List<ValidationError>> errors) throws DAOException{		
		
		//DAOHelpers.removeAll(Institute.class, Institute.find);

		saveInstitutes(errors);	
	}
		

	
	public static void saveInstitutes(Map<String,List<ValidationError>> errors) throws DAOException{
		List<Institute> l = new ArrayList<Institute>();
				
		l.add(newInstitute("Centre National de Génomique","CNG"));
		l.add(newInstitute("Centre National de Séquençage","CNS"));
		
		DAOHelpers.saveModels(Institute.class, l, errors);
	}

}
