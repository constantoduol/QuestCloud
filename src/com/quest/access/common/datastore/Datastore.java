
package com.quest.access.common.datastore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

/**
 *
 * @author constant oduol
 * @version 1.0(2/7/2014)
 */

//update,select, insert,delete


public class Datastore {
   
   private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
   public static void insert(String entityName,String primaryValue,HashMap<String,Object> values){
	   Key key = KeyFactory.createKey(entityName,primaryValue);
	   Entity entity = new Entity(entityName,key);
	   Iterator iter = values.keySet().iterator();
	   while(iter.hasNext()){
		  String propertyName = (String) iter.next(); 
		  Object propertValue = values.get(propertyName);
		  entity.setProperty(propertyName, propertValue);
	   }
	  datastore.put(entity);
   }
   
   public static void insert(Entity en){
	   datastore.put(en);
   }
   
   public static void insert(String entityName,String primaryValue,JSONObject values){
	   Key key = KeyFactory.createKey(entityName,primaryValue);
	   Entity entity = new Entity(entityName,key);
	   Iterator iter = values.keys();
	   try{
		   while(iter.hasNext()){
			   String propertyName = (String) iter.next(); 
			   Object propertValue = values.get(propertyName);
			   entity.setProperty(propertyName, propertValue);
		   }
	   }
	   catch(Exception e){
		   
	   }
	  datastore.put(entity);
   }
   
   public static void insert(String entityName,HashMap<String,Object> values){
	   Entity entity = new Entity(entityName);
	   Iterator iter = values.keySet().iterator();
	   while(iter.hasNext()){
		  String propertyName = (String) iter.next(); 
		  Object propertValue = values.get(propertyName);
		  entity.setProperty(propertyName, propertValue);
	   }
	  datastore.put(entity);
   }
   
   public static void insert(String entityName,JSONObject values){
	   Entity entity = new Entity(entityName);
	   Iterator iter = values.keys();
	   try{
		   while(iter.hasNext()){
			   String propertyName = (String) iter.next(); 
			   Object propertValue = values.get(propertyName);
			   entity.setProperty(propertyName, propertValue);
		   }
	   }
	   catch(Exception e){
		   
	   }
	  datastore.put(entity);
   }
   
   
   public static Entity getSingleEntity(String entityName,String propertyName,String value,FilterOperator filterOperator){
		  Filter filter = new FilterPredicate(propertyName,filterOperator,value);
		  Query query = new Query(entityName).setFilter(filter);
		  PreparedQuery pq = datastore.prepare(query);
		  return pq.asSingleEntity();
   }
   
   public static Entity getSingleEntity(String entityName,Filter...filters){
	   Query query = new Query(entityName);
	   for(int x = 0; x < filters.length; x++){
		   query.setFilter(filters[x]);
	   }
	   PreparedQuery pq = datastore.prepare(query);
	   return pq.asSingleEntity();
   }
   
   public static Iterable<Entity> getMultipleEntities(String entityName,Filter...filters){
	   Query query = new Query(entityName);
	   for(int x = 0; x < filters.length; x++){
		   query.setFilter(filters[x]);
	   }
	   PreparedQuery pq = datastore.prepare(query);
	   return pq.asIterable();
   }
   
   public static Iterable<Entity> getMultipleEntities(String entityName,String sortProperty, SortDirection direction ,Filter...filters){
	   Query query = new Query(entityName);
	   for(int x = 0; x < filters.length; x++){
		   query.setFilter(filters[x]);
	   }
	   query.addSort(sortProperty, direction);
	   PreparedQuery pq = datastore.prepare(query);
	   return pq.asIterable();
   }
   
   public static Iterable<Entity> getMultipleEntities(String entityName,String sortProperty, SortDirection direction,FetchOptions options, Filter...filters){
	   Query query = new Query(entityName);
	   for(int x = 0; x < filters.length; x++){
		   query.setFilter(filters[x]);
	   }
	   query.addSort(sortProperty, direction);
	   PreparedQuery pq = datastore.prepare(query);
	   return pq.asIterable(options);
   }
   
   
   
   public static Iterable<Entity> getMultipleEntities(String entityName,String propertyName,String value,FilterOperator filterOperator){
	      Filter filter = new FilterPredicate(propertyName,filterOperator,value);
		  Query query = new Query(entityName).setFilter(filter);
		  PreparedQuery pq = datastore.prepare(query);
		  return pq.asIterable();
   }
   
   
   
   public static JSONObject entityToJSON(Entity en){
	   JSONObject obj = new JSONObject();
	   try{
		   Map map = en.getProperties(); 
		   Iterator<String> iter = map.keySet().iterator();
		   while(iter.hasNext()){
			   String propertyName = iter.next();
			   Object value = map.get(propertyName);
			   obj.put(propertyName,value);
			 }
		   }
	   catch(Exception e){
		 
	 }
	 return obj;
   }
   
