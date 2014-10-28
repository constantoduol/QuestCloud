/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
package com.quest.school;

import com.quest.access.common.mysql.Database;
import com.quest.access.control.Server;
import com.quest.access.useraccess.Serviceable;
import com.quest.access.useraccess.services.Message;
import com.quest.access.useraccess.services.MessageName;
import com.quest.servlets.ClientWorker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smslib.AGateway;
import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;


public class ModemMessageService implements Serviceable {
    private static Database db;
    private static Service modemService;
    private static ConcurrentHashMap<String,ArrayList<String>> failedRecipients=new ConcurrentHashMap();
    private static ConcurrentHashMap<String,ArrayList<String>> failedMessages=new ConcurrentHashMap();
    private static ConcurrentHashMap<String, Integer> messageSent=new ConcurrentHashMap();
    @Override
    public void service() {
      //dummy method
    }

    @Override
    public void onCreate() {
       // the smslib database should also be created
       // the default tables are created using MySQL.sql file
    }
    
    @MessageName(name="start_message_service")
    public void startModem(Server serv,ClientWorker worker){
         try {
             JSONObject requestData=worker.getRequestData();
             String com=requestData.optString("modem_com");
             String manuf=requestData.optString("modem_manuf");
             String model=requestData.optString("modem_model");
             String pin=requestData.optString("modem_pin");
            // OutboundNotification outboundNotification = new OutboundNotification();
             System.out.println("com : "+com+" manuf: "+manuf+" model: "+model+" pin: ****");
             SerialModemGateway gateway = new SerialModemGateway("modem.com1", com, 115200,manuf, model);
             gateway.setInbound(true);
             gateway.setOutbound(true);
             gateway.setSimPin(pin);
             // Explicit SMSC address set is required for some modems.
             // Below is for VODAFONE GREECE - be sure to set your own!
             //gateway.setSmscNumber("+306942190000");
            // Service.getInstance().setOutboundMessageNotification(outboundNotification);
             Service.getInstance().addGateway(gateway);
             Service.getInstance().startService();
             modemService=Service.getInstance();
             System.out.println("Modem started successfully");
             serv.messageToClient(worker,Message.SUCCESS);

        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
           // serv.exceptionToClient(worker, ex);
        } 
    }

