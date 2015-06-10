package containers;

import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import models.laboratory.common.instance.Comment;
import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.ListObject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import controllers.containers.api.ContainerBatchElement;
import controllers.containers.api.Containers;
import controllers.containers.api.ContainersSearchForm;
import controllers.containers.api.ContainersUpdateForm;
import fr.cea.ig.MongoDBDAO;
import utils.AbstractTests;
import utils.ContainerBatchElementHelper;
import utils.DatatableBatchResponseElementForTest;
import utils.DatatableResponseForTest;
import utils.InitDataHelper;
import utils.MapperHelper;

public class ContainerControllerTests extends AbstractTests{
	
	@BeforeClass
	public static void initData() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InitDataHelper.initForProcessesTest();
	}

	@AfterClass	
	public static  void resetData(){
		InitDataHelper.endTest();
	}
	
	
	
	@Test
	public void valideGet() {			
		Container c = MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, ContainerTestHelper.getFakeContainerWithCode("valideGet"));		
		assertThat(status(Containers.get("valideGet"))).isEqualTo(play.mvc.Http.Status.OK);			
		MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, c);		
	}
	
	@Test
	public void valideGetNotFound() {		
		assertThat(status(Containers.get("not found"))).isEqualTo(play.mvc.Http.Status.NOT_FOUND);		
		
	}
	
	@Test
	public void validateHead() {			
		Container c = MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, ContainerTestHelper.getFakeContainerWithCode("validateHead"));		
		assertThat(status(Containers.head("validateHead"))).isEqualTo(play.mvc.Http.Status.OK);		
		MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, c);
	}
	
	@Test
	public void validateHeadNotFound() {		
		assertThat(status(Containers.head("Not found"))).isEqualTo(play.mvc.Http.Status.NOT_FOUND);			
	}
	
	@Test
	public void validateUpdateBatch() {				
		Container c1 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,"C2EV3ACXX_1");				
		Container c2 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,"C2EV3ACXX_2");			
		Container c3 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,"C2EV3ACXX_3");		
		List<ContainerBatchElement> ld = ContainerTestHelper.getFakeListContainerBatchElements(c1,c2,c3);		
		Result result = callAction(controllers.containers.api.routes.ref.Containers.updateBatch(), fakeRequest().withJsonBody(Json.toJson((ld))));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
	}
	
	@Test
	public void validateUpdateBatchBadRequestWithNull() throws JsonProcessingException {
		
		Container c1 = null;			
		Container c2 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,"C2EV3ACXX_2");	
		c2.state.code="A";
		c2.inputProcessCodes=null;
		Container c3 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,"C2EV3ACXX_3");
		c3.state.code="N";
		List<ContainerBatchElement> lc = ContainerTestHelper.getFakeListContainerBatchElements(c1,c2,c3);	
		Result result = callAction(controllers.containers.api.routes.ref.Containers.updateBatch(), fakeRequest().withJsonBody(Json.toJson(lc)));		
		List<DatatableBatchResponseElementForTest<Container>> ld = ContainerBatchElementHelper.getElementListObjectMapper(result);
		DatatableBatchResponseElementForTest<Container> db1 = ld.get(0);
		DatatableBatchResponseElementForTest<Container> db2 = ld.get(1);
		DatatableBatchResponseElementForTest<Container> db3 = ld.get(2);
		Logger.debug("");
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		assertThat(db1.status).isEqualTo(play.mvc.Http.Status.NOT_FOUND);
		assertThat(db2.status).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);
		assertThat(db3.status).isEqualTo(play.mvc.Http.Status.OK);
			
	}
	
	@Test
	public void validateUpdateBatchBadRequestWithErrors() {
		
		//TODO: validation d'un status de container null pour BAD REQUEST
		//Container c1 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,"C2EV3ACXX_5");
		//c1.state.code = null;	
		Container c1 = null;
		Container c2 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,"C2EV3ACXX_6");
		c2.code = "Error";
		Container c3 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,"C2EV3ACXX_7");		
		List<ContainerBatchElement> lc = ContainerTestHelper.getFakeListContainerBatchElements(c1,c2,c3);	
		Result result = callAction(controllers.containers.api.routes.ref.Containers.updateBatch(), fakeRequest().withJsonBody(Json.toJson(lc)));		
		List<DatatableBatchResponseElementForTest<Container>> ld = ContainerBatchElementHelper.getElementListObjectMapper(result);
		DatatableBatchResponseElementForTest<Container> db1 = ld.get(0);
		DatatableBatchResponseElementForTest<Container> db2 = ld.get(1);
		DatatableBatchResponseElementForTest<Container> db3 = ld.get(2);
		Logger.debug("");
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		//assertThat(db1.status).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);
		assertThat(db1.status).isEqualTo(play.mvc.Http.Status.NOT_FOUND);
		assertThat(db2.status).isEqualTo(play.mvc.Http.Status.NOT_FOUND);
		assertThat(db3.status).isEqualTo(play.mvc.Http.Status.OK);		
	}	
	
	@Test
	public void validateUpdate() {		
		String code = "C2EV3ACXX_8";
		Container c1 = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,code);		
		c1.comments.add(new Comment("TEST UNITAIRE validateUpdate "));		
		Result result = callAction(controllers.containers.api.routes.ref.Containers.update(code),fakeRequest().withJsonBody(Json.toJson(c1)));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void validateListWithDatatable() {
		ContainersSearchForm csf = ContainerTestHelper.getFakeContainersSearchForm();
		csf.datatable=true;
		MapperHelper mh = new MapperHelper();
		Container c = new Container();
		List <Container> lc = new ArrayList<Container>();
		DatatableResponseForTest<Container> dr= new DatatableResponseForTest<Container>();
		
		//Test with projectCode (good projectCode)		
		csf.projectCode = "ADI";
		Result result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&projectCodes="+csf.projectCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
				
		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});		
		lc = dr.data;
		for (int i=0;i<lc.size();i++){
			c = (Container) lc.get(i);
			assertThat(csf.projectCode).isIn(c.projectCodes);	
		}				
		
		//Test with projectCode (bad projectCode)
		csf.projectCode = "validateListWithDatatableBadRequest";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&projectCodes="+csf.projectCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});		
		lc = dr.data;		
		assertThat(lc).isNullOrEmpty();		

		//Test with samples (good request)
		c = ContainerTestHelper.getFakeContainerWithCode("validateListWithDatatableContainer");
		Sample s1 = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,"AHX_AAV");
		Sample s2 = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,"AHX_AAQ");
		Sample s3 = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,"AHX_AAS");		
		String projectCode = s1.projectCodes.get(0);		
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&projectCodes="+projectCode+"&sampleCodes="+s1.code+"&sampleCodes="+s2.code+"&sampleCodes="+s3.code));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});		
		lc = dr.data;
		for(int i=0;i<lc.size();i++){
			c = (Container) lc.get(i);
			for(String code:c.sampleCodes){
				assertThat(code).matches(projectCode+"_.*");
			}
			
		}
		
		//Test with samples (bad request)
		String projectBadCode = "validateListWithDatatableContainerBadprojectCode";
		String st1="validateListWithDatatableContainerBadsampleCode1";
		String st2="validateListWithDatatableContainerBadsampleCode2";
		String st3="validateListWithDatatableContainerBadsampleCode3";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&projectCodes="+projectBadCode+"&sampleCodes="+st1+"&sampleCodes="+st2+"&sampleCodes="+st3));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});		
		lc = dr.data;
		for(int i=0;i<lc.size();i++){
			c = (Container) lc.get(i);
			for(String code:c.sampleCodes){
				assertThat(code).doesNotMatch("validateListWithDatatable.*");
			}

		}		

		//Test with dates (matched period)		
		csf.fromDate = new Date(2014-1900, 2, 19) ;
		csf.toDate = new Date(2014-1900, 2, 21) ;		
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&fromDate="+csf.fromDate.getTime()+"&toDate="+csf.toDate.getTime()));		
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});
		lc = dr.data;
		for (int i=0;i<lc.size();i++){
			c = (Container) lc.get(i);
			assertThat(c.traceInformation.creationDate).isBetween(csf.fromDate, csf.toDate, true,true);
		}
				
		
		//Test with dates (unmatched period)
		csf.fromDate = new Date(2014-1900, 0, 1) ;
		csf.toDate = new Date(2014-1900, 2, 19) ;		
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&fromDate="+csf.fromDate.getTime()+"&toDate="+csf.toDate.getTime()));		
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});
		lc = dr.data;
		for(int i=0;i<lc.size();i++){
			c = (Container) lc.get(i);
			assertThat(c.traceInformation.creationDate).isNotBetween(csf.fromDate, csf.toDate, true,true);	
		}
		
		//Test with containerSupportCategory (good request)			
		csf.containerSupportCategory = "tube";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&containerSupportCategory="+csf.containerSupportCategory));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});
		lc = dr.data;
		
		for(int i=0;i<lc.size();i++){
			c = (Container) lc.get(i);
			Logger.info(c.categoryCode);
			assertThat(c.categoryCode).isEqualTo(csf.containerSupportCategory);
			Logger.info("");			
		}
		
		
		
		//Test with containerSupportCategory (bad request)
		csf.containerSupportCategory = "validateListWithDatatableBadContainerSupportCategory";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&containerSupportCategory="+csf.containerSupportCategory));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});
		lc = dr.data;
		assertThat(lc).isNullOrEmpty();
		
		//Test with nextExperimentTypeCode (good request)		
		csf.nextExperimentTypeCode= "prepa-flowcell";		
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&nextExperimentTypeCode="+csf.nextExperimentTypeCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});
		lc = dr.data;
		
		for(int i=0;i<lc.size();i++){
			c = (Container) lc.get(i);
			assertThat("solution-stock").isIn(c.fromExperimentTypeCodes);			
			Logger.info("");
		}
		
		
		//Test with nextExperimentTypeCode (bad request)
		csf.nextExperimentTypeCode= "solution-stock";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable="+String.valueOf(csf.datatable)+"&nextExperimentTypeCode="+csf.nextExperimentTypeCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		dr = mh.convertValue(mh.resultToJsNode(result), new TypeReference<DatatableResponseForTest<Container>>(){});
		lc = dr.data;
		assertThat(lc).isNullOrEmpty();
	}
	
	/*
	 * Use in a outer project
	 * 
	 * 
	//@Test
	public void validateListWithCount() {
		ContainersSearchForm csf = ContainerTestHelper.getFakeContainersSearchForm();
		csf.count = true;
		Result result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest( play.test.Helpers.GET, "?datatable=false&count="+csf.count));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
	}
	*/
	
	@SuppressWarnings("deprecation")
	@Test
	public void validateListWithList() {
		ContainersSearchForm csf = ContainerTestHelper.getFakeContainersSearchForm();
		csf.list=true;
		MapperHelper mh = new MapperHelper();
		ListObject lo = new ListObject();
		List <ListObject> lc = new ArrayList<ListObject>();
		
		
		//Test with code (good request)		
		csf.code="BFB_msCGP_d1";		
		Result result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&code="+csf.code));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});
		for (int i=0;i<lc.size();i++){
		assertThat(lc.get(i).code).isEqualTo(csf.code);
		}
		//Test with code (bad request)
		csf.code="validateListWithListBadCode";		
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&code="+csf.code));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});		
		assertThat(lc).isNullOrEmpty();
		
		//Test with projectCode (good request)		
		csf.projectCode = "AHX";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&projectCode="+csf.projectCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});		
		for(int i=0;i<lc.size();i++){
			lo = lc.get(i);
			assertThat(csf.projectCode).isIn((MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, lo.code)).projectCodes);					
			Logger.info("");
		}
		
		
		csf.projectCode = "BFB";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&projectCode="+csf.projectCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});		
		for(int i=0;i<lc.size();i++){
			lo = lc.get(i);
			assertThat(csf.projectCode).isIn((MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, lo.code)).projectCodes);					
			Logger.info("");
		}
		//Test with projectCode (bad request)
		csf.projectCode = "validateListWithListBadProjectCode";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&projectCode="+csf.projectCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});	
		assertThat(lc).isNullOrEmpty();		
		
		//Test with date (good request)		
		csf.fromDate = new Date(2014-1900, 9, 9) ;
		csf.toDate = new Date(2014-1900, 9, 11) ;
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&fromDate="+csf.fromDate.getTime()+"&toDate="+csf.toDate.getTime()));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});		
		for(int i=0;i<lc.size();i++){
			lo = lc.get(i);
			assertThat((MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, lo.code)).traceInformation.creationDate).isBetween(csf.fromDate, csf.toDate, true, true);					
			Logger.info("");
		}
			
		
		//Test with date (bad request)
		csf.fromDate = new Date(2014-1900, 0, 1) ;
		csf.toDate = new Date(2014-1900, 0, 5) ;
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&fromDate="+csf.fromDate.getTime()+"&toDate="+csf.toDate.getTime()));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<ListObject>>(){});		
		assertThat(lc).isNullOrEmpty();		
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void validateList() {
		ContainersSearchForm csf = ContainerTestHelper.getFakeContainersSearchForm();
		csf.list=false;
		csf.datatable=false;
		MapperHelper mh = new MapperHelper();
		Container c = new Container();
		List <Container> lc = new ArrayList<Container>();

		//Test with code (good request)		
		
		csf.code="BFB_msCGP_d1";		
		Result result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&code="+csf.code));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Container>>(){});				
		for (int i=0; i<lc.size();i++){
		assertThat(lc.get(i).code).isEqualTo(csf.code);
		}
		//Test with code (bad request)
		csf.code="validateListWithListBadCode";		
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&code="+csf.code));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Container>>(){});		
		assertThat(lc).isNullOrEmpty();

		//Test with projectCode (good request)		
		csf.projectCode = "AHX";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&projectCode="+csf.projectCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Container>>(){});		
		for(int i=0; i<lc.size();i++){
			assertThat(csf.projectCode).isIn(lc.get(i).projectCodes);
		}
		
		csf.projectCode = "BFB";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&projectCode="+csf.projectCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Container>>(){});		
		for(int i=0; i<lc.size();i++){
			assertThat(csf.projectCode).isIn(lc.get(i).projectCodes);
		}		

		//Test with projectCode (bad request)
		csf.projectCode = "validateListWithListBadProjectCode";
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&projectCode="+csf.projectCode));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);		
		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Container>>(){});	
		assertThat(lc).isNullOrEmpty();		

		//Test with date (good request)		
		csf.fromDate = new Date(2014-1900, 9, 9);
		csf.toDate = new Date(2014-1900, 9, 11);
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&fromDate="+csf.fromDate.getTime()+"&toDate="+csf.toDate.getTime()));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Container>>(){});		
		for(int i=0; i<lc.size();i++){
		c = lc.get(i);
		assertThat(c.traceInformation.creationDate).isBetween(csf.fromDate, csf.toDate, true,true);	
		}
		//Test with date (bad request)
		csf.fromDate = new Date(2014-1900, 0, 1);
		csf.toDate = new Date(2014-1900, 0, 5);
		result = callAction(controllers.containers.api.routes.ref.Containers.list(), fakeRequest(play.test.Helpers.GET, "?list="+String.valueOf(csf.list)+"&fromDate="+csf.fromDate.getTime()+"&toDate="+csf.toDate.getTime()));
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);

		lc = mh.convertValue(mh.resultToJsNode(result), new TypeReference<ArrayList<Container>>(){});		
		assertThat(lc).isNullOrEmpty();		
	}
	
		
	@Test
	public void validateUpdateStateCode() {
		ContainersUpdateForm cuf = ContainerTestHelper.getFakeContainersUpdateForm();
		Container container = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.and(DBQuery.is("state.code", "IS"),DBQuery.notExists("processTypeCode"),DBQuery.notExists("inputProcessCodes"))).toList().get(0);
		cuf.stateCode = "IW-P";
		Result result = callAction(controllers.containers.api.routes.ref.Containers.updateStateCode(container.code), fakeRequest().withJsonBody((Json.toJson(cuf))));		
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		cuf.stateCode = "IS";
		result = callAction(controllers.containers.api.routes.ref.Containers.updateStateCode(container.code), fakeRequest().withJsonBody((Json.toJson(cuf))));		
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
				
		container = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.and(DBQuery.is("state.code", "IS"),DBQuery.exists("processTypeCode"),DBQuery.exists("inputProcessCodes"),DBQuery.regex("code", Pattern.compile("^BEG")))).toList().get(0);
		cuf.stateCode = "A";
		result = callAction(controllers.containers.api.routes.ref.Containers.updateStateCode(container.code), fakeRequest().withJsonBody((Json.toJson(cuf))));		
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
		container = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.and(DBQuery.is("state.code", "A"),DBQuery.exists("processTypeCode"),DBQuery.exists("inputProcessCodes"),DBQuery.regex("code", Pattern.compile("^BEG")))).toList().get(0);
		cuf.stateCode = "IS";
		result = callAction(controllers.containers.api.routes.ref.Containers.updateStateCode(container.code), fakeRequest().withJsonBody((Json.toJson(cuf))));		
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.OK);
		
	}
	
	@Test
	public void validateBadUpdateStateCode() {
		ContainersUpdateForm cuf = ContainerTestHelper.getFakeContainersUpdateForm();
		Container container = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.and(DBQuery.is("state.code", "IS"),DBQuery.notExists("processTypeCode"),DBQuery.notExists("inputProcessCodes"))).toList().get(0);
		cuf.stateCode = "A";
		Result result = callAction(controllers.containers.api.routes.ref.Containers.updateStateCode(container.code), fakeRequest().withJsonBody((Json.toJson(cuf))));		
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);
		
		container = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.and(DBQuery.is("state.code", "IS"),DBQuery.exists("processTypeCode"),DBQuery.exists("inputProcessCodes"),DBQuery.regex("code", Pattern.compile("^BEG")))).toList().get(0);
		cuf.stateCode = "IW-P";
		result = callAction(controllers.containers.api.routes.ref.Containers.updateStateCode(container.code), fakeRequest().withJsonBody((Json.toJson(cuf))));		
		assertThat(status(result)).isEqualTo(play.mvc.Http.Status.BAD_REQUEST);
		
	}
	

}
