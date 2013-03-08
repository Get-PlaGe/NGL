package models.laboratory.container.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Resolution;
import models.laboratory.common.description.State;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;
import models.utils.HelperObjects;
import net.vz.mongodb.jackson.MongoCollection;

import org.codehaus.jackson.annotate.JsonIgnore;

import fr.cea.ig.DBObject;



/**
 * 
 * Instances Container are stored in MongoDB collection named Container 
 * Container is referenced in collection Experiment, Purifying, TransferMethod, Extraction, QC in embedded class ListInputOutputContainer
 * The Relationship between containers aren't storing in the container but in class/collection RelationshipContainer 
 * In Container, the link with experiment are the attribut 'fromExperimentTypes' who help to manage Container in workflow 
 *  
 * @author mhaquell
 *
 */
@MongoCollection(name="Container")
public class Container extends DBObject {

	
	@JsonIgnore
	public final static String HEADER="Container.code;Container.categoryCode;Container.comments;ContainerSupport.categorycode;ContainerSupport.x;ContainerSupport.y;ContainerSupport.barecode";
	
	//ContainerCategory Ref
	public String categoryCode;
	
	// State Ref ??
	//TODO
	public String stateCode;
	public TBoolean valid;
	// Resolution Ref
	public String resolutionCode; //used to classify the final state (ex : ) 
	
	// Container informations
	public TraceInformation traceInformation;
	public Map<String, PropertyValue> properties;
	public List<Comment> comments;

	
	//Relation with support
	public ContainerSupport support; 
	
	//Embedded content with values;
	public List<Content> contents;

	// Embedded QC result, this data are copying from collection QC
	public List<QualityControlResult> qualityControlResults;

	
	//Stock management 
	public List<Volume> mesuredVolume;
	public List<Volume> calculedVolume;
	
	// For search optimisation
	public List<String> projectCodes; // getProjets
	public List<String> sampleCodes; // getSamples
	// ExperimentType must be an internal or external experiment ( origine )
	// List for pool experimentType
	public List<String> fromExperimentTypeCodes; // getExperimentType
	
	// Propager au container de purif ??
	//public String fromExperimentCode; ??
	public String fromPurifingCode;
	//public String fromExtractionTypeCode;
	

	public Container(){
		traceInformation=new TraceInformation();
	}
	
	//Delete method if possibles
	@JsonIgnore
	public Container(SampleUsed sampleUsed){

		this.contents=new ArrayList<Content>();
		this.contents.add(new Content(sampleUsed));
		this.traceInformation=new TraceInformation();
	}

	
	@JsonIgnore
	public ContainerCategory getContainerCategory(){
		return new HelperObjects<ContainerCategory>().getObject(ContainerCategory.class, categoryCode, null);

	}
	
	@JsonIgnore
	public List<Project> getProjects() {
		return new HelperObjects<Project>().getObjects(Project.class, projectCodes);
		
	}
	
	@JsonIgnore
	public List<Sample> getSamples() {
		return new HelperObjects<Sample>().getObjects(Sample.class, sampleCodes);
	}

	@JsonIgnore
	public List<ExperimentType> getFromExperimentTypes() {
		return new HelperObjects<ExperimentType>().getObjects(ExperimentType.class, fromExperimentTypeCodes);
	}
	
	
	@JsonIgnore
	public State getState(){
		return new HelperObjects<State>().getObject(State.class, stateCode, null);
	}
	
	@JsonIgnore
	public Resolution getResolution(){
		return new HelperObjects<Resolution>().getObject(Resolution.class, resolutionCode, null);
	}
	
	@JsonIgnore
	public void addContent(Sample sample){
		
		//Create new content
		if(contents==null){
			this.contents=new ArrayList<Content>();
		}
		
		this.contents.add(new Content(new SampleUsed(sample.code, sample.typeCode, sample.categoryCode)));
		
		//Add projet code if not exist
		if(this.projectCodes==null){
			this.projectCodes=new ArrayList<String>();
		}
		if(!projectCodes.contains(sample.projectCode)){
			this.projectCodes.add(sample.projectCode);
		}
		
		//Add sample code if not exist
		if(this.sampleCodes==null){
			this.sampleCodes=new ArrayList<String>();
		}
		if(!sampleCodes.contains(sample.code)){
			sampleCodes.add(sample.code);
		}
		
	}
		

}
