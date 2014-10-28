/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quest.school;

import com.quest.access.useraccess.Serviceable;
import com.quest.access.useraccess.services.annotations.Endpoint;

/**
 *
 * @author connie
 */
public class QuestionService implements Serviceable {

    private static String QUESTION_SVC_URL = "";
    @Override
    public void service() {
      //dummy
    }

    @Override
    public void onCreate() {
      
    }

    @Override
    public void onStart() {
       
    }
    
    @Endpoint(name = "add_question")
    public void addQuestion(){
        
    }
    
    @Endpoint(name = "edit_question")
    public void editQuestion(){
        
    }
    
    @Endpoint(name = "edit_choice")
    public void editChoice(){
        
    }
    
    @Endpoint(name = "add_choice")
    public void addChoice(){
        
    }
    
    @Endpoint(name = "add_image")
    public void addImage(){
        
    }
    
    @Endpoint(name = "remove_image")
    public void removeImage(){
        
    }
    
    @Endpoint(name = "add_tag")
    public void addTag(){
        
    }
    
    
    @Endpoint(name = "remove_tag")
    public void removeTag(){
        
    }
    
    @Endpoint(name = "generate_exam")
    public void generateExam(){
        
    }
}
