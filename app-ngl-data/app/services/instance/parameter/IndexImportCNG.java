package services.instance.parameter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import play.Logger;
import models.LimsCNGDAO;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.parameter.Parameter;
import models.laboratory.parameter.index.Index;
import models.laboratory.parameter.index.IlluminaIndex;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import scala.concurrent.duration.FiniteDuration;
import services.instance.AbstractImportDataCNG;
import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;

/**
 * @author dnoisett
 * Import Indexes from CNG's LIMS to NGL, ( no update for index)
 * FDS remplacement de l'appel a Logger par logger
 */

public class IndexImportCNG extends AbstractImportDataCNG{

	public IndexImportCNG(FiniteDuration durationFromStart,
			FiniteDuration durationFromNextIteration) {
		super("IndexImportCNS",durationFromStart, durationFromNextIteration);
	}

	@Override
	public void runImport() throws SQLException, DAOException {
		importIndexIllumina(limsServices,contextError);  //01/03/2017 chgt de nom + remise des params....
		createIndexChromium(contextError); // 01/03/2017 ajout
	}

	//01/03/2017 chgt de nom + remise des params....
	public void importIndexIllumina(LimsCNGDAO limsServices,ContextValidation contextValidation) throws SQLException, DAOException{
		logger.info("start loading indexes");
		
		//-1- chargement depuis la base source Postgresql
		logger.info("1/3 loading from source database...");
		List<Index> indexes = limsServices.findIndexIlluminaToCreate(contextValidation) ;
		logger.info("found "+indexes.size() + " items");
		
		//-2a- trouver les samples concernés dans la base mongoDB et les supprimer
		logger.info("2/3 delete from dest database...");
		for (Index index:indexes) {
			if (MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Index.class, index.code)) {
				MongoDBDAO.deleteByCode(InstanceConstants.PARAMETER_COLL_NAME, Index.class, index.code);
			}
		}

		//-3- sauvegarde dans la base cible MongoDb
		logger.info("3/3 saving to dest database...");
		InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,indexes,contextError);
		
		logger.info("end loading indexes");
	}
	
	// FDS 01/03/2017 creation des index pour processus Chromium (mais utilisés au final en sequencage-illumina)
	// Plaque=> 96 index SI-GA-<ligne>-<col>
	public void createIndexChromium(ContextValidation contextValidation){
	
		for ( int row = 1; row <=8; row++){
			for(int col = 1 ; col <= 12 ; col++){
				Index index = getChromiumIndex(row,col);				
				if(!MongoDBDAO.checkObjectExistByCode(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, index.code)){
					logger.info("creation index : "+ index.code +" / "+ index.categoryCode);
					InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME,index,contextValidation);
				}
			}
		}
	}

	// FDS 16/03/2017 !!! si on remplace la sequence par qq chose (ici un nom) il faut que la longueur soit la meme
	// sinon lors du pooling, une regle drools de validation va generer une erreur
	//==> utiliser le format A01 et non A1 pour la position !!! seulement pour la sequence
	private static Index getChromiumIndex(int row, int col) {
		Index index = new IlluminaIndex();
		
		String code = "SI-GA-"+ (char)(64 + row);
		String seq=code;
		if (col < 10 ) { seq = seq +"0"; }
		code=code + col;
		seq=seq+ col;
		
		index.code = code;
		index.name = code;
		index.shortName = code;
		index.sequence = seq ;  //Voir plus tard: il y a 4 sequences pour les POOL-INDEX...Chromium
		index.categoryCode = "POOL-INDEX";
		index.supplierName = new HashMap<String,String>();
		index.supplierName.put("10x Genomics", code);
		index.traceInformation=new TraceInformation("ngl-data");
		
		return index;
	}
	
}
