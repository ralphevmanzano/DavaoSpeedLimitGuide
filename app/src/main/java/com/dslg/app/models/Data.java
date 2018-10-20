package com.dslg.app.models;

import java.io.Serializable;

public class Data implements Serializable
{
  private static volatile Data ourInstance;
  
  private Area   area;
  private String status;
  
  private Data()
  {
    if (ourInstance != null)
    {
      throw new RuntimeException("Use getInstance() method to get the singleton instance");
    }
  }
  
  public static Data getInstance()
  {
    if (ourInstance == null)
    { //if there is no instance available... create new one
      synchronized (Data.class)
      {
        if (ourInstance == null)
        {
          ourInstance = new Data();
        }
      }
    }
    
    return ourInstance;
  }
  
  protected Data readResolve()
  {
    return getInstance();
  }
  
  public Area getArea()
  {
    return area;
  }
  
  public void setArea(Area area)
  {
    this.area = area;
  }
  
  public String getStatus()
  {
    return status;
  }
  
  public void setStatus(String status)
  {
    this.status = status;
  }
  
  
}
