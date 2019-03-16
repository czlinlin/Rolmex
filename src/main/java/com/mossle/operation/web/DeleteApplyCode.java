package com.mossle.operation.web;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.core.page.Page;
import com.mossle.operation.persistence.domain.CodeEntity;
import com.mossle.operation.persistence.manager.CodeManager;
/** 
 * @author  cz 
 * @version 2017年9月29日
 * 用户发起流程时从oa_bpm_code表中清除userid 
 */

public class DeleteApplyCode {
	
	 private CodeManager codeManager;
	
	 public void deleteApplyCode(String receiptNumber){

		 CodeEntity newCodeEntity = new CodeEntity();
		 
		 
		 List<CodeEntity> codeEntity = codeManager.findBy("receiptNumber",receiptNumber);
		 
		 
			for(CodeEntity c :codeEntity){
				newCodeEntity.setCode(c.getCode()); 
				newCodeEntity.setCreateTime(c.getCreateTime());
				newCodeEntity.setId(c.getId());
				newCodeEntity.setReceiptNumber(receiptNumber);
				newCodeEntity.setShortName(c.getShortName());
				newCodeEntity.setUserID("");
			}
			
			codeManager.save(newCodeEntity);
		 }

	 	@Resource
	    public void setCodeManager(CodeManager codeManager) {
	        this.codeManager = codeManager;
	    }
	

}
