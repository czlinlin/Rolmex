/**
 *
 */
package com.dwcx.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Bing
 */
@SuppressWarnings("serial")
public class JobServlet extends HttpServlet {

    private String autostartup;

    private String msgJobTime = "0 * * * * ?";
    private String cmsMsgJobTime = "0 * * * * ?";
    private String quitJobTime = "0 0 2 * * ?";

    public void init() throws ServletException {
        System.out.println("JobServlet has been initialized!");

        ResourceBundle rb = ResourceBundle.getBundle("application", Locale.getDefault());
        autostartup = rb.getString("quartz.autostartup");
        msgJobTime = rb.getString("quartz.msgJobTime");
        //公告定时任务时间设置
        cmsMsgJobTime = rb.getString("quartz.cmsMsgJobTime");
        //查询修改离职人员
        quitJobTime = rb.getString("quartz.quitPersonTime");
        System.out.println("JobServlet 初始化参数 autostartup=" + autostartup);
        if (autostartup.equals("true")) {
            try {
                run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void run() throws Exception {
        System.out.println("------- Initializing ----------------------");
        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();
        System.out.println("------- Initialization Complete -----------");

        //region 关于直销流程发送短信或者调用直销的定时任务
        System.out.println("------- Scheduling Job -------------------");
        // define the job and tie it to our Job class
        JobDetail job = newJob(OaJob.class).withIdentity("job1", "group1").build();
        CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1")
                .withSchedule(cronSchedule("0 * * * * ?")).build();

        Date ft = sched.scheduleJob(job, trigger);

        // Start up the scheduler (nothing can actually run until the
        // scheduler has been started)
        System.out.println("------- Scheduling Job Start -------------------");
        //endregion

        //region 系统发送消息的定时任务
        System.out.println("------- Scheduling MsgJob -------------------");
        JobDetail jobMsg = newJob(MsgJob.class).withIdentity("jobMsg", "groupMsg").build();
        CronTrigger triggerMsg = newTrigger().withIdentity("jobMsg", "groupMsg")
                .withSchedule(cronSchedule(msgJobTime)).build();

        Date ftMsg = sched.scheduleJob(jobMsg, triggerMsg);
        // System.out.println(job.getKey() + " will run at: " + runTime);
        System.out.println(jobMsg.getKey() + " has been scheduled to run at: " + ftMsg + " and repeat based on expression: "
                + triggerMsg.getCronExpression());

        // Start up the scheduler (nothing can actually run until the
        // scheduler has been started)
        //endregion

        //regin 公告发布定时任务

    /*    System.out.println("------- Scheduling CmsJob -------------------");
        JobDetail jobCms = newJob(CmsJob.class).withIdentity("jobCms", "group2").build();
        CronTrigger triggerCms = newTrigger().withIdentity("trigger1", "group2")
                .withSchedule(cronSchedule(cmsMsgJobTime)).build();
        Date cmsMsg = sched.scheduleJob(jobCms, triggerCms);
        System.out.println(jobCms.getKey() + " has been scheduled to run at: " + cmsMsg + " and repeat based on expression: "
                + triggerCms.getCronExpression());
*/
        
        //region 
        System.out.println("------- Scheduling QuitJob Start-------------------");
        // define the job and tie it to our Job class
        JobDetail jobQuit = newJob(QuitJob.class).withIdentity("jobQuit", "groupQuit").build();
        CronTrigger triggerQuit = newTrigger().withIdentity("triggerQuit", "groupQuit")
                .withSchedule(cronSchedule(quitJobTime)).build();

        Date quitMsg = sched.scheduleJob(jobQuit, triggerQuit);

        System.out.println(jobQuit.getKey() + " has been scheduled to run at: " + quitMsg + " and repeat based on expression: "
                + triggerQuit.getCronExpression());
        System.out.println("------- Scheduling QuitJob End -------------------");
        //endregion
        
        
        sched.start();
    }

}
