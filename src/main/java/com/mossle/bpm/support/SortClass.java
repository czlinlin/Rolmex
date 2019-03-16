package com.mossle.bpm.support;
import java.util.Comparator;  

import com.mossle.humantask.persistence.domain.UnfinishProcessInstance;
/**
 * @author chengze:
 * @version 创建时间：2017年9月27日 下午6:02:52
 * 类说明
 */


public class SortClass implements Comparator{  
    public int compare(Object arg0,Object arg1){  
    	UnfinishProcessInstance user0 = (UnfinishProcessInstance)arg0;  
    	UnfinishProcessInstance user1 = (UnfinishProcessInstance)arg1;  
        int flag = user0.getStartTime().compareTo(user1.getStartTime());  
        return flag;  
    }  
}  