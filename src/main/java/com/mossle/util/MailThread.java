package com.mossle.util;

import com.mossle.user.service.SalaryService;
/**
 * 2018/12/18
 * @author ckx
 *
 */
public class MailThread implements Runnable{

	private String ids;
	private String emailPassword;
	private String mailServer;
	private SalaryService salaryService;
	
	@Override
	public void run() {
        this.salaryService = BeanContext.getApplicationContext().getBean(SalaryService.class);
        try {
			salaryService.sendPayslip(mailServer,ids, emailPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}

	public void setMailServer(String mailServer) {
		this.mailServer = mailServer;
	}

	
	
}
