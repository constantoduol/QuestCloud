
package com.quest.access.useraccess;

/**
 *
 * @author constant oduol
 * @version 1.0(30/12/2011)
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This file defines a resource group
 * A resource group extends java.util.Hashset 
 * It is a way of grouping resources together
 * A resource group is a set of closely related resources
 * That can be accessed by a given user. resource group is the super class
 * of permanent privilege and temporary privilege
 */
public class ResourceGroup extends HashSet implements java.io.Serializable {
    
    /*
     * This stores the name of the resource group
     * Names of resource groups should be descriptive
     * For example a resource group could be an administrator resource group
     * or it could be a normal resource group
     */
     private String resourceGroupName;
     
     /*
      * this defines the level of the resource group
      */
     private int groupLevel;
     
     /*
      *This stores a unique id representing the resource group 
      */
     
     private int resourceGroupId;
     
      /*
      * This stores the total number of resource groups that have been initialized so far
      */
     private static int resourceGroupCount;
     
     
     /**
      * 
      * @param name this is the name of this resource group
      *             Names of resource groups should be descriptive
      *              For example a resource group could be an administrator resource group
      *              or it could be a normal resource group
      * @param level This is used to distinguish the hierachy of resource groups
      *              a resource group with a higher groupLevel is superior to a resourceGroup with lower groupLevel
      *              groupLevels are issued by the person defining the resource groups
      *              A resource group which enables more access to privileged information is considered to have a higher groupLevel
      *              than a resource group that enables access to lower priority services or information
      *              A developer has to choose a maximum groupLevel and a minimum from where to start numbering
      *              For example one could start from 0,1,2,3,4.... upto 9 giving a total of 10 resource groups
      * 
      */
      public ResourceGroup (String name, int level){
           super();
           this.resourceGroupName=name;
           this.groupLevel=level;
           resourceGroupCount++;
           this.resourceGroupId=resourceGroupCount;
      }
    
      /**
       * this method returns the name of the resource group
       */
      
      public String getName(){
          return this.resourceGroupName;
        }
      
      /**
       * This method is used to change the name of the resource group
       */
      
      public void setName(String name){
          this.resourceGroupName=name;
        }
      
     
      /**
       * This method returns the id of the resource group
       */
      
      public int getId(){
          return this.resourceGroupId;
      }
      
      /**
       * This method returns the group level of the resource group
       */
      
      public Integer getGroupLevel(){
          return this.groupLevel;
      } 
      
      /**
       * This method sets a new value for the group level
       */
      public void setGroupLevel(Integer level){
          this.groupLevel=level;
      }
      
      /**
       * This method adds a resource to this resource group
       */
      public boolean addResource(Resource res){
          return super.add(res);
          
      }
      
      /**
       * This method removes a resource from the particular resource group
       */
      
      public boolean removeResource(Resource res){
           return super.remove(res);
      }
     
      /**
       * This method is used to add many resources to a resource group
       * for example where res, res1, res2, res3 are resources then:
       *   <code>
       *     ResourceGroup group=new ResourceGroup("Admin",100);
       *      group.addManyResources(res,res1,res2,res3....);
       * </code>
       */
      
      public void addManyResources(Resource... resources){
            for(Resource resource : resources){
                this.addResource(resource);
            }
      }
      
      /**
       * This method removes many resources from a resource group
       */
      
      public void removeManyResources(Resource... resources){
            for(Resource resource : resources){
                this.removeResource(resource);
            }
      }
      
      /**
       * This method returns the number of resource groups that have been initialized so far
       */
      public static int getResourceGroupCount(){
          return resourceGroupCount;
      }
      
      /*
       * This method returns an arraylist of strings containing the names of
       * the resources in the resource group
       */
      
      public ArrayList getMemberNames(){
          ArrayList names=new ArrayList();
           Iterator iter=this.iterator();
            for(int x=0; iter.hasNext(); x++ ){
               names.add(((Resource)iter.next()).getResourceName());
            }
           return names;
       }
      
      /**
       * This method returns an array of resources of the resources in this resource group
       */
      public Resource [] getMemberResources (){
           // create an empty array to store the resource objects
           Resource [] resources=new Resource[this.size()];
            //go through the whole resource group getting the resources
           
           Iterator iter =this.iterator();
            for(int x=0; iter.hasNext(); x++){
                 Object next=iter.next();
                  Resource res=(Resource)next;
                   resources[x]=res;
            }
             return resources;
      }
      
      /*
       * This method returns an array of classes of the interfaces 
       * wrapped in the resources in the given resource group
       */
      public Class[] getMemberClasses(){
         Resource[] res=getMemberResources();
           Class[] classes=new Class[res.length];
           for(int x=0; x<res.length; x++){
               classes[x]=res[x].getInterface();
           }
           return classes;
      }    
      
      /**
       * This method is the same as addResource
       * this method is overriden from java.util.Hashset and only resource
       * objects are added through this method, any other object is rejected
       * 
       */
    @Override
      public  boolean add(Object obj){
         if(obj instanceof Resource){
           Resource res=(Resource)obj;
            return this.addResource(res); 
            }
          return false;
      }
    
    /**
     * This method is the same as removeResource
     * This method is overriden to allow only resource objects to
     * be removed, any other object is rejected
     */
    @Override
      public boolean remove(Object obj){
          if(obj instanceof Resource){
             Resource res=(Resource)obj;
              return this.removeResource(res); 
              }
             return false;
      }
    
    /**
     * This method returns a string representation of a resource group
     * It prints out the name and id of the resource group
     * in this format ResourceGroup[name: id]
     */
    @Override
    public String toString(){
        return "ResourceGroup["+this.resourceGroupName+" : "+this.resourceGroupId+"]";
    }
    
    /**
     * Two resource groups are considered equal if and only if they have exactly the same resources
     * <code>
     *  <br>
     *   Resource res=new Resource(test.class);<br>
     *   Resource res1=new Resource(test1.class);<br>
     *   Resource res2=new Resource(test.class);<br>
     *    ResourceGroup group=new ResourceGroup("Admin",100);<br>
     *    ResourceGroup group1=new ResourceGroup("Normal",90);<br>
     *    group.addManyResources(res,res1);<br>
     *    group1.addManyResources(res,res1);<br>
     *    group.equals(group1)==true;<br>
     *    group1.equals(group)==true;<br>
     *    group.addResource(res2);<br>
     *    </code>
     * Since res=res2 adding res to group and then adding res2 has no effect
     * since 
     *    <code>
     *    res.equals(res2)==true<br>
     *     </code>
     * Therefore resource groups are considered equal if they have exactly the same resources 
     * contained in them
     */
    @Override
    public boolean equals(Object obj){
        if(obj instanceof ResourceGroup){
          ResourceGroup group=(ResourceGroup)obj;
            // check the resource group member names
            // the member names are unique to the resources contained in the resource group
            ArrayList firstMemberNames=this.getMemberNames();
               ArrayList secondMemberNames = group.getMemberNames();
            
            // check whether the current resource group and the passed resource group contain the same member names
            if(firstMemberNames.containsAll(secondMemberNames)==true && secondMemberNames.containsAll(firstMemberNames)==true){
                   return true;
                 
                  }
               else{
                   return false;
                   }
        
                }
        
        return false;
    }

    /*
     * generates a unique hash code for a resource group
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.resourceGroupName != null ? this.resourceGroupName.hashCode() : 0);
        hash = 31 * hash + this.groupLevel;
        hash = 31 * hash + this.resourceGroupId;
        return hash;
    }
    
        
        
        
    }
      

