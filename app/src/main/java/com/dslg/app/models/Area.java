package com.dslg.app.models;

import java.io.Serializable;

public class Area implements Serializable
{
  private static volatile Area ourInstance;
  
  private int    id;
  private String label;
  private long   maxSpeed;
  
  private Area()
  {
    if (ourInstance != null)
    {
      throw new RuntimeException("Use getInstance() method to get the singleton instance");
    }
  }
  
  public static Area getInstance()
  {
    if (ourInstance == null)
    { //if there is no instance available... create new one
      synchronized (Area.class)
      {
        if (ourInstance == null)
        {
          ourInstance = new Area();
        }
      }
    }
    
    return ourInstance;
  }
  
  protected Area readResolve()
  {
    return getInstance();
  }
  
  public long getMaxSpeed()
  {
    return maxSpeed;
  }
  
  public void setMaxSpeed(long maxSpeed)
  {
    this.maxSpeed = maxSpeed;
  }
  
  public void setId(int id)
  {
    this.id = id;
  }
  
  public void setLabel(String label)
  {
    this.label = label;
  }
  
  public String getLabel()
  {
    return label;
  }
}
