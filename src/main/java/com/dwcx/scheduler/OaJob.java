package com.dwcx.scheduler;

import java.util.Date;

import javax.annotation.Resource;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mossle.core.spring.ApplicationContextHelper;
import com.mossle.operation.service.WSApplyService;
import com.mossle.spi.process.InternalProcessConnector;

public class OaJob implements Job {
	public OaJob() {
	}
	
	WSApplyService wSApplyService=null;
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if(wSApplyService==null)
			wSApplyService = ApplicationContextHelper.getBean(WSApplyService.class);
		System.out.println("麦联作业 -begin: " + new Date());
		wSApplyService.SendMsg();
		System.out.println("麦联作业 -end: " + new Date());
	}

}
