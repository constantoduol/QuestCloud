
/*
 * 
 *
package com.quest.school;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.quest.access.common.UniqueRandom;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.control.Server;
import com.quest.access.useraccess.Serviceable;
import com.quest.access.useraccess.services.annotations.Endpoint;
import com.quest.access.useraccess.services.annotations.WebService;
import com.quest.servlets.ClientWorker;





@WebService (name = "mark_service", level = 10, privileged = "yes")
public class MarkService implements Serviceable {
    private static Datastore db;
    private static ScriptEngine engine;
    @Override
    public void service() {
      //dummy method 
    }
    
    
    @Endpoint(name="all_exams")
    public void allExams(Server serv,ClientWorker worker){
       JSONObject data = db.query("SELECT * FROM EXAM_DATA ORDER BY EXAM_DEADLINE ASC");
       serv.messageToClient(worker,data); 
    }
    
    @Endpoint(name="student_report_data")
    public void studentReportData(Server serv,ClientWorker worker){
       JSONObject requestData = worker.getRequestData();
       String classId=requestData.optString("class_id");
       String streamId=requestData.optString("stream_id");
       JSONObject students;
       if(streamId.equals("all")){
          students=db.query("SELECT ID, STUDENT_NAME, STUDENT_CLASS, STUDENT_STREAM FROM STUDENT_DATA WHERE STUDENT_CLASS=? ORDER BY STUDENT_NAME ASC", classId);
       }
       else{
         students=db.query("SELECT ID, STUDENT_NAME, STUDENT_CLASS, STUDENT_STREAM FROM STUDENT_DATA WHERE STUDENT_CLASS=?  "
                 + "AND STUDENT_STREAM=? ORDER BY STUDENT_NAME ASC", classId,streamId);
       }
       serv.messageToClient(worker,students);
    }
    
    
    
    @Endpoint(name="fetch_mark_sheet_design",shareMethodWith = {"edit_mark_service"})
    public void fetchMarkSheetDesign(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        String classId=requestData.optString("class_id");  
        JSONObject data = db.query("SELECT SUBJECT_ID,DESIGN FROM MARK_SHEET_DESIGN WHERE CLASS_ID=?",classId);
        serv.messageToClient(worker, data);
    }
    
    
    @Endpoint(name="all_exams_and_relations")
    public void allExamsAndRelations(Server serv,ClientWorker worker){
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
        builder.append(" FROM EXAM_DATA");
        JSONObject data = db.query(builder.toString());
        serv.messageToClient(worker,data); 
    }
    
    @Endpoint(name="all_archive_exams_and_relations")
    public void allArchiveExamsAndRelations(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            JSONArray fields=requestData.optJSONArray("fields");  
            StringBuilder builder=new StringBuilder("SELECT ID, ");
            if(fields.length()==0){
               builder=new StringBuilder("SELECT ID");
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
              builder.append(" FROM EXAM_ARCHIVE_DATA");
              JSONObject data = db.query(builder.toString());
              JSONArray restore = new JSONArray();
              int length = data.optJSONArray("EXAM_NAME").length();
              for(int x=0; x<length; x++){
                String id = data.optJSONArray("ID").optString(x);
                restore.put("<a href='#' onclick=undeleteExam('"+id+"')>Restore Exam</a>");
              }
              data.remove("ID");
              data.put("RESTORE",restore);
              serv.messageToClient(worker,data);
        } catch (JSONException ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private JSONObject generateReportTemplate(){
        try {
            //here we are going to generate the address block
            //the student details block
            //the subject comments,scores and grades for each subject
            //the teachers and principal's comments
            //the teachers who teach specific subjects in specific streams
            JSONObject data = db.query("SELECT * FROM REPORT_FORM_DATA ORDER BY CREATED ASC");
            JSONObject schoolDetails=db.query("SELECT * FROM SCHOOL_DETAILS");
            List schoolKeys = schoolDetails.optJSONArray("SCHOOL_KEY").toList();
            List schoolValues=schoolDetails.optJSONArray("SCHOOL_VALUE").toList();
            List fieldTypes = data.optJSONArray("FIELD_TYPE").toList();
            List fieldValues = data.optJSONArray("FIELD_VALUE").toList();
            String scAddress="";
            String scEmail="";
            String scWeb="";
            String scTel="";
            String scName="";
            String scExtra="";
           try {
            int sIndex=fieldTypes.indexOf("sc_address");
            if(sIndex==-1){
               scAddress="";
            }
            else{
               int index = schoolKeys.indexOf(fieldValues.get(sIndex));
               scAddress=(String) schoolValues.get(index); 
            }
            
            int sIndex1=fieldTypes.indexOf("sc_email");
            if(sIndex1==-1){
               scEmail="";
            }
            else{
               int index1 = schoolKeys.indexOf(fieldValues.get(sIndex1));
               scEmail=(String) schoolValues.get(index1); 
            }
            
            int sIndex2=fieldTypes.indexOf("sc_web");
            if(sIndex2==-1){
               scWeb="";
            }
            else{
               int index2 = schoolKeys.indexOf(fieldValues.get(sIndex2));
               scWeb=(String) schoolValues.get(index2); 
            }
            
            int sIndex3=fieldTypes.indexOf("sc_tel");
            if(sIndex3==-1){
               scTel="";
            }
            else{
               int index3 = schoolKeys.indexOf(fieldValues.get(sIndex3));
               scTel=(String) schoolValues.get(index3); 
            }
            
            int sIndex4=fieldTypes.indexOf("sc_name");
            if(sIndex4==-1){
               scName="";
            }
            else{
               int index4 = schoolKeys.indexOf(fieldValues.get(sIndex4));
               scName=(String) schoolValues.get(index4); 
            }
            
            int sIndex5=fieldTypes.indexOf("sc_extra_details");
            if(sIndex5==-1){
               scExtra="";
            }
            else{
               int index5 = schoolKeys.indexOf(fieldValues.get(sIndex5));
               scExtra=(String) schoolValues.get(index5);
            }
            } catch (Exception ex) {
               Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
               ex.printStackTrace();
            }
            JSONObject addressBlock=new JSONObject();
            JSONArray studentDetails=new JSONArray();
            JSONArray classTeacher =new JSONArray();
            JSONArray principal =new JSONArray();
            JSONArray subjectDetails =new JSONArray();
            JSONArray meanScore =new JSONArray();
            JSONObject all=new JSONObject();
            addressBlock.put("sc_name", scName);
            addressBlock.put("sc_address",scAddress);
            addressBlock.put("sc_email", scEmail);
            addressBlock.put("sc_web",scWeb);
            addressBlock.put("sc_tel",scTel);
            addressBlock.put("sc_extra_details", scExtra);
            JSONArray ids=data.optJSONArray("FIELD_ID");
            for(int x=0; x<fieldTypes.size(); x++){
               String type=(String) fieldTypes.get(x);
               String value=(String) fieldValues.get(x);
               if(type.equals("student_field")){
                  studentDetails.put(value);
               }
               else if(type.equals("class_teacher")){
                  classTeacher.put(value);
               }
               else if(type.equals("principal")){
                 principal.put(value);
               }
               else if(type.equals("subject_field")){
                  subjectDetails.put(ids.optString(x)+","+value);
               }  
                else if(type.equals("mean_score")){
                  meanScore.put(value);
               } 
            }
            all.put("address_block",addressBlock);
            all.put("student_details",studentDetails);
            all.put("subject_details", subjectDetails);
            all.put("class_teacher", classTeacher);
            all.put("principal",principal);
            all.put("mean_score",meanScore);
            return all;
        } catch (JSONException ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private JSONObject studentDetails(JSONArray details,String studentId){
      try{
       List theDetails=details.toList();
       StringBuilder sql=new StringBuilder("SELECT ");
       if(details.length()==0){
          return new JSONObject();
       }
       for(int x=0; x<details.length(); x++){
           if(x==details.length()-1){
             sql.append(details.optString(x).replace(" ","_"));    
           }
           else{
             sql.append(details.optString(x).replace(" ","_")).append(",");   
           }
       }
       sql.append(" FROM STUDENT_DATA WHERE ID=?");
       JSONObject data=db.query(sql.toString(),studentId);
       if(theDetails.indexOf("STUDENT CLASS")!=-1){
            String clazz=data.optJSONArray("STUDENT_CLASS").optString(0);
            clazz=db.query("SELECT CLASS_NAME FROM CLASS_DATA WHERE ID=?",clazz).optJSONArray("CLASS_NAME").optString(0);
            JSONArray sClass = data.optJSONArray("STUDENT_CLASS");
            if(sClass!=null){
               sClass.put(0, clazz);
            }
       }
       
       if(theDetails.indexOf("STUDENT STREAM")!=-1){
             String stream=data.optJSONArray("STUDENT_STREAM").optString(0);
             stream=db.query("SELECT STREAM_NAME FROM STREAM_DATA WHERE ID=?",stream).optJSONArray("STREAM_NAME").optString(0);
             JSONArray sStream = data.optJSONArray("STUDENT_STREAM");
               if(sStream!=null){
                 sStream.put(0, stream);
               }
         }
       return data;
      }
      catch(Exception e){
         return null;
      }
    }
    
    
   public static JSONObject openAdvancedReportForm(String studentId,String examIds,String streamId,String classId){
      try {
         StringTokenizer st=new StringTokenizer(examIds,",");
         ArrayList<String> subList=new ArrayList();
         int tokenCount=0;
         while(st.hasMoreTokens()){
            if(tokenCount==3){
              break;
            }
            subList.add(st.nextToken());
            tokenCount++;
         }
         JSONObject studentClassData=db.query("SELECT ID, STUDENT_NAME FROM STUDENT_DATA WHERE STUDENT_CLASS=?",classId);
         String studentStream=db.query("SELECT STUDENT_STREAM FROM STUDENT_DATA WHERE ID=?",studentId).optJSONArray("STUDENT_STREAM").optString(0);
         JSONObject teacherData = db.query("SELECT TEACHER_NAME,SUBJECT_ID FROM TEACHER_SUBJECTS,TEACHER_DATA WHERE STREAM_ID=? AND TEACHER_DATA.ID=TEACHER_SUBJECTS.TEACHER_ID",studentStream);
         double accountBalance=db.query("SELECT ACCOUNT_BALANCE FROM ACCOUNT_DATA WHERE ID=? ",studentId).optJSONArray("ACCOUNT_BALANCE").optDouble(0);
         JSONObject design = db.query("SELECT * FROM MARK_SHEET_DESIGN WHERE CLASS_ID=?",classId);
         String grandFormula = design.optJSONArray("DESIGN").optString(design.optJSONArray("SUBJECT_ID").toList().indexOf("grand"));
         String averageFormula = design.optJSONArray("DESIGN").optString(design.optJSONArray("SUBJECT_ID").toList().indexOf("average"));
         String streamName = streamId.equals("all") ? "all" : db.query("SELECT STREAM_NAME FROM STREAM_DATA WHERE ID=?",streamId).optJSONArray("STREAM_NAME").optString(0);
         streamName = streamName.toLowerCase().replace(" ", "_");
         JSONObject allSubjects = db.query("SELECT ID,SUBJECT_NAME FROM SUBJECT_DATA");
         JSONObject streamMarkData;
         JSONObject studentStreamData;
         JSONObject subjectData;
         if(streamId.equals("all")){
             studentStreamData=db.query("SELECT ID, STUDENT_NAME FROM STUDENT_DATA WHERE  STUDENT_CLASS=?",classId);
               //on the report we want to see subjects that the student is doing currently and not subjects that dont exist
               //or have been removed
               JSONObject streamIds = db.query("SELECT STREAM_ID FROM CLASS_STREAMS WHERE CLASS_ID=? ",classId);
               JSONArray streams=streamIds.optJSONArray("STREAM_ID");
               ArrayList found=new ArrayList();
               JSONObject allCurrentSubjects=new JSONObject();
               for(int n=0; n<streams.length(); n++){
                  subjectData=db.query("SELECT  ID, SUBJECT_NAME,CREATED,SUBJECT_ID FROM SUBJECT_DATA,STREAM_SUBJECTS WHERE SUBJECT_DATA.ID=STREAM_SUBJECTS.SUBJECT_ID AND STREAM_SUBJECTS.STREAM_ID=? ORDER BY CREATED ASC",streams.optString(n));
                  for(int m=0; m<subjectData.optJSONArray("SUBJECT_NAME").length(); m++){
                     String subjectName=subjectData.optJSONArray("SUBJECT_NAME").optString(m);
                     if(found.indexOf(subjectName)==-1){
                         allCurrentSubjects.accumulate("SUBJECT_NAME",subjectName);
                         allCurrentSubjects.accumulate("CREATED",subjectData.optJSONArray("CREATED").optString(m));
                         allCurrentSubjects.accumulate("ID",subjectData.optJSONArray("ID").optString(m));
                         found.add(subjectName);
                     }
                  }
               }
               subjectData=allCurrentSubjects;
         }
         else{
             studentStreamData=db.query("SELECT ID, STUDENT_NAME FROM STUDENT_DATA WHERE STUDENT_STREAM=? AND STUDENT_CLASS=?",streamId,classId);  
             subjectData=db.query("SELECT  ID, SUBJECT_NAME,CREATED FROM SUBJECT_DATA,STREAM_SUBJECTS WHERE SUBJECT_DATA.ID=STREAM_SUBJECTS.SUBJECT_ID AND STREAM_SUBJECTS.STREAM_ID=? ORDER BY CREATED ASC",streamId);
         }   
         int totalStudentsInStream = studentStreamData.optJSONArray("ID").length();
         int totalStudentsInClass = studentClassData.optJSONArray("ID").length();
         JSONArray allStudentMarks=new JSONArray();
         JSONArray allStreamMarkData=new JSONArray();
         JSONArray allClassMarkData=new JSONArray();
         for(int m=subList.size()-1; m>-1;m--){
            String currentExamId=subList.get(m);
           //select marks for subjects only,ignore papers
            JSONObject markData = db.query("SELECT STUDENT_ID, SUBJECT_ID, PAPER_ID, MARK_VALUE FROM MARK_DATA WHERE EXAM_ID = ? AND STUDENT_ID=? AND SUBJECT_ID=PAPER_ID ORDER BY SUBJECT_ID DESC",currentExamId,studentId);
            allStudentMarks.put(markData);
            JSONObject classMarkData = db.query("SELECT STUDENT_ID, SUBJECT_ID, PAPER_ID, MARK_VALUE FROM MARK_DATA WHERE EXAM_ID = ? AND  CLASS_ID=? AND SUBJECT_ID=PAPER_ID ORDER BY STUDENT_ID ASC,SUBJECT_ID ASC",currentExamId,classId);
            if(streamId.equals("all")){
                streamMarkData = classMarkData;
            }
            else{
               streamMarkData = db.query("SELECT STUDENT_ID, SUBJECT_ID, PAPER_ID, MARK_VALUE FROM MARK_DATA WHERE EXAM_ID = ? AND STREAM_ID=? AND CLASS_ID=? AND SUBJECT_ID=PAPER_ID ORDER BY STUDENT_ID ASC,SUBJECT_ID ASC",currentExamId,streamId,classId);  
            }
            
            allStreamMarkData.put(streamMarkData);
            allClassMarkData.put(classMarkData);
           
            //--------------------------stream
           
            //now we need to know the rank per subject
            //we need to know the total score
            //we need to know the rank in stream
            //we need to know the rank in class
        }
         
         JSONArray studentIdStreams=allStreamMarkData.optJSONObject(0).optJSONArray("STUDENT_ID");
         JSONArray studentIdClasses=allClassMarkData.optJSONObject(0).optJSONArray("STUDENT_ID");
         JSONArray subjectIdClasses=allClassMarkData.optJSONObject(0).optJSONArray("SUBJECT_ID");
         //here find the mean of the three exams
        JSONArray avMarks=new JSONArray(); //average marks of the 3 exams
        if(allStudentMarks.length()==1){
              JSONArray marks= allStudentMarks.optJSONObject(0).optJSONArray("MARK_VALUE"); 
              for(int x=0; x<marks.length(); x++){
                Double mark = marks.optDouble(x,0);
                avMarks.put(mark);
           }
        }
        else if(allStudentMarks.length()==2){
           JSONArray marks= allStudentMarks.optJSONObject(0).optJSONArray("MARK_VALUE"); 
           JSONArray marks1= allStudentMarks.optJSONObject(1).optJSONArray("MARK_VALUE");
           for(int x=0; x<marks.length(); x++){
               Double mark = marks.optDouble(x,0);
               Double mark1= marks1.optDouble(x,0);
               avMarks.put((mark+mark1)/2);
           }  
        }
        else {
           JSONArray marks= allStudentMarks.optJSONObject(0).optJSONArray("MARK_VALUE"); 
           JSONArray marks1= allStudentMarks.optJSONObject(1).optJSONArray("MARK_VALUE"); 
           JSONArray marks2=allStudentMarks.optJSONObject(2).optJSONArray("MARK_VALUE"); 
           for(int x=0; x<marks.length(); x++){
               double mark = marks.optDouble(x,0);
               double mark1= marks1.optDouble(x,0);
               double mark2 = marks2.optDouble(x,0);
               avMarks.put((mark+mark1+mark2)/3);
            }   
          }
            JSONArray subjectNames=subjectData.optJSONArray("SUBJECT_NAME");
            JSONArray currentSubjectIds = subjectData.optJSONArray("ID");
            JSONArray studentSubjectIds = allStudentMarks.optJSONObject(0).optJSONArray("SUBJECT_ID");
            //Integer subjectCount = countOccurrences(grandFormula, "+")+1;
            Integer subjectCount = avMarks.length();
            int grandTotal = subjectCount*100;
            grandFormula=grandFormula.replace("allSubjectsLength", ((Integer)subjectNames.length()).toString() );
            grandFormula=grandFormula.replace("enteredSubjectsLength", subjectCount.toString());
            grandFormula=grandFormula.replace("currentStream","'"+streamName+"'");
            averageFormula=averageFormula.replace("allSubjectsLength", ((Integer)subjectNames.length()).toString() );
            averageFormula=averageFormula.replace("enteredSubjectsLength", subjectCount.toString());
            averageFormula=averageFormula.replace("currentStream","'"+streamName+"'");
            
            JSONArray allSubjectNames = allSubjects.optJSONArray("SUBJECT_NAME");
            JSONArray allSubjectIds = allSubjects.optJSONArray("ID");
            for(int x=0; x<allSubjectIds.length(); x++){ //here we go through all subjects created on the system
              String subjectName = allSubjectNames.optString(x).toLowerCase().replace(" ","_");
              String subjectId = allSubjectIds.optString(x);
              int index = studentSubjectIds.toList().indexOf(subjectId);
              int index1 = currentSubjectIds.toList().indexOf(subjectId); //this will tell us if this stream is still doing this subject
              //if index1 == -1 it means this student no longer does this subject
              //if it is greater than -1 it means the student still does the subject
              Double mark = index > -1 && index1 > -1 ? avMarks.optDouble(index) : 0; 
              grandFormula=grandFormula.replace(subjectName, mark.toString());
              averageFormula=averageFormula.replace(subjectName, mark.toString());
            }
            
            Double total=(Double) engine.eval(grandFormula);
            Double average=(Double) engine.eval(averageFormula);
            JSONArray streamMarks=new JSONArray();
             //average marks of the 3 exams
              if(allStudentMarks.length()==1){
                 JSONArray marks= allStreamMarkData.optJSONObject(0).optJSONArray("MARK_VALUE");
                 for(int y=0; y<marks.length(); y++){
                   double mark = marks.optDouble(y,0);
                   streamMarks.put(mark);
                 }
               }
              else if(allStudentMarks.length()==2){
                JSONArray marks= allStreamMarkData.optJSONObject(0).optJSONArray("MARK_VALUE");
                JSONArray marks1= allStreamMarkData.optJSONObject(1).optJSONArray("MARK_VALUE");
                for(int y=0; y<marks.length(); y++){
                  double mark = marks.optDouble(y,0);
                  double mark1= marks1.optDouble(y,0);
                  streamMarks.put((mark+mark1)/2);
                  
               }  
             }
            else {
              JSONArray marks= allStreamMarkData.optJSONObject(0).optJSONArray("MARK_VALUE");
              JSONArray marks1= allStreamMarkData.optJSONObject(1).optJSONArray("MARK_VALUE");
              JSONArray marks2= allStreamMarkData.optJSONObject(2).optJSONArray("MARK_VALUE");
              for(int y=0; y<marks.length(); y++){
                double mark = marks.optDouble(y,0);
                double mark1= marks1.optDouble(y,0);
                double mark2 = marks2.optDouble(y,0);
                streamMarks.put((mark+mark1+mark2)/3);
              }   
             } 
            
            
            
              JSONArray classMarks=new JSONArray();
             //average marks of the 3 exams
              if(allStudentMarks.length()==1){
                 JSONArray marks= allClassMarkData.optJSONObject(0).optJSONArray("MARK_VALUE");
                 for(int y=0; y<marks.length(); y++){
                   double mark = marks.optDouble(y,0);
                   classMarks.put(mark);
                 }
              }
             else if(allStudentMarks.length()==2){
                JSONArray marks= allClassMarkData.optJSONObject(0).optJSONArray("MARK_VALUE");
                JSONArray marks1= allClassMarkData.optJSONObject(1).optJSONArray("MARK_VALUE");
                for(int y=0; y<marks.length(); y++){
                  double mark = marks.optDouble(y,0);
                  double mark1= marks1.optDouble(y,0);
                  classMarks.put((mark+mark1)/2);
               }  
            }
           else {
              JSONArray marks= allClassMarkData.optJSONObject(0).optJSONArray("MARK_VALUE");
              JSONArray marks1= allClassMarkData.optJSONObject(1).optJSONArray("MARK_VALUE");
              JSONArray marks2= allClassMarkData.optJSONObject(2).optJSONArray("MARK_VALUE");
              for(int y=0; y<marks.length(); y++){
                double mark = marks.optDouble(y,0);
                double mark1= marks1.optDouble(y,0);
                double mark2 = marks2.optDouble(y,0);
                classMarks.put((mark+mark1+mark2)/3);
              }   
             }
            
            HashMap streamRankData=new HashMap();
            for(int x=0; x<studentIdStreams.length(); x++){ //this will tell us the ranks of students in streams
               String id=studentIdStreams.optString(x);
               Double score=(Double) streamRankData.get(id);
               if(score==null){ 
                  score=0.0; 
               }
               double subScore = streamMarks.optDouble(x);
               score=score+subScore;
               streamRankData.put(id, score);
            }
        
           //------------------------------------
           //------------------classes
            HashMap classRankData=new HashMap();
            for(int x=0; x<subjectIdClasses.length(); x++){ //this will tell us the ranks of students in classes
               String id=studentIdClasses.optString(x);
               Double score=(Double) classRankData.get(id);
               if(score==null){
                  score=0.0; 
               }
               double subScore = classMarks.optDouble(x);
               score=score+subScore;
               classRankData.put(id, score);
            }
            
           ArrayList listStreams=new ArrayList();
           listStreams.addAll(streamRankData.keySet());
           int streamCount=0;
           for(int x=0; x<listStreams.size(); x++){
              String id=(String) listStreams.get(x);
              Double score=(Double) streamRankData.get(id);
              if(score==null){
                score=0.0;  
              }
              if(score>total){
                 streamCount++;
              } 
           }
            
            
           ArrayList listClasses=new ArrayList();
           listClasses.addAll(classRankData.keySet());
           int classCount=0;
           for(int x=0; x<listClasses.size(); x++){
              String id=(String) listClasses.get(x);
              Double score=(Double) classRankData.get(id);
              if(score==null){
                 score=0.0; 
              }
              if(score>total){
                 classCount++;
              }
           }
           
            JSONObject subjectRankData=new JSONObject();
            JSONArray studentSubjects=allStudentMarks.optJSONObject(0).optJSONArray("SUBJECT_ID");
            int count=0;
            for(int y=0; y<studentSubjects.length(); y++){
              String subId=studentSubjects.optString(y); //the current subject
              double currentValue=avMarks.optDouble(y); //the current mark
              subjectRankData.put(subId,1);
              for(int x=0; x<subjectIdClasses.length(); x++){
                  double markValue=classMarks.optDouble(x);
                  String dbSubId=subjectIdClasses.optString(x);
                  if(currentValue > markValue && subId.equals(dbSubId)){
                    count++;
                    subjectRankData.put(subId,count+1);
                  }
              }
              count=0;
            }
            
            JSONObject obj=new JSONObject();
            obj.put("grand_total", total);
            obj.put("max_score", grandTotal);
            average = average == null || average.isNaN() ? 0 : average;
            obj.put("average",average);
            obj.put("total_in_stream", totalStudentsInStream);
            obj.put("total_in_class", totalStudentsInClass);
            obj.put("stream_rank", streamCount+1);
            obj.put("average_marks", avMarks);
            obj.put("class_rank", classCount+1);
            obj.put("subject_ranks",subjectRankData);
            obj.put("student_marks", allStudentMarks);
            obj.put("subject_data", subjectData);
            obj.put("fee_balance", accountBalance);  
            obj.put("teacher_data",teacherData);
            obj.put("formulas",design);
            return obj;
       }
       catch(Exception e){
         return null; 
      }  
    }
   
   
    
    @Endpoint(name="student_trend")
    public void fetchStudentTrend(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String studentId=requestData.optString("student_id");
            JSONObject examData=db.query("SELECT * FROM EXAM_DATA ORDER BY EXAM_DEADLINE DESC");
            JSONObject subjectData=db.query("SELECT * FROM SUBJECT_DATA");
            JSONArray subjectNames = subjectData.optJSONArray("SUBJECT_NAME");
            JSONArray orderedSubjectIds = subjectData.optJSONArray("ID");
            JSONArray examIds=examData.optJSONArray("ID");
            JSONArray allSubjects=new JSONArray();
            JSONObject average = new JSONObject();
            JSONObject subject=new JSONObject();
            JSONArray theExams=new JSONArray();
            for(int x=0; x<examIds.length(); x++){
               String examId=examIds.optString(x);
               JSONObject markData = db.query("SELECT SUBJECT_ID, PAPER_ID, MARK_VALUE FROM MARK_DATA WHERE EXAM_ID = ? AND  STUDENT_ID=? AND SUBJECT_ID=PAPER_ID ORDER BY SUBJECT_ID DESC",examId,studentId);
               JSONArray subjectIds=markData.optJSONArray("SUBJECT_ID");
               JSONArray marks=markData.optJSONArray("MARK_VALUE");
               float examAverage=0f;
               for(int y=0; y<subjectIds.length(); y++){
                  String subjectId=subjectIds.optString(y);
                  String markValue = marks.optString(y);
                  if(markValue.trim().equals("")){
                     markValue="0"; 
                  }
                  float mark = Float.parseFloat(markValue);
                  examAverage=examAverage+mark;
                  int index=orderedSubjectIds.toList().indexOf(subjectId);
                  String subjectName=subjectNames.optString(index);
                  subject.accumulate(subjectName, markValue);
               }
               examAverage=examAverage/subjectIds.length();
               if(subjectIds.length()==0){
                  
               }
               else{
                   average.accumulate("average",examAverage);
                   theExams.put(examData.optJSONArray("EXAM_NAME").optString(x).replace("_", " "));
               }
            }
            allSubjects.put(subject);
            JSONObject all=new JSONObject();
            all.put("subject_data",allSubjects);
            all.put("average", average);
            all.put("exam_names",theExams);
            serv.messageToClient(worker,all);
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    private static int countOccurrences(String str,String theString){
        int lastIndex = 0;
        int count =0;
        while(lastIndex != -1){
            lastIndex = str.indexOf(theString,lastIndex);
            if( lastIndex != -1){
             count ++;
             lastIndex+=theString.length();
          }
        }
        return count;
    } 
   
    public static JSONObject openSingleReportForm(String studentId,String examIds,String streamId,String classId){
        try {
            StringTokenizer st=new StringTokenizer(examIds,",");
            String examId=st.nextToken();
            //select marks for subjects only,ignore papers
            String streamName = streamId.equals("all") ? "all" : db.query("SELECT STREAM_NAME FROM STREAM_DATA WHERE ID=?",streamId).optJSONArray("STREAM_NAME").optString(0);
            streamName = streamName.toLowerCase().replace(" ", "_");
            JSONObject markData = db.query("SELECT STUDENT_ID, SUBJECT_ID, PAPER_ID, MARK_VALUE FROM MARK_DATA WHERE EXAM_ID = ? AND STUDENT_ID=? AND SUBJECT_ID=PAPER_ID",examId,studentId);
            JSONArray marks=markData.optJSONArray("MARK_VALUE");
            JSONObject design = db.query("SELECT * FROM MARK_SHEET_DESIGN WHERE CLASS_ID=?",classId);
            String grandFormula = design.optJSONArray("DESIGN").optString(design.optJSONArray("SUBJECT_ID").toList().indexOf("grand"));
            String averageFormula = design.optJSONArray("DESIGN").optString(design.optJSONArray("SUBJECT_ID").toList().indexOf("average"));
            JSONObject classMarkData = db.query("SELECT STUDENT_ID, SUBJECT_ID, PAPER_ID, MARK_VALUE FROM MARK_DATA WHERE EXAM_ID = ? AND  CLASS_ID=? AND SUBJECT_ID=PAPER_ID",examId,classId);
            JSONObject studentClassData=db.query("SELECT ID, STUDENT_NAME FROM STUDENT_DATA WHERE STUDENT_CLASS=?",classId);
            String studentStream=db.query("SELECT STUDENT_STREAM FROM STUDENT_DATA WHERE ID=?",studentId).optJSONArray("STUDENT_STREAM").optString(0);
            JSONObject teacherData = db.query("SELECT TEACHER_NAME,SUBJECT_ID FROM TEACHER_SUBJECTS,TEACHER_DATA WHERE STREAM_ID=? AND TEACHER_DATA.ID=TEACHER_SUBJECTS.TEACHER_ID",studentStream);
            JSONObject allSubjects = db.query("SELECT ID,SUBJECT_NAME FROM SUBJECT_DATA");
            double accountBalance=db.query("SELECT ACCOUNT_BALANCE FROM ACCOUNT_DATA WHERE ID=? ",studentId).optJSONArray("ACCOUNT_BALANCE").optDouble(0);
            JSONObject streamMarkData;
            JSONObject studentStreamData;
            JSONObject subjectData;
            if(streamId.equals("all")){
                streamMarkData = db.query("SELECT STUDENT_ID, SUBJECT_ID, PAPER_ID, MARK_VALUE FROM MARK_DATA WHERE EXAM_ID = ? AND CLASS_ID=? AND SUBJECT_ID=PAPER_ID",examId,classId);
                studentStreamData=db.query("SELECT ID, STUDENT_NAME FROM STUDENT_DATA WHERE  STUDENT_CLASS=?",classId);
               //on the report we want to see subjects that the student is doing currently and not subjects that dont exist
               //or have been removed
               JSONObject streamIds = db.query("SELECT STREAM_ID FROM CLASS_STREAMS WHERE CLASS_ID=? ",classId);
               JSONArray streams=streamIds.optJSONArray("STREAM_ID");
               ArrayList found=new ArrayList();
               JSONObject allCurrentSubjects=new JSONObject(); // these are subjects of a single stream if it is selected or for a whole class if it is selected
               for(int n=0; n<streams.length(); n++){
                  subjectData=db.query("SELECT  ID, SUBJECT_NAME,CREATED,SUBJECT_ID FROM SUBJECT_DATA,STREAM_SUBJECTS WHERE SUBJECT_DATA.ID=STREAM_SUBJECTS.SUBJECT_ID AND STREAM_SUBJECTS.STREAM_ID=? ORDER BY CREATED ASC",streams.optString(n));
                  for(int m=0; m<subjectData.optJSONArray("SUBJECT_NAME").length(); m++){
                     String subjectName=subjectData.optJSONArray("SUBJECT_NAME").optString(m);
                     if(found.indexOf(subjectName)==-1){
                         allCurrentSubjects.accumulate("SUBJECT_NAME",subjectName);
                         allCurrentSubjects.accumulate("CREATED",subjectData.optJSONArray("CREATED").optString(m));
                         allCurrentSubjects.accumulate("ID",subjectData.optJSONArray("ID").optString(m));
                         found.add(subjectName);
                     }
                  }
               }
               subjectData=allCurrentSubjects;
            }
            else{
               streamMarkData = db.query("SELECT STUDENT_ID, SUBJECT_ID, PAPER_ID, MARK_VALUE FROM MARK_DATA WHERE EXAM_ID = ? AND STREAM_ID=? AND CLASS_ID=? AND SUBJECT_ID=PAPER_ID",examId,streamId,classId);
               studentStreamData=db.query("SELECT ID, STUDENT_NAME FROM STUDENT_DATA WHERE STUDENT_STREAM=? AND STUDENT_CLASS=?",streamId,classId);  
               subjectData=db.query("SELECT ID, SUBJECT_NAME,CREATED FROM SUBJECT_DATA,STREAM_SUBJECTS WHERE SUBJECT_DATA.ID=STREAM_SUBJECTS.SUBJECT_ID AND STREAM_SUBJECTS.STREAM_ID=? ORDER BY CREATED ASC",streamId);
            }
            
            JSONArray subjectNames=subjectData.optJSONArray("SUBJECT_NAME");
            JSONArray studentSubjectIds = markData.optJSONArray("SUBJECT_ID");
            JSONArray currentSubjectIds = subjectData.optJSONArray("ID");
            
            Integer subjectCount = marks.length();
            int grandTotal = subjectCount*100;
            grandFormula=grandFormula.replace("allSubjectsLength", ((Integer)subjectNames.length()).toString() );
            grandFormula=grandFormula.replace("enteredSubjectsLength", subjectCount.toString());
            grandFormula=grandFormula.replace("currentStream","'"+streamName+"'");
            averageFormula=averageFormula.replace("allSubjectsLength", ((Integer)subjectNames.length()).toString() );
            averageFormula=averageFormula.replace("enteredSubjectsLength", subjectCount.toString());
            averageFormula=averageFormula.replace("currentStream","'"+streamName+"'");
            
            JSONArray allSubjectNames = allSubjects.optJSONArray("SUBJECT_NAME");
            JSONArray allSubjectIds = allSubjects.optJSONArray("ID");
            for(int x=0; x<allSubjectIds.length(); x++){ //here we go through all subjects created on the system
              String subjectName = allSubjectNames.optString(x).toLowerCase().replace(" ","_");
              String subjectId = allSubjectIds.optString(x);
              int index = studentSubjectIds.toList().indexOf(subjectId);
              int index1 = currentSubjectIds.toList().indexOf(subjectId); //this will tell us if this stream is still doing this subject
              //if index1 == -1 it means this student no longer does this subject
              //if it is greater than -1 it means the student still does the subject
              Double mark = index > -1 && index1 > -1 ? marks.optDouble(index) : 0; 
              grandFormula=grandFormula.replace(subjectName, mark.toString());
              averageFormula=averageFormula.replace(subjectName, mark.toString());
            }
            
            Double total=(Double) engine.eval(grandFormula);
            Double average=(Double) engine.eval(averageFormula);
            
            JSONArray streamMarks=streamMarkData.optJSONArray("MARK_VALUE");
            JSONArray classMarks=classMarkData.optJSONArray("MARK_VALUE");
            int totalStudentsInStream = studentStreamData.optJSONArray("ID").length();
            int totalStudentsInClass = studentClassData.optJSONArray("ID").length();
            JSONArray studentIdStreams=streamMarkData.optJSONArray("STUDENT_ID");
            JSONArray studentIdClasses=classMarkData.optJSONArray("STUDENT_ID");
            JSONArray subjectIdClasses=classMarkData.optJSONArray("SUBJECT_ID");
            //--------------------------stream
            HashMap streamRankData=new HashMap();
            for(int x=0; x<studentIdStreams.length(); x++){ //this will tell us the ranks of students in streams
               String id=studentIdStreams.optString(x);
               Double score=(Double) streamRankData.get(id);
               if(score==null){ 
                  score=0.0; 
               }
               double subScore = streamMarks.optDouble(x);
               score=score+subScore;
               streamRankData.put(id, score);
            }
            
           ArrayList listStreams=new ArrayList();
           listStreams.addAll(streamRankData.keySet());
           int streamCount=0;
           for(int x=0; x<listStreams.size(); x++){
              String id=(String) listStreams.get(x);
              Double score=(Double) streamRankData.get(id);
              if(score==null){
                score=0.0;  
              }
              if(score>total){
                 streamCount++;
              } 
           }
           //------------------------------------
           //------------------classes
            HashMap classRankData=new HashMap();
            for(int x=0; x<subjectIdClasses.length(); x++){ //this will tell us the ranks of students in classes
               String id=studentIdClasses.optString(x);
               Double score=(Double) classRankData.get(id);
               if(score==null){
                  score=0.0; 
               }
            
                 double subScore = classMarks.optDouble(x);
                 score=score+subScore;
                 classRankData.put(id, score);
               
            }
           
           ArrayList listClasses=new ArrayList();
           listClasses.addAll(classRankData.keySet());
           int classCount=0;
           for(int x=0; x<listClasses.size(); x++){
              String id=(String) listClasses.get(x);
              Double score=(Double) classRankData.get(id);
              if(score==null){
                 score=0.0; 
              }
              if(score>total){
                 classCount++;
              }
           }
           
            JSONObject subjectRankData=new JSONObject();
            JSONArray studentSubjects=markData.optJSONArray("SUBJECT_ID");
            
            for(int y=0; y<studentSubjects.length(); y++){
              String subId=studentSubjects.optString(y); //the current subject
              double currentValue=marks.optDouble(y); //the current mark
              subjectRankData.put(subId,1);
              int count=0;
              for(int x=0; x<subjectIdClasses.length(); x++){
                  double markValue=classMarks.optDouble(x);
                  String dbSubId=subjectIdClasses.optString(x);
                  if(markValue > currentValue && subId.equals(dbSubId)){
                    count++;
                    subjectRankData.put(subId,count+1);
                  }
              }
             
            }
            JSONObject obj=new JSONObject();
            obj.put("grand_total", total);// e.g 450
            obj.put("max_score", grandTotal);// e.g 500
            average = average == null || average.isNaN() ? 0 : average;
            obj.put("average",average);//
            obj.put("total_in_stream", totalStudentsInStream);
            obj.put("total_in_class", totalStudentsInClass);
            obj.put("stream_rank", streamCount+1);//
            obj.put("class_rank", classCount+1);//
            obj.put("subject_ranks",subjectRankData);
            obj.put("student_marks", markData);
            obj.put("subject_data", subjectData);
            obj.put("fee_balance", accountBalance);
            obj.put("teacher_data",teacherData);
            obj.put("formulas",design);
            //now we need to know the rank per subject
            //we need to know the total score
            //we need to know the rank in stream
            //we need to know the rank in class
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    
    
    @Endpoint(name="open_report_form")
    public void openReportForm(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String studentId = requestData.optString("student_id");
            String examIds = requestData.optString("exam_ids");
            String streamId=requestData.optString("stream_id");
            String classId=requestData.optString("class_id");
            String templateName = requestData.optString("template");
            JSONObject data=new JSONObject();
            if(templateName.equals("basic")){
               data=openSingleReportForm(studentId, examIds, streamId, classId);
            }
            else if(templateName.equals("advanced")){
               data=openAdvancedReportForm(studentId, examIds, streamId, classId); 
            }
            JSONObject template = generateReportTemplate();
            if(template!=null){
                data.put("report_form_template",template);
                JSONObject studentDetails = studentDetails(template.optJSONArray("student_details"),studentId);
                template.put("student_details",studentDetails);
            }
            serv.messageToClient(worker,data);
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="fetch_report_form_details")
    public void fetchReportFormDetails(Server serv,ClientWorker worker){
        JSONObject data = db.query("SELECT * FROM REPORT_FORM_DATA ORDER BY CREATED ASC");
        serv.messageToClient(worker, data);
    }
    
    @Endpoint(name="all_exams_and_relations")
    public void allPapersAndRelations(Server serv,ClientWorker worker){
      JSONObject requestData = worker.getRequestData();
      JSONArray fields=requestData.optJSONArray("fields");  
      StringBuilder builder=new StringBuilder("SELECT EXAM_NAME , EXAM_DEADLINE, ");
      if(fields.length()==0){
         builder=new StringBuilder("SELECT EXAM_NAME, EXAM_DEADLINE");   
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
        builder.append(" FROM EXAM_DATA");
        JSONObject data = db.query(builder.toString());
        serv.messageToClient(worker,data); 
    }
    
    @Endpoint(name="all_students")
    public void allStudents(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        String classId=requestData.optString("class_id");
        String streamId=requestData.optString("stream_id");
        JSONObject data;
        if(streamId.equals("all")){
          data= db.query("SELECT ID FROM STUDENT_DATA WHERE STUDENT_CLASS=?",classId);  
        }
        else{
           data= db.query("SELECT ID FROM STUDENT_DATA WHERE STUDENT_CLASS=? AND STUDENT_STREAM=?",classId,streamId);
        }
        serv.messageToClient(worker, data);
    }
    
   

    @Override
    public void onCreate() {
        try {
            // create database tables
            db=Datastore.getExistingDatabase("school_data");
            if(!Table.ifTableExists("EXAM_FIELD_META_DATA", db.getDatabaseName())){
                Table fields= db.addTable("FIELD_ID", "EXAM_FIELD_META_DATA", "VARCHAR(10)");
                fields.addColumn("FIELD_NAME", "VARCHAR(50)");
                fields.addColumn("FIELD_REQUIRED", DataType.BOOL);
                fields.addColumn("FIELD_DATA","VARCHAR(10)");
                fields.addColumn("CREATED", DataType.DATETIME);
                UniqueRandom rand=new UniqueRandom(8);
                db.doInsert("EXAM_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"EXAM NAME","1","unique","!NOW()"});
                Thread.sleep(1000);
                db.doInsert("EXAM_FIELD_META_DATA", new String[]{rand.nextMixedRandom(),"EXAM DEADLINE","1","datetime","!NOW()"});
                
         }
         if(!Table.ifTableExists("EXAM_DATA", db.getDatabaseName())){
                 Table table= db.addTable("ID", "EXAM_DATA", "VARCHAR(10)");
                 table.addColumns(new String[]{
                     "EXAM_NAME", "TEXT",
                     "EXAM_DEADLINE",DataType.DATETIME
                 });
          }
          if(!Table.ifTableExists("MARK_DATA",db.getDatabaseName())){ 
                  Table marks=db.addTable("RECORD_ID","MARK_DATA", "VARCHAR(50)");
                  marks.addColumns(new String[]{
                      "EXAM_ID", "TEXT",
                      "CLASS_ID","VARCHAR(10)",
                      "STREAM_ID","VARCHAR(10)",
                      "STUDENT_ID","VARCHAR(10)",
                      "SUBJECT_ID","VARCHAR(10)",
                      "PAPER_ID", "VARCHAR(10)",
                      "MARK_VALUE", DataType.FLOAT,
                      "CREATED", DataType.DATETIME
                    });
                 }
         if(!Table.ifTableExists("EXAM_ARCHIVE_DATA", db.getDatabaseName())){
                 Table table= db.addTable("ID", "EXAM_ARCHIVE_DATA", "VARCHAR(10)");
                 table.addColumns(new String[]{
                     "EXAM_NAME", "TEXT",
                     "EXAM_DEADLINE",DataType.DATETIME
                 });
          }
         if(!Table.ifTableExists("MARK_SHEET_DESIGN", db.getDatabaseName())){
                 Table table= db.addTable("ID", "MARK_SHEET_DESIGN", "VARCHAR(10)");
                 table.addColumns(new String[]{
                     "CLASS_ID","VARCHAR(10)",
                     "SUBJECT_ID","VARCHAR(10)",
                     "DESIGN", "TEXT",
                     "CREATED",DataType.DATETIME
                 });
          }
           if(!Table.ifTableExists("SCHOOL_DETAILS", db.getDatabaseName())){
                 Table table= db.addTable("ID", "SCHOOL_DETAILS", "VARCHAR(10)");
                 table.addColumns(new String[]{
                     "SCHOOL_KEY","TEXT",
                     "SCHOOL_VALUE","TEXT",
                     "CREATED",DataType.DATETIME
                 });
          }
           
            if(!Table.ifTableExists("REPORT_FORM_DATA", db.getDatabaseName())){
                 Table table= db.addTable("ID", "REPORT_FORM_DATA", "VARCHAR(10)");
                 table.addColumns(new String[]{
                     "FIELD_TYPE","TEXT",
                     "FIELD_ID","TEXT",
                     "FIELD_VALUE","TEXT",
                     "CREATED",DataType.DATETIME
                 });
          }
            
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="all_school_details", shareMethodWith = {"edit_mark_service"})
    public void allSchoolDetails(Server serv,ClientWorker worker){
        JSONObject data = db.query("SELECT SCHOOL_KEY, SCHOOL_VALUE FROM SCHOOL_DETAILS");
        serv.messageToClient(worker,data);
    }
    
    @Endpoint(name="fetch_extra_mark_sheet_columns")
    public void fetchExtraMarkSheetColumns(Server serv,ClientWorker worker){
        JSONObject data = db.query("SELECT FIELD_VALUE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='extra_mark_sheet_field'");
        serv.messageToClient(worker, data.optJSONArray("FIELD_VALUE").optString(0));
    }
    
    /**
   
    @Endpoint(name="save_school_details")
    public void saveSchoolDetails(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData(); 
        JSONArray keys = requestData.optJSONArray("school_keys");
        JSONArray values=requestData.optJSONArray("school_values");
        UniqueRandom rand=new UniqueRandom(6);
        for(int x=0; x<keys.length(); x++){
           String key=keys.optString(x).toLowerCase();
           String value=values.optString(x).toLowerCase();
           boolean exists = db.ifValueExists(key,"SCHOOL_DETAILS","SCHOOL_KEY");
           if(value.isEmpty()){
             Datastore.executeQuery("DELETE FROM SCHOOL_DETAILS WHERE SCHOOL_KEY=?",db.getDatabaseName(),key);
           }
           else if(exists){
              db.execute("UPDATE SCHOOL_DETAILS SET SCHOOL_VALUE='"+value+"' WHERE SCHOOL_KEY='"+key+"'");
           }
           else if(!key.isEmpty()){
              db.doInsert("SCHOOL_DETAILS", new String[]{rand.nextMixedRandom(),key,value,"!NOW()"});
           }
        }
        serv.messageToClient(worker, Message.SUCCESS);
    }
    
    @Endpoint(name="save_mark_sheet")
    public void saveMarkData(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String examId = requestData.optString("exam_id");
            JSONObject markData = requestData.optJSONObject("mark_data");
            Iterator ids = markData.keys();
            UniqueRandom rand=new UniqueRandom(50);
            String classId="";
            while(ids.hasNext()){
               String studentId=(String)ids.next(); //student id
               JSONObject studentData=db.query("SELECT STUDENT_STREAM,STUDENT_CLASS FROM STUDENT_DATA WHERE ID=?",studentId);
               String streamId=studentData.optJSONArray("STUDENT_STREAM").optString(0);
               classId=studentData.optJSONArray("STUDENT_CLASS").optString(0);
               JSONArray marks=markData.optJSONArray(studentId);
               for(int x=0; x<marks.length(); x++){
                  JSONArray mark=marks.optJSONArray(x); 
                  String recordId=rand.nextMixedRandom();
                  String paperId=mark.optString(0);
                  String subjectId=mark.optString(1);
                  String markValue=mark.optString(2);
                  boolean exists=db.ifValueExists(new String[]{examId,streamId,studentId,subjectId,paperId},"MARK_DATA",new String[]{"EXAM_ID","STREAM_ID","STUDENT_ID","SUBJECT_ID","PAPER_ID"});
                  if(!exists){ 
                 //insert the mark only if it does not exist before
                      db.doInsert("MARK_DATA",new String[]{recordId,examId,classId,streamId,studentId,subjectId,paperId,markValue,"!NOW()"});  
                 }
                  else{
                    db.execute("UPDATE MARK_DATA SET MARK_VALUE='"+markValue+"' WHERE EXAM_ID = '"+examId+"' AND STUDENT_ID='"+studentId+"' AND SUBJECT_ID='"+subjectId+"' AND PAPER_ID='"+paperId+"'");
                  } 
             }
               
            }    
           UserAction action =new UserAction(serv, worker, "SAVE MARK SHEET DATA FOR EXAM "+examId+" FOR CLASS "+classId);
           action.saveAction();
           serv.messageToClient(worker, Message.SUCCESS); 
        } catch (Exception ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    */

