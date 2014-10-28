/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quest.school;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
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

@WebService (name = "edit_mark_service", level = 10, privileged = "yes")
public class EditMarkService implements Serviceable {
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
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
     @Endpoint(name="create_exam")
    public void createExam(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            JSONArray fieldNames = requestData.optJSONArray("field_names");
            JSONArray fieldValues = requestData.optJSONArray("field_values");
            JSONArray fieldData = requestData.optJSONArray("field_data");
            String examName=requestData.optString("exam_name").toUpperCase();
            String examDeadline=requestData.optString("exam_deadline");
            UniqueRandom rand=new UniqueRandom(8);
            String examId=rand.nextMixedRandom();
            boolean ifExists=db.ifValueExists(examName, "EXAM_DATA", "EXAM_NAME");
            if(ifExists){
               worker.setReason("Exam already exists");
               serv.messageToClient(worker, Message.FAIL);
               return;  
            }
            StringBuilder builder=new StringBuilder("INSERT INTO EXAM_DATA (ID,EXAM_NAME,EXAM_DEADLINE,  ");
            if(fieldNames.length()==0){
                builder=new StringBuilder("INSERT INTO EXAM_DATA (ID,EXAM_NAME,EXAM_DEADLINE) VALUES('"+examId+"', '"+examName+"','"+examDeadline+"')");
            }
            else{
              for(int x=0; x<fieldNames.length(); x++){
                  // save only a value that exists
               String colName=fieldNames.optString(x).replace(" ", "_");
               if(x==fieldNames.length()-1){
                 builder.append(colName).append(") VALUES('").append(examId).append("', '").append(examName).append("','").append(examDeadline).append("',  ");  
               }
               else{
                 builder.append(colName).append(", ");
               }
             }
             for(int x=0; x<fieldNames.length(); x++){
                  // save only a value that exists
                     String value=fieldValues.optString(x);
                      String dataType=fieldData.optString(x);
                      String fieldName=fieldNames.optString(x).replace(" ","_");
                       if(dataType.equals("unique")){
                            boolean exists=db.ifValueExists(value,"EXAM_DATA", fieldName);   
                            if(exists){
                                worker.setReason("Field "+fieldName+" must be a unique value");
                                serv.messageToClient(worker, Message.FAIL);
                                return;
                    }
                 }
                      if(!dataType.equals("long")){
                          value="'"+value+"'";
                      }
                      if(x==fieldNames.length()-1){
                          builder.append(value).append(") ");
                      }
                      else{
                          builder.append(value).append(", ");
                      }
               }
            }
           UserAction action=new UserAction(serv, worker, "CREATE EXAM "+examName);
           action.saveAction();
           db.execute(builder.toString());
           serv.messageToClient(worker, Message.SUCCESS);
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
     @Endpoint(name="save_mark_sheet_design")
    public void saveMarkSheetDesign(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String classId=requestData.optString("class_id");  
            JSONArray subjectIds = requestData.optJSONArray("subject_ids");
            JSONArray designValues=requestData.optJSONArray("design_values");
            String grandFormula = requestData.optString("grand_formula");
            String averageFormula = requestData.optString("average_formula");
            String round = requestData.optString("round_precision");
            UserAction action=new UserAction(serv, worker, "SAVE MARK SHEET DESIGN FOR "+classId);
            UniqueRandom rand=new UniqueRandom(6);
            for(int x=0; x<subjectIds.length(); x++){
               String subjectId=subjectIds.optString(x);
               String designValue=designValues.optString(x);
              
                 db.execute("DELETE FROM MARK_SHEET_DESIGN WHERE CLASS_ID='"+classId+"' AND SUBJECT_ID='"+subjectId+"'");
                  //insert a new value
                 db.doInsert("MARK_SHEET_DESIGN",new String[]{rand.nextMixedRandom(),classId,subjectId,designValue,"!NOW()"});
               
            }
            //save for the average and grand total
        
               db.execute("DELETE FROM MARK_SHEET_DESIGN  WHERE CLASS_ID='"+classId+"' AND SUBJECT_ID='grand'");
               db.execute("DELETE FROM MARK_SHEET_DESIGN  WHERE CLASS_ID='"+classId+"' AND SUBJECT_ID='average'"); 
               db.execute("DELETE FROM MARK_SHEET_DESIGN  WHERE CLASS_ID='"+classId+"' AND SUBJECT_ID='round'"); 
          
               db.doInsert("MARK_SHEET_DESIGN",new String[]{rand.nextMixedRandom(),classId,"grand",grandFormula,"!NOW()"});
               db.doInsert("MARK_SHEET_DESIGN",new String[]{rand.nextMixedRandom(),classId,"average",averageFormula,"!NOW()"});
               db.doInsert("MARK_SHEET_DESIGN",new String[]{rand.nextMixedRandom(),classId,"round",round,"!NOW()"});
            
            action.saveAction();
            serv.messageToClient(worker,Message.SUCCESS);
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
     
    @Endpoint(name="save_extra_mark_sheet_columns")
    public void saveExtraMarkSheetColumns(Server serv,ClientWorker worker){
         try {
             JSONObject requestData = worker.getRequestData(); 
             JSONArray fields = requestData.optJSONArray("fields");
             StringBuilder builder=new StringBuilder();
             UserAction action =new UserAction(serv, worker,"SAVE EXTRA MARK SHEET COLUMNS");
             for(int x=0; x<fields.length(); x++){
                if(x==(fields.length()-1)){  
                   builder.append(fields.optString(x));  
                }
                else{
                   builder.append(fields.optString(x)).append(",");
                }
             }
             UniqueRandom rand=new UniqueRandom(10);
             db.execute("DELETE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='extra_mark_sheet_field'");
             db.doInsert("REPORT_FORM_DATA", new String[]{rand.nextRandom(),"extra_mark_sheet_field","",builder.toString(),"!NOW()"});
             action.saveAction();
             serv.messageToClient(worker,Message.SUCCESS);
         } catch (Exception ex) {
             Logger.getLogger(EditMarkService.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
      
   @Endpoint(name="save_address_block")
    public void saveAddressBlock(Server serv,ClientWorker worker){
         try {
             JSONObject requestData = worker.getRequestData();
             String scAddress = requestData.optString("sc_address"); 
             String scEmail = requestData.optString("sc_email");
             String wSite= requestData.optString("sc_web");
             String scTel = requestData.optString("sc_tel");
             String scName = requestData.optString("sc_name");
             String scExtra = requestData.optString("sc_extra_details");
             UniqueRandom rand=new UniqueRandom(10);
             UserAction action=new UserAction(serv, worker, "SAVE REPORT FORM ADDRESS BLOCK");
               db.execute("DELETE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='sc_address'");
               db.execute("DELETE FROM REPORT_FORM_DATA  WHERE FIELD_TYPE='sc_email'");
               db.execute("DELETE FROM REPORT_FORM_DATA  WHERE FIELD_TYPE='sc_web'");
               db.execute("DELETE FROM REPORT_FORM_DATA  WHERE FIELD_TYPE='sc_tel'");
               db.execute("DELETE FROM REPORT_FORM_DATA  WHERE FIELD_TYPE='sc_name'");
               db.execute("DELETE FROM REPORT_FORM_DATA  WHERE FIELD_TYPE='sc_extra_details'");
               if(scAddress.trim().length()>0){
                  db.doInsert("REPORT_FORM_DATA",new String[]{rand.nextRandom(),"sc_address","",scAddress,"!NOW()"});  
               }
               if(scEmail.trim().length()>0){
                  db.doInsert("REPORT_FORM_DATA",new String[]{rand.nextRandom(),"sc_email","",scEmail,"!NOW()"});  
               }
               if(wSite.trim().length()>0){
                  db.doInsert("REPORT_FORM_DATA",new String[]{rand.nextRandom(),"sc_web","",wSite,"!NOW()"});   
               }
               if(scTel.trim().length()>0){
                 db.doInsert("REPORT_FORM_DATA",new String[]{rand.nextRandom(),"sc_tel","",scTel,"!NOW()"});  
               }
               if(scName.trim().length()>0){
                  db.doInsert("REPORT_FORM_DATA",new String[]{rand.nextRandom(),"sc_name","",scName,"!NOW()"});  
               }
               if(scExtra.trim().length()>0){
                 db.doInsert("REPORT_FORM_DATA",new String[]{rand.nextRandom(),"sc_extra_details","",scExtra,"!NOW()"});   
               }
              action.saveAction();
             serv.messageToClient(worker, "success");
         } catch (Exception ex) {
             Logger.getLogger(EditMarkService.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    @Endpoint(name="save_mean_score_details")
    public void saveMeanScoreDetails(Server serv,ClientWorker worker){
         try {
             JSONObject requestData = worker.getRequestData();
             JSONArray arr = requestData.optJSONArray("mean_score_details");
             db.execute("DELETE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='mean_score'");
             UniqueRandom rand=new UniqueRandom(10);
             UserAction action=new UserAction(serv, worker, "SAVE REPORT FORM MEAN SCORE DETAILS");
             for(int x=0; x<arr.length(); x++){
                JSONObject obj = arr.optJSONObject(x); 
                String start=obj.optString("start");
                String stop=obj.optString("stop");
                String grade=obj.optString("grade");
                // start,stop,grade,comments
                StringBuilder builder=new StringBuilder();
                builder.append(start).append(",").append(stop).append(",").append(grade).append(",").append(" ");
                db.doInsert("REPORT_FORM_DATA", new String[]{rand.nextRandom(),"mean_score","",builder.toString(),"!NOW()"});
             }
             action.saveAction();
            serv.messageToClient(worker, "success");
         } catch (Exception ex) {
             Logger.getLogger(EditMarkService.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    @Endpoint(name="save_subject_details")
    public void saveSubjectDetails(Server serv,ClientWorker worker){
         try {
             JSONObject requestData = worker.getRequestData();
             JSONArray arr = requestData.optJSONArray("subject_details");
             UserAction action=new UserAction(serv, worker, "SAVE REPORT FORM SUBJECT DETAILS");
             db.execute("DELETE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='subject_field'");
             UniqueRandom rand=new UniqueRandom(10);
             for(int x=0; x<arr.length(); x++){
                JSONObject obj = arr.optJSONObject(x); 
                String subjectId=obj.optString("subject_id");
                String start=obj.optString("start");
                String stop=obj.optString("stop");
                String grade=obj.optString("grade");
                String comm=obj.optString("comm");
                String id=obj.optString("id");
                // start,stop,grade,comments
                StringBuilder builder=new StringBuilder();
                builder.append(start).append(",").append(stop).append(",").append(grade).append(",").append(comm).append(",").append(id);
                db.doInsert("REPORT_FORM_DATA", new String[]{rand.nextRandom(),"subject_field",subjectId,builder.toString(),"!NOW()"});
             }
             action.saveAction();
            serv.messageToClient(worker, "success");
         } catch (Exception ex) {
             Logger.getLogger(EditMarkService.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    @Endpoint(name="save_principal_details")
    public void savePrincipalDetails(Server serv,ClientWorker worker){
         try {
             JSONObject requestData = worker.getRequestData();
             JSONArray arr = requestData.optJSONArray("principal_details");
             UserAction action=new UserAction(serv, worker, "SAVE REPORT FORM PRINCIPAL DETAILS");
             db.execute("DELETE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='principal'");
             UniqueRandom rand=new UniqueRandom(10);
             for(int x=0; x<arr.length(); x++){
                JSONObject obj = arr.optJSONObject(x); 
                String start=obj.optString("start");
                String stop=obj.optString("stop");
                String comm=obj.optString("comm");
                // start,stop,grade,comments
                StringBuilder builder=new StringBuilder();
                builder.append(start).append(",").append(stop).append(",").append(" ").append(",").append(comm);
                db.doInsert("REPORT_FORM_DATA", new String[]{rand.nextRandom(),"principal","",builder.toString(),"!NOW()"});
             }
             action.saveAction();
            serv.messageToClient(worker, "success");
         } catch (Exception ex) {
             Logger.getLogger(EditMarkService.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
       
    @Endpoint(name="save_class_teacher_details")
    public void saveClassTeacherDetails(Server serv,ClientWorker worker){
         try {
             JSONObject requestData = worker.getRequestData();
             JSONArray arr = requestData.optJSONArray("class_teacher_details");
             UserAction action=new UserAction(serv, worker, "SAVE REPORT FORM CLASS TEACHER DETAILS");
             db.execute("DELETE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='class_teacher'");
             UniqueRandom rand=new UniqueRandom(10);
             for(int x=0; x<arr.length(); x++){
                JSONObject obj = arr.optJSONObject(x); 
                String start=obj.optString("start");
                String stop=obj.optString("stop");
                String comm=obj.optString("comm");
                // start,stop,grade,comments
                StringBuilder builder=new StringBuilder();
                builder.append(start).append(",").append(stop).append(",").append(" ").append(",").append(comm);
                db.doInsert("REPORT_FORM_DATA", new String[]{rand.nextRandom(),"class_teacher","",builder.toString(),"!NOW()"});
             }
             action.saveAction();
            serv.messageToClient(worker, "success");
         } catch (Exception ex) {
             Logger.getLogger(EditMarkService.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
        
    @Endpoint(name="save_student_fields")
    public void saveStudentFields(Server serv,ClientWorker worker){
         try {
             JSONObject requestData = worker.getRequestData();
             JSONArray fields = requestData.optJSONArray("fields");
             UserAction action=new UserAction(serv, worker, "SAVE REPORT FORM STUDENT FIELDS");
             db.execute("DELETE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='student_field'");
             UniqueRandom rand=new UniqueRandom(10);
             for(int x=0; x<fields.length(); x++){
                 try {
                     db.doInsert("REPORT_FORM_DATA", new String[]{rand.nextRandom(),"student_field","",fields.optString(x),"!NOW()"});
                     Thread.sleep(1000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             action.saveAction();
             serv.messageToClient(worker, "success");
         } catch (Exception ex) {
             Logger.getLogger(EditMarkService.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
     
    @Endpoint(name="update_exam")
    public void updateExam(Server serv,ClientWorker worker){
        try {
            JSONObject requestData=worker.getRequestData();
            JSONArray fieldNames = requestData.optJSONArray("field_names");
            String examId = requestData.optString("exam_id");
            String examName=requestData.optString("exam_name").toUpperCase();
            String examDeadline=requestData.optString("exam_deadline");
            JSONArray fieldValues = requestData.optJSONArray("field_values");
            JSONArray fieldData = requestData.optJSONArray("field_data");
            JSONObject oldExamName=db.query("SELECT EXAM_NAME FROM EXAM_DATA WHERE ID=?",examId);
            StringBuilder builder=new StringBuilder("UPDATE EXAM_DATA SET EXAM_NAME = '"+examName+"' , EXAM_DEADLINE='"+examDeadline+"', ");
            if(fieldNames.length()==0){
                  builder=new StringBuilder("UPDATE EXAM_DATA SET EXAM_NAME='"+examName+"', EXAM_DEADLINE='"+examDeadline+"'  WHERE ID='"+examId+"'");
                  db.execute(builder.toString());
                  serv.messageToClient(worker, Message.SUCCESS);
                  return;
              }
            for(int x=0; x<fieldNames.length(); x++){
              String fieldName=fieldNames.optString(x);
              String value=fieldValues.optString(x);
              String dataType=fieldData.optString(x);
              if(dataType.equals("unique")){  
                  boolean exists=db.ifValueExists(new String[]{examName,value},"EXAM_DATA", new String[]{"EXAM_NAME",fieldName});
                  if(exists){
                      worker.setReason("Field "+fieldName+" must be a unique value");
                      serv.messageToClient(worker, Message.FAIL);
                      return;
                      }
                   }
              if(!dataType.equals("long")){
                   value="'"+value+"'";
                }
              if(x==fieldNames.length()-1){
                   builder.append(fieldName).append("=").append(value).append(" WHERE ID='").append(examId).append("'"); 
              }
              else{
                 builder.append(fieldName).append("=").append(value).append(" ,");    
              }
            }
            String oldExamTableName=oldExamName.getJSONArray("EXAM_NAME").optString(0).replace(" ", "_");
            String newExamTableName=examName.replace(" ", "_");
            db.execute(builder.toString());
            UserAction action=new UserAction(serv, worker, "UPDATE EXAM FROM "+oldExamTableName+" TO "+newExamTableName);
            action.saveAction();
            serv.messageToClient(worker, Message.SUCCESS);
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="delete_exam")
    public void deleteExam(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String examId = requestData.optString("exam_id");
            //we archive the exam by putting it in the EXAM_ARCHIVE_DATA table and removing it from the EXAM_DATA
            db.execute("INSERT INTO EXAM_ARCHIVE_DATA SELECT * FROM EXAM_DATA WHERE ID='"+examId+"'");
            Datastore.executeQuery("DELETE FROM EXAM_DATA WHERE ID=?",db.getDatabaseName(),examId);
            UserAction action=new UserAction(serv, worker, "ARCHIVE EXAM "+examId);
            action.saveAction();
            serv.messageToClient(worker, Message.SUCCESS);
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Endpoint(name="undelete_exam")
    public void undeleteExam(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String examId = requestData.optString("exam_id");
            db.execute("INSERT INTO EXAM_DATA SELECT * FROM EXAM_ARCHIVE_DATA WHERE ID='"+examId+"'");
            Datastore.executeQuery("DELETE FROM EXAM_ARCHIVE_DATA WHERE ID=?",db.getDatabaseName(),examId);
            UserAction action=new UserAction(serv, worker, "UNARCHIVE EXAM "+examId);
            action.saveAction();
            serv.messageToClient(worker, Message.SUCCESS);
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}

*/
