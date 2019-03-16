package com.dwcx.scheduler;

import com.mossle.cms.service.RenderService;
import com.mossle.core.spring.ApplicationContextHelper;
import com.mossle.user.service.PersonInfoService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class QuitJob implements Job {
	private static Logger logger = LoggerFactory.getLogger(QuitJob.class);
	public QuitJob() {
	}

	PersonInfoService personInfoService =null;
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if(personInfoService==null)
			personInfoService = ApplicationContextHelper.getBean(PersonInfoService.class);
		logger.info("============查询修改离职人员状态 -begin: " + new Date()+"============");
		personInfoService.updateQuit();
		logger.info("============查询修改离职人员状态 -end: " + new Date()+"============");
	}
}