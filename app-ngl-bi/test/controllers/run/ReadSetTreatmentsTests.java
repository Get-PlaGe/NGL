package controllers.run;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.running;
import static play.test.Helpers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyImgValue;
import models.laboratory.common.instance.property.PropertyObjectListValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import org.mongojack.DBQuery;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.Logger;
import play.mvc.Result;
import utils.AbstractTests;
import utils.RunMockHelper;
import fr.cea.ig.MongoDBDAO;

public class ReadSetTreatmentsTests extends AbstractTests {
	
	static Container c;
	
	@BeforeClass
	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ContainerSupport cs = new ContainerSupport();
		cs.code = "containerName";
		cs.categoryCode = "lane";
		   
		MongoDBDAO.save(InstanceConstants.SUPPORT_COLL_NAME, cs);
		
	   Container c = new Container();
	   c.code ="containerTest1";
	   c.support = new LocationOnContainerSupport(); 
	   c.support.code = cs.code; 
	   
	   MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, c);
	}
	
	
	@AfterClass
	public static void deleteData(){
		List<ContainerSupport> containerSupports = MongoDBDAO.find(InstanceConstants.SUPPORT_COLL_NAME, ContainerSupport.class).toList();
		for (ContainerSupport cs : containerSupports) {
			if (cs.code.equals("containerName")) {
				MongoDBDAO.delete(InstanceConstants.SUPPORT_COLL_NAME, cs);
			}
		}
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();
		for (Container container : containers) {
			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, container);
		}
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class).toList();
		for (Sample sample : samples) {
			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, sample);
		}
	}
	
	private void createRdCode(String suffixe) {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","DIDIER_TESTFORTRT"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode" + suffixe));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,readSetDelete._id);
		}
		Sample sample = MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code","SampleCode"));
		if (sample!= null) {
			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,sample._id);
		}
		Project project = MongoDBDAO.findOne(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.is("code","ProjectCode"));
		if (project!= null) {
			MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, Project.class, project._id);
		}
		
		sample = RunMockHelper.newSample("SampleCode");
		project = RunMockHelper.newProject("ProjectCode");
		
		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, sample);
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
		
		Run run = RunMockHelper.newRun("DIDIER_TESTFORTRT");
		run.dispatch = true; // For the archive test
		Lane lane = RunMockHelper.newLane(1);
		Lane lane2 = RunMockHelper.newLane(2);
		List<Lane> lanes = new ArrayList<Lane>();
		
		ReadSet readset = RunMockHelper.newReadSet("rdCode" + suffixe);
		readset.runCode = run.code;
		
		lanes.add(lane);
		lanes.add(lane2);
		run.lanes = lanes;
		
		Result result = callAction(controllers.runs.api.routes.ref.Runs.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonRun(run)));
		assertThat(status(result)).isEqualTo(OK);
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSets.save(),fakeRequest().withJsonBody(RunMockHelper.getJsonReadSet(readset)));
        assertThat(status(result)).isEqualTo(OK);
	}
	
	private Treatment getNewTreatmentForReadSet() {
		Treatment t = new Treatment();
		t.code =  "ngsrg";		
		t.typeCode = "ngsrg-illumina";
		t.categoryCode = "ngsrg";
		//define map of single property values
		Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();
		m.put("nbCluster", new PropertySingleValue(100)); // valeur simple
		m.put("nbBases", new PropertySingleValue(100));
		m.put("fraction", new PropertySingleValue(100));
		m.put("Q30", new PropertySingleValue(100));
		m.put("qualityScore", new PropertySingleValue(100));
		m.put("nbReadIllumina", new PropertySingleValue(100));
		t.set("default", m);
		
		return t;
	}
	
	private byte[] getData() {
		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
			    0x30, 0x30, (byte)0x9d };
		return data;
	}
	
	private PropertyImgValue getPropertyImgValue() {
			PropertyImgValue pImg = new PropertyImgValue();
			pImg.value = getData();
			pImg.fullname = "titi.jpg";
			pImg.extension = "jpg";
			pImg.width = 400;
			pImg.height = 300;
			return pImg;
	}
	 
	private List<Float> getListFloat() {
			List<Float> lf = new ArrayList<Float>();
			Float f = 2.F;
			for (int i=0; i<101; i++) {
				lf.add(f);
			}
			return lf;
	}
	 
	private Treatment getNewTreatmentPropertyDetailsOK() {			
				Treatment t = new Treatment();
				t.code =  "readQualityRaw";		
				t.typeCode =  "read-quality";
				t.categoryCode = "quality";
				
				//define map of property values
				Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();				 
				
				m.put("sampleInput", new PropertySingleValue(100));
				m.put("qualScore",getPropertyImgValue());
				m.put("nuclDistribution", getPropertyImgValue());
				m.put("readWithNpercent", getPropertyImgValue());
				m.put("readSizeDistribution", getPropertyImgValue());
				m.put("adapterContamination", getPropertyImgValue());
				m.put("GCDistribution", getPropertyImgValue());
				m.put("positionN", getPropertyImgValue());
				m.put("maxSizeReads", new PropertySingleValue(100));
				m.put("maxSizeReadsPercent", new PropertySingleValue(100));

				PropertyObjectListValue lpObj = new PropertyObjectListValue();
				List l = new ArrayList();
				
				HashMap<String, Object> m2 = new HashMap<String, Object>(); 
				m2.put("adapterName", "RNA_PCR_MK1(rev)");
				m2.put("contaminationIntensities", getListFloat());

				HashMap<String, Object> m3 = new HashMap<String, Object>();
				m3.put("adapterName", "totoAdaptateur");
				m3.put("contaminationIntensities", getListFloat());
				
				l.add(m2);
				l.add(m3);
				lpObj.value=l;				
				m.put("adapterDetails", lpObj);
				
				PropertyObjectListValue lpObj2 = new PropertyObjectListValue();
				List l2 = new ArrayList();
				
				HashMap<String, Object> m4 = new HashMap<String, Object>();
				m4.put("nbOfN",1);
				m4.put("percentOfReads",2.F);
				
				HashMap<String, Object> m5 = new HashMap<String, Object>();
				m5.put("nbOfN",2);
				m5.put("percentOfReads",2.F);
				
				l2.add(m4);
				l2.add(m5);
				lpObj2.value=l2;
				m.put("readWithNpercentDetails", lpObj2);
				
				
				/*set the context*/
				t.set("read1", m);
				return t; 
	}
	
	
	@Test
	public void testSave() {
		createRdCode("");
		    	 
		Treatment t = getNewTreatmentForReadSet();

		Result result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
		
		//query for control
        ReadSet r = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"));
        assertThat(r.treatments.size()).isEqualTo(1);
        Map.Entry<String, Treatment> entry = r.treatments.entrySet().iterator().next();
        assertThat(entry.getKey()).isEqualTo("ngsrg");
	}
	
	@Test
	public void testSavePropertyDetailsOK() {
		String suffixe = "2";
		createRdCode(suffixe);
		
		Treatment t = getNewTreatmentPropertyDetailsOK();
		
		Result result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.save("rdCode"+suffixe),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
		
		//query for control
        ReadSet r = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"+suffixe));
        assertThat(r.treatments.size()).isEqualTo(1);
        Map.Entry<String, Treatment> entry = r.treatments.entrySet().iterator().next();
        assertThat(entry.getKey()).isEqualTo("readQualityRaw");
		
	}
	
	
	@Test
	public void testUpdate() {
	    createRdCode("");
		    	 
		Treatment t = getNewTreatmentForReadSet();
		
		Result result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));		
		assertThat(status(result)).isEqualTo(OK);
		
		Map<String,PropertyValue> m2 = new HashMap<String,PropertyValue>();
		m2.put("nbCluster", new PropertySingleValue(18)); // simple value
		m2.put("nbBases", new PropertySingleValue(18));
		m2.put("fraction", new PropertySingleValue(18));
		m2.put("Q30", new PropertySingleValue(18));
		m2.put("qualityScore", new PropertySingleValue(18));
		m2.put("nbReadIllumina", new PropertySingleValue(18));
		
		t.results().remove("default");
		t.set("default", m2);
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.update("rdCode", t.code),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
		
		//query for control
        ReadSet r = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "rdCode");
        assertThat(r.treatments.size()).isEqualTo(1);
        Map.Entry<String, Treatment> entry = r.treatments.entrySet().iterator().next();
        assertThat(entry.getKey()).isEqualTo("ngsrg");
        assertThat(entry.getValue().results().get("default").get("nbCluster").value.toString()).isEqualTo("18"); 
	}
	
	
	@Test
	public void testDelete() {		
	    createRdCode("");
		    	 
		Treatment t = getNewTreatmentForReadSet();
		
		Result result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
		
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.delete("rdCode", t.code),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
		
		//query for control
        //ReadSet r = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code","rdCode"));
        //assertThat(r.treatments.size()).isEqualTo(0);
	}
	
	@Test
	public void testGet() { 
	    createRdCode("");
		    	 
		Treatment t = getNewTreatmentForReadSet();
		
		Result result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.get("rdCode", t.code),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
	}
	
	@Test
	public void testHead() { 
	    createRdCode("");
		    	 
		Treatment t = getNewTreatmentForReadSet();
		
		Result result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.save("rdCode"),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
		
		result = callAction(controllers.readsets.api.routes.ref.ReadSetTreatments.head("rdCode", t.code),fakeRequest().withJsonBody(RunMockHelper.getJsonTreatment(t)));
		assertThat(status(result)).isEqualTo(OK);
	}
	
		
}
