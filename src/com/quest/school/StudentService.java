/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
package com.quest.school;

import com.quest.access.common.UniqueRandom;
import com.quest.access.common.datastore.DataType;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.common.datastore.NonExistentDatabaseException;
import com.quest.access.common.datastore.Table;
import com.quest.access.control.Server;
import com.quest.access.useraccess.NonExistentUserException;
import com.quest.access.useraccess.PermanentPrivilege;
import com.quest.access.useraccess.Serviceable;
import com.quest.access.useraccess.User;
import com.quest.access.useraccess.services.Message;
import com.quest.access.useraccess.services.annotations.Endpoint;
import com.quest.access.useraccess.services.annotations.WebService;
import com.quest.servlets.ClientWorker;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



@WebService (name = "student_service", level = 10, privileged = "yes")
public class StudentService implements Serviceable {
    private static Datastore db;
    @Override
    public void service() {
      //dummy method 
    }

    @Override
    public void onCreate() {
      // create database tables
       db=new Datastore("school_data");
        if(!Table.ifTableExists("STUDENT_DATA", db.getDatabaseName())){
          Table table= db.addTable("ID", "STUDENT_DATA", "VARCHAR(10)");
          table.addColumn("STUDENT_NAME","TEXT");
          table.addColumn("STUDENT_CLASS","TEXT");
          table.addColumn("STUDENT_STREAM","TEXT");
          table.addColumn("CREATED","DATETIME");
        }
        if(!Table.ifTableExists("CLASS_DATA", db.getDatabaseName())){
          Table table= db.addTable("ID", "CLASS_DATA", "VARCHAR(10)");
          table.addColumns(new String[]{
             "CLASS_NAME", "TEXT",
             "CREATED", "DATETIME"
          });
        }
        if(!Table.ifTableExists("SUBJECT_DATA", db.getDatabaseName())){
          Table table= db.addTable("ID", "SUBJECT_DATA", "VARCHAR(10)");
          table.addColumns(new String[]{
             "SUBJECT_NAME", "TEXT",
              "CREATED", "DATETIME"
          });
        }
        if(!Table.ifTableExists("TEACHER_DATA", db.getDatabaseName())){
          //db.executeQuery("CREATE TABLE TEACHER_DATA(TEACHER_ID VARCHAR(10), TEACHER_NAME TEXT)");
          Table table= db.addTable("ID", "TEACHER_DATA", "VARCHAR(10)");
          table.addColumns(new String[]{
             "TEACHER_NAME", "TEXT",
              "CREATED", "DATETIME"
          });
        }
        if(!Table.ifTableExists("STREAM_DATA", db.getDatabaseName())){
          //db.executeQuery("CREATE TABLE STREAM_DATA(STREAM_ID VARCHAR(10),STREAM_NAME TEXT)");
          Table table= db.addTable("ID", "STREAM_DATA", "VARCHAR(10)");
          table.addColumns(new String[]{
             "STREAM_NAME", "TEXT",
             "CREATED", "DATETIME"
          });
        }
        
        if(!Table.ifTableExists("PAPER_DATA", db.getDatabaseName())){
          //db.executeQuery("CREATE TABLE PAPER_DATA(PAPER_ID VARCHAR(10),PAPER_NAME TEXT)");
          Table table= db.addTable("ID", "PAPER_DATA", "VARCHAR(10)");
          table.addColumns(new String[]{
             "PAPER_NAME", "TEXT",
             "CREATED", "DATETIME"
          });
        }
       if(!Table.ifTableExists("STUDENT_FIELD_META_DATA", db.getDatabaseName())){
           try {
               Table fields= db.addTable("FIELD_ID", "STUDENT_FIELD_META_DATA", "VARCHAR(10)");   
               fields.addColumn("FIELD_NAME", "VARCHAR(50)");
               fields.addColumn("FIELD_REQUIRED", DataType.BOOL);
               fields.addColumn("FIELD_DATA","VARCHAR(10)");
               fields.addColumn("CREATED", DataType.DATETIME);
               UniqueRandom rand=new UniqueRandom(8);
               db.doInsert("STUDENT_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"STUDENT NAME","1","TEXT","!NOW()"});
               Thread.sleep(1000);
               db.doInsert("STUDENT_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"STUDENT CLASS","1","TEXT","!NOW()"});
               Thread.sleep(1000);
               db.doInsert("STUDENT_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"STUDENT STREAM","1","TEXT","!NOW()"});
           } catch (InterruptedException ex) {
               Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
         if(!Table.ifTableExists("TEACHER_FIELD_META_DATA", db.getDatabaseName())){
           Table fields= db.addTable("FIELD_ID", "TEACHER_FIELD_META_DATA", "VARCHAR(10)");   
           fields.addColumn("FIELD_NAME", "VARCHAR(50)");
           fields.addColumn("FIELD_REQUIRED", DataType.BOOL);
           fields.addColumn("FIELD_DATA","VARCHAR(10)");
           fields.addColumn("CREATED", DataType.DATETIME);
            UniqueRandom rand=new UniqueRandom(8);
           db.doInsert("TEACHER_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"TEACHER NAME","1","TEXT","!NOW()"});
          
        }
        if(!Table.ifTableExists("CLASS_FIELD_META_DATA", db.getDatabaseName())){
           Table fields= db.addTable("FIELD_ID", "CLASS_FIELD_META_DATA", "VARCHAR(10)");   
           fields.addColumn("FIELD_NAME", "VARCHAR(50)");
           fields.addColumn("FIELD_REQUIRED", DataType.BOOL);
           fields.addColumn("FIELD_DATA","VARCHAR(10)");
           fields.addColumn("CREATED", DataType.DATETIME);
           UniqueRandom rand=new UniqueRandom(8);
           db.doInsert("CLASS_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"CLASS NAME","1","unique","!NOW()"});
        }
        if(!Table.ifTableExists("SUBJECT_FIELD_META_DATA", db.getDatabaseName())){
           Table fields= db.addTable("FIELD_ID", "SUBJECT_FIELD_META_DATA", "VARCHAR(10)");   
           fields.addColumn("FIELD_NAME", "VARCHAR(50)");
           fields.addColumn("FIELD_REQUIRED", DataType.BOOL);
           fields.addColumn("FIELD_DATA","VARCHAR(10)");
           fields.addColumn("CREATED", DataType.DATETIME);
           UniqueRandom rand=new UniqueRandom(8);
           db.doInsert("SUBJECT_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"SUBJECT NAME","1","unique","!NOW()"});
        }
          if(!Table.ifTableExists("STREAM_FIELD_META_DATA", db.getDatabaseName())){
           Table fields= db.addTable("FIELD_ID", "STREAM_FIELD_META_DATA", "VARCHAR(10)");   
           fields.addColumn("FIELD_NAME", "VARCHAR(50)");
           fields.addColumn("FIELD_REQUIRED", DataType.BOOL);
           fields.addColumn("FIELD_DATA","VARCHAR(10)");
           fields.addColumn("CREATED", DataType.DATETIME);
           UniqueRandom rand=new UniqueRandom(8);
           db.doInsert("STREAM_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"STREAM NAME","1","unique","!NOW()"});
        }
          if(!Table.ifTableExists("PAPER_FIELD_META_DATA", db.getDatabaseName())){
           Table fields= db.addTable("FIELD_ID", "PAPER_FIELD_META_DATA", "VARCHAR(10)");   
           fields.addColumn("FIELD_NAME", "VARCHAR(50)");
           fields.addColumn("FIELD_REQUIRED", DataType.BOOL);
           fields.addColumn("FIELD_DATA","VARCHAR(10)");
           fields.addColumn("CREATED", DataType.DATETIME);
           UniqueRandom rand=new UniqueRandom(8);
           db.doInsert("PAPER_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"PAPER NAME","1","unique","!NOW()"});
        }
          
         if(!Table.ifTableExists("STREAM_SUBJECTS", db.getDatabaseName())){
           db.execute("CREATE TABLE STREAM_SUBJECTS(STREAM_ID VARCHAR(10),SUBJECT_ID VARCHAR(10))");
         }
         
         if(!Table.ifTableExists("SUBJECT_PAPERS", db.getDatabaseName())){
          db.execute("CREATE TABLE SUBJECT_PAPERS(SUBJECT_ID VARCHAR(10),PAPER_ID VARCHAR(10))");
         }
          if(!Table.ifTableExists("CLASS_STREAMS", db.getDatabaseName())){
          db.execute("CREATE TABLE CLASS_STREAMS(CLASS_ID VARCHAR(10),STREAM_ID VARCHAR(10))");
         }
         if(!Table.ifTableExists("TEACHER_SUBJECTS", db.getDatabaseName())){
          db.execute("CREATE TABLE TEACHER_SUBJECTS(TEACHER_ID VARCHAR(10),SUBJECT_ID VARCHAR(10),STREAM_ID VARCHAR(10))");
         }
         if(!Table.ifTableExists("STUDENT_HISTORY", db.getDatabaseName())){
          //db.executeQuery("CREATE TABLE PAPER_DATA(PAPER_ID VARCHAR(10),PAPER_NAME TEXT)");
          Table table= db.addTable("ID", "STUDENT_HISTORY", "VARCHAR(20)");
          table.addColumns(new String[]{
             "STUDENT_ID", "VARCHAR(10)",
             "HISTORY", "TEXT",
             "OWNER_ID", "VARCHAR(20)",
             "CREATED", "DATETIME"
          });
        }
    }
    
  

    
    
    
    
 
    
    @Endpoint(name="create_paper")
    public void createPaper(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        JSONArray fieldNames = requestData.optJSONArray("field_names");
        JSONArray fieldValues = requestData.optJSONArray("field_values");
        JSONArray fieldData = requestData.optJSONArray("field_data");
        String paperName=requestData.optString("paper_name");
        UniqueRandom rand=new UniqueRandom(8);
        String paperId=rand.nextMixedRandom();
        boolean ifExists=db.ifValueExists(paperName, "PAPER_DATA", "PAPER_NAME");
        if(ifExists){
           worker.setReason("Paper already exists");
           serv.messageToClient(worker, Message.FAIL);
           return;  
        }
        StringBuilder builder=new StringBuilder("INSERT INTO PAPER_DATA ( ID,PAPER_NAME,CREATED, ");
        if(fieldNames.length()==0){
            builder=new StringBuilder("INSERT INTO PAPER_DATA (ID,PAPER_NAME,CREATED) VALUES('"+paperId+"', '"+paperName+"',NOW())");
        }
        else{
          for(int x=0; x<fieldNames.length(); x++){
              // save only a value that exists
           String colName=fieldNames.optString(x).replace(" ", "_");
           if(x==fieldNames.length()-1){
              builder.append(colName).append(") VALUES('").append(paperId).append("', '").append(paperName).append("', ").append("NOW()").append(", ");
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
                        boolean exists=db.ifValueExists(value,"PAPER_DATA", fieldName);   
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
       db.execute(builder.toString());
       serv.messageToClient(worker, Message.SUCCESS);    
    }
    
    @Endpoint(name="create_subject") 
    public void createSubject(Server serv,ClientWorker worker){
        String [] values=new String[]{"subject_name","paper_ids","SUBJECT_DATA","SUBJECT_NAME","Subject already exists","SUBJECT_ID","SUBJECT_PAPERS","PAPER_ID"};
        createObject(worker, serv, values);
    }
    

    
    @Endpoint(name="create_stream") 
    public void createStream(Server serv,ClientWorker worker){
       String [] values=new String[]{"stream_name","subject_ids","STREAM_DATA","STREAM_NAME","Stream already exists","STREAM_ID","STREAM_SUBJECTS","SUBJECT_ID"};
       createObject(worker, serv, values);  
    }
    
    private void createObject(ClientWorker worker,Server serv,String [] values){
        //values
        //0 class_name
        //1 stream_ids
        //2 CLASS_DATA
        //3 CLASS_NAME
        //4 failureMessage
        //5 CLASS_ID
        //6 CLASS_STREAMS
        //7 STREAM_ID
        JSONObject requestData = worker.getRequestData();
        JSONArray fieldNames = requestData.optJSONArray("field_names");
        JSONArray fieldValues = requestData.optJSONArray("field_values");
        JSONArray fieldData = requestData.optJSONArray("field_data");
        String name=requestData.optString(values[0]);
        JSONArray ids=requestData.optJSONArray(values[1]);
        UniqueRandom rand=new UniqueRandom(8);
        String id=rand.nextMixedRandom();
        boolean recordExists=db.ifValueExists(name, values[2], values[3]);
        if(recordExists){
            worker.setReason(values[4]);
            serv.messageToClient(worker, Message.FAIL);
            return;  
        }
        StringBuilder builder=new StringBuilder("INSERT INTO "+values[2]+" ( ID, "+values[3]+",CREATED, ");
        if(fieldNames.length()==0){
            builder=new StringBuilder("INSERT INTO "+values[2]+" (ID,"+values[3]+",CREATED) VALUES('"+id+"', '"+name+"',NOW())");
        }
        else{
          for(int x=0; x<fieldNames.length(); x++){
              // save only a value that exists
              String colName=fieldNames.optString(x).replace(" ", "_");
              if(x==fieldNames.length()-1){
                  builder.append(colName).append(") VALUES('").append(id).append("', '").append(name).append("', ").append("NOW()").append(", ");
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
                 boolean exists=db.ifValueExists(value,values[2], fieldName);
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
       db.execute(builder.toString());
       //here we are inserting references to the object e.g stream_subjects, teacher_subjects etc
       for(int x=0; x<ids.length(); x++){
           String patId = ids.optString(x);
           boolean exists=db.ifValueExists(new String[]{id,patId},values[6], new String[]{values[5],values[7]});
           if(!exists){
             db.doInsert(values[6], new String[]{id,patId});
           }
       }
       serv.messageToClient(worker, Message.SUCCESS);  
    }
    
    
    
    @Endpoint(name="create_teacher")
    public void createTeacher(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            JSONArray fieldNames = requestData.optJSONArray("field_names");
            JSONArray fieldValues = requestData.optJSONArray("field_values");
            JSONArray fieldData = requestData.optJSONArray("field_data");
            String name=requestData.optString("teacher_name");
            JSONArray ids=requestData.optJSONArray("subject_ids");
            JSONArray streamIds=requestData.optJSONArray("stream_ids");
            JSONArray teacherPrivs=requestData.optJSONArray("teacher_privs");
            UniqueRandom rand=new UniqueRandom(8);
            String id=rand.nextMixedRandom();
            boolean recordExists=db.ifValueExists(name, "TEACHER_DATA","TEACHER_NAME");
            if(recordExists){
                worker.setReason("Teacher already exists");
                serv.messageToClient(worker, Message.FAIL);
                return;  
            }
            StringBuilder builder=new StringBuilder("INSERT INTO TEACHER_DATA ( ID,TEACHER_NAME,CREATED, ");
            if(fieldNames.length()==0){
                builder=new StringBuilder("INSERT INTO TEACHER_DATA (ID,TEACHER_NAME,CREATED) VALUES('"+id+"', '"+name+"',NOW())");
            }
            else{
              for(int x=0; x<fieldNames.length(); x++){
                  // save only a value that exists
                  String colName=fieldNames.optString(x).replace(" ", "_");
                  if(x==fieldNames.length()-1){
                      builder.append(colName).append(") VALUES('").append(id).append("', '").append(name).append("', ").append("NOW()").append(",");
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
                     boolean exists=db.ifValueExists(value,"TEACHER_DATA", fieldName);
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
           
           db.execute(builder.toString());
           //here we are inserting references to the object e.g stream_subjects, teacher_subjects etc
           for(int x=0; x<ids.length(); x++){
               String patId = ids.optString(x);
               String streamId=streamIds.optString(x);
               boolean exists=db.ifValueExists(new String[]{id,patId,streamId},"TEACHER_SUBJECTS", new String[]{"TEACHER_ID","SUBJECT_ID,STREAM_ID"});
               if(!exists){
                 db.doInsert("TEACHER_SUBJECTS", new String[]{id,patId,streamId});
               }
           }
           //create a user in quest access
           worker.setMessage("create_user");
           worker.setService("user_service");
           JSONObject userData=new JSONObject();
           userData.put("name", name);
           userData.put("privs", decodeTeacherPrivileges(teacherPrivs));
           userData.put("host","localhost");
           worker.setRequestData(userData);
           serv.processClientRequest(worker);
        } catch (JSONException ex) {
            Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="create_class")
    public void createClass(Server serv,ClientWorker worker){
        String [] values=new String[]{"class_name","stream_ids","CLASS_DATA","CLASS_NAME","Class already exists","CLASS_ID","CLASS_STREAMS","STREAM_ID"};
        createObject(worker, serv, values);
    }
    
    private void updateObject(Server serv,ClientWorker worker,String [] values){
       
        * values[0] -- class_id
        * values[1] -- stream_ids
        * values[2] -- CLASS_DATA
         * values[3] -- CLASS_STREAMS
         * values[4] -- CLASS_ID
         * values[5] -- STREAM_ID
         * values[6] -- class_name
         * values[7] -- CLASS_NAME
        
      JSONObject requestData=worker.getRequestData();
      JSONArray fieldNames = requestData.optJSONArray("field_names");
      String id = requestData.optString(values[0]); //values[0]
      String name=requestData.optString(values[6]);
      JSONArray fieldValues = requestData.optJSONArray("field_values");
      JSONArray fieldData = requestData.optJSONArray("field_data");
      JSONArray ids=requestData.optJSONArray(values[1]); //values[1]
      StringBuilder builder=new StringBuilder("UPDATE "+values[2]+" SET "+values[7]+"='"+name+"' , "); //values[2]
      if(fieldNames.length()==0){
            builder=new StringBuilder("UPDATE "+values[2]+" SET "+values[7]+"='"+name+"' WHERE ID='"+id+"'");
        }
      for(int x=0; x<fieldNames.length(); x++){
        String fieldName=fieldNames.optString(x).replace(" ","_");
        String dataType=fieldData.optString(x);
        String value=fieldValues.optString(x);
        if(dataType.equals("unique")){
            boolean exists=db.ifValueExists(new String[]{id,value},values[2], new String[]{"ID",fieldName});
            boolean valueExists=db.ifValueExists(value,values[2], fieldName);
                //check if value exists with the specified name, thats ok
              if(exists==false && valueExists==true){
                    //this means that this value exists for another student and not this student
                   worker.setReason("Field "+fieldName+" must be a unique value");
                   serv.messageToClient(worker, Message.FAIL); 
                   return;
                }
              else if(exists && valueExists){
                  //this means this value exists for this student only so ignore  
              }
        }
        if(!dataType.equals("long")){ 
            value="'"+value+"'";  
          }
        if(x==fieldNames.length()-1){
            builder.append(fieldName).append("=").append(value).append(" WHERE ID='").append(id).append("'"); 
        }
        else{
            builder.append(fieldName).append("=").append(value).append(" ,");    
        }
      }
      db.execute(builder.toString());
      //Database.executeQuery("DELETE FROM "+values[3]+" WHERE "+values[4]+"=? ",db.getDatabaseName(),id); //values[3],values[4]
      for(int x=0; x<ids.length(); x++){
           String streamId = ids.optString(x);
           boolean exists=db.ifValueExists(new String[]{id,streamId},values[3], new String[]{values[4],values[5]}); //values[5]
           if(!exists){
             db.doInsert(values[3], new String[]{id,streamId});
           }
       }
      
      serv.messageToClient(worker, Message.SUCCESS);  
    }
    
    
    private JSONArray decodeTeacherPrivileges(JSONArray privs){
      JSONArray teacherPrivs=new JSONArray();
      for(int x=0; x<privs.length(); x++){
         String thePriv=privs.optString(x);
         if(thePriv.equals("sms service")){
             teacherPrivs.put("message_service");
         }
         else if(thePriv.equals("mark analysis")){
             teacherPrivs.put("mark_service");
         }
         else if(thePriv.equals("mark analysis admin")){
            teacherPrivs.put("edit_mark_service");  
         }
         else if(thePriv.equals("school records")){
            teacherPrivs.put("student_service");
         }
         else if(thePriv.equals("school records admin")){
             teacherPrivs.put("edit_student_service");
         }
         else if(thePriv.equals("school accounts")){
             teacherPrivs.put("account_service");
         }
      }
     return teacherPrivs;
    }
    
  
    
    @Endpoint(name="update_teacher")
    public void updateTeacher(Server serv,ClientWorker worker){
        try {
           JSONObject requestData=worker.getRequestData();
           JSONArray fieldNames = requestData.optJSONArray("field_names");
           String id = requestData.optString("teacher_id"); //values[0]
           String name=requestData.optString("teacher_name");
           JSONArray fieldValues = requestData.optJSONArray("field_values");
           JSONArray fieldData = requestData.optJSONArray("field_data");
           JSONArray ids=requestData.optJSONArray("subject_ids"); //values[1]
           JSONArray streamIds=requestData.optJSONArray("stream_ids");
           JSONArray teacherPrivs=requestData.optJSONArray("teacher_privs");
           String oldTeacherName= db.query("SELECT TEACHER_NAME FROM TEACHER_DATA WHERE ID=?",id).optJSONArray("TEACHER_NAME").optString(0);
           StringBuilder builder=new StringBuilder("UPDATE TEACHER_DATA SET TEACHER_NAME='"+name+"' , "); //values[2]
           if(fieldNames.length()==0){
                 builder=new StringBuilder("UPDATE TEACHER_DATA SET TEACHER_NAME='"+name+"' WHERE ID='"+id+"'");
             }
           for(int x=0; x<fieldNames.length(); x++){
             String fieldName=fieldNames.optString(x).replace(" ","_");
             String dataType=fieldData.optString(x);
             String value=fieldValues.optString(x);
             if(dataType.equals("unique")){
                 boolean exists=db.ifValueExists(new String[]{id,value},"TEACHER_DATA", new String[]{"ID",fieldName});
                 boolean valueExists=db.ifValueExists(value,"TEACHER_DATA", fieldName);
                     //check if value exists with the specified name, thats ok
                   if(exists==false && valueExists==true){
                         //this means that this value exists for another student and not this student
                        worker.setReason("Field "+fieldName+" must be a unique value");
                        serv.messageToClient(worker, Message.FAIL);
                        return;
                     }
                   else if(exists && valueExists){
                       //this means this value exists for this student only so ignore  
                   }
             }
             if(!dataType.equals("long")){ 
                 value="'"+value+"'";  
               }
             if(x==fieldNames.length()-1){
                 builder.append(fieldName).append("=").append(value).append(" WHERE ID='").append(id).append("'"); 
             }
             else{
                 builder.append(fieldName).append("=").append(value).append(" ,");    
             }
           }
           db.execute(builder.toString());
           //Database.executeQuery("DELETE FROM TEACHER_SUBJECTS WHERE TEACHER_ID=? ",db.getDatabaseName(),id); //values[3],values[4]
           for(int x=0; x<ids.length(); x++){
                String streamId = ids.optString(x);
                String teacherId=streamIds.optString(x);
                boolean exists=db.ifValueExists(new String[]{id,streamId,teacherId},"TEACHER_SUBJECTS", new String[]{"TEACHER_ID","SUBJECT_ID","STREAM_ID"}); //values[5]
                if(!exists){
                  db.doInsert("TEACHER_SUBJECTS", new String[]{id,streamId,teacherId});
                }
            }
            //update user in quest access
             JSONArray decodedPrivs=decodeTeacherPrivileges(teacherPrivs);
             PermanentPrivilege[] grantPrivs=new PermanentPrivilege[decodedPrivs.length()];
             for(int x=0; x<decodedPrivs.length(); x++){
               grantPrivs[x]=PermanentPrivilege.getPrivilege(decodedPrivs.optString(x), serv);   
             }
             User user=User.getExistingUser(oldTeacherName, serv);
             user.grantPrivileges(grantPrivs);
             user.setUserName(name);
             serv.messageToClient(worker, Message.SUCCESS);
        } catch (Exception ex) {
            Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Endpoint(name="update_class")
    public void updateClass(Server serv,ClientWorker worker){
    
       String [] values=new String[]{"class_id","stream_ids","CLASS_DATA","CLASS_STREAMS","CLASS_ID","STREAM_ID","class_name","CLASS_NAME"};
       updateObject(serv, worker, values);
    }
    
    @Endpoint(name="update_stream")
    public void updateStream(Server serv,ClientWorker worker){
       String [] values=new String[]{"stream_id","subject_ids","STREAM_DATA","STREAM_SUBJECTS","STREAM_ID","SUBJECT_ID","stream_name","STREAM_NAME"};
       updateObject(serv, worker, values);  
    }
    
    @Endpoint(name="update_subject")
    public void updateSubject(Server serv,ClientWorker worker){
       String [] values=new String[]{"subject_id","paper_ids","SUBJECT_DATA","SUBJECT_PAPERS","SUBJECT_ID","PAPER_ID","subject_name","SUBJECT_NAME"};
       updateObject(serv, worker, values);  
    }  
    
    @Endpoint(name="update_paper")
    public void updatePaper(Server serv,ClientWorker worker){
      JSONObject requestData=worker.getRequestData();
      JSONArray fieldNames = requestData.optJSONArray("field_names");
      String paperId = requestData.optString("paper_id");
      String paperName=requestData.optString("paper_name");
      JSONArray fieldValues = requestData.optJSONArray("field_values");
      JSONArray fieldData = requestData.optJSONArray("field_data");
      StringBuilder builder=new StringBuilder("UPDATE PAPER_DATA SET PAPER_NAME = '"+paperName+"' , ");
      if(fieldNames.length()==0){
            builder=new StringBuilder("UPDATE PAPER_DATA SET PAPER_NAME='"+paperName+"'  WHERE ID='"+paperId+"'");
            db.execute(builder.toString());
            serv.messageToClient(worker, Message.SUCCESS);
            return;
        }
      for(int x=0; x<fieldNames.length(); x++){
        String fieldName=fieldNames.optString(x);
        String value=fieldValues.optString(x);
        String dataType=fieldData.optString(x);
        if(dataType.equals("unique")){
               // boolean exists=db.ifValueExists(value,"STUDENT_DATA", fieldName);  
            boolean exists=db.ifValueExists(new String[]{paperName,value},"PAPER_DATA", new String[]{"PAPER_NAME",fieldName});
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
            builder.append(fieldName).append("=").append(value).append(" WHERE ID='").append(paperId).append("'"); 
        }
        else{
            builder.append(fieldName).append("=").append(value).append(" ,");    
        }
      }
      db.execute(builder.toString());
      serv.messageToClient(worker, Message.SUCCESS);
       
    }
    
    @Endpoint(name="update_student")
    public void updateStudent(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        JSONArray fieldNames = requestData.optJSONArray("field_names");
        JSONArray fieldValues = requestData.optJSONArray("field_values");
        JSONArray fieldData = requestData.optJSONArray("field_data");
        String studentName=requestData.optString("student_name");
        String studentStreamId=requestData.optString("student_stream_id");
        JSONObject classId = db.query("select class_id from class_streams where stream_id=?",studentStreamId);
        String studentClassId=classId.optJSONArray("class_id").optString(0);
        if(studentStreamId.startsWith("AR_")){
           worker.setReason("You cannot update a student to an archived stream");
           serv.messageToClient(worker, Message.FAIL);
           return; 
        }
         if(studentClassId.trim().equals("")){
           worker.setReason("The specified stream does not belong to any class");
           serv.messageToClient(worker, Message.FAIL);
           return;
        }
        String studentId=requestData.optString("student_id");
        StringBuilder builder=new StringBuilder("UPDATE STUDENT_DATA SET STUDENT_NAME='"+studentName+"' , STUDENT_CLASS='"+studentClassId+"' , STUDENT_STREAM='"+studentStreamId+"' , ");
        if(fieldNames.length()==0){
            builder=new StringBuilder("UPDATE STUDENT_DATA SET STUDENT_NAME='"+studentName+"' , STUDENT_CLASS='"+studentClassId+"' , STUDENT_STREAM='"+studentStreamId+"' WHERE ID='"+studentId+"'");
            db.execute(builder.toString());
            serv.messageToClient(worker, Message.SUCCESS);
            return;
        }
         for(int x=0; x<fieldNames.length(); x++){
             String fieldName=fieldNames.optString(x).replace(" ", "_");
             String value=fieldValues.optString(x);
             String dataType=fieldData.optString(x);
             if(dataType.equals("unique")){
                boolean exists=db.ifValueExists(new String[]{studentId,value},"STUDENT_DATA", new String[]{"ID",fieldName});
                boolean valueExists=db.ifValueExists(value,"STUDENT_DATA", fieldName);
                //check if value exists with the specified name, thats ok
                if(exists==false && valueExists==true){
                    //this means that this value exists for another student and not this student
                   worker.setReason("Field "+fieldName+" must be a unique value");
                   serv.messageToClient(worker, Message.FAIL); 
                   return;
                }
                else if(exists && valueExists){
                  //this means this value exists for this student only so ignore  
                }
             }
             if(!dataType.equals("long")){
                  value="'"+value+"'";
             }
             if(x==fieldNames.length()-1){
                builder.append(fieldName).append("=").append(value).append(" WHERE ID='").append(studentId).append("'");
             }
             else{
                builder.append(fieldName).append("=").append(value).append(" ,");
             }    
        }
      
      db.execute(builder.toString());
      serv.messageToClient(worker, Message.SUCCESS);
       
    }
     
    
    @Endpoint(name="delete_class")
    public void deleteClass(Server serv,ClientWorker worker){
        JSONObject requestData=worker.getRequestData();
        String ID = requestData.optString("object_id");
        Datastore.executeQuery("DELETE FROM CLASS_DATA WHERE ID=?",db.getDatabaseName(),ID);
        Datastore.executeQuery("DELETE FROM CLASS_STREAMS WHERE CLASS_ID=?",db.getDatabaseName(),ID);
        Datastore.executeQuery("DELETE FROM STUDENT_DATA WHERE STUDENT_CLASS=?",db.getDatabaseName(),ID);
        Datastore.executeQuery("DELETE FROM MARK_DATA WHERE CLASS_ID=?",db.getDatabaseName(),ID);
        serv.messageToClient(worker, Message.SUCCESS);
    }
    
   @Endpoint(name="delete_teacher")
    public void deleteTeacher(Server serv,ClientWorker worker){
        try {
            JSONObject requestData=worker.getRequestData();
            String ID = requestData.optString("object_id");
            String name=db.query("SELECT TEACHER_NAME FROM TEACHER_DATA WHERE ID=?",ID).optJSONArray("TEACHER_NAME").optString(0);
            Datastore.executeQuery("DELETE FROM TEACHER_DATA WHERE ID=?",db.getDatabaseName(),ID);
            Datastore.executeQuery("DELETE FROM TEACHER_SUBJECTS WHERE TEACHER_ID=?",db.getDatabaseName(),ID);
            //create a user in quest access
            worker.setMessage("delete_user");
            worker.setService("user_service");
            JSONObject userData=new JSONObject();
            userData.put("name", name);
            worker.setRequestData(userData);
            serv.processClientRequest(worker);
        } catch (JSONException ex) {
            Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
     
   @Endpoint(name="delete_subject")
    public void deleteSubject(Server serv,ClientWorker worker){
        deleteObject(serv,worker,"SUBJECT_DATA","SUBJECT_PAPERS", "SUBJECT_ID");
    }
   
    @Endpoint(name="delete_stream")
    public void deleteStream(Server serv,ClientWorker worker){
        JSONObject requestData=worker.getRequestData();
        String ID = requestData.optString("object_id");
        Datastore.executeQuery("DELETE FROM STREAM_DATA WHERE ID=?",db.getDatabaseName(),ID);
        Datastore.executeQuery("DELETE FROM STREAM_SUBJECTS WHERE STREAM_ID=?",db.getDatabaseName(),ID);
        Datastore.executeQuery("DELETE FROM STUDENT_DATA WHERE STUDENT_STREAM=?",db.getDatabaseName(),ID);
        Datastore.executeQuery("DELETE FROM MARK_DATA WHERE STREAM_ID=?",db.getDatabaseName(),ID); 
        serv.messageToClient(worker, Message.SUCCESS);
    }
    
     
    private void deleteObject(Server serv,ClientWorker worker,String tableName,String relationTable,String relationColumn){
        JSONObject requestData=worker.getRequestData();
        String ID = requestData.optString("object_id");
        Datastore.executeQuery("DELETE FROM "+tableName+" WHERE ID=?",db.getDatabaseName(),ID);
        Datastore.executeQuery("DELETE FROM "+relationTable+" WHERE "+relationColumn+"=?",db.getDatabaseName(),ID);
        serv.messageToClient(worker, Message.SUCCESS);
    }
    
    @Endpoint(name="delete_student")
    public void deleteStudent(Server serv,ClientWorker worker){
       JSONObject requestData=worker.getRequestData();
        String studentId = requestData.optString("object_id");
        Datastore.executeQuery("DELETE FROM STUDENT_DATA WHERE ID=?",db.getDatabaseName(),studentId);
        Datastore.executeQuery("DELETE FROM MARK_DATA WHERE STUDENT_ID=?",db.getDatabaseName(),studentId);
        serv.messageToClient(worker,Message.SUCCESS);
    }
    
    @Endpoint(name="delete_paper")
    public void deletePaper(Server serv,ClientWorker worker){
       JSONObject requestData=worker.getRequestData();
        String paperID = requestData.optString("object_id");
        Datastore.executeQuery("DELETE FROM PAPER_DATA WHERE ID=?",db.getDatabaseName(),paperID);
        serv.messageToClient(worker, Message.SUCCESS);
    }
    
    
    
   
    
    @Endpoint(name="all_streams",shareMethodWith = {"mark_service","message_service","account_service"})
    public void allStreams(Server serv,ClientWorker worker){
       JSONObject data = db.query("SELECT * FROM STREAM_DATA ORDER BY CREATED ASC");
       serv.messageToClient(worker,data);
    }
    
    @Endpoint(name="all_teachers")
    public void allTeachers(Server serv,ClientWorker worker){
       JSONObject data = db.query("SELECT * FROM TEACHER_DATA ORDER BY CREATED ASC");
       serv.messageToClient(worker,data);
    }
    
    @Endpoint(name="all_subjects",shareMethodWith = {"mark_service","edit_mark_service"})
    public void allSubjects(Server serv,ClientWorker worker){
       JSONObject data = db.query("SELECT * FROM SUBJECT_DATA ORDER BY CREATED ASC");
       serv.messageToClient(worker,data); 
    }
    
    @Endpoint(name="all_papers")
    public void allPapers(Server serv,ClientWorker worker){
       JSONObject data = db.query("SELECT * FROM PAPER_DATA ORDER BY CREATED ASC");
       serv.messageToClient(worker,data); 
    }
    
    @Endpoint(name="all_classes",shareMethodWith = {"mark_service","message_service","account_service","edit_mark_service"})
    public void allClasses(Server serv,ClientWorker worker){
       JSONObject data = db.query("SELECT * FROM CLASS_DATA ORDER BY CREATED ASC");
       serv.messageToClient(worker,data);
    }
    
    @Endpoint(name="all_teachers_and_relations")
    public void allTeachersAndRelations(Server serv,ClientWorker worker){
       JSONObject requestData = worker.getRequestData();
       JSONArray fields=requestData.optJSONArray("fields");
       StringBuilder builder=new StringBuilder("SELECT STREAM_NAME,SUBJECT_NAME, "); //values[0] -- SUBJECT_NAME, values[1] -- TEACHER_NAME
       if(fields.length()==0){
          builder=new StringBuilder("SELECT STREAM_NAME,SUBJECT_NAME"); 
       }
       for(int x=0; x<fields.length(); x++){
            String fieldName=fields.optString(x).replace(" ","_");
            if(x==fields.length()-1){
                builder.append(fieldName);
            }
            else{
             builder.append(fieldName).append(" , ");
            }
      }
        builder.append(" FROM SUBJECT_DATA,TEACHER_SUBJECTS,TEACHER_DATA,STREAM_DATA WHERE  "
                + "SUBJECT_DATA.ID=SUBJECT_ID AND TEACHER_ID=TEACHER_DATA.ID AND "
                + "STREAM_DATA.ID=TEACHER_SUBJECTS.STREAM_ID ORDER BY TEACHER_NAME ASC");
        JSONObject data = db.query(builder.toString());
       // SELECT SUBJECT_NAME,TEACHER_NAME FROM SUBJECT_DATA,TEACHER_SUBJECTS,TEACHER_DATA WHERE  SUBJECT_DATA.ID=SUBJECT_ID AND TEACHER_ID=TEACHER_DATA.ID ORDER BY TEACHER_NAME ASC;
        serv.messageToClient(worker,data);
    }
    
    
    @Endpoint(name="all_classes_and_relations")
    public void allClassesAndRelations(Server serv,ClientWorker worker){
       String [] values =new String[]{"STREAM_NAME","CLASS_NAME","STREAM_DATA","CLASS_STREAMS","CLASS_DATA","STREAM_ID","CLASS_ID"};
       allObjectsAndRelations(serv, worker, values);
    }
    
    @Endpoint(name="all_subjects_and_relations")
    public void allSubjectsAndRelations(Server serv,ClientWorker worker){
       String [] values =new String[]{"PAPER_NAME","SUBJECT_NAME","PAPER_DATA","SUBJECT_PAPERS","SUBJECT_DATA","PAPER_ID","SUBJECT_ID"};
       allObjectsAndRelations(serv, worker, values);
    }
    
   @Endpoint(name="all_streams_and_relations")
    public void allStreamsAndRelations(Server serv,ClientWorker worker){
       String [] values =new String[]{"SUBJECT_NAME","STREAM_NAME","SUBJECT_DATA","STREAM_SUBJECTS","STREAM_DATA","SUBJECT_ID","STREAM_ID"};
       allObjectsAndRelations(serv, worker, values);
    }
    
    private void allObjectsAndRelations(Server serv,ClientWorker worker,String [] values){
       JSONObject requestData = worker.getRequestData();
       JSONArray fields=requestData.optJSONArray("fields");
       StringBuilder builder=new StringBuilder("SELECT "+values[0]+",  "); //values[0] -- SUBJECT_NAME, values[1] -- TEACHER_NAME
       if(fields.length()==0){
          builder=new StringBuilder("SELECT "+values[0]+""); 
       }
       for(int x=0; x<fields.length(); x++){
            String fieldName=fields.optString(x).replace(" ","_");
            if(x==fields.length()-1){
                builder.append(fieldName);
            }
            else{
             builder.append(fieldName).append(" , ");
            }
      }
        //values[2] -- SUBJECT_DATA, values[3] -- TEACHER_SUBJECTS  ,values[4] -- TEACHER_DATA,values[5] -- SUBJECT_ID
        //values[6] -- TEACHER_ID
        builder.append(" FROM ").append(values[2]).append(",").append(values[3]).append(",").
        append(values[4]).append(" WHERE  ").append(values[2]).append(".ID=").append(values[5]).
        append(" " + "AND ").append(values[6]).append("=").append(values[4]).append(".ID ORDER BY ").append(values[1]).append(" ASC");
        JSONObject data = db.query(builder.toString());
       // SELECT SUBJECT_NAME,TEACHER_NAME FROM SUBJECT_DATA,TEACHER_SUBJECTS,TEACHER_DATA WHERE  SUBJECT_DATA.ID=SUBJECT_ID AND TEACHER_ID=TEACHER_DATA.ID ORDER BY TEACHER_NAME ASC;
        serv.messageToClient(worker,data); 
    }
    
    @Endpoint(name="all_papers_and_relations")
    public void allPapersAndRelations(Server serv,ClientWorker worker){
      JSONObject requestData = worker.getRequestData();
      JSONArray fields=requestData.optJSONArray("fields");  
      StringBuilder builder=new StringBuilder("SELECT ");
      for(int x=0; x<fields.length(); x++){
            String fieldName=fields.optString(x).replace(" ","_");
            if(x==fields.length()-1){
                builder.append(fieldName);
            }
            else{
             builder.append(fieldName).append(" , ");
            }
        
      }
        builder.append(" FROM PAPER_DATA");
        JSONObject data = db.query(builder.toString());
        serv.messageToClient(worker,data); 
    }
   
    
    @Endpoint(name="all_students")
    public void allStudentsAndRelations(Server serv,ClientWorker worker){
      JSONObject requestData=worker.getRequestData();
      String classId= requestData.optString("class_id");
      String streamId=requestData.optString("stream_id");
      JSONArray fields=requestData.optJSONArray("fields");
      StringBuilder builder=new StringBuilder("SELECT STUDENT_NAME,CLASS_NAME,STREAM_NAME, ");
      String [] params=new String[]{streamId,classId,streamId,classId};
      if(fields.length()==0){
        builder=new StringBuilder("SELECT STUDENT_NAME, CLASS_NAME,STREAM_NAME ");   
      }
      for(int x=0; x<fields.length(); x++){
        String fieldName=fields.optString(x).replace(" ","_");
        if(x==fields.length()-1){
              builder.append(fieldName);
        }
        else{
             builder.append(fieldName).append(" , ");  
        }
        
      }
      if(streamId.equals("all") && classId.equals("all")){
            builder.append(" FROM CLASS_DATA,STREAM_DATA,STUDENT_DATA , CLASS_STREAMS WHERE  STREAM_DATA.ID=CLASS_STREAMS.STREAM_ID"); 
            builder.append(" AND CLASS_DATA.ID=CLASS_STREAMS.CLASS_ID  AND STREAM_DATA.ID=STUDENT_STREAM ORDER BY STUDENT_CLASS ASC, STUDENT_STREAM ASC, STUDENT_NAME ASC");
            params=new String[]{};
        }
      else if(streamId.equals("all") && !classId.equals("all")){
            builder.append(" FROM  CLASS_DATA,STREAM_DATA,STUDENT_DATA ,CLASS_STREAMS WHERE  STREAM_DATA.ID=CLASS_STREAMS.STREAM_ID"); 
            builder.append(" AND CLASS_DATA.ID=CLASS_STREAMS.CLASS_ID  AND STUDENT_CLASS=?  AND CLASS_STREAMS.CLASS_ID=?");  
            builder.append(" AND STREAM_DATA.ID=STUDENT_STREAM ORDER BY STUDENT_CLASS ASC, STUDENT_STREAM ASC, STUDENT_NAME ASC");
            params=new String[]{classId,classId};
        }
        else {
         builder.append(" FROM CLASS_DATA,STREAM_DATA,STUDENT_DATA WHERE  STREAM_DATA.ID=? AND CLASS_DATA.ID=? AND STUDENT_STREAM=?  AND STUDENT_CLASS=? ORDER BY STUDENT_CLASS ASC, STUDENT_STREAM ASC,STUDENT_NAME ASC");   
        }
      
      JSONObject data = db.query(builder.toString(), params);
      serv.messageToClient(worker, data);
    }
    
   
    
    
    
    
    
    @Endpoint(name="create_student")
    public void createStudent(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        JSONArray fieldNames = requestData.optJSONArray("field_names");
        JSONArray fieldValues = requestData.optJSONArray("field_values");
        JSONArray fieldData = requestData.optJSONArray("field_data");
        String studentName=requestData.optString("student_name");
        String studentStreamId=requestData.optString("student_stream_id");
        JSONObject classId = db.query("select class_id from class_streams where stream_id=?",studentStreamId);
        String studentClassId=classId.optJSONArray("class_id").optString(0);
        UniqueRandom rand=new UniqueRandom(10);
        String studentId=rand.nextRandom();
        if(studentStreamId.startsWith("AR_")){
           worker.setReason("You cannot create a student in an archived stream");
           serv.messageToClient(worker, Message.FAIL);
           return; 
        }
        if(studentClassId.trim().equals("")){
           worker.setReason("The specified stream does not belong to any class");
           serv.messageToClient(worker, Message.FAIL);
           return;
        }
        StringBuilder builder=new StringBuilder("INSERT INTO STUDENT_DATA (ID,STUDENT_NAME,STUDENT_CLASS,STUDENT_STREAM,CREATED,");
        if(fieldNames.length()==0){
            builder=new StringBuilder("INSERT INTO STUDENT_DATA (ID,STUDENT_NAME,STUDENT_CLASS,STUDENT_STREAM,CREATED) VALUES"
                    + " ('"+studentId+"','"+studentName+"','"+studentClassId+"','"+studentStreamId+"',NOW())");
        }
        else {
              for(int x=0; x<fieldNames.length(); x++){
                  String colName=fieldNames.optString(x).replace(" ", "_");
                  if(x==fieldNames.length()-1){
                      builder.append(colName).append(") VALUES('").append(studentId).append("','");
                      builder.append(studentName).append("','").append(studentClassId).append("','");
                      builder.append(studentStreamId).append("' , ").append("NOW()").append(" , ");
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
                        boolean exists=db.ifValueExists(value,"STUDENT_DATA", fieldName);   
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
      
      AccountService.createAccount(studentName,studentId,studentName, "0","student", serv, worker,studentClassId,studentStreamId); //create an account for the student
      db.execute(builder.toString());
      serv.messageToClient(worker, Message.SUCCESS);
    }
    
    
    @Endpoint(name="auto_suggest",shareMethodWith = {"mark_service","account_service"})
    public void autoSuggest(Server serv, ClientWorker worker){
       JSONObject requestData=worker.getRequestData();
      String fieldName = requestData.optString("field_name");
      String type = requestData.optString("type");
      String like=requestData.optString("like");
      String tableName="";
      if("student".equals(type)){
          tableName="STUDENT_DATA";
      }
      else if("teacher".equals(type)){
          tableName="TEACHER_DATA";
      }
      else if("class".equals(type)){
          tableName="CLASS_DATA";
      }
      else if("subject".equals(type)){
          tableName="SUBJECT_DATA";
      }
      else if("stream".equals(type)){
          tableName="STREAM_DATA";
      }
      else if("paper".equals(type)){
          tableName="PAPER_DATA";
      }
       else if("exam".equals(type)){
          tableName="EXAM_DATA";
      }
      //select class_name from class_data  where class_name like 'e%';
      StringBuilder builder=new StringBuilder("SELECT ID, ").append(fieldName).append(" FROM ");
      builder.append(tableName).append(" WHERE ").append(fieldName);
      builder.append(" LIKE '%").append(like).append("%' LIMIT 10");// ORDER BY ").append(fieldName).append(" ASC");

      JSONObject data = db.query(builder.toString());
      serv.messageToClient(worker, data);
    }
    
    @Endpoint(name="remove_teacher_privilege")
    public void removeTeacherPrivilege(Server serv, ClientWorker worker){
        try {
            JSONObject requestData=worker.getRequestData();
            String teacherId = requestData.optString("teacher_id");
            String privName=requestData.optString("priv_name"); 
            String name=db.query("SELECT TEACHER_NAME FROM TEACHER_DATA WHERE ID=?",teacherId).optJSONArray("TEACHER_NAME").optString(0);
            User user=User.getExistingUser(name, serv);
            user.revokePrivileges(PermanentPrivilege.getPrivilege(privName, serv));
            serv.messageToClient(worker,Message.SUCCESS);
        } catch (Exception ex) {
            Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="remove_child_object")
    public void removeChildObject(Server serv, ClientWorker worker){
      JSONObject requestData=worker.getRequestData();
      String type = requestData.optString("type");
      String id=requestData.optString("id");
      String parentId=requestData.optString("parent_id");
      String relationTable="";
      String relationColumn="";
      String relationColumn1="";
      if("teacher".equals(type)){
          relationTable="TEACHER_SUBJECTS";
          relationColumn="SUBJECT_ID";
          relationColumn1="TEACHER_ID";
      }
      else if("class".equals(type)){
          relationTable="CLASS_STREAMS";
          relationColumn="STREAM_ID";
          relationColumn1="CLASS_ID";
      }
      else if("subject".equals(type)){
          relationTable="SUBJECT_PAPERS";
          relationColumn="PAPER_ID";
          relationColumn1="SUBJECT_ID";
      }
      else if("stream".equals(type)){
          relationTable="STREAM_SUBJECTS";
          relationColumn="SUBJECT_ID";
          relationColumn1="STREAM_ID";
          
      }
     //e.g DELETE FROM STREAM_SUBJECTS WHERE SUBJECT_ID =rurf AND STREAM_ID=ajssj
     db.execute("DELETE FROM "+relationTable+" WHERE "+relationColumn+" = '"+id+"' AND  "+relationColumn1+" = '"+parentId+"'");
     serv.messageToClient(worker,Message.SUCCESS);
    }
    
    @Endpoint(name="complete_auto_suggest",shareMethodWith = {"mark_service","account_service"})
    public void completeAutoSuggest(Server serv, ClientWorker worker){
      JSONObject requestData=worker.getRequestData();
      String type = requestData.optString("type");
      String id=requestData.optString("id");
      String tableName="";
      String relationTable="";
      String relationColumn="";
      String requiredColumn="";
      String tableName1="";
      String relationColumn1="";
      String extraSql="";
      JSONObject teacherPrivs=new JSONObject();
      if("student".equals(type)){
          tableName="STUDENT_DATA";
      }
      else if("teacher".equals(type)){
          try {
              tableName="TEACHER_DATA";
              tableName1="SUBJECT_DATA";
              relationTable="TEACHER_SUBJECTS,STREAM_DATA";
              relationColumn="TEACHER_ID";
              relationColumn1="SUBJECT_ID";
              requiredColumn="SUBJECT_NAME,STREAM_NAME";
              extraSql="AND STREAM_DATA.ID=TEACHER_SUBJECTS.STREAM_ID";
              String name=db.query("SELECT TEACHER_NAME FROM TEACHER_DATA WHERE ID=?",id).optJSONArray("TEACHER_NAME").optString(0);
              User user=User.getExistingUser(name, serv);
              teacherPrivs=new JSONObject(user.getUserPrivileges());
          } catch (NonExistentUserException ex) {
              Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
      else if("class".equals(type)){
          tableName="CLASS_DATA";
          tableName1="STREAM_DATA";
          relationTable="CLASS_STREAMS";
          relationColumn="CLASS_ID";
          requiredColumn="STREAM_NAME";
          relationColumn1="STREAM_ID";
      }
      else if("subject".equals(type)){
          tableName="SUBJECT_DATA";
          tableName1="PAPER_DATA";
          relationTable="SUBJECT_PAPERS";
          relationColumn="SUBJECT_ID";
          requiredColumn="PAPER_NAME";
           relationColumn1="PAPER_ID";
      }
      else if("stream".equals(type)){
          tableName="STREAM_DATA";
          tableName1="SUBJECT_DATA";
          relationTable="STREAM_SUBJECTS";
          relationColumn="STREAM_ID";
          requiredColumn="SUBJECT_NAME";
          relationColumn1="SUBJECT_ID";
      }
      else if("paper".equals(type)){
          tableName="PAPER_DATA";
      }
       else if("exam".equals(type)){
          tableName="EXAM_DATA";
      }
      //SELECT PAPER_NAME FROM PAPER_DATA,SUBJECT_PAPERS WHERE  ID=PAPER_ID AND SUBJECT_ID='l9wgbn4q';
      StringBuilder builder=new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE ID=? ");
      JSONObject data = db.query(builder.toString(),id);
     
      JSONObject data1=new JSONObject();
      JSONObject all=new JSONObject();
      if(!relationTable.isEmpty()){
        StringBuilder builder1=new StringBuilder("SELECT "+tableName1+".ID, "+requiredColumn+" FROM ").append(tableName1).append(" ,").append(relationTable).append(" ");
        builder1.append(" WHERE ").append(tableName1).append(".ID=").append(relationColumn1).append(" AND ").append(relationColumn).append(" =? ").append(extraSql);
        data1 = db.query(builder1.toString(),new String[]{id});
      }
        try {
            all.put("relation", data1);
            all.put("data",data);
            all.put("key",requiredColumn);
            all.put("teacher_privs",teacherPrivs);
        } catch (JSONException ex) {
            Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
        }
      serv.messageToClient(worker, all);
    }
    
    
    @Endpoint(name="save_student_history")
    public void saveStudentHistory(Server serv, ClientWorker worker){
      JSONObject requestData = worker.getRequestData();
      String hist = requestData.optString("history");
      String userid=(String) worker.getSession().getAttribute("userid");
      String studentId = requestData.optString("student_id");
      UniqueRandom rand=new UniqueRandom(20);
      db.doInsert("STUDENT_HISTORY", new String[]{rand.nextMixedRandom(),studentId,hist,userid,"!NOW()"});
      serv.messageToClient(worker, Message.SUCCESS);
    }
    
    @Endpoint(name="all_student_entries")
    public void allStudentHistory(Server serv, ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String studentId = requestData.optString("student_id");
            JSONObject data = db.query("SELECT SCHOOL_DATA.STUDENT_HISTORY.HISTORY,SCHOOL_DATA.STUDENT_HISTORY.ID,USER_SERVER.USERS.USER_NAME,USER_SERVER.USERS.USER_ID,"
                    + " SCHOOL_DATA.STUDENT_HISTORY.CREATED  FROM SCHOOL_DATA.STUDENT_HISTORY,USER_SERVER.USERS WHERE "
                    + " SCHOOL_DATA.STUDENT_HISTORY.STUDENT_ID=? AND USER_SERVER.USERS.USER_ID = SCHOOL_DATA.STUDENT_HISTORY.OWNER_ID ORDER BY SCHOOL_DATA.STUDENT_HISTORY.CREATED DESC ",studentId);
            String userid=(String) worker.getSession().getAttribute("userid");
            JSONObject obj=new JSONObject();
            obj.put("userid", userid);
            obj.put("data", data);
            serv.messageToClient(worker,obj);
        } catch (JSONException ex) {
            Logger.getLogger(StudentService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="remove_student_history")
    public void removeStudentHistory(Server serv, ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        String recId = requestData.optString("record_id");
        Datastore.executeQuery("DELETE FROM STUDENT_HISTORY WHERE ID=?",db.getDatabaseName(),recId);
        serv.messageToClient(worker, Message.SUCCESS);
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
    
}

*
*/
