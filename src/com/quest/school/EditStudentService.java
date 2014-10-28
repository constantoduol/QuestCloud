/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quest.school;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quest.access.common.UniqueRandom;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.control.Server;
import com.quest.access.useraccess.Serviceable;
import com.quest.access.useraccess.services.Message;
import com.quest.access.useraccess.services.annotations.Endpoint;
import com.quest.access.useraccess.services.annotations.WebService;
import com.quest.access.useraccess.verification.UserAction;
import com.quest.servlets.ClientWorker;

/**
 *
 * @author connie
 */
/**
@WebService (name = "edit_student_service", level = 10, privileged = "yes")
public class EditStudentService implements Serviceable {
    private static Datastore db;
    @Override
    public void service() {
    
    }

    @Override
    public void onCreate() {
       
    }

    @Override
    public void onStart() {
      try {
            //get an instance of our database
            db=Datastore.getExistingDatabase("school_data");
        } catch (NonExistentDatabaseException ex) {
            //create it if does not exist
            onCreate();
        }  
    }
    
    @Endpoint(name="all_fields",shareMethodWith = {"edit_mark_service","student_service","mark_service","account_service"})
    public void allFields(Server serv,ClientWorker worker){
     JSONObject requestData=worker.getRequestData();
        String type =requestData.optString("type");
        String metaTableName="";
        if(type.equals("teacher")){
            metaTableName="TEACHER_FIELD_META_DATA";
        }
        else if(type.equals("student")){
            metaTableName="STUDENT_FIELD_META_DATA";
        }
        else  if(type.equals("class")){
            metaTableName="CLASS_FIELD_META_DATA";
        }
        else  if(type.equals("subject")){
          metaTableName="SUBJECT_FIELD_META_DATA";
        }
         else  if(type.equals("stream")){
          metaTableName="STREAM_FIELD_META_DATA";
        }
        else  if(type.equals("paper")){
          metaTableName="PAPER_FIELD_META_DATA";
        }
        else  if(type.equals("exam")){
          metaTableName="EXAM_FIELD_META_DATA";
        }
        JSONObject data = db.query("SELECT * FROM "+metaTableName+" ORDER BY CREATED ASC");
        serv.messageToClient(worker,data);
    }
    
    
    @Endpoint(name="save_fields",shareMethodWith = {"edit_mark_service"})
    public void saveFields(Server serv,ClientWorker worker){
      JSONObject requestData=worker.getRequestData();
      JSONArray required = requestData.optJSONArray("required");
      JSONArray fieldNames = requestData.optJSONArray("field_names");
      JSONArray oldFieldNames = requestData.optJSONArray("old_field_names");
      JSONArray fieldData = requestData.optJSONArray("field_data");
      String type=requestData.optString("type");
      for(int x=0; x<fieldNames.length(); x++){
            try {
                // if old field name is empty then this is a new field
                String oldFieldName=oldFieldNames.getString(x);
                String fieldName=fieldNames.getString(x);
                Integer isRequired=required.getInt(x);
                String dataType=fieldData.optString(x);
                if(oldFieldName.isEmpty()){
                   // this is a completely new field
                    addField(fieldName,type,dataType,isRequired,serv, worker);
                }
                else{
                   //this field already exists so remove the old one and add the new one
                   //do not remove fields that already existed anyway
                   if(!fieldName.isEmpty() && !oldFieldName.equals(fieldName) ){
                       removeField(oldFieldName,type,serv, worker);
                       addField(fieldName,type,dataType, isRequired, serv, worker);
                   }
                }
            } catch (JSONException ex) {
                Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
            }
         
      }
      serv.messageToClient(worker, Message.SUCCESS);
    }
    
     private void removeField(String name,String type,Server serv,ClientWorker worker){
        String metaTableName="";
        String tableName="";
        if(type.equals("teacher")){
            metaTableName="TEACHER_FIELD_META_DATA";
            tableName="TEACHER_DATA";
        }
        else if(type.equals("student")){
            metaTableName="STUDENT_FIELD_META_DATA";
            tableName="STUDENT_DATA";
        }
        else  if(type.equals("class")){
            metaTableName="CLASS_FIELD_META_DATA";
            tableName="CLASS_DATA";
        }
        else  if(type.equals("subject")){
          metaTableName="SUBJECT_FIELD_META_DATA";  
          tableName="SUBJECT_DATA";
        }
         else  if(type.equals("stream")){
          metaTableName="STREAM_FIELD_META_DATA";  
          tableName="STREAM_DATA";
        }
        else  if(type.equals("paper")){
          metaTableName="PAPER_FIELD_META_DATA";  
          tableName="PAPER_DATA";
        }
        else  if(type.equals("exam")){
          metaTableName="EXAM_FIELD_META_DATA";  
          tableName="EXAM_DATA";
        }
        
        
        try {
            boolean fieldExists =db.ifValueExists(name,metaTableName,"FIELD_NAME");
            if(!fieldExists){
               return; 
            }
            UserAction action=new UserAction(serv, worker, "EDIT_STUDENT_SERVICE REMOVE FIELD: "+name+" from "+metaTableName);
            db.getTable(tableName).getColumn(name.replace(" ", "_")).dropColumn();
            Datastore.executeQuery("DELETE FROM "+metaTableName+" WHERE FIELD_NAME=?", db.getDatabaseName(), name);
            action.saveAction();
        } catch (Exception ex) {
            Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
      private void addField(String name,String type,String dataType,Integer required,Server serv, ClientWorker worker){
        String metaTableName="";
        String tableName="";
        if(type.equals("teacher")){
            metaTableName="TEACHER_FIELD_META_DATA";
            tableName="TEACHER_DATA";
        }
        else if(type.equals("student")){
            metaTableName="STUDENT_FIELD_META_DATA";
            tableName="STUDENT_DATA";
        }
        else  if(type.equals("class")){
            metaTableName="CLASS_FIELD_META_DATA";
            tableName="CLASS_DATA";
        }
        else  if(type.equals("subject")){
          metaTableName="SUBJECT_FIELD_META_DATA";  
          tableName="SUBJECT_DATA";
        }
        else  if(type.equals("stream")){
          metaTableName="STREAM_FIELD_META_DATA";  
          tableName="STREAM_DATA";
        }
         else  if(type.equals("paper")){
          metaTableName="PAPER_FIELD_META_DATA";  
          tableName="PAPER_DATA";
        }
        else  if(type.equals("exam")){
          metaTableName="EXAM_FIELD_META_DATA";  
          tableName="EXAM_DATA";
        }
        try {
            boolean fieldExists =db.ifValueExists(name,metaTableName,"FIELD_NAME");
            if(fieldExists){
               return; 
            }
            UniqueRandom rand=new UniqueRandom(8);
            UserAction action=new UserAction(serv, worker, "EDIT_STUDENT_SERVICE ADD FIELD: "+name+" to "+metaTableName);
            db.doInsert(metaTableName, new String[]{rand.nextMixedRandom(),name,required.toString(),dataType,"!NOW()"});
            if(dataType.equals("unique")){
              dataType="text";   
            }
            db.getTable(tableName).addColumn(name.replace(" ", "_"), dataType);
            action.saveAction();
        } catch (Exception ex) {
            Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
     
    @Endpoint(name="remove_field",shareMethodWith = {"edit_mark_service"})
    public void removeFieldName(Server serv,ClientWorker worker){
        String name=worker.getRequestData().optString("name");
        String type=worker.getRequestData().optString("type");
        removeField(name,type, serv, worker);
        serv.messageToClient(worker, Message.SUCCESS);
    }
    
    /**
     *  we need a way of effectively archiving students who have left the school
     *  so that we can view their marks and data even after they have left the school
     *  
     * @param serv
     * @param worker 
     */
