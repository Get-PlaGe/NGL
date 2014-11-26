package validation.container.instance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.BusinessValidationHelper;
import validation.utils.ValidationConstants;

public class ContentValidationHelper extends CommonValidationHelper {

	public static void validateSampleTypeCode(String typeCode,
			ContextValidation contextValidation) {
		BusinessValidationHelper.validateRequiredDescriptionCode(contextValidation, typeCode, "typeCode",SampleType.find,false);
	}
	
	public static void validateSampleCode(String sampleCode,
			ContextValidation contextValidation) {
		BusinessValidationHelper.validateRequiredInstanceCode(contextValidation, sampleCode, "sampleCode", Sample.class, InstanceConstants.SAMPLE_COLL_NAME, false);

	}
	
	public static void validatePercentageContent(Double percentage, ContextValidation contextValidation){
		Pattern ptn = Pattern.compile("\\d*\\.\\d\\d$");
		Matcher mtr = ptn.matcher(String.valueOf(percentage));
		//pecentage is mandatory
		if(percentage<0.0 ||percentage>100.00 || !mtr.matches()){
			contextValidation.addErrors("percentage", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, percentage);
		}
	}
	

}