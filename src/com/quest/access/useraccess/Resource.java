
package com.quest.access.useraccess;

/**
 *
 * @author constant oduol
 * @version 1.0 (29/12/2011)
 * 
 */
import java.lang.reflect.Field;
import java.lang.reflect.Method;




/**
 * <p>
 * A resource is an object that is used for user access control
 * a resource is an object that wraps an interface class object.
 * The interface defines methods that should not be accessible to
 * every user, therefore by wrapping the interface in a resource object
 * a server's privilege handler is able to determine which user has access
 * to a given resource or not, resources are stored in the RESOURCES table in the
 * database. Resources can be enabled or disabled, enabling access to a resource
 * ensures that those having access to it can call its methods, a disabled resource
 * cannot be accessed by any user.
 * </p>
 * 
 */
public class Resource implements java.io.Serializable{
    /*
     * this determines whether a resource can be assigned as a temporary privilege
     */
    private boolean isAssignableAsTemporaryPrivilege;
    /*
     * This determines whether the resource is accessible or not
     */
    private boolean isEnabled;
    
    /*
     * This is a unique number that identifies a resource
     */
    private int resourceId;
    /*
     * This stores the number of resources that have been initialized so far
     */
    private static int resourceCount;
    /*
     * This is an instance variable to store the interface passed to the resource;
     */
    private Class cls;
    /*
     * This is the name of the resource
     * The default name is the name of the interface
     */
    private String name;
    
    /*
     * This is a description of what a user does when he/she accesses the resource
     */
    private String resourceText;
    
    /**
     * @param cls this is the interface defining the methods to control access to
     * for example
     * <code>
     *   interface access {
     *     void privilege1();
     *     void privilege2();
     *    }
     * 
     * Resource res=new Resource(access.class);
     * </code>
     */
    
    public Resource(Class cls) {
        // we only want interfaces, check to ensure we have an interface
       if(!cls.isInterface()){
           // if the object passed is not an interface throw an illegalargumentexception
            throw new IllegalArgumentException(""+cls+" is not an interface");
          }
           //count the resource
           resourceCount++;

           //assign an id to the resource
           this.resourceId=resourceCount;
           // from here we are dealing with an interface
       
           this.cls=cls;
           
           //this resource can be assigned to a user temporarily at runtime
           this.isAssignableAsTemporaryPrivilege=true;
           
           //assign the name of the interface to be the resource's name         
           this.name=cls.getSimpleName();
           //make the resource to be enabled
           this.isEnabled=true;
           try{
            Field field= this.cls.getDeclaredField("isTemporary");
            this.isAssignableAsTemporaryPrivilege= field.getBoolean(null);
            
              }
           catch(Exception e){
              this.isAssignableAsTemporaryPrivilege=false;
            
           }

    }
    
    
    /**
     * @param cls the class of the interface 
     * @param name the name to be assigned to the resource in place of the name of the interface
     * @param resourceText this is the text that describes what a user does when he/she accesses this resource
     * 
     * 
     */
    public Resource(Class cls, String resourceText){
          // call the constructor Resource(Class cls)   
        this(cls);
        this.resourceText=resourceText;
    }
    
    /**
     * This method changes the interface wrapped in the resource object
     * @param cls this represents the interface we want the resource to wrap
     */

    public void setInterface(Class cls){
        // check to ensure the class passed is that of an interface
        if(!cls.isInterface()){
           // if the object passed is not an interface throw an illegalargumentexception
            throw new IllegalArgumentException(""+cls+" is not an interface");
          }
          this.cls=cls;
      }
    
    
    /**
     * @return this method returns the class object of the wrapped interface 
     */
    public Class getInterface(){
        return this.cls;
     }
     
    /**
     * This method returns an array of Strings containing  the names of the methods in the wrapped interface
     */
    public String [] getResourceMethodNames(){
        Method[] methods = getInterface().getDeclaredMethods();
          // a temporary array of strings to hold the method names 
          String[] temp=new String[methods.length];
          for(int x=0; x<methods.length; x++){
             temp[x]= methods[x].getName();
              
             }
            return temp;
     }
     
    /**
     * This method returns an array of methods in the wrapped interface 
     */
    public Method[] getResourceMethods(){
        return this.cls.getDeclaredMethods();
    }
    