/**

    @Endpoint(name="graduate_students")
    public void graduateStudents(Server serv,ClientWorker worker){
       try{
        JSONObject data = worker.getRequestData();
        JSONArray currentStreamIds=data.optJSONArray("class_ids");
        JSONArray newStreamIds = data.optJSONArray("new_class_ids");
        UserAction action=new UserAction(serv, worker, "GRADUATE STUDENTS");
        //update references in exam tables
        //in account records
        //in account data
        UniqueRandom rand =new UniqueRandom(7);
        UniqueRandom rand1 =new UniqueRandom(6);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        for(int x=0; x<currentStreamIds.length(); x++){
           String oldStreamId = currentStreamIds.optString(x);
           String newStreamId=newStreamIds.optString(x);
           String newClassId = db.query("SELECT CLASS_ID FROM CLASS_STREAMS WHERE STREAM_ID = ?",newStreamId).optJSONArray("CLASS_ID").optString(0);
           String oldClassId = db.query("SELECT CLASS_ID FROM CLASS_STREAMS WHERE STREAM_ID = ?",oldStreamId).optJSONArray("CLASS_ID").optString(0);
           String archiveClassId = "AR_"+rand.nextMixedRandom();
           boolean exists = db.ifValueExists(archiveClassId,"MARK_SHEET_DESIGN","CLASS_ID");
           if(!exists){
                JSONObject marksheetDesign = db.query()
                   .select("SUBJECT_ID,DESIGN")
                   .from("MARK_SHEET_DESIGN")
                   .where("CLASS_ID = '"+oldClassId+"'")
                   .execute();
                JSONArray subjectIds = marksheetDesign.optJSONArray("SUBJECT_ID");
                JSONArray design = marksheetDesign.optJSONArray("DESIGN");
                for(int z = 0; z<subjectIds.length(); z++){
                   db.query()
                           .insert("MARK_SHEET_DESIGN")
                           .columns("ID,CLASS_ID,SUBJECT_ID,DESIGN,CREATED")
                           .values("'"+rand1.nextMixedRandom()+"','"+archiveClassId+"', '"+subjectIds.optString(z)+"', '"+design.optString(z)+"',NOW()")
                           .execute();
                }
           }
           if(newStreamId.equals("archive")){
              //this is a request to archive the records of this stream
              //so we will generate a new archive stream id and archive class id
              
              String currentClassName = db.query()
                      .select("CLASS_NAME")
                      .from("CLASS_DATA")
                      .where("ID='"+oldClassId+"'")
                      .execute()
                      .optJSONArray("CLASS_NAME")
                      .optString(0);
              String currentStreamName = db.query()
                      .select("STREAM_NAME")
                      .from("STREAM_DATA")
                      .where("ID='"+oldStreamId+"'")
                      .execute()
                      .optJSONArray("STREAM_NAME")
                      .optString(0);
              String archiveClassName = "AR "+year+" "+currentClassName; //AR 2014 8
              String archiveStreamId = "AR_"+rand.nextMixedRandom();
              String archiveStreamName = "AR "+year+" "+currentStreamName; //AR 2014 8A
              boolean classExists = db.ifValueExists(archiveClassName,"CLASS_DATA","CLASS_NAME");
              boolean streamExists = db.ifValueExists(archiveClassName,"STREAM_DATA","STREAM_NAME");
              if(!classExists){
              db.query()
                      .insert("CLASS_DATA")
                      .columns("ID,CLASS_NAME,CREATED")
                      .values("'"+archiveClassId+"','"+archiveClassName+"',NOW()")
                      .execute();
              }
              if(!streamExists){
                db.query()
                      .insert("STREAM_DATA")
                      .columns("ID,STREAM_NAME,CREATED")
                      .values("'"+archiveStreamId+"','"+archiveStreamName+"',NOW()")
                      .execute();
                  db.query()  //associate this stream with this class
                      .insert("CLASS_STREAMS")
                      .columns("CLASS_ID,STREAM_ID")
                      .values("'"+archiveClassId+"','"+archiveStreamId+"'")
                      .execute();
                   
                 JSONArray currentSubjects = db.query()   //create the specified subjects for this stream
                      .select("SUBJECT_ID")
                      .from("STREAM_SUBJECTS")
                      .where("STREAM_ID='"+oldStreamId+"'")
                      .execute()
                      .optJSONArray("SUBJECT_ID");
                for(int y=0; y<currentSubjects.length(); y++){
                    db.query()
                      .insert("STREAM_SUBJECTS")
                      .columns("STREAM_ID,SUBJECT_ID")
                      .values("'"+archiveStreamId+"','"+currentSubjects.optString(y)+"'")
                      .execute(); 
                }
              }
              
                //update the formulas for this classes
                newStreamId = archiveStreamId;
                newClassId = archiveClassId;
           }     
           
          db.execute("UPDATE STUDENT_DATA SET STUDENT_STREAM='"+newStreamId+"' , STUDENT_CLASS='"+newClassId+"' WHERE STUDENT_STREAM='"+oldStreamId+"'");
          db.execute("UPDATE ACCOUNT_RECORDS SET STREAM_ID='"+newStreamId+"', CLASS_ID='"+newClassId+"' WHERE STREAM_ID='"+oldStreamId+"'");
          db.execute("UPDATE MARK_DATA SET STREAM_ID='"+newStreamId+"' , CLASS_ID='"+newClassId+"' WHERE STREAM_ID='"+oldStreamId+"'");
        }
        action.saveAction();
        serv.messageToClient(worker,Message.SUCCESS);
       }
       catch(Exception e){
          e.printStackTrace();
       }
    }
    
}

*/