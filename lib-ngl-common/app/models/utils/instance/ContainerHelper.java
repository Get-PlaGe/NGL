package models.utils.instance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;

import org.apache.commons.collections.CollectionUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;

import com.google.common.collect.Multiset.Entry;

import validation.ContextValidation;
import validation.utils.BusinessValidationHelper;
import fr.cea.ig.MongoDBDAO;

public class ContainerHelper {


	public static void addContent(Container container, Sample sample) throws DAOException{
		addContent(container,sample,null);
	}

	public static void addContent(Container container,Sample sample, Content content) throws DAOException{

		Content finalContent =new Content(sample.code, sample.typeCode, sample.categoryCode);
		finalContent.projectCode = content.projectCode;
		finalContent.percentage=content.percentage;
		finalContent.referenceCollab=content.referenceCollab;
		
		SampleType sampleType =BusinessValidationHelper.validateExistDescriptionCode(null, sample.typeCode, "typeCode", SampleType.find,true);
		ImportType importType =BusinessValidationHelper.validateExistDescriptionCode(null, sample.importTypeCode, "importTypeCode", ImportType.find,true);

		if(importType !=null){
			InstanceHelpers.copyPropertyValueFromPropertiesDefinition(importType.getPropertyDefinitionByLevel(Level.CODE.Content), sample.properties,finalContent.properties);
		}
		
		if(sampleType !=null){
			InstanceHelpers.copyPropertyValueFromPropertiesDefinition(sampleType.getPropertyDefinitionByLevel(Level.CODE.Content), sample.properties,finalContent.properties);
		}

		if(content.properties!=null)
			finalContent.properties.putAll(content.properties);

		container.contents.add(finalContent);

		container.projectCodes.addAll(sample.projectCodes);

		container.sampleCodes.add(sample.code);

	}
	/**
	 * fusion content if same projectCode, sampleCode and tag if exist.
	 * 
	 * the fusion : 
	 * 	- sum the percentage of content 
	 *  - keep properties with same key and same value
	 *  - remove properties  with same key and different value
	 *  - add properties that exists only in one content
	 *  
	 * @param contents
	 * @return
	 */
	public static List<Content> fusionContents(List<Content> contents) {
		
		//groupb by a key
		Map<String, List<Content>> contentsByKey = contents.stream().collect(Collectors.groupingBy((Content c ) -> getContentKey(c)));
		
		//extract values with only one content
		Map<String, Content> contentsByKeyWithOneValues = contentsByKey.entrySet().stream()
				.filter((Map.Entry<String, List<Content>> e) -> e.getValue().size() == 1)
				.collect(Collectors.toMap((Map.Entry<String, List<Content>> e) -> e.getKey(), (Map.Entry<String, List<Content>> e) -> e.getValue().get(0)));
		
		//extract values with several contents and fusion the contents
		Map<String, Content> contentsByKeyWithSeveralValues = contentsByKey.entrySet().stream()
				.filter((Map.Entry<String, List<Content>> e) -> e.getValue().size() > 1)
				.collect(Collectors.toMap((Map.Entry<String, List<Content>> e) -> e.getKey(), (Map.Entry<String, List<Content>> e) -> fusionSameContents(e.getValue())));
		
		
		contentsByKeyWithOneValues.putAll(contentsByKeyWithSeveralValues);
		
		return new ArrayList(contentsByKeyWithOneValues.values());
	}

