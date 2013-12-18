package models.laboratory.experiment.description;

import java.util.List;

import models.laboratory.common.description.State;
import models.laboratory.common.description.dao.StateDAO;
import models.laboratory.experiment.description.dao.ProtocolDAO;
import models.laboratory.reagent.description.ReagentType;
import models.utils.Model;
import models.utils.Model.Finder;
import models.utils.dao.DAOException;
import play.api.modules.spring.Spring;

public class Protocol extends Model<Protocol>{


	public String name;
	public String filePath;
	public String version;
	
	public ProtocolCategory category;
	
	public List<ReagentType> reagentTypes;

	public static ProtocolFinder find = new ProtocolFinder(); 
	
	public Protocol() {
		super(ProtocolDAO.class.getName());
	}
	
	
	public static class ProtocolFinder extends Finder<Protocol> {

		public ProtocolFinder() {
			super(ProtocolDAO.class.getName());
		}
		
		public List<Protocol> findByExperimentTypeCode(String code) throws DAOException
		{
			return ((ProtocolDAO)getInstance()).findByExperimentTypeCode(code);
		}
		
	}
}
