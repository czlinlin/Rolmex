package com.mossle.pim.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by wanghan on 2017\8\16 0016.
 * 汇报抄送 实体类
 */
@Entity
@Table(name = "work_report_cc_presetting_node")
public class WorkReportCcPresettingNode implements java.io.Serializable {
	private static final long serialVersionUID = 0L;
	
		//主键ID
      private  Long id;
     
	 //外键ID
      private  Long presettingId;
     
	 //状态(1：正常，0：删除)
      private  String status;
     
	 //节点名称
      private  String title;
      
      //节点级别
      private  Integer nodeLevel;
     	 
	//分配策略
      private  String presettingType;
     
	 //岗位ID
      private  String positionId;
     
	 //备注
      private  String note;
     
      @Id
      //@GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(name="id")
      public  Long  getId(){
      		return  this.id;
      };
      public  void  setId(Long id){
      	this.id=id;
      }  
     
      @Column(name="presetting_id")
      public  Long  getPresettingId(){
      		return  this.presettingId;
      };
      public  void  setPresettingId(Long presettingId){
      	this.presettingId=presettingId;
      }  
     
      @Column(name="status")
      public  String  getStatus(){
      		return  this.status;
      };
      public  void  setStatus(String status){
      	this.status=status;
      }  
     
      @Column(name="title")
      public  String  getTitle(){
      		return  this.title;
      };
      public  void  setTitle(String title){
      	this.title=title;
      }  
      
      @Column(name="node_level")
      public Integer getNodeLevel() {
  		return nodeLevel;
	  	}
	  	public void setNodeLevel(Integer nodeLevel) {
	  		this.nodeLevel = nodeLevel;
	  	}
     
      @Column(name="presetting_type")
      public  String  getPresettingType(){
      		return  this.presettingType;
      };
      public  void  setPresettingType(String presettingType){
      	this.presettingType=presettingType;
      }  
     
      @Column(name="positionId")
      public  String  getPositionId(){
      		return  this.positionId;
      };
      public  void  setPositionId(String positionId){
      	this.positionId=positionId;
      }  
     
      @Column(name="note")
      public  String  getNote(){
      		return  this.note;
      };
      public  void  setNote(String note){
      	this.note=note;
      }  
}
