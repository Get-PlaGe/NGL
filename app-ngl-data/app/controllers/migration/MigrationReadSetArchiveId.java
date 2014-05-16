package controllers.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import models.LimsCNSDAO;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.util.DataMappingCNS;
import models.util.Workflows;
import models.utils.InstanceConstants;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBUpdate;

import org.springframework.jdbc.core.RowMapper;

import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import services.instance.run.UpdateReadSetCNS;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class MigrationReadSetArchiveId  extends CommonController {

	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);


	public static Result migration(){

		ContextValidation contextError=new ContextValidation();
		
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,  
				DBQuery.and(DBQuery.is("dispatch", true), DBQuery.is("archiveId", null))).toList();
		
		Logger.info("nb ReadSet ="+readSets.size());
		
		for(ReadSet rs : readSets){
			ReadSet updateRS;
			try {
				updateRS = limsServices.findReadSetToUpdate(rs, contextError);
				//Logger.info("Update ReadSet ="+rs.getCode());
				if(updateRS.archiveDate != null && updateRS.archiveId != null){
					
					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class
							, DBQuery.is("code", rs.code)
							, DBUpdate.set("archiveDate",updateRS.archiveDate)
										.set("archiveId", updateRS.archiveId)
										.set("traceInformation.modifyDate", new Date())
										.set("traceInformation.modifyUser", "lims"));
															
				}else if(updateRS.archiveDate == null && updateRS.archiveId != null){
					Logger.error("Probleme archivage date null / id not null : "+rs.getCode());
				}else if(updateRS.archiveDate != null && updateRS.archiveId == null){
					Logger.error("Probleme archivage date not null / id null : "+rs.getCode());
				}
			} catch (Exception e) {
				Logger.error(e.getMessage());
			}
		}
		
		return ok("Update "+readSets.size()+" ReadSet");
	}



}