    @Override
    public void onStart() {
            try {
                //get an instance of our database
                db=Database.getExistingDatabase("school_data");
          	
            } catch (Exception ex) {
                Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
   @MessageName(name="recipient_fields")
   public void allRecipientFields(Server serv,ClientWorker worker){
        try {
            
            JSONObject studentFields=db.queryDatabase("SELECT FIELD_NAME FROM STUDENT_FIELD_META_DATA");
            JSONObject all=new JSONObject();
            all.put("student_fields", studentFields);
            serv.messageToClient(worker, all);
        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   
   public class OutboundNotification implements IOutboundMessageNotification
	{
		public void process(AGateway gateway, OutboundMessage msg)
		{
		}
	}
  @MessageName(name="target_reach")
  public void getTargetReach(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            JSONArray fields = requestData.optJSONArray("student_fields");
            String classId=requestData.optString("class_id");
            String streamId=requestData.optString("stream_id");
            String correctFieldName=fields.optString(0).replace(" ","_");
            JSONObject count=new JSONObject();
            Integer finalCount;
            if(!correctFieldName.isEmpty()){
                if(classId.equals("all")){
                    count = db.queryDatabase("SELECT COUNT("+correctFieldName+") AS TARGET_REACH FROM STUDENT_DATA");
                }
                else if(streamId.equals("all")){
                    count = db.queryDatabase("SELECT COUNT("+correctFieldName+") AS TARGET_REACH FROM STUDENT_DATA WHERE STUDENT_CLASS=?",classId);
                }
                else {
                    count = db.queryDatabase("SELECT COUNT("+correctFieldName+") AS TARGET_REACH FROM STUDENT_DATA WHERE STUDENT_CLASS=? AND STUDENT_STREAM=?",classId,streamId);
                }
                JSONArray dbCount = count.optJSONArray("TARGET_REACH");
                Integer realCount=Integer.parseInt(dbCount.optString(0));
                finalCount=fields.length()*realCount;
            }
            else{
              finalCount=0;  
            }
            count.put("TARGET_REACH", finalCount);
            serv.messageToClient(worker,count);
        } catch (JSONException ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
  
  @MessageName(name="send_message")
  public synchronized void sendMessage(Server serv,ClientWorker worker){
    JSONObject requestData = worker.getRequestData();
    String msg=requestData.optString("msg");
    String id=worker.getSession().getId();
    messageSent.put(id,0);
    failedMessages.put(id, new ArrayList<String>());
    failedRecipients.put(id, new ArrayList<String>());
    Object[] tempData=parseMessage(msg, serv, worker);
    if(tempData==null){
       //we cannot send the message 
    }
    else{
      sendFinalMessage(serv,worker,tempData);
    }
    
  }
  
  private void sendFinalMessage(Server serv,ClientWorker worker,Object [] tempData){
        try {
            JSONObject requestData = worker.getRequestData();
            JSONArray fields = requestData.optJSONArray("student_fields");
            String classId=requestData.optString("class_id");
            String streamId=requestData.optString("stream_id");
            String [] messageTemplate=(String[]) tempData[0];
            HashMap locations=(HashMap<Integer,JSONObject>) tempData[1];
            JSONObject studentData;
            if(classId.equals("all")){
               studentData = db.queryDatabase("SELECT * FROM STUDENT_DATA");
            }
            else if(streamId.equals("all")){
               studentData = db.queryDatabase("SELECT * FROM STUDENT_DATA WHERE STUDENT_CLASS=?",classId);
            }
            else {
               studentData = db.queryDatabase("SELECT * FROM STUDENT_DATA WHERE STUDENT_CLASS=? AND STUDENT_STREAM=?",classId,streamId);
            }
            JSONObject subjectData = db.queryDatabase("SELECT SUBJECT_NAME, ID FROM SUBJECT_DATA");
            studentData.put("SUBJECT_DATA", subjectData);
            int length=studentData.optJSONArray("STUDENT_NAME").length();
            serv.messageToClient(worker, Message.SUCCESS);
            for(int x=0; x<length; x++){
              Iterator iter=locations.keySet().iterator();
              while(iter.hasNext()){
               Integer count=(Integer) iter.next();
               JSONObject template=(JSONObject) locations.get(count);
               String temp = template.optString("template");
               JSONObject data = template.optJSONObject("data");
               String table=data.optString("table");
               String column=data.optString("column");
               if(!temp.isEmpty()){
                 String value=resolveTemplates(table,temp,studentData,x);
                 messageTemplate[count]=value;
               }
               else{
                   JSONArray col = studentData.optJSONArray(column.toUpperCase());
                   String value = col.optString(x);
                   messageTemplate[count]=value;
               }
              }
                String [] sent = disperseSingleMessage(messageTemplate, studentData, fields, x);
                if(sent!=null){
                   String ifSent=sent[2];
                   String id=worker.getSession().getId();
                   if(ifSent.equals("true")){
                      messageWasSent(id);
                   }
                   else{
                     messageWasNotSent(id, sent[1],sent[0]);
                   }
                    
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
      
  }
  
  private void messageWasSent(String id){
    Integer val = messageSent.get(id);
    val++;
    messageSent.put(id, val);
  }
  
private void messageWasNotSent(String id,String msg,String rcp){
  failedMessages.get(id).add(msg);
  failedRecipients.get(id).add(rcp);     
 }
  
  @MessageName(name="resend_failed_messages")
  public synchronized void resendFailedMessages(Server serv, ClientWorker worker){
     String id=worker.getSession().getId();
     ArrayList<String> failedRcp=failedRecipients.get(id);
     if(failedRcp==null){
        return;
     }
     ArrayList<String> failedMsg=failedMessages.get(id);
     ArrayList<String> newFailedRcp=new ArrayList();
     ArrayList<String> newFailedMsg=new ArrayList();
     newFailedRcp.addAll(failedRcp);
     newFailedMsg.addAll(failedMsg);
     failedRecipients.put(id,new ArrayList<String>());
     failedMessages.put(id, new ArrayList<String>());
     messageSent.put(id,0);
     for(int x=0; x<failedRcp.size(); x++){
        String rcp=newFailedRcp.get(x);
        String msg=newFailedMsg.get(x);
        OutboundMessage outMsg=new OutboundMessage(rcp,msg);
        try {
            System.out.println("request_send: "+rcp+" --> "+msg);
            boolean sent = modemService.sendMessage(outMsg);
            if(sent){
               //if it was not sent, queue it again for sending 
               System.out.println("message_sent: "+rcp+" --> "+msg);
               messageWasSent(id);
            }
            else{
              messageWasNotSent(id, msg, rcp);
            }
         } catch (Exception ex) {
             Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
             messageWasNotSent(id, msg, rcp);
         }
     }
    failedRecipients.put(id,new ArrayList<String>());
    failedMessages.put(id,new ArrayList<String>());
  }
  
  @MessageName(name="check_message_progress")
  public void checkMessageProgress(Server serv, ClientWorker worker){
        try {
            String id=worker.getSession().getId();
            JSONObject data=new JSONObject();
            Integer val = messageSent.get(id);
            ArrayList<String> failed = failedMessages.get(id);
            int size;
            if(val==null){
             val=0;
            }
            if(failed==null){
               size=0; 
            }
            else{
              size=failed.size();
            }
            
            data.put("message_sent", val);
            data.put("message_fail", size);
            serv.messageToClient(worker, data);
        } catch (JSONException ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
  
  private String[] disperseSingleMessage(String [] messageTemplate,JSONObject data,JSONArray fields,int count){
     String msg="";
     for(int z=0; z<messageTemplate.length; z++){
         msg=msg+messageTemplate[z];
     }
     for(int y=0; y<fields.length(); y++){
        String field=fields.optString(y).replace(" ", "_");
        String phoneNo = data.optJSONArray(field.toUpperCase()).optString(count);
        OutboundMessage message = new OutboundMessage(phoneNo, msg);
        System.out.println("request_send: "+phoneNo+" --> "+msg);
        try {
             boolean sent = modemService.sendMessage(message);
             if(sent){
               System.out.println("message_sent: "+phoneNo+" --> "+msg);
               return new String[]{phoneNo,msg,"true"};
             }
            return new String[]{phoneNo,msg,"false"};
        } catch (Exception ex) {
             Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
             return new String[]{phoneNo,msg,"false"};
        } 
     }
     return null;
  }
  
  private Object[] parseMessage(String msg,Server serv,ClientWorker worker){
       // we are going to send custom messages to each phone number
      StringTokenizer token=new StringTokenizer(msg, "$");
      String [] msgTemplate =new String[token.countTokens()];
      HashMap locations=new HashMap();
      int count=0;
      while(token.hasMoreTokens()){
        String tk=token.nextToken();
        if(tk.startsWith("{")){
           try {
               //this is a json object template, so parse it
               JSONObject template =new JSONObject(tk);
               //check the data key
               JSONObject msgData= template.optJSONObject("data");
               if(msgData==null ){
                    //this information is insufficient we need a data key
                 worker.setReason("The data key is missing");
                 serv.messageToClient(worker, Message.FAIL);
                 return null;
               }
               else{
                  String table = msgData.optString("table");
                  String col = msgData.optString("column");
                  if(table.isEmpty() && col.isEmpty()){
                     worker.setReason("No table or column specified for data key");
                     serv.messageToClient(worker, Message.FAIL);
                     return null;  
                  }
                  else{
                    locations.put(count,template);
                  }
               }
           } catch (Exception ex) {
               //this means this is an invalid json string,tell the client that.
               serv.exceptionToClient(worker, new Exception("The template "+tk+" is invalid"));
               return null;
           }
       }
       else{ 
         msgTemplate[count]=tk;
       }
      count++;
     }
   return new Object [] {msgTemplate,locations};
}
  private String resolveTemplates(String table,String template,JSONObject data,int count){
      if(template.equals("exam")){
         return examTemplate(table,data,count);
      }
      return "";
  }
  
  
  
  private String examTemplate(String table,JSONObject data,int count){
      String studentId=data.optJSONArray("ID").optString(count);
      JSONObject studentMarks = db.queryDatabase("SELECT * FROM "+table+" WHERE STUDENT_ID=?",studentId);
      JSONArray markValues=studentMarks.optJSONArray("MARK_VALUE");
      JSONArray paperIds=studentMarks.optJSONArray("PAPER_ID");
      JSONArray subjectIds=studentMarks.optJSONArray("SUBJECT_ID");
      JSONObject subjectData = data.optJSONObject("SUBJECT_DATA");
      JSONArray ids = subjectData.optJSONArray("ID");
      JSONArray names = subjectData.optJSONArray("SUBJECT_NAME");
      StringBuilder results=new StringBuilder();
      Float grandTotal=0F;
      int total=0;
      for(int x=0; x<subjectIds.length(); x++){
          String subId=subjectIds.optString(x);
          String papId=paperIds.optString(x);
          for(int y=0; y<ids.length(); y++){
             if(subId.equals(ids.optString(y))){
               //note the location ,get subject name and value
               if(papId.equals(subId)){
                  //this is a subject not a paper 
                  String subjectName=names.optString(y);
                  String value=markValues.optString(x);
                  results.append(" ").append(subjectName).append(" : ").append(value);
                  grandTotal+=Float.parseFloat(value);
                  total+=100;
               }
             } 
          }
      }
      results.append(" Grand Total : ").append(grandTotal.intValue()).append("/").append(total);
     return results.toString();
   }
 
}

*/