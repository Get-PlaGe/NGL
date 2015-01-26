package services.instance.protocol;

import static services.instance.InstanceFactory.newProtocol;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import play.Logger;
import services.instance.InstanceFactory;
import validation.ContextValidation;

public class ProtocolServiceCNS {	

	private final static String institute = "CNS";

	public static void main(ContextValidation ctx) {	

		Logger.info("Start to create protocols collection for "+institute+"...");
		Logger.info("Save protocols ...");
		saveProtocols(ctx);
		Logger.info(institute+" Protocols collection creation is done!");
	}
	
	
	public static void saveProtocols(ContextValidation ctx){		
		List<Protocol> lp = new ArrayList<Protocol>();
		
		lp.add(newProtocol("fragmentation_ptr_sox140_1","Fragmentation_ptr_sox140_1","path1","1","production", InstanceFactory.setExperimentTypeCodes("fragmentation")));
		lp.add(newProtocol("bqspri_ptr_sox142_1","BqSPRI_ptr_sox142_1","path2","1","production", InstanceFactory.setExperimentTypeCodes("librairie-indexing", "librairie-dualindexing")));
		lp.add(newProtocol("amplif_ptr_sox144_1","Amplif_ptr_sox144_1","path3","1","production", InstanceFactory.setExperimentTypeCodes("amplification", "solution-stock")));
		lp.add(newProtocol("depot_illumina_ptr_1","Depot_Illumina_prt_1","path4","1","production", InstanceFactory.setExperimentTypeCodes("illumina-depot")));
		lp.add(newProtocol("depot_illumina_ptr_2","Depot_Illumina_prt_2","path5","1","production", InstanceFactory.setExperimentTypeCodes("illumina-depot")));
		lp.add(newProtocol("depot_illumina_ptr_3","Depot_Illumina_prt_3","path6","1","production", InstanceFactory.setExperimentTypeCodes("illumina-depot")));
		lp.add(newProtocol("depot_opgen_ptr_1","Depot_Opgen_prt_1","path7","1","production", InstanceFactory.setExperimentTypeCodes("opgen-depot")));
		lp.add(newProtocol("prepfc_cbot_ptr_sox139_1","PrepFC_CBot_ptr_sox139_1","path7","1","production", InstanceFactory.setExperimentTypeCodes("prepa-flowcell")));
		lp.add(newProtocol("proto_qc_v1","Proto_QC_v1","path7","1","production", InstanceFactory.setExperimentTypeCodes("chip-migration-post-pcr", "chip-migration-pre-pcr", "fluo-quantification", "qpcr-quantification")));
	
		
		for(Protocol protocole:lp){
			InstanceHelpers.save(InstanceConstants.PROTOCOL_COLL_NAME, protocole,ctx);
			Logger.debug("");
		}
	}
	
	





}