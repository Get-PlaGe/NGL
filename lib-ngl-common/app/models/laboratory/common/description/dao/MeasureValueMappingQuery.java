package models.laboratory.common.description.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureValue;
import models.utils.dao.DAOException;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

import play.api.modules.spring.Spring;

public class MeasureValueMappingQuery extends MappingSqlQuery<MeasureValue>{

	public MeasureValueMappingQuery()
	{
		super();
	}
	public MeasureValueMappingQuery(DataSource ds, String sql,SqlParameter sqlParameter)
	{
		super(ds,sql);
		if(sqlParameter!=null)
			super.declareParameter(sqlParameter);
		compile();
	}
	
	@Override
	protected MeasureValue mapRow(ResultSet rs, int rowNum) throws SQLException {
		MeasureValue measureValue = new MeasureValue();
		measureValue.id=rs.getLong("id");
		measureValue.value=rs.getString("value");
		measureValue.defaultValue=rs.getBoolean("default_value");
		measureValue.code=rs.getString("code");
		long idCategory = rs.getLong("measure_category_id");
		
		MeasureCategoryDAO measureCategoryDAO = Spring.getBeanOfType(MeasureCategoryDAO.class);
		MeasureCategory measureCategory=null;
		try {
			measureCategory = measureCategoryDAO.findById(idCategory);
		} catch (DAOException e) {
			throw new SQLException(e);
		}
		measureValue.measureCategory=measureCategory;
		return measureValue;
	}

}