   public static JSONObject entityToJSON(Iterable<Entity> iterable){
	   JSONObject obj = new JSONObject();
	   try{
		   for(Entity en : iterable){
			   Map map = en.getProperties(); 
			   Iterator<String> iter = map.keySet().iterator();
			   while(iter.hasNext()){
				   String propertyName = iter.next();
				   Object value = map.get(propertyName);
				   if(obj.opt(propertyName) == null){
					  obj.put(propertyName,new JSONArray().put(value));
				   }
				   else {
					  ((JSONArray)obj.opt(propertyName)).put(value); 
				   }
			  }
		   }
	   }
	   catch(Exception e){
		 
	 }
	 return obj;
   }
   

  
   public static Iterable<Entity> getAllEntities(String entityName,FetchOptions options){
	   Query query=new Query(entityName);
	   PreparedQuery pq=datastore.prepare(query);
	   return pq.asIterable(options);
   }
   
   public static Iterable<Entity> getAllEntities(String entityName){
	   Query query=new Query(entityName);
	   PreparedQuery pq=datastore.prepare(query);
	   return pq.asIterable();
   }
   
   public static void updateSingleEntity(String entityName,String primaryKey,String primaryKeyValue,String propertyName, String propertyValue,FilterOperator filterOperator){
	   Filter filter = new FilterPredicate(primaryKey,filterOperator,primaryKeyValue);
	   Query query=new Query(entityName).setFilter(filter);
	   PreparedQuery pq=datastore.prepare(query);
	   Entity en = pq.asSingleEntity();
	   en.setProperty(propertyName,propertyValue);
	   datastore.put(en);
   }
   
   public static void updateSingleEntity(String entityName,String primaryKey,String primaryKeyValue,String[] propertyNames, String [] propertyValues,FilterOperator filterOperator){
	   Filter filter = new FilterPredicate(primaryKey,filterOperator,primaryKeyValue);
	   Query query=new Query(entityName).setFilter(filter);
	   PreparedQuery pq=datastore.prepare(query);
	   Entity en = pq.asSingleEntity();
	   for(int x = 0 ; x < propertyNames.length; x++){
		   en.setProperty(propertyNames[x],propertyValues[x]);  
	   }
	   datastore.put(en);
   }
   
   public static void updateMultipeEntities(String entityName,String primaryKey,String primaryKeyValue,String propertyName, String propertyValue,FilterOperator filterOperator){
	   Filter filter =new FilterPredicate(primaryKey,filterOperator,primaryKeyValue);
	   Query query=new Query(entityName).setFilter(filter);
	   PreparedQuery pq=datastore.prepare(query);
	   for(Entity en : pq.asIterable()){
		  en.setProperty(propertyName, propertyValue);
		  datastore.put(en);
	   }
   }
   
   public static void updateAllEntities(String entityName,String propertyName,String newValue){
	   Query query=new Query(entityName);
	   PreparedQuery pq=datastore.prepare(query);
	   for(Entity en : pq.asIterable()){
		   en.setProperty(propertyName, newValue);
		   datastore.put(en);
	    }
   }
   
   
   public static void deleteSingleEntity(String entityName,String propertyName,String value,FilterOperator filterOperator){
		  Filter filter =new FilterPredicate(propertyName,filterOperator,value);
		  Query query=new Query(entityName).setFilter(filter);
		  PreparedQuery pq=datastore.prepare(query);
		  Entity en = pq.asSingleEntity();
		  if(en != null)
			  datastore.delete(en.getKey());  
   }
   
   public static void deleteSingleEntity(String entityName,Filter ... filters){
		  Query query=new Query(entityName);
		  for(int x = 0; x < filters.length; x++){
			  query.setFilter(filters[x]);
		  }
		  PreparedQuery pq=datastore.prepare(query);
		  Entity en = pq.asSingleEntity();
		  if(en != null)
			  datastore.delete(en.getKey());
  }

   public static void deleteMultipleEntities(String entityName,String propertyName,String value,FilterOperator filterOperator){
	      Filter filter =new FilterPredicate(propertyName,filterOperator,value);
		  Query query = new Query(entityName).setFilter(filter);
		  PreparedQuery pq = datastore.prepare(query);
		  for(Entity en : pq.asIterable()){
			 datastore.delete(en.getKey());
		  }
   }

   public static void deleteAllEntities(String entityName,FetchOptions options){
	   Query query=new Query(entityName);
	   PreparedQuery pq=datastore.prepare(query);
	   for(Entity en : pq.asIterable()){
			 datastore.delete(en.getKey());
	   }

   }
   
   
   
     
    
}