	private static Content fusionSameContents(List<Content> contents) {
		Content finalContent = new Content();
		
		finalContent.projectCode = contents.get(0).projectCode;
		finalContent.sampleCode = contents.get(0).sampleCode;
		finalContent.sampleCategoryCode = contents.get(0).sampleCategoryCode;
		finalContent.sampleTypeCode = contents.get(0).sampleTypeCode;
		finalContent.referenceCollab = contents.get(0).referenceCollab;
		finalContent.percentage = new BigDecimal(contents.stream().mapToDouble((Content c) -> c.percentage).sum()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		
		for(Content c : contents){
			for(String key : c.properties.keySet()){
				PropertyValue<?> pv = c.properties.get(key);
				finalContent.properties.computeIfAbsent(key, k -> pv);
				finalContent.properties.computeIfPresent(key, (k,v) -> fusionSameProperty(v, pv));
			}
		}
		
		return finalContent;
	}

	private static  PropertyValue<?> fusionSameProperty(PropertyValue<?> currentPv, PropertyValue<?> newPv) {
		if(currentPv.value.equals(newPv.value)){
			return currentPv;
		}else{
			return null;
		}		
	}

	private static String getContentKey(Content content) {
		if(content.properties.containsKey("tag")){
			return content.projectCode+"_"+content.sampleCode+"_"+content.properties.get("tag").value;
		}else{
			return content.projectCode+"_"+content.sampleCode;
		}		
	}

	public static List<Content> calculPercentageContent(List<Content> contents, Double percentage){
		if(percentage!=null){
			for(Content cc:contents){
				if(cc.percentage != null){
					cc.percentage = (new BigDecimal((cc.percentage*percentage)/100.00)).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
				}else{
					cc.percentage = percentage;
				}						
			}
		}
		return contents;
	}


	private static SimpleDateFormat getSimpleDateFormat(){
		return new SimpleDateFormat("yyyyMMdd_HHmmss");
	}

	// generation d'un code de container pour les cas ou aucun n'est fourni: <CODE>-<date><RANDOMNUM>
	public static String generateContainerCode(String categoryCode){
		Random randomGenerator = new Random();
		return (categoryCode+"-"+getSimpleDateFormat().format(new Date())+randomGenerator.nextInt(100)).toUpperCase();
	}


	public static List<ContainerSupport> createSupportFromContainers(List<Container> containers, Map<String, PropertyValue<String>> mapSupportsCodeSeq, ContextValidation contextValidation){

		HashMap<String,ContainerSupport> mapSupports = new HashMap<String,ContainerSupport>();

		for (Container container : containers) {
			Logger.debug(" createSupportFromContainers; container.code "+ container.code );
			
			if (container.support != null) {
				ContainerSupport newSupport = null;
				
				Logger.debug(" createSupportFromContainers; creating support "+ container.support.code );
				
				// mapSupportsCodeSeq n'existe que pour les flowcell...
				// NW utilisation d'un operateur ternaire
				newSupport = ContainerSupportHelper.createContainerSupport(container.support.code,
																			(mapSupportsCodeSeq != null ? mapSupportsCodeSeq.get(container.support.code) : null),
																			container.support.categoryCode, "ngl");
				
				newSupport.projectCodes = new  HashSet<String>(container.projectCodes);
				newSupport.sampleCodes = new  HashSet<String>(container.sampleCodes);
				newSupport.state=container.state;
				newSupport.storageCode=container.support.storageCode; //FDS ajout 14/10/2015
				
				if(null != container.fromTransformationTypeCodes){
					newSupport.fromTransformationTypeCodes = new  HashSet<String>(container.fromTransformationTypeCodes);
				}
				
				if (!mapSupports.containsKey(newSupport.code)) {
					newSupport.nbContainers = 1;
					newSupport.nbContents = container.contents.size();
					mapSupports.put(newSupport.code, newSupport);
				}
				else {
					ContainerSupport oldSupport = (ContainerSupport) mapSupports.get(newSupport.code);
					oldSupport.projectCodes.addAll(newSupport.projectCodes); 
					oldSupport.sampleCodes.addAll(newSupport.sampleCodes);
					oldSupport.nbContainers++;
					oldSupport.nbContents = oldSupport.nbContents + container.contents.size();
					if(null != newSupport.fromTransformationTypeCodes && null != oldSupport.fromTransformationTypeCodes){
						oldSupport.fromTransformationTypeCodes.addAll(newSupport.fromTransformationTypeCodes);
					}
				}
			}
		}

		return InstanceHelpers.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, new ArrayList<ContainerSupport>(mapSupports.values()), contextValidation, true);
	}

