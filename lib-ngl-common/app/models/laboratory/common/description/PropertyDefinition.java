package models.laboratory.common.description;

import java.util.List;

import models.laboratory.common.description.dao.PropertyDefinitionDAO;
import models.utils.Model;
import play.data.validation.Constraints.Required;

/**
 * Type property definition 
 * @author ejacoby
 *
 */
public class PropertyDefinition extends Model<PropertyDefinition>{

	@Required	
	public String name;

	public String description;

	public Boolean required = Boolean.FALSE;

	public Boolean active = Boolean.TRUE;
	public Boolean choiceInList = Boolean.FALSE;
	
	@Required	
	public String type;
	public String displayFormat;
	public Integer displayOrder;

	public String level;
	//Propagation des propriétés
	public boolean propagation;
	//propriétés d'entrée ou sortie pour les niveaux contenant et contenu
	public String inOut;

	public List<Value> possibleValues;

	public String defaultValue;

	public MeasureCategory measureCategory;

	public MeasureValue measureValue;

	public static Finder<PropertyDefinition> find = new Finder<PropertyDefinition>(PropertyDefinitionDAO.class.getName()); 
	
	public PropertyDefinition() {
		super(PropertyDefinitionDAO.class.getName());
	}
}
