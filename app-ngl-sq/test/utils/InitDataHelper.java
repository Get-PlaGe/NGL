package utils;

import java.util.ArrayList;
import java.util.List;

import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;

public class InitDataHelper {
	private final static String INIT_MONGO_SUFFIX = "_init";
	
	public static void initForProcessesTest(){
		initContainers();
		initSamples();
		initSupports();
		initProjects();
		initProcesses();
	}
	
	private static void initProcesses() {
		List<Process> process = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME+INIT_MONGO_SUFFIX, Process.class).toList();
		MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME, process);
		
	}

	public static void initSamples(){
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME+INIT_MONGO_SUFFIX, Sample.class).toList();
		MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, samples);
	}
	
	public static void initProjects(){
		List<Project> projects = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME+INIT_MONGO_SUFFIX, Project.class).toList();
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, projects);
	}
	
	public static void initSupports(){
		List<ContainerSupport> supports = MongoDBDAO.find(InstanceConstants.SUPPORT_COLL_NAME+INIT_MONGO_SUFFIX, ContainerSupport.class).toList();
		MongoDBDAO.save(InstanceConstants.SUPPORT_COLL_NAME, supports);
	}
	
	public static void initContainers(){
			List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME+INIT_MONGO_SUFFIX, Container.class).toList();
			MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, containers);
	}
	
	public static void initContainers(List<String> containersCode){
			List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME+INIT_MONGO_SUFFIX, Container.class, DBQuery.in("code", containersCode)).toList();
			MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, containers);
	}
	
	public static void endTest(){
		 MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.exists("code"));
		 MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.exists("code"));
		 MongoDBDAO.delete(InstanceConstants.PROJECT_COLL_NAME, Project.class, DBQuery.exists("code"));
		 MongoDBDAO.delete(InstanceConstants.SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.exists("code"));
		 MongoDBDAO.delete(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("code"));

	}
	
	public static List<String> getContainerCodesInContext(){
		List<String> codes = new ArrayList<String>();
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();
		for(Container c: containers){
			codes.add(c.code);
		}
		return codes;
	}
	
	public static List<String> getSupportCodesInContext(String categoryCode){
		List<String> codes = new ArrayList<String>();
		List<ContainerSupport> containerSupports = MongoDBDAO.find(InstanceConstants.SUPPORT_COLL_NAME, ContainerSupport.class, DBQuery.is("categoryCode", categoryCode)).toList();
		for(ContainerSupport cs: containerSupports){
			codes.add(cs.code);
		}
		return codes;
	}
}
