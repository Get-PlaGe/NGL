package models.sra.experiment.instance;

import java.util.Date;

import validation.ContextValidation;
import validation.IValidation;
import fr.cea.ig.DBObject;

public class Run extends DBObject implements IValidation {
	// RunType
	//public String alias;         // required mais remplacé par code herité de DBObject, et valeur = projectCode_num
	public String projectCode;     // required pour nos stats                   varchar(10), 
	public Date runDate;           
	public String runCenter;       // required pour nos stats valeur fixee à GSC 
	public String accession;       // numeros d'accession attribué par ebi 
	public Date releaseDate;       // required, date de mise à disposition en public par l'EBI
		 
	@Override
	public void validate(ContextValidation contextValidation) {
		// TODO Auto-generated method stub	
	}

}
