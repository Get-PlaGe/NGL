package models.administration.authorisation.description.dao;

import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import models.administration.authorisation.Role;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;

/**
 * 
 * @author michieli
 *
 */
@Repository
public class RoleDAO extends AbstractDAOMapping<Role>{
	
	protected RoleDAO(){
		super("role", Role.class, RoleMappingQuery.class,
				"SELECT r.id, r.label FROM role as r ", true);
	}
	
	/*
	 * findAll()
	 */
	public List<Role> findAll() throws DAOException{
		String sql = sqlCommon ;
		BeanPropertyRowMapper<Role> mapper = new BeanPropertyRowMapper<Role>(entityClass);
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	/*
	 * findByUserLogin()
	 */
	public List<Role> findByUserLogin(String aLogin) throws DAOException{
		if(null == aLogin){
			throw new DAOException("login is mandatory");
		}
		String sql = sqlCommon
				+ "INNER JOIN user_role as ur ON r.id = ur.role_id "
				+ "INNER JOIN user as u ON ur.user_id = u.id "
				+ "WHERE u.login=?";
		return initializeMapping(sql, new SqlParameter("login", Types.VARCHAR)).execute(aLogin);
	}

	@Override
	public long save(Role value) throws DAOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update(Role value) throws DAOException {
		// TODO Auto-generated method stub
		
	}
}
