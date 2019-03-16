package com.dwcx.scheduler;

import com.mossle.cms.service.RenderService;
import com.mossle.core.spring.ApplicationContextHelper;
import com.mossle.operation.service.WSApplyService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

public class CmsJob implements Job {
	public CmsJob() {
	}

	RenderService renderService =null;
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if(renderService==null)
			renderService = ApplicationContextHelper.getBean(RenderService.class);
		System.out.println("============公告定时消息 -begin: " + new Date()+"============");
		renderService.PushCmsMsg();
		System.out.println("============公告定时消息 -end: " + new Date()+"============");
	}
}