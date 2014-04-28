package lims.cns.services;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.list.TreeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import play.Logger;
import play.Logger.ALogger;

import lims.cns.dao.LimsAbandonDAO;
import lims.models.LotSeqValuation;
import lims.models.experiment.ContainerSupport;
import lims.models.experiment.Experiment;
import lims.models.instrument.Instrument;
import lims.models.runs.TacheHD;
import lims.services.ILimsRunServices;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;


@Service
public class LimsRunServices implements ILimsRunServices{

	@Autowired
	LimsAbandonDAO dao;

	ALogger logger = Logger.of("CNS");

	private Map<String, Integer> crmapping;

	/*
	 *
Conta:mat ori        					9	TAXO-contaMatOri
Qlte:duplicat>30    					42	Qlte-duplicat
Qlte:repartition bases       			41	Qlte-repartitionBases

Conta mat ori + duplicat>30				43	TAXO-contaMatOri ; Qlte-duplicat
Conta mat ori + rep bases				44	TAXO-contaMatOri ; Qlte-repartitionBases
Duplicat>30 + rep bases					45	Qlte-duplicat ; Qlte-repartitionBases
Conta mat ori + duplicat>30 + rep bases	46	TAXO-contaMatOri ; Qlte-duplicat ; Qlte-repartitionBases


	 */

	public LimsRunServices() {
		crmapping = new HashMap<String, Integer>();
		crmapping.put("TAXO-contaMatOri;", 9);
		crmapping.put("Qlte-duplicat;", 42);
		crmapping.put("Qlte-repartitionBases;", 41);
		crmapping.put("Qlte-duplicat;TAXO-contaMatOri;", 43);
		crmapping.put("Qlte-repartitionBases;TAXO-contaMatOri;", 44);
		crmapping.put("Qlte-duplicat;Qlte-repartitionBases;", 45);
		crmapping.put("Qlte-duplicat;Qlte-repartitionBases;TAXO-contaMatOri;", 46);
	}

	@Override
	public List<Instrument> getInstruments() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public Experiment getExperiments(Experiment experiment) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public ContainerSupport getContainerSupport(String supportCode) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public void valuationRun(Run run) {
		try{
			dao.updateRunAbandon(run.code, getAbandon(run.valuation, run.code), 47);
			for(Lane lane: run.lanes){
				dao.updatePisteAbandon(run.code, lane.number, getAbandon(lane.valuation, run.code), 47);
			}
		}catch(Throwable t){
			//TODO mail to prod ???
			logger.error(run.code+" : "+t.getMessage());
		}
	}
	@Override
	public void valuationReadSet(ReadSet readSet, boolean firstTime) {
		try{
			
			if(firstTime){
				List<TacheHD> taches = dao.listTacheHD(readSet.code);
				Integer tacheId = null;
				if(taches.size() > 1){
					Logger.warn("several tachehd "+readSet.code);
					logger.error(readSet.code+" : Plusieurs Tache");
					//TODO mail to prod ???
				}else if(taches.size() == 1){
					tacheId = taches.get(0).tacco;
				}else{
					Logger.warn("0 tachehd "+readSet.code);
				}
				dao.updateLotsequenceAbandon(readSet.code, getSeqVal(readSet.productionValuation, readSet.code), getCR(readSet.productionValuation), tacheId, 55);
				if(!TBoolean.UNSET.equals(readSet.bioinformaticValuation.valid)){
					dao.updateLotsequenceAbandonBI(readSet.code, getAbandon(readSet.bioinformaticValuation, readSet.code));
				}
			}else{
				LotSeqValuation lsv = dao.getLotsequenceValuation(readSet.code);
				dao.updateLotsequenceAbandon(readSet.code, getSeqVal(readSet.productionValuation, readSet.code), getCR(readSet.productionValuation), lsv.tacco, 55);
				if(!TBoolean.UNSET.equals(readSet.bioinformaticValuation.valid)){
					dao.updateLotsequenceAbandonBI(readSet.code, getAbandon(readSet.bioinformaticValuation, readSet.code));
				}
			}
		}catch(Throwable t){
			//TODO mail to prod ???
			logger.error(readSet.code+" : "+t.getMessage());
		}
	}


	private Integer getCR(Valuation valuation) {
		Set<String> resos = new TreeSet<String>();
		if(null != valuation.resolutionCodes){
			resos.addAll(valuation.resolutionCodes);
		}
		StringBuilder sb = new StringBuilder();
		for(String s : resos){
			sb.append(s+";");
		}
		Integer v = null;
		if(sb.length() > 0){
			v = crmapping.get(sb.toString());			
		}
		return (v != null) ? v : 47;		
	}

	private Integer getAbandon(Valuation valuation, String code) {
		if(TBoolean.FALSE.equals(valuation.valid)){
			return 1; //abandon=true
		}else if(TBoolean.TRUE.equals(valuation.valid)){
			return 0; //abandon = false;
		}else{
			throw new RuntimeException("Abandon : Mise à jour abandon run ou readset ("+code+") dans lims mais valuation à UNSET");
		}
	}

	private Integer getSeqVal(Valuation valuation, String code) {
		if(TBoolean.FALSE.equals(valuation.valid)){
			return 0; //a abandonner
		}else if(TBoolean.TRUE.equals(valuation.valid)){
			return 1; //valide;
		}else{
			return 2;
		}
	}

}