	public static void updateSupportFromUpdatedContainers(List<Container> updatedContainers, Map<String, PropertyValue<String>> mapSupportsCodeSeq, ContextValidation contextValidation){

		HashMap<String,ContainerSupport> mapSupports = new HashMap<String,ContainerSupport>();

		for (Container container : updatedContainers) {
			if (container.support != null) {
				
				ContainerSupport newSupport = null;
				
				//FDS note 22/06/2015: mapSupportsCodeSeq n'est defini que pour les containers de type lane!!
				//21/01/2016  NW utilisation d'un operateur ternaire
				newSupport = ContainerSupportHelper.createContainerSupport(container.support.code,
																			(mapSupportsCodeSeq != null ? mapSupportsCodeSeq.get(container.support.code) : null),
																			container.support.categoryCode, "ngl");
					
				newSupport.projectCodes = new  HashSet<String>(container.projectCodes);
				newSupport.sampleCodes = new  HashSet<String>(container.sampleCodes);		
				newSupport.fromTransformationTypeCodes = new  HashSet<String>(container.fromTransformationTypeCodes); // FDS ajout 22/01/2016	
				
				//FDS ajout 14/10/2015
				if ( container.support.storageCode != null ){
					newSupport.storageCode=container.support.storageCode;
				}
				
				if (!mapSupports.containsKey(newSupport.code)) {
					mapSupports.put(newSupport.code, newSupport);
					newSupport.nbContainers = 1;
					newSupport.nbContents = container.contents.size();
				}
				else {
					ContainerSupport oldSupport = (ContainerSupport) mapSupports.get(newSupport.code);
					oldSupport.projectCodes.addAll(newSupport.projectCodes); 
					//Logger.debug("[updateSupportFromUpdatedContainers] adding projectCodes " +  newSupport.projectCodes + " to containerSupport " + oldSupport.code);
					
					oldSupport.sampleCodes.addAll(newSupport.sampleCodes); 
					//Logger.debug("[updateSupportFromUpdatedContainers] adding sampleCodes "+ newSupport.sampleCodes + " to containerSupport " + oldSupport.code);

					// et si null ??
					oldSupport.fromTransformationTypeCodes.addAll(newSupport.fromTransformationTypeCodes); 
					//Logger.debug("[updateSupportFromUpdatedContainers] adding fromTransformationTypeCodes "+ newSupport.fromTransformationTypeCodes + " to containerSupport " + oldSupport.code);
					
					oldSupport.nbContainers++;
					oldSupport.nbContents = oldSupport.nbContents + container.contents.size();
				}
			}
		}

        // boucler sur les containers supports modifiés plus haut..
		for (Map.Entry<String,ContainerSupport> support : mapSupports.entrySet()) {
			
			//Logger.debug("[updateSupportFromUpdatedContainers] get from mongo ContainerSupport: "+ support.getKey() );
			ContainerSupport dbCs = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, support.getKey());

			ContainerSupport updatedCs = support.getValue();

			// FDS: la constante ngl.app.models.Constants NGL_DATA_USER n'est pas accessible ici...
			updatedCs.traceInformation = InstanceHelpers.getUpdateTraceInformation(dbCs.traceInformation, "ngl-data");

			// FDS 22/01/2016 Pourquoi ce test ??? pourquoi tout containerSupport modifié n'est pas automatiquement supprimé ???
			//     NOTE: projectCodes et sampleCodes sont des listes: il faut faire les tests d'inclusions dans les 2 sens !
			// GA 02/11/2015 prise en compte de la modification du storageCode ( !!peut etre null )
			if (   !dbCs.projectCodes.containsAll(updatedCs.projectCodes)
				|| !updatedCs.projectCodes.containsAll(dbCs.projectCodes) 
				|| !dbCs.sampleCodes.containsAll(updatedCs.sampleCodes) 
				|| !updatedCs.sampleCodes.containsAll(dbCs.sampleCodes) 
				|| !dbCs.fromTransformationTypeCodes.containsAll(updatedCs.fromTransformationTypeCodes) 
				|| !updatedCs.fromTransformationTypeCodes.containsAll(dbCs.fromTransformationTypeCodes) 
				|| (null != updatedCs.storageCode && !updatedCs.storageCode.equals(dbCs.storageCode))
				|| (null != dbCs.storageCode && !dbCs.storageCode.equals(updatedCs.storageCode))) {
				
				//Logger.debug("[updateSupportFromUpdatedContainers] detete from mongo ContainerSupport: "+ support.getKey() );
				MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, support.getKey());
				
				//Logger.debug("[updateSupportFromUpdatedContainers] save to mongo ContainerSupport: "+ updatedCs.code);
				InstanceHelpers.save(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, updatedCs, contextValidation, true);
			}
			// pour debug..
			//else { Logger.debug("[updateSupportFromUpdatedContainers] NOT deleting an NOT saving in mongo "+ support.getKey() );}
		}
	}

	public static Set<Content> contentFromSampleCode(List<Content> contents,
			String sampleCode) {
		Set<Content> contentsFind=new HashSet<Content>();
		for(Content content:contents){
			if(content.sampleCode.equals(sampleCode)){
				contentsFind.add(content);
			}
		}
		return contentsFind;
	}

	public static void save(Container outputContainer,
			ContextValidation contextValidation) {
		contextValidation.addKeyToRootKeyName("container["+outputContainer.code+"]");
		contextValidation.setCreationMode();
		InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME,outputContainer, contextValidation);
		contextValidation.removeKeyFromRootKeyName("container["+outputContainer.code+"]");
	}
	
	public static Double getEquiPercentValue(int size){
		BigDecimal p = (new BigDecimal(100.00/size)).setScale(2, RoundingMode.HALF_UP);						
		return p.doubleValue();
	}
}
