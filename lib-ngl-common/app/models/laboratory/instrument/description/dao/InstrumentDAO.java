package models.laboratory.instrument.description.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.utils.dao.AbstractDAODefault;
import models.utils.dao.DAOException;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import play.Logger;

@Repository
public class InstrumentDAO extends AbstractDAODefault<Instrument>{

	protected InstrumentDAO() {
		super("instrument", Instrument.class, true);
	}

	public List<Instrument> findByInstrumentUsedType(long idInstrumentUsedType) {
		String sql = "SELECT id,name,code FROM instrument WHERE fk_instrument_used_type=?";
		BeanPropertyRowMapper<Instrument> mapper = new BeanPropertyRowMapper<Instrument>(Instrument.class);
		return this.jdbcTemplate.query(sql, mapper, idInstrumentUsedType);
	}
	
	public List<Instrument> findByInstrumentUsedTypeCode(String instrumentUsedTypeCode) {
		String sql = "SELECT i.id,i.name,i.code FROM instrument i, instrument_used_type iut, common_info_type cit WHERE i.fk_instrument_used_type=iut.id AND iut.fk_common_info_type =cit.id AND cit.code=?";
		BeanPropertyRowMapper<Instrument> mapper = new BeanPropertyRowMapper<Instrument>(Instrument.class);
		return this.jdbcTemplate.query(sql, mapper, instrumentUsedTypeCode);
	}

	
	public Instrument save(Instrument instrument, long idInstrumentUsedType) throws DAOException {
		Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", instrument.name);
        parameters.put("code", instrument.code);
        parameters.put("fk_instrument_used_type", idInstrumentUsedType);
        parameters.put("active", instrument.active);
        parameters.put("path", instrument.path);
        Long newId = (Long) jdbcInsert.executeAndReturnKey(parameters);
        instrument.id = newId;
        
        insertInstitutes(instrument.institutes, instrument.id, false);
        
        return instrument;
	}
	
	
	private void insertInstitutes(List<Institute> institutes, Long instrumentId, boolean deleteBefore) throws DAOException {
		if(deleteBefore){
			removeInstitutes(instrumentId);
		}
		//Add institutes list		
		if(institutes!=null && institutes.size()>0){
			String sql = "INSERT INTO instrument_institute (fk_instrument, fk_institute) VALUES (?,?)";
			for(Institute institute : institutes){
				if(institute == null || institute.id == null ){
					throw new DAOException("institute is mandatory");
				}
				jdbcTemplate.update(sql, instrumentId, institute.id);
			}
		}
	}
	
	private void removeInstitutes( long instrumentId) {
		String sql = "DELETE FROM instrument_institute WHERE fk_instrument=?";
		jdbcTemplate.update(sql, instrumentId);
	}
	
	
	public void remove(Instrument instrument) throws DAOException {
		if(null == instrument){
			throw new IllegalArgumentException("instrument is null");
		}
		
		//remove institute
		removeInstitutes(instrument.id);

		super.remove(instrument);
	}
	
}
