/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quest.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import com.quest.access.control.Server;


/** 
 *
 * @author connie
 */


public class ServerLink extends HttpServlet {
    private static Server server;
  
    private static  ArrayList skippablesMessages = new ArrayList();
    private static  ArrayList skippablesServices = new ArrayList();
    static{
       skippablesMessages.add("login");
       skippablesMessages.add("changepass");
       skippablesMessages.add("validate_key");
       skippablesMessages.add("logout");
       skippablesMessages.add("activation_details");
       
       skippablesServices.add("");
       skippablesServices.add("");
       skippablesServices.add("activation_service");
       skippablesServices.add("");
       skippablesServices.add("activation_service");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try { 
             response.setContentType("text/html;charset=UTF-8");
             String json = request.getParameter("json");
             if(json==null){
                return;
             }
             HttpSession session=request.getSession();
             String [] currentUser;
            //session, request, response
             JSONObject obj=new JSONObject(json);
             JSONObject headers = obj.optJSONObject("request_header");
             String msg=headers.optString("request_msg");
             int index = skippablesMessages.indexOf(msg);
             if(index > -1 && skippablesServices.get(index).equals("")){
                 // when someone is changing his password we dont expect them to be logged in
                 //when someone is logging in we dont expect them to be logged in
                 currentUser = new String[]{"dummy"};
             }
             else{
               currentUser = ensureIntegrity(request,response,session);   
             }
             String service = headers.optString("request_svc");
             JSONObject requestData=(JSONObject)obj.optJSONObject("request_object");
             ClientWorker worker = new ClientWorker(msg, service, requestData, session,response);
             worker.work();
        } catch (JSONException ex) {
            Logger.getLogger(ServerLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void redirect(HttpServletResponse response,String address){
        try {
            JSONObject object=new JSONObject();  
            object.put("request_msg","redirect");
            object.put("url",address);
            response.getWriter().print(object);
        } catch (Exception ex) {
            Logger.getLogger(ServerLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String[] ensureIntegrity(HttpServletRequest request,HttpServletResponse response,HttpSession ses){
       String [] currentUser=new String[2];
       try{
           Cookie[] cookies = request.getCookies();
           if(cookies==null || cookies.length==0){
           redirect(response, "index.html");
           return null;
           }
           String username;
           String rand = null;
           for(int x=0; x<cookies.length; x++){
               String name=cookies[x].getName();
               String value=cookies[x].getValue();
               if("user".equals(name)){
                   username=value;
                   currentUser[0]=username;
               }
               else if(name.equals("rand")){
                   rand=value;
                   currentUser[1]=rand;
               }
           }
           if(ses==null){
               redirect(response, "index.html");
               return null;
           }
           if(ses.getId().equals(rand)){
           // this is the right user
               return currentUser;
           }
           else{
                //make the user login again
               redirect(response, "index.html");
               return null;
           }
       }
      catch(Exception e){
        return null;
        
      }
    }
    
    
  

 
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */ 
    @Override
    public String getServletInfo() {
        return "Quest Server Link";
    }// </editor-fold>
    
    @Override 
    public void init(){
         ServletConfig config=getServletConfig();
         String passExpires=config.getInitParameter("password-expires");
         String maxRetries=config.getInitParameter("max-password-retries");
         String clientTimeout=config.getInitParameter("client-timeout");
         String mLogin=config.getInitParameter("multiple-login");
         server = new Server(); 
         server.setPassWordLife(Integer.parseInt(passExpires));
         server.setMaxPasswordAttempts(Integer.parseInt(maxRetries));
         server.setClientTimeout(Double.parseDouble(clientTimeout));
         server.setMultipleLoginState(Boolean.parseBoolean(mLogin));
         server.createNativeServices();
         server.setConfig(config);
         server.startAllServices(); 
    } 
    
    public static Server getServerInstance(){
    	return server;
    }

}