    /**
     * this method checks whether a specified method is defined in a given resource
     * @param res the resource we want to check whether the specified method is found in it
     * @param met the method we want to know if it is defined in this resource
     * 
     */
    public static boolean isMethodInResource(Resource res,Method met){
        Method[] declaredMethods = res.getInterface().getDeclaredMethods();
         for(int x=0; x<declaredMethods.length; x++){
           if(declaredMethods[x].equals(met)){
               return true;
           }  
         }
         return false;
    }
   
    /**
     * checks if the specified method is this resource
     * @see #isMethodInResource(com.qaccess.useraccess.Resource, java.lang.reflect.Method) 
     */
    public boolean isMethodInResource(Method met){
        return isMethodInResource(this, met);
    }
    
    /**
     * This method returns the name of the interface wrapped in the resource
     */
   
    public String getResourceName(){
       return this.getInterface().getSimpleName();
    }
    
    /**
     * This method returns the description of the resource
     */
    
    public String getResourceText(){
        return this.resourceText;
    }
    
    /**
     * This method changes the description of the resource
     * @param text this is the new description of the resource
     */
    public void setResourceText(String text){
        this.resourceText=text;
    }
    
    /**
     * This method returns the number of resources that have been initialized so far
     */
    public static int getResourceCount(){
        return resourceCount;
    }
    
    /**
     * This method returns the unique id of the particular resource
     */
    public int getResourceId(){
        return this.resourceId;
    }
    
    
    
    /**
     * this method tells us whether this resource is assignable as a temporary
     * privilege or not
     */
    public boolean getAssignableState(){
        return this.isAssignableAsTemporaryPrivilege;
    }
    
    /**
     * This methods returns a string description of a resource
     * The string contains the name and id of the resource
     * in this format Resource[name: id]
     */
    @Override
    public String toString(){
        return "Resource["+this.name+" : "+this.resourceId+"]";
    }

    /**
     * One resource is equal to another resource if and only if they wrap the same interface
     * <code>
     * Resource res=new Resource(test.class);
     * Resource res1=new Resource(test1.class);
     * Resource res2=new Resource(test.class);
     *    res.equals(res1)==false;
     *    res.equals(res2)==true;
     *    res2.equals(res)==true;
     * </code>
     * Therefore two resources are only equal if the interface wrapped in them 
     * is the same and if the resources belong
     * to the same package;
     */
    
    @Override
    public boolean equals(Object obj){
         if(!(obj instanceof Resource)){
             return false;
         }
           // from here we are dealing with resources only
           Resource res=(Resource)obj;
              //similar resources have similar names and belong to the same package
             String firstPkg=this.getInterface().getPackage().getName();
             String secondPkg=res.getInterface().getPackage().getName();
              String firstResourceName = this.getResourceName();
               String secondResourceName=res.getResourceName();
                  if(firstPkg.equals(secondPkg) && firstResourceName.equals(secondResourceName)){
                      return true;
                  }
                return false;
          
    }
    
    /**
     * This method overrides the method hash code in object
     * It returns the hash code of a resource
     * the hash code returned depends on
     *   <ol>
     *     <li>The id of a resource</li>
     *     <li>The interface wrapped in the resource</li>
     *     <li>The resource text of the particular resource</li>
     *  </ol>
     */

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.resourceId;
        hash = 29 * hash + (this.cls != null ? this.cls.hashCode() : 0);
        hash = 29 * hash + (this.resourceText != null ? this.resourceText.hashCode() : 0);
        return hash;
    }
    
    
    /**
     * This method is used to turn a resource on
     * A resource is on if 
     *   <code>
     *      this.isEnabled==true;
     *    <code>
     * An enabled resource can be accessed by those who have access rights to it
     * A disabled resource cannot be accessed by anyone
     * If the resource is already enabled calling the method has no effect
     * 
     */
    public void enableAccess(){
        this.isEnabled=true;
    }
    
    /**
     * This method disables access to a resource
     * When the resource is off or disabled it cannot be accessed by anyone
     */
    public void disableAccess(){
        this.isEnabled=false;
    }
    
    /**
     * This method is used to tell whether the resource is enabled or disabled
     * If the resource is enabled it returns true
     * if the resource is disabled it returns false
     */
    
    public boolean getAccessState(){
        return this.isEnabled;
    }
}




