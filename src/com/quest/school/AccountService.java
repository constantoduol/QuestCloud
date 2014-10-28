/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quest.school;


/**
 *
 * @author connie
 */
/**
@WebService (name = "account_service", level = 10, privileged = "yes")
@Models(models = {
    @Model(
       database = "school_data", table = "ACCOUNT_DATA",
       columns = {"ID VARCHAR(10) PRIMARY KEY", 
                  "ACCOUNT_DESCRIP TEXT",
                  "ACCOUNT_NAME TEXT",
                  "CREATED DATETIME",
                  "ACCOUNT_BALANCE FLOAT",
                  "ACTION_ID VARCHAR(60)",
                  "ACCOUNT_TYPE VARCHAR(10)"
       }),
     @Model(
       database = "school_data", table = "ACCOUNT_RECORDS",
       columns = {"ID VARCHAR(60) PRIMARY KEY", 
                  "ACCOUNT_ID VARCHAR(10)",
                  "DOUBLE_ENTRY_ID VARCHAR(20)",
                  "TRAN_TYPE BOOL",
                  "TRAN_DATE DATETIME",
                  "TRAN_AMOUNT FLOAT",
                  "TRAN_NARRATION TEXT",
                  "STREAM_ID VARCHAR(10)",
                  "CLASS_ID VARCHAR(10)"
       })
   }
)

public class AccountService implements Serviceable {
    private static Datastore db;
    
   
    
    @Override
    public void service() {
      //dummy method 
    }

    @Override
    public void onCreate() {
      // create database tables
        onStart();
    }
    
    @Endpoint(name="create_account")
    public void createAccount(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String acName = requestData.optString("account_name");
            String acDescrip = requestData.optString("account_descrip");
            String initialAmount = requestData.optString("initial_amount");
            UserAction action=new UserAction(serv, worker, "CREATE ACCOUNT "+acName);
            UniqueRandom rand=new UniqueRandom(10);
            String id=rand.nextRandom();
            createAccount(acName, id,acDescrip, initialAmount,"system", serv, worker,"","");
            action.saveAction();
            serv.messageToClient(worker, id);
        } catch (Exception ex) {
            Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
   public static String createAccount(String acName,String id,String acDescrip,String initialAmount,String type,Server serv,ClientWorker worker,String classId,String streamId){
        try {
            UserAction action=new UserAction(serv, worker, "ACCOUNT_SERVICE CREATE ACCOUNT "+acName+" : "+id);
            action.saveAction();
            db.doInsert("ACCOUNT_DATA", new String[]{id,acDescrip,acName,"!NOW()",initialAmount,action.getActionID(),type});
            UniqueRandom rand1=new UniqueRandom(20);
            db.doInsert("ACCOUNT_RECORDS",new String[]{action.getActionID(),id,rand1.nextRandom(),"1","!NOW()",initialAmount,"Balance Brought Forward",streamId,classId});
            return id;
        } catch (Exception ex) {
            Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
   }
    
    
    @Endpoint(name="account_auto_suggest")
    public void autoSuggest(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        String fieldName = requestData.optString("field_name");
        String like = requestData.optString("like");
        String sql=null;
        if(fieldName.equals("account_no")){
            sql="SELECT * FROM ACCOUNT_DATA WHERE ID LIKE '"+like+"%' LIMIT 10";
        }
        else if(fieldName.equals("account_name")){
            sql="SELECT * FROM ACCOUNT_DATA WHERE ACCOUNT_NAME LIKE '"+like+"%' LIMIT 10"; 
        }
        JSONObject data = db.query(sql);
        serv.messageToClient(worker, data);
    }
    
    @Endpoint(name="account_complete_auto_suggest")
    public void completeAutoSuggest(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        String id = requestData.optString("id");
        JSONObject data = db.query("SELECT * FROM ACCOUNT_DATA WHERE ID=?",id);
        serv.messageToClient(worker, data);
    }
    
    @Endpoint(name="edit_account")
    public void editAccount(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String id = requestData.optString("id");
            String acName= requestData.optString("account_name");
            String acDescrip = requestData.optString("account_descrip");
            Float initialAmount =Float.parseFloat(requestData.optString("initial_amount"));
            Float accountBalance=Float.parseFloat(db.query("SELECT ACCOUNT_BALANCE FROM ACCOUNT_DATA WHERE ID=?",id).optJSONArray("ACCOUNT_BALANCE").optString(0));
            accountBalance=accountBalance+initialAmount;
            UniqueRandom rand=new UniqueRandom(20);
            UserAction action=new UserAction(serv, worker, "EDIT ACCOUNT "+acName+" : "+id);
            action.saveAction();
            Datastore.executeQuery("UPDATE ACCOUNT_DATA  SET ACCOUNT_NAME='"+acName+"', ACCOUNT_DESCRIP='"+acDescrip+"', ACCOUNT_BALANCE="+accountBalance+" WHERE ID=?",db.getDatabaseName(),id);
            db.doInsert("ACCOUNT_RECORDS",new String[]{action.getActionID(),id,rand.nextRandom(),"1","!NOW()",initialAmount.toString(),"Balance brought forward","",""});
            serv.messageToClient(worker, "success");
        } catch (Exception ex) {
            Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    @Endpoint(name="delete_account")
    public void deleteAccount(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String id = requestData.optString("id");
            Datastore.executeQuery("DELETE FROM ACCOUNT_DATA WHERE ID=?",db.getDatabaseName(),id);
            Datastore.executeQuery("DELETE FROM ACCOUNT_RECORDS WHERE ID=?",db.getDatabaseName(),id);
            UserAction action=new UserAction(serv, worker, "ACCOUNT_SERVICE DELETE ACCOUNT "+id);
            action.saveAction();
            serv.messageToClient(worker, "success");
        } catch (Exception ex) {
            Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="batch_transact")
    public void batchTransact(Server serv,ClientWorker worker){
         JSONObject requestData = worker.getRequestData();
         String accountId=requestData.optString("account_id");
         Float amount=Float.parseFloat(requestData.optString("amount"));
         String narration=requestData.optString("narration");
         String tranType=requestData.optString("tran_type");  
         String classId=requestData.optString("class_id");
         String streamId=requestData.optString("stream_id");
         JSONObject data;
         JSONArray failList=new JSONArray();
         if(classId.equals("all") && streamId.equals("all")){
             data = db.query("SELECT ID FROM STUDENT_DATA");
         }
         else if(classId.equals("all")){
            data = db.query("SELECT ID,STUDENT_CLASS FROM STUDENT_DATA WHERE STUDENT_CLASS=?",classId);  
         }
         else if(streamId.equals("all")){
           data = db.query("SELECT ID,STUDENT_STREAM FROM STUDENT_DATA WHERE STUDENT_STREAM=?",streamId);    
         }
         else{
           data = db.query("SELECT ID,STUDENT_STREAM,STUDENT_CLASS FROM STUDENT_DATA WHERE STUDENT_STREAM=? AND STUDENT_CLASS=?",streamId,classId);
         }
        JSONArray doubleEntryIds=data.optJSONArray("ID");
        for(int x=0; x<doubleEntryIds.length(); x++){
           String doubleEntryAcc=doubleEntryIds.optString(x);
           String[] tranData = doTransaction(accountId, doubleEntryIds.optString(x), amount, narration, tranType, serv, worker);
           if(tranData[0].equals("fail")){
               try {
                   JSONObject obj=new JSONObject();
                   obj.put("account_id",doubleEntryAcc);
                   obj.put("amount", amount);
                   obj.put("reason", tranData[1]);
                   failList.put(obj);
               } catch (JSONException ex) {
                   Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
        }
       serv.messageToClient(worker, failList);
    }
    
    private String[] doTransaction(String accountId,String doubleEntryAcc,Float amount,String narration,String tranType,Server serv,ClientWorker worker){
       try{
            JSONObject data;
            UniqueRandom rand=new UniqueRandom(20);
            String doubleEntryId=rand.nextRandom();
            JSONObject studentData;
            if(accountId.equals(doubleEntryAcc)){
               return new String[]{"fail","Originating account same as double entry account"};
            }
            if(tranType.equals("0")){
               //we are debiting the current account and crediting the double entry account
               data=db.query("SELECT ACCOUNT_BALANCE,ACCOUNT_TYPE FROM ACCOUNT_DATA WHERE ID=?",accountId);
               if(data.optJSONArray("ACCOUNT_TYPE").optString(0).equals("student")){
                   //this is a student account
                   studentData=db.query("SELECT STUDENT_STREAM, STUDENT_CLASS FROM STUDENT_DATA WHERE ID=?",accountId);
               }
               else{
                  //the double entry account is a student account
                   studentData=db.query("SELECT STUDENT_STREAM, STUDENT_CLASS FROM STUDENT_DATA WHERE ID=?",doubleEntryAcc);
               }
            }
            else{
                //we are crediting the current account and debiting the double entry account
               data=db.query("SELECT ACCOUNT_BALANCE,ACCOUNT_TYPE FROM ACCOUNT_DATA WHERE ID=?",doubleEntryAcc);    
               if(data.optJSONArray("ACCOUNT_TYPE").optString(0).equals("student")){
                   //this is a student account
                   studentData=db.query("SELECT STUDENT_STREAM, STUDENT_CLASS FROM STUDENT_DATA WHERE ID=?",doubleEntryAcc);
               }
               else{
                 //the double entry account is a student account
                   studentData=db.query("SELECT STUDENT_STREAM, STUDENT_CLASS FROM STUDENT_DATA WHERE ID=?",accountId);
               }
            }
            
            String stream=studentData.optJSONArray("STUDENT_STREAM").optString(0);
            String clazz=studentData.optJSONArray("STUDENT_CLASS").optString(0);
            Float balance=Float.parseFloat(data.optJSONArray("ACCOUNT_BALANCE").optString(0));
            if(amount>balance){
               return new String[]{"fail","Insufficient amount in account to complete transaction"};
            }
            UserAction action=new UserAction(serv, worker, "ACCOUNT_SERVICE TRANSACTION : "+amount);
            Float accountBalance=Float.parseFloat(db.query("SELECT ACCOUNT_BALANCE FROM ACCOUNT_DATA WHERE ID=?",accountId).optJSONArray("ACCOUNT_BALANCE").optString(0));
            Float doubleEntryBalance=Float.parseFloat(db.query("SELECT ACCOUNT_BALANCE FROM ACCOUNT_DATA WHERE ID=?",doubleEntryAcc).optJSONArray("ACCOUNT_BALANCE").optString(0));
            db.doInsert("ACCOUNT_RECORDS",new String[]{action.getActionID(),accountId,doubleEntryId,tranType,"!NOW()",amount.toString(),narration,stream,clazz});
            if(tranType.equals("0")){
               accountBalance=accountBalance-amount;
               doubleEntryBalance=doubleEntryBalance+amount;
               db.execute("UPDATE ACCOUNT_DATA SET ACCOUNT_BALANCE='"+accountBalance+"' WHERE ID='"+accountId+"'");
               db.execute("UPDATE ACCOUNT_DATA SET ACCOUNT_BALANCE='"+doubleEntryBalance+"' WHERE ID='"+doubleEntryAcc+"'");
            }
            else{
               accountBalance=accountBalance+amount;
               doubleEntryBalance=doubleEntryBalance-amount;
               db.execute("UPDATE ACCOUNT_DATA SET ACCOUNT_BALANCE='"+accountBalance+"' WHERE ID='"+accountId+"'");   
               db.execute("UPDATE ACCOUNT_DATA SET ACCOUNT_BALANCE='"+doubleEntryBalance+"' WHERE ID='"+doubleEntryAcc+"'");
            }
            tranType=tranType.equals("1") ? "0" : "1";
            UserAction action1=new UserAction(serv, worker, "ACCOUNT_SERVICE DOUBLE ENTRY TRANSACTION : "+amount);
            studentData=db.query("SELECT STUDENT_STREAM, STUDENT_CLASS FROM STUDENT_DATA WHERE ID=?",doubleEntryAcc);
            stream=studentData.optJSONArray("STUDENT_STREAM").optString(0);
            clazz=studentData.optJSONArray("STUDENT_CLASS").optString(0);
            db.doInsert("ACCOUNT_RECORDS",new String[]{action1.getActionID(),doubleEntryAcc,doubleEntryId,tranType,"!NOW()",amount.toString(),narration,stream,clazz});
            action.saveAction();
            action1.saveAction();
            return new String[]{"success",doubleEntryId};
        } catch (Exception ex) {
            Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } 
    }
    
    @Endpoint(name="transact")
    public void transact(Server serv,ClientWorker worker){
            JSONObject requestData = worker.getRequestData();
            String accountId=requestData.optString("account_id");
            String doubleEntryAcc=requestData.optString("double_entry_id");
            Float amount=Float.parseFloat(requestData.optString("amount"));
            String narration=requestData.optString("narration");
            String tranType=requestData.optString("tran_type");
            String[] tranData = doTransaction(accountId, doubleEntryAcc, amount, narration, tranType,serv,worker);
            if(tranData[0].equals("fail")){
               worker.setReason(tranData[1]);
               serv.messageToClient(worker, "fail");  
            }
            else{
               serv.messageToClient(worker, tranData[1]);    
            }
           
           
    }
    
    
    @Endpoint(name="open_history")
    public void openHistory(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        String startDate = requestData.optString("start_date");
        String endDate = requestData.optString("end_date");
        String accountId=requestData.optString("account_id");
        String classId=requestData.optString("class_id");
        String streamId=requestData.optString("stream_id");
        String type=requestData.optString("type");
        JSONObject data;
        if(type.equals("student")){
            data=db.query("SELECT ID,DOUBLE_ENTRY_ID,TRAN_TYPE,TRAN_AMOUNT,TRAN_NARRATION,TRAN_DATE"
                    + " FROM ACCOUNT_RECORDS WHERE TRAN_DATE >= ? AND TRAN_DATE <= ? AND "
                    + "ACCOUNT_ID=? ORDER BY TRAN_DATE ASC",startDate,endDate,accountId);
        }
        else{
          if(classId.equals("all") && streamId.equals("all")){
             data=db.query("SELECT ACCOUNT_RECORDS.ID,DOUBLE_ENTRY_ID,TRAN_TYPE,TRAN_AMOUNT,TRAN_NARRATION,TRAN_DATE"
                + "  FROM ACCOUNT_RECORDS WHERE TRAN_DATE >=? AND "
                + "TRAN_DATE <= ?  AND ACCOUNT_ID=? ORDER BY TRAN_DATE ASC",startDate,endDate,accountId);  
          }
          else if(streamId.equals("all")){
              data=db.query("SELECT ACCOUNT_RECORDS.ID,DOUBLE_ENTRY_ID,TRAN_TYPE,TRAN_AMOUNT,TRAN_NARRATION,TRAN_DATE"
                + " FROM ACCOUNT_RECORDS WHERE TRAN_DATE >=? AND "
                + "TRAN_DATE <= ? AND CLASS_ID=? AND ACCOUNT_ID=? ORDER BY TRAN_DATE ASC",startDate,endDate,classId,accountId); 
          }
          else{
             
            data=db.query("SELECT ACCOUNT_RECORDS.ID,DOUBLE_ENTRY_ID,TRAN_TYPE,TRAN_AMOUNT,TRAN_NARRATION,TRAN_DATE"
                + " FROM ACCOUNT_RECORDS WHERE TRAN_DATE >=? AND "
                + "TRAN_DATE <= ? AND CLASS_ID=? AND STREAM_ID=? AND ACCOUNT_ID=? ORDER BY TRAN_DATE ASC",startDate,endDate,classId,streamId,accountId);  
           }
          }
       
        serv.messageToClient(worker, data); 
    }
    
    
    
    
    @Override
    public void onStart() {
      //get an instance of our database
         try {
            //get an instance of our database
            db=Datastore.getExistingDatabase("school_data");
        } catch (NonExistentDatabaseException ex) {
            Logger.getLogger(MarkService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

*/