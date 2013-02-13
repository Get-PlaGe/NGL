package models.laboratory.experiment.description.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.dao.CommonInfoTypeDAO;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.Protocol;
import models.laboratory.experiment.description.PurificationMethodType;
import models.laboratory.experiment.description.QualityControlType;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.description.dao.InstrumentUsedTypeDAO;
import models.utils.dao.DAOException;

import org.springframework.asm.Type;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import play.modules.spring.Spring;


@Repository
public class ExperimentTypeDAO extends AbstractExperimentDAO<ExperimentType>{

	public ExperimentTypeDAO() {
		super("experiment_type", ExperimentType.class,ExperimentTypeMappingQuery.class,
				"SELECT t.id, doPurification, mandatoryPurification, doQualityControl, mandatoryQualityControl, fk_experiment_category, fk_common_info_type "+
						"FROM experiment_type as t "+
				"JOIN common_info_type as c ON c.id=fk_common_info_type ");
	}

	public List<ExperimentType> findNextExperiments(long idExperimentType)
	{
		NextExperimentTypeMappingQuery nextExperimentTypeMappingQuery = new NextExperimentTypeMappingQuery(dataSource);
		return nextExperimentTypeMappingQuery.execute(idExperimentType);
	}

	public List<ExperimentType> findByProcessId(long id)
	{
		String sql = "SELECT et.id, doPurification, mandatoryPurification, doQualityControl, mandatoryQualityControl,fk_common_info_type, fk_experiment_category, fk_common_info_type "+
				"FROM experiment_type as et JOIN process_experiment_type as p ON p.fk_experiment_type=et.id "+
				"WHERE p.fk_process_type = ?";
		ExperimentTypeMappingQuery experimentTypeMappingQuery = new ExperimentTypeMappingQuery(dataSource, sql, new SqlParameter("p.fk_process_type", Type.LONG));
		return experimentTypeMappingQuery.execute(id);
	}
	public long add(ExperimentType experimentType) throws DAOException
	{
		//Add commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		experimentType.id = commonInfoTypeDAO.add(experimentType);

		//Check if category exist
		if(experimentType.experimentCategory!=null && experimentType.experimentCategory.id==null)
		{
			ExperimentCategoryDAO experimentCategoryDAO = Spring.getBeanOfType(ExperimentCategoryDAO.class);
			experimentType.experimentCategory.id = experimentCategoryDAO.add(experimentType.experimentCategory);
		}
		//Create experimentType 
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", experimentType.id);
		parameters.put("fk_common_info_type", experimentType.id);
		parameters.put("fk_experiment_category", experimentType.experimentCategory.id);
		parameters.put("doPurification", experimentType.doPurification);
		parameters.put("mandatoryPurification", experimentType.mandatoryPurification);
		parameters.put("doQualityControl", experimentType.doQualityControl);
		parameters.put("mandatoryQualityControl", experimentType.mandatoryQualityControl);
		jdbcInsert.execute(parameters);

		//Add list protocols
		ProtocolDAO protocolDAO = Spring.getBeanOfType(ProtocolDAO.class);
		protocolDAO.add(experimentType.protocols, experimentType.id);
		//Add list instruments
		InstrumentUsedTypeDAO instrumentUsedTypeDAO = Spring.getBeanOfType(InstrumentUsedTypeDAO.class);
		instrumentUsedTypeDAO.add(experimentType.instrumentUsedTypes, experimentType.id);
		//Add nextExperimentTypes list
		List<ExperimentType> nextExpTypes = experimentType.previousExperimentTypes;
		if(nextExpTypes!=null && nextExpTypes.size()>0){
			String sql = "INSERT INTO next_experiment_types(fk_experiment_type,fk_next_experiment_type) VALUES(?,?)";
			for(ExperimentType expType : nextExpTypes){
				jdbcTemplate.update(sql, experimentType.id, expType.id);
			}
		}

		//Add QualityControl
		List<QualityControlType> qualityControlTypes = experimentType.possibleQualityControlTypes;
		if(qualityControlTypes!=null && qualityControlTypes.size()>0){
			QualityControlTypeDAO qualityControlTypeDAO = Spring.getBeanOfType(QualityControlTypeDAO.class);
			String sql = "INSERT INTO experiment_quality_control(fk_quality_control_type,fk_experiment_type) VALUES(?,?)";
			for(QualityControlType qualityControlType : qualityControlTypes){
				if(qualityControlType.id==null)
					qualityControlType.id = qualityControlTypeDAO.add(qualityControlType);
				jdbcTemplate.update(sql, qualityControlType.id, experimentType.id);
			}
		}
		//Add Purification method
		List<PurificationMethodType> purificationMethodTypes = experimentType.possiblePurificationMethodTypes;
		if(purificationMethodTypes!=null && purificationMethodTypes.size()>0){
			PurificationMethodTypeDAO purificationMethodTypeDAO = Spring.getBeanOfType(PurificationMethodTypeDAO.class);
			String sql = "INSERT INTO experiment_purification_method(fk_purification_method_type,fk_experiment_type) VALUES(?,?)";
			for(PurificationMethodType purificationMethodType : purificationMethodTypes){
				if(purificationMethodType.id==null)
					purificationMethodType.id = purificationMethodTypeDAO.add(purificationMethodType);
				jdbcTemplate.update(sql, purificationMethodType.id, experimentType.id);
			}
		}
		return experimentType.id;
	}