/**
 * 
 *
    
    @Endpoint(name="students_and_marks")
    public void studentsAndMarks(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String examId=requestData.optString("exam_id"); 
            String streamId=requestData.optString("stream_id");
            String classId=requestData.optString("class_id");
            String subjectId=requestData.optString("subject_id"); 
            String teacherName=requestData.optString("current_user");
            String order=requestData.optString("order");
            String hidePapers=requestData.optString("hide_papers");
            String orderBy="STUDENT_NAME ASC";
            if(order.equals("true")){
               orderBy="STUDENT_NAME ASC";
            }
            else if(order.equals("false")){
               orderBy="CREATED ASC"; 
            }
            //we need to fetch all the names of students
            //fetch all the marks associated to students
            //fetch the specific subjects and their papers
            //we need to fetch mean score details in order to correctly grade the students
            //we need to fetch the stream of the students
          
            JSONObject data = db.query("SELECT * FROM REPORT_FORM_DATA ORDER BY CREATED ASC");
            //these are extra columns that we need to display on the marksheet grid
            JSONArray extraColumns = db.query("SELECT FIELD_VALUE FROM REPORT_FORM_DATA WHERE FIELD_TYPE='extra_mark_sheet_field'").optJSONArray("FIELD_VALUE"); 
            String theColumns=extraColumns.optString(0);
            String[] allExtraCols = theColumns.split(",");
            StringBuilder extraColBuilder=new StringBuilder("SELECT ID ,");
            for(int x=0; x<allExtraCols.length; x++){
               if(x==allExtraCols.length-1){
                  extraColBuilder.append(allExtraCols[x].replace(" ","_")); 
               }
               else{
                  extraColBuilder.append(allExtraCols[x].replace(" ","_")).append(" ,");
               }
            }
            extraColBuilder.append(" FROM STUDENT_DATA WHERE ");
            //-------------------------- fetch the data from student data about the extra columns
            List fieldTypes = data.optJSONArray("FIELD_TYPE").toList();
            List fieldValues = data.optJSONArray("FIELD_VALUE").toList();
            JSONArray meanScore =new JSONArray();
            for(int x=0; x<fieldTypes.size(); x++){
               String type=(String) fieldTypes.get(x);
               String value=(String) fieldValues.get(x);
               if(type.equals("mean_score")){
                  meanScore.put(value);
               } 
            }
            //------------------------------------------
            JSONObject marks;
            JSONObject subjects;
            JSONObject papers;
            JSONObject students;
            //remove any unwanted values;
            JSONObject examDeadline=db.query("SELECT EXAM_DEADLINE FROM EXAM_DATA WHERE ID=?", examId);
            JSONObject teacherSubjects=db.query("SELECT SUBJECT_ID,STREAM_ID FROM TEACHER_SUBJECTS WHERE TEACHER_ID=(SELECT ID FROM TEACHER_DATA WHERE TEACHER_NAME=?)",teacherName);
            //JSONObject teacherSubjects=db.queryDatabase("SELECT ID, SUBJECT_NAME,CREATED,SUBJECT_ID FROM SUBJECT_DATA,STREAM_SUBJECTS WHERE SUBJECT_DATA.ID=STREAM_SUBJECTS.SUBJECT_ID AND STREAM_SUBJECTS.STREAM_ID=? ORDER BY CREATED DESC",streamId);
            JSONObject formulas=db.query("SELECT DESIGN, SUBJECT_ID FROM MARK_SHEET_DESIGN WHERE CLASS_ID=?",classId);
            if(subjectId.equals("all") && streamId.equals("all")){
                //these are all streams and all subjects
                //here we want the subjects displayed for a whole class to be inclusive of subjects for all the streams
                //for example if stream A has subjects {Math, physics,chemistry} and stream B has {math,biology,history}
                //the class will have subjects {math,physics,chemistry,biology,history}
                //the extra columns to be displayed display data for the whole class
               extraColBuilder.append(" STUDENT_CLASS='").append(classId).append("'");
               JSONObject streamIds = db.query("SELECT STREAM_ID FROM CLASS_STREAMS WHERE CLASS_ID=? ",classId);
               JSONArray streams=streamIds.optJSONArray("STREAM_ID");
               ArrayList found=new ArrayList();
               JSONObject allSubjects=new JSONObject();
               for(int n=0; n<streams.length(); n++){
                  subjects=db.query("SELECT  ID, SUBJECT_NAME,CREATED,SUBJECT_ID FROM SUBJECT_DATA,STREAM_SUBJECTS WHERE SUBJECT_DATA.ID=STREAM_SUBJECTS.SUBJECT_ID AND STREAM_SUBJECTS.STREAM_ID=? ORDER BY CREATED DESC",streams.optString(n));
                  for(int m=0; m<subjects.optJSONArray("SUBJECT_NAME").length(); m++){
                     String subjectName=subjects.optJSONArray("SUBJECT_NAME").optString(m);
                     if(found.indexOf(subjectName)==-1){
                         allSubjects.accumulate("SUBJECT_NAME",subjectName);
                         allSubjects.accumulate("CREATED",subjects.optJSONArray("CREATED").optString(m));
                         allSubjects.accumulate("ID",subjects.optJSONArray("ID").optString(m));
                         found.add(subjectName);
                     }
                  }
               }
               subjects=allSubjects;
               marks=db.query("select student_id, subject_id, paper_id, mark_value from mark_data where exam_id = ? and  class_id=?",examId,classId);
               students=db.query("SELECT ID, STUDENT_NAME, STUDENT_STREAM, CREATED FROM STUDENT_DATA WHERE STUDENT_CLASS=? ORDER BY STUDENT_STREAM ASC,"+orderBy+"",classId);
              // subjects=db.queryDatabase("SELECT ID, SUBJECT_NAME,CREATED FROM SUBJECT_DATA ORDER BY CREATED DESC");
               papers=db.query("SELECT PAPER_DATA.ID AS PAPER_ID,PAPER_NAME,SUBJECT_DATA.ID AS SUBJECT_ID,SUBJECT_NAME,PAPER_DATA.CREATED AS CREATION_TIME  FROM PAPER_DATA,SUBJECT_PAPERS,SUBJECT_DATA WHERE  PAPER_DATA.ID=PAPER_ID AND SUBJECT_ID=SUBJECT_DATA.ID ORDER BY CREATION_TIME ASC");
               if(hidePapers.equals("true")){
                   papers=new JSONObject();
                   marks=db.query("select student_id, subject_id, paper_id, mark_value from mark_data where exam_id = ? and class_id=? and subject_id=paper_id",examId,classId);
               } 
            }
            else if(subjectId.equals("all")){
               //this is a specific stream but all subjects are selected
               //extra column builder fetch data for specific stream
               extraColBuilder.append(" STUDENT_CLASS='").append(classId).append("' AND ").append(" STUDENT_STREAM='").append(streamId).append("'");
               marks=db.query("select student_id, subject_id, paper_id, mark_value from mark_data where exam_id = ? and stream_id=? and class_id=?",examId,streamId,classId);
               students=db.query("SELECT ID, STUDENT_NAME,STUDENT_STREAM,CREATED FROM STUDENT_DATA WHERE STUDENT_STREAM=? AND STUDENT_CLASS=? ORDER BY "+orderBy+"",streamId,classId);
               subjects=db.query("SELECT  ID, SUBJECT_NAME,CREATED FROM SUBJECT_DATA,STREAM_SUBJECTS WHERE SUBJECT_DATA.ID=STREAM_SUBJECTS.SUBJECT_ID AND STREAM_SUBJECTS.STREAM_ID=? ORDER BY CREATED DESC",streamId);
               papers = db.query("SELECT PAPER_DATA.ID AS PAPER_ID,PAPER_NAME,SUBJECT_DATA.ID AS SUBJECT_ID,SUBJECT_NAME,PAPER_DATA.CREATED AS CREATION_TIME  FROM PAPER_DATA,SUBJECT_PAPERS,SUBJECT_DATA WHERE  PAPER_DATA.ID=PAPER_ID AND SUBJECT_ID=SUBJECT_DATA.ID ORDER BY CREATION_TIME ASC");
               if(hidePapers.equals("true")){
                   papers=new JSONObject();
                   marks=db.query("select student_id, subject_id, paper_id, mark_value from mark_data where exam_id = ? and stream_id=? and class_id=? and subject_id=paper_id",examId,streamId,classId);
               }
            }
           else if(streamId.equals("all")){
               //these are all streams but a specific subject
               extraColBuilder.append(" STUDENT_CLASS='").append(classId).append("'");
               marks=db.query("select student_id, subject_id, paper_id, mark_value from mark_data where exam_id = ? and class_id=? and subject_id=?",examId,classId,subjectId);
               students=db.query("SELECT ID, STUDENT_NAME, STUDENT_STREAM, CREATED FROM STUDENT_DATA WHERE  STUDENT_CLASS=? ORDER BY STUDENT_STREAM ASC,"+orderBy+"",classId);
               subjects=db.query("SELECT ID, SUBJECT_NAME,CREATED  FROM SUBJECT_DATA WHERE ID=? ORDER BY CREATED DESC",subjectId);
               papers=db.query("SELECT PAPER_DATA.ID AS PAPER_ID,PAPER_NAME,SUBJECT_DATA.ID AS SUBJECT_ID, SUBJECT_NAME,PAPER_DATA.CREATED AS CREATION_TIME FROM PAPER_DATA,SUBJECT_PAPERS,SUBJECT_DATA WHERE  PAPER_DATA.ID=PAPER_ID AND SUBJECT_ID=SUBJECT_DATA.ID AND SUBJECT_ID=? ORDER BY CREATION_TIME ASC",subjectId);
               if(hidePapers.equals("true")){
                   papers=new JSONObject();
                   marks=db.query("select student_id, subject_id, paper_id, mark_value from mark_data where exam_id = ? and  class_id=? and subject_id=? and subject_id=paper_id",examId,classId,subjectId);
               } 
           }
            else { 
                //this is a specific subject and a specific stream
               extraColBuilder.append(" STUDENT_CLASS='").append(classId).append("' AND ").append(" STUDENT_STREAM='").append(streamId).append("'");
               marks=db.query("select student_id, subject_id, paper_id, mark_value from mark_data where exam_id = ? and stream_id=? and subject_id=? and class_id=? ",examId,streamId,subjectId,classId);
               students=db.query("SELECT ID, STUDENT_NAME, STUDENT_STREAM, CREATED  FROM STUDENT_DATA WHERE STUDENT_STREAM=? AND STUDENT_CLASS=? ORDER BY "+orderBy+"",streamId,classId);
               subjects=db.query("SELECT ID, SUBJECT_NAME,CREATED FROM SUBJECT_DATA WHERE ID=? ORDER BY CREATED DESC",subjectId);
               papers=db.query("SELECT PAPER_DATA.ID AS PAPER_ID,PAPER_NAME,SUBJECT_DATA.ID AS SUBJECT_ID, SUBJECT_NAME,PAPER_DATA.CREATED AS CREATION_TIME FROM PAPER_DATA,SUBJECT_PAPERS,SUBJECT_DATA WHERE  PAPER_DATA.ID=PAPER_ID AND SUBJECT_ID=SUBJECT_DATA.ID AND SUBJECT_ID=? ORDER BY CREATION_TIME ASC",subjectId);
               if(hidePapers.equals("true")){
                   papers=new JSONObject();
                   marks=db.query("select student_id, subject_id, paper_id, mark_value from mark_data where exam_id = ? and stream_id=? and subject_id=? and class_id=? and subject_id=paper_id ",examId,streamId,subjectId,classId);
               }
            }
            //extra columns data
           JSONObject extraColData=new JSONObject();
           if(theColumns.length()>0){
               extraColData = db.query(extraColBuilder.toString());
           }
           //student ranks in class and stream 
           JSONObject studentRankData=new JSONObject();
          
           for(int x=0; x<students.optJSONArray("ID").length(); x++){
               String studentId=students.optJSONArray("ID").optString(x);
               JSONObject rankData=openSingleReportForm(studentId, examId, streamId, classId);
               studentRankData.put(studentId,rankData);
           }
           
           JSONObject all=new JSONObject();
           all.put("teacher_subjects",teacherSubjects);
           all.put("marks",marks);
           all.put("students",students);
           all.put("student_rank_data",studentRankData);
           all.put("subjects", subjects);
           all.put("papers",papers);
           all.put("formulas", formulas);
           all.put("exam_deadline",examDeadline);
           all.put("grades",meanScore);
           all.put("extra_col_data",extraColData);
           serv.messageToClient(worker, all);
        } catch (JSONException ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   *
   */
   /**
    * 
    @Override
    public void onStart() {
        try {
            //get an instance of our database
             db=Datastore.getExistingDatabase("school_data");
             ScriptEngineManager manager = new ScriptEngineManager();
             engine = manager.getEngineByName("js");
        } catch (NonExistentDatabaseException ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
*/
