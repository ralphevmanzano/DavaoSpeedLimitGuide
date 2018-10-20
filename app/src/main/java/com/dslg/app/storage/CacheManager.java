package com.dslg.app.storage;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CacheManager
{
  private static volatile CacheManager ourInstance;
  private final Context context;
  
  private CacheManager(Context context)
  {
    this.context = context;
    if (ourInstance != null)
    {
      throw new RuntimeException("Use getInstance() method to get the singleton instance");
    }
  }
  
  public static CacheManager getInstance(Context context)
  {
    if (ourInstance == null)
    { //if there is no instance available... create new one
      synchronized (CacheManager.class)
      {
        if (ourInstance == null)
        {
          ourInstance = new CacheManager(context);
        }
      }
    }
    
    return ourInstance;
  }
  
  public boolean deleteFromFile(String fileName)
  {
    if (context == null)
    {
      return false;
    }
    File file = context.getFileStreamPath(fileName);
    if (!file.exists())
    {
      return false;
    }
    return file.delete();
  }
  
  public String readFromFile(String filename)
  {
    String ret = "";
    
    try
    {
      InputStream inputStream = context.openFileInput(filename);
      
      if (inputStream != null)
      {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String receiveString;
        StringBuilder stringBuilder = new StringBuilder();
        
        while ((receiveString = bufferedReader.readLine()) != null)
        {
          stringBuilder.append(receiveString);
        }
        
        inputStream.close();
        ret = stringBuilder.toString();
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return ret;
  }
  
  
  public void writeToFile(String data, String filename)
  {
    try
    {
      OutputStreamWriter outputStreamWriter =
          new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
      outputStreamWriter.write(data);
      outputStreamWriter.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
