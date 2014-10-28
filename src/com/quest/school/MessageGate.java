package com.quest.school;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.quest.access.common.datastore.Datastore;


@SuppressWarnings("serial")
public class MessageGate {
	
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	 private static final String username = "constant.oduol";
	 
	 private static final String apiKey = "a3f38be6a02558ae907f2ded2707499dcba5ec5569caeb407dac3cdba1fdd4a4";
	 
	 private static AfricasTalkingGateway messageService = new AfricasTalkingGateway(username, apiKey);
	 
	
	
	
	public  static JSONObject fetchSMSBalance(String userId){
		Entity account = Datastore.getSingleEntity("SMS_ACCOUNT","USER_ID",userId,FilterOperator.EQUAL);
		return Datastore.entityToJSON(account);  
	}
	
	
	
	
  public static void noteSMSUsage(String userId,int smsCount){	 
	  Entity cost=new Entity("SMS_USAGE");
	  cost.setProperty("SMS_COUNT",smsCount);
	  cost.setProperty("USER_ID",userId);
	  cost.setProperty("TIMESTAMP",System.currentTimeMillis());
	  Datastore.insert(cost);
	}

   public static boolean sendMessage(String userId,String phoneNo,String msg){
	 try {
	   JSONArray result = messageService.sendMessage(phoneNo, msg);
	   if(result==null)
        	return false;
	   JSONObject response = result.getJSONObject(0);
	   String status=response.getString("status");
	   if(status.equalsIgnoreCase("success")){
              //if it was not sent, queue it again for sending 
		   Integer smsCount = ((int)Math.ceil(msg.length()/160.0) ); //calculate the number of messages sent, one message is 160 characters
		   noteChargeAccount(userId, smsCount);  
		   return true;
        }
	   else{
          return false;
       } 
	 }
	 catch(Exception e){
		 return false;
	 }
   }
   
   private static void noteChargeAccount(String userId,Integer smsCount){
	  try{
	     Key key = KeyFactory.createKey("SMS_ACCOUNT", userId);
		 Filter filter = new FilterPredicate("USER_ID",FilterOperator.EQUAL,userId);
		 Query query = new Query("SMS_ACCOUNT",key).setAncestor(key).setFilter(filter);
		 PreparedQuery pq = datastore.prepare(query);
		 long balance = 0;
		 long totalCount = 0;
		 Float smsCost = 0f;
		 Float profit = 0f;
		 String accType = "";
		 Entity smsAccount=null;
		 Float sPrice =1f;
		 Float bPrice = 1f;
		 for(Entity sc : pq.asIterable()){
		   balance = Long.parseLong(sc.getProperty("sms_balance").toString());
		   totalCount = Long.parseLong(sc.getProperty("sms_count").toString());
		   accType = (String) sc.getProperty("sms_account_type");
		   sPrice = Float.parseFloat((String) sc.getProperty("sp_price_per_sms"));
		   bPrice = Float.parseFloat(sc.getProperty("price_per_sms").toString());
		   smsCost = Float.parseFloat(sc.getProperty("sms_cost").toString());
		   profit = Float.parseFloat(sc.getProperty("sms_profit").toString());
		   smsAccount = sc;
		 }
       if(accType.equals("prepaid")){
      	 //a prepaid account reduce the sms balance
      	 //update the sms cost
      	 //update the sms profit
    		   long val=smsCount;
    		   float price=sPrice*val;
    		   balance= balance - val;
    		   smsCost=smsCost+price;
    		   profit=(sPrice-bPrice)*val+profit;
    		   smsAccount.setProperty("sms_balance",balance); 
    		   smsAccount.setProperty("sms_cost",smsCost); 
    		   smsAccount.setProperty("sms_profit",profit); 
       }
       else{
      	 //a postpaid account no sms balance
      	 //so increase sms_cost, sms_count and sms_profit
    		   long val=smsCount;
    		   totalCount= totalCount + val;
    		   float price=sPrice*val;
    		   smsCost=smsCost+price;
    		   profit=(sPrice-bPrice)*val+profit;
    		   smsAccount.setProperty("sms_count",totalCount); 
    		   smsAccount.setProperty("sms_cost",smsCost); 
    		   smsAccount.setProperty("sms_profit",profit);
       }
       
		 datastore.put(smsAccount);
		 Logger.getLogger(MessageGate.class.getName()).log(Level.SEVERE,"Account "+userId+" charged successfully",(Exception)null);
	   }
	   catch(Exception e){
		  Logger.getLogger(MessageGate.class.getName()).log(Level.SEVERE,"Account  charging failed",e);
	   }  
   }
	
	
	
}
