package models.utils.instance;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import validation.ContextValidation;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.ContainerUsed;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.OneToVoidContainer;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import controllers.authorisation.PermissionHelper;
import fr.cea.ig.MongoDBDAO;

public class ExperimentHelper {


	public static void generateOutputContainerUsed(Experiment exp, ContextValidation contextValidation){


		List<Container> containers = new ArrayList<Container>();
		if (!contextValidation.hasErrors()) {
			for(int i=0;i<exp.atomicTransfertMethods.size();i++){
				if(!(exp.atomicTransfertMethods.get(i) instanceof  OneToVoidContainer)){
					containers.addAll(exp.atomicTransfertMethods.get(i).createOutputContainerUsed(exp));
				}
			}

			MongoDBDAO.save(InstanceConstants.EXPERIMENT_COLL_NAME, exp);
			InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME, containers,new ContextValidation( contextValidation.errors));
		}


	}

	//TODO
	public static void saveOutputContainerUsed(Experiment exp, ContextValidation contextValidation){


		List<Container> containers = new ArrayList<Container>();
		if (!contextValidation.hasErrors()) {
			for(int i=0;i<exp.atomicTransfertMethods.size();i++){
				if(!(exp.atomicTransfertMethods.get(i) instanceof  OneToVoidContainer)){
					containers.addAll(exp.atomicTransfertMethods.get(i).createOutputContainerUsed(exp));
				}
			}


			/*Builder builder = new DBUpdate.Builder();
			builder=builder.set("atomicTransfertMethods",exp.atomicTransfertMethods);

			MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", code),builder);
			 */


			MongoDBDAO.save(InstanceConstants.EXPERIMENT_COLL_NAME, exp);


			InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME, containers,new ContextValidation( contextValidation.errors));
		}


	}


	/**
	 * Add/Create trace informations to the experiment object
	 * @param exp: the Experiment object
	 * @return the new experiment object with traces
	 */
	public static Experiment traceInformation(Experiment exp,String user){
		if (null == exp._id) {
			//init
			exp.traceInformation = new TraceInformation();
			exp.traceInformation.setTraceInformation(user);
		} else {
			exp.traceInformation.setTraceInformation(user);
		}

		return exp;
	}


	public static Experiment updateInstrumentCategory(Experiment exp) throws DAOException{
		Logger.debug("Test categoryCode :"+exp.instrument.categoryCode+" .");
		if((exp.instrument.categoryCode == null ||exp.instrument.categoryCode.equals("") ) && exp.instrumentUsedTypeCode!=null){
			InstrumentUsedType instrumentUsedType=InstrumentUsedType.find.findByCode(exp.instrumentUsedTypeCode);
			Logger.debug("Result categoryCode"+instrumentUsedType.category.code);
			exp.instrument.categoryCode=instrumentUsedType.category.code;
		}
		return exp;	
	}

	public static Experiment setProjetAndSamples(Experiment exp) {
		exp.sampleCodes = new ArrayList<String>();
		exp.projectCodes  = new ArrayList<String>();

		for(int i=0;i<exp.atomicTransfertMethods.size();i++)
			for(ContainerUsed c:exp.atomicTransfertMethods.get(i).getInputContainers()){
				Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, c.containerCode);
				exp.sampleCodes = InstanceHelpers.addCodesList(exp.sampleCodes, container.sampleCodes);
				exp.projectCodes = InstanceHelpers.addCodesList(exp.projectCodes, container.projectCodes);
			}	
	return exp;
	}


}
