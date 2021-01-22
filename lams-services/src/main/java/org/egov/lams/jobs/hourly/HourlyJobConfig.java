package org.egov.lams.jobs.hourly;

import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
public class HourlyJobConfig {

	@Value("${bel.esign.cron.schedule}")
	private String schedule;
    @Bean
    JobDetailFactoryBean processStatusUpdateJob() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(HourlyJob.class);
        jobDetailFactory.setGroup("esign-clear-maps");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean
    @Autowired
    CronTriggerFactoryBean processStatusUpdateTrigger(JobDetail processStatusUpdateJob) {
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(processStatusUpdateJob);
        cronTriggerFactoryBean.setCronExpression(schedule);
        cronTriggerFactoryBean.setGroup("esign-clear-maps");
        return cronTriggerFactoryBean;
    }

}
