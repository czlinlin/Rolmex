package com.dwcx.scheduler;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mossle.core.spring.ApplicationContextHelper;
import com.mossle.operation.service.WSApplyService;

public class MsgJob implements Job {
	public MsgJob() {
	}
	
	WSApplyService wSApplyService =null;
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if(wSApplyService==null)
			wSApplyService = ApplicationContextHelper.getBean(WSApplyService.class);
		System.out.println("============麦联消息作业 -begin: " + new Date()+"============");
		wSApplyService.PushAPPMsg();
		System.out.println("============麦联消息作业 -end: " + new Date()+"============");
	}
}