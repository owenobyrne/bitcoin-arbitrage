package com.owenobyrne.bitcoinarbitrage.scheduler;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.owenobyrne.bitcoinarbitrage.Arbitrator;

@DisallowConcurrentExecution
public class CheckArbitragePossibilityJob extends QuartzJobBean {
	private static Logger log = Logger.getLogger(CheckArbitragePossibilityJob.class);
	private ApplicationContext ctx;
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		ctx = applicationContext;
	}

	protected void executeInternal(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		try {
			
			log.info("running scheduled task ----------------------------------");
			Arbitrator arbitrator = (Arbitrator) ctx.getBean("arbitrator");
			arbitrator.checkArbitragePossibility();
			
		} catch (Throwable thro) {
			System.out.println(ExceptionUtils.getFullStackTrace(thro));
		}
	}

}
