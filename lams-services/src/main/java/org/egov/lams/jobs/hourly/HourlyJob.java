package org.egov.lams.jobs.hourly;

import java.util.Calendar;

import org.egov.lams.util.PdfSignUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HourlyJob implements Job {

	@Value("${bel.session.max.time.diffinmilli:300000l}")
	private Long esignMaxTimeMilli;
	
	@Autowired
	PdfSignUtils pdfSignUtils;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) {
		log.info(" esignMaxTimeMilli " + esignMaxTimeMilli);
		pdfSignUtils.checkandupdatemap();
	}
}