	public void update(ExperimentType experimentType) throws DAOException
	{
		ExperimentType expTypeDB = findById(experimentType.id);

		//Update experimentType attributes 
		String sqlUpdate = "UPDATE experiment_type SET doPurification=?, mandatoryPurification=?, doQualityControl=?,  mandatoryQualityControl=? WHERE id=?";
		jdbcTemplate.update(sqlUpdate, experimentType.doPurification, experimentType.mandatoryPurification, experimentType.doQualityControl, experimentType.mandatoryQualityControl, experimentType.id);
		//Update commonInfoType
		CommonInfoTypeDAO commonInfoTypeDAO = Spring.getBeanOfType(CommonInfoTypeDAO.class);
		commonInfoTypeDAO.update(experimentType);

		//Update protocols list (add new)
		List<Protocol> protocols = experimentType.protocols;
		if(protocols!=null && protocols.size()>0){
			ProtocolDAO protocolDAO = Spring.getBeanOfType(ProtocolDAO.class);
			for(Protocol protocol : protocols){
				if(expTypeDB.protocols==null || (expTypeDB.protocols!=null && !expTypeDB.protocols.contains(protocol)))
					protocolDAO.add(protocol, experimentType.id);
			}
		}
		//Update InstrumentUsedTypes list
		List<InstrumentUsedType> instrumentUsedTypes = experimentType.instrumentUsedTypes;
		if(instrumentUsedTypes!=null && instrumentUsedTypes.size()>0){
			InstrumentUsedTypeDAO instrumentUsedTypeDAO = Spring.getBeanOfType(InstrumentUsedTypeDAO.class);
			for(InstrumentUsedType instrumentUsedType : instrumentUsedTypes){
				if(expTypeDB.instrumentUsedTypes==null || (expTypeDB.instrumentUsedTypes!=null && !expTypeDB.instrumentUsedTypes.contains(instrumentUsedType))){
					instrumentUsedTypeDAO.add(instrumentUsedType, experimentType.id);
				}
			}
		}

		//Update experimentCategory
		if(expTypeDB.experimentCategory.id!=experimentType.experimentCategory.id){
			sqlUpdate = "UPDATE experiment_type SET fk_experiment_category=? WHERE id=?";
			jdbcTemplate.update(sqlUpdate, experimentType.experimentCategory.id, experimentType.id);
		}
		//Update nexExperiment list (add new)
		List<ExperimentType> nextExpTypes = experimentType.previousExperimentTypes;
		System.out.println("Next experiment db "+expTypeDB.previousExperimentTypes);
		for(ExperimentType nextExpTypeDB : expTypeDB.previousExperimentTypes){
			System.out.println(nextExpTypeDB.id);
		}
		if(nextExpTypes!=null && nextExpTypes.size()>0){
			String sql = "INSERT INTO next_experiment_types(fk_experiment_type,fk_next_experiment_type) VALUES(?,?)";
			for(ExperimentType expType : nextExpTypes){
				if(expTypeDB.previousExperimentTypes==null || (expTypeDB.previousExperimentTypes!=null && !expTypeDB.previousExperimentTypes.contains(expType))){
					play.Logger.debug("Add next experiment types "+expType.id);
					jdbcTemplate.update(sql, experimentType.id, expType.id);
				}
			}
		}

		//Update QualityControl List
		List<QualityControlType> qualityControlTypes = experimentType.possibleQualityControlTypes;
		if(qualityControlTypes!=null && qualityControlTypes.size()>0){
			QualityControlTypeDAO qualityControlTypeDAO = Spring.getBeanOfType(QualityControlTypeDAO.class);
			String sql = "INSERT INTO experiment_quality_control(fk_quality_control_type,fk_experiment_type) VALUES(?,?)";
			for(QualityControlType qualityControlType : qualityControlTypes){
				if(expTypeDB.possibleQualityControlTypes==null || (expTypeDB.possibleQualityControlTypes!=null && !expTypeDB.possibleQualityControlTypes.contains(qualityControlType))){
					if(qualityControlType.id==null)
						qualityControlType.id=qualityControlTypeDAO.add(qualityControlType);
					jdbcTemplate.update(sql, qualityControlType.id,experimentType.id);
				}
			}
		}
		//Update Purification Method List
		List<PurificationMethodType> purificationMethodTypes = experimentType.possiblePurificationMethodTypes;
		if(purificationMethodTypes!=null && purificationMethodTypes.size()>0){
			PurificationMethodTypeDAO purificationMethodTypeDAO = Spring.getBeanOfType(PurificationMethodTypeDAO.class);
			String sql = "INSERT INTO experiment_purification_method(fk_purification_method_type,fk_experiment_type) VALUES(?,?)";
			for(PurificationMethodType purificationMethodType : purificationMethodTypes){
				if(expTypeDB.possiblePurificationMethodTypes==null || (expTypeDB.possiblePurificationMethodTypes!=null && !expTypeDB.possiblePurificationMethodTypes.contains(purificationMethodType))){
					if(purificationMethodType.id==null)
						purificationMethodType.id=purificationMethodTypeDAO.add(purificationMethodType);
					jdbcTemplate.update(sql, purificationMethodType.id,experimentType.id);
				}
			}
		}
	}

	@Override
	public void remove(ExperimentType experimentType) {
		super.remove(experimentType);
		//Remove next experiment next_experiment_types
		String sqlNextExp = "DELETE FROM next_experiment_types WHERE fk_experiment_type=?";
		jdbcTemplate.update(sqlNextExp, experimentType.id);
		//Remove quality control experiment_quality_control
		String sqlQuality = "DELETE FROM experiment_quality_control WHERE fk_experiment_type=?";
		jdbcTemplate.update(sqlQuality, experimentType.id);
		//Remove purification experiment_purification_method
		String sqlPurif = "DELETE FROM experiment_purification_method WHERE fk_experiment_type=?";
		jdbcTemplate.update(sqlPurif, experimentType.id);
	}




}
