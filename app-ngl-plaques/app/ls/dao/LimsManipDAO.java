package ls.dao;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import ls.models.Manip;
import ls.models.Plate;
import ls.models.Well;
import models.utils.ListObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import play.Logger;


@Repository
public class LimsManipDAO {
        private JdbcTemplate jdbcTemplate;
   

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);              
    }


    public List<Manip> findManips(Integer emnco, Integer ematerielco,String prsco){
    	Logger.info("pl_MaterielmanipChoisi @prsco='"+prsco+"', @emnco="+emnco+", @ematerielco="+ematerielco+", @plaque=1 ");
        List<Manip> results = this.jdbcTemplate.query("pl_MaterielmanipChoisi @prsco=?, @emnco=?, @ematerielco=?, @plaque=?", 
        		new Object[]{prsco, emnco, ematerielco, 1},new BeanPropertyRowMapper<Manip>(Manip.class));        
        return results;
    }
    
    public void createPlate(Plate plate){
    	Logger.info("pc_PlaqueSolexa @plaqueId="+plate.code+", @emnco="+plate.typeCode);
    	this.jdbcTemplate.update("pc_PlaqueSolexa @plaqueId=?, @emnco=?", new Object[]{plate.code, plate.typeCode});    	
    }
       
    public void updatePlate(Plate plate){
    	Logger.info("ps_MaterielmanipPlaque @plaqueId="+plate.code);
    	this.jdbcTemplate.update("ps_MaterielmanipPlaque @plaqueId=?", new Object[]{plate.code});
    	for(Well well: plate.wells){
    		Logger.info("pm_MaterielmanipPlaque @matmaco="+well.code+", @plaqueId="+plate.code+", @plaqueX="+well.x+", @plaqueY="+well.y+"");
    		this.jdbcTemplate.update("pm_MaterielmanipPlaque @matmaco=?, @plaqueId=?, @plaqueX=?, @plaqueY=?", well.code, plate.code, well.x, well.y);
    	}
    }

    public List<Plate> findPlates(Integer emnco, String projetValue) {
    	Logger.info("pl_PlaqueSolexa @prsco="+projetValue+", @emnco="+emnco);
		List<Plate> plates = this.jdbcTemplate.query("pl_PlaqueSolexa @prsco=?, @emnco=?", new Object[]{projetValue, emnco}, new RowMapper<Plate>() {
	        public Plate mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	Plate plate = new Plate();
	        	//well.plateCode = rs.getString("plaqueId");
	        	plate.code = rs.getString("plaqueId");
	        	plate.typeCode = rs.getInt("emnco");
	        	plate.typeName = rs.getString("emnnom");
	        	plate.nbWells = rs.getInt("nombrePuitUtilises");	        	
	            return plate;
	        }
	    });
		return plates;
	}

	/**
	 * Return a plate with coordinate
	 * @param code
	 * @return
	 */
	public Plate getPlate(String code) {
		Logger.info("pl_PlaqueSolexa @plaqueId="+code);
		List<Plate> plates = this.jdbcTemplate.query("pl_PlaqueSolexa @plaqueId=?", new Object[]{code}, new RowMapper<Plate>() {
	        public Plate mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	Plate plate = new Plate();
	        	plate.code = rs.getString("plaqueId");
	        	plate.typeCode = rs.getInt("emnco");
	        	plate.typeName = rs.getString("emnnom");
	        	plate.nbWells = rs.getInt("nombrePuitUtilises");		        	
	            return plate;
	        }
	    });
		
		
		if(plates.size() == 1){
			Plate plate = plates.get(0);
			Logger.info("pl_MaterielmanipPlaque @plaqueId="+plate.code);
			List<Well> wells = this.jdbcTemplate.query("pl_MaterielmanipPlaque @plaqueId=?", new Object[]{code}, new RowMapper<Well>() {
		        public Well mapRow(ResultSet rs, int rowNum) throws SQLException {
		        	Well well = new Well();
		        	well.name = rs.getString("matmanom");
		        	well.code = rs.getInt("matmaco");
		        	well.x = rs.getString("plaqueX");
		        	well.y = rs.getString("plaqueY");	
		        	well.typeCode = rs.getInt("emnco");
		        	well.typeName = rs.getString("emnnom");
		            return well;
		        }
		    });
		
			plate.wells = wells.toArray(new Well[wells.size()]);
			return plate;
		}else{
			return null;
		}
	}
    

	public boolean isPlateExist(String code) {
		Logger.info("pl_PlaqueSolexa @plaqueId="+code);
		List<Plate> plates = this.jdbcTemplate.query("pl_PlaqueSolexa @plaqueId=?", new Object[]{code}, new RowMapper<Plate>() {
	        public Plate mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	Plate plate = new Plate();
	        	plate.code = rs.getString("plaqueId");	        		        
	            return plate;
	        }
	    });
		return (plates.size() > 0);
	}
	
	public List<ListObject> getListObjectFromProcedureLims(String procedure) {
		List<ListObject> listObjects = this.jdbcTemplate.query(procedure,
				new RowMapper<ListObject>() {
					public ListObject mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ListObject value = new ListObject();
						value.name = rs.getString(1);
						value.code = rs.getString(2);
						return value;
					}
				});
		return listObjects;
	}


	public void deletePlate(String plateCode) {
		Logger.info("ps_PlaqueSolexa @plaqueId="+plateCode);
		this.jdbcTemplate.update("ps_PlaqueSolexa @plaqueId=?", new Object[]{plateCode});
	}


	
}

