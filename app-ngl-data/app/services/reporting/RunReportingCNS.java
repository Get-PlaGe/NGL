package services.reporting;

import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;
import services.instance.ImportDataUtil;

public class RunReportingCNS {
	
	public RunReportingCNS() {
		new ReportingCNS(Duration.create(ImportDataUtil.nextExecutionInSeconds(8, 0),TimeUnit.SECONDS)
				,Duration.create(1,TimeUnit.DAYS));
	}

}




