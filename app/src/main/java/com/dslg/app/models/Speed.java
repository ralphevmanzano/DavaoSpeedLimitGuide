package com.dslg.app.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.dslg.app.BR;
import com.dslg.app.constants.Values;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Speed extends BaseObservable
{
	private long time;
  private int  currentSpeed;
  private int  maxSpeed;
	private int type;
	private boolean hasExceeded;
	private String listSpeed;
	private List<String> listLabel;
	public static final  int CHART       = 1;
	public static final  int TABLE_LABEL = 2;
	private static final int TABLE_ROW   = 3;
  
  public Speed(long time, int currentSpeed, int maxSpeed)
  {
    this.time = time;
    this.currentSpeed = currentSpeed;
    this.maxSpeed = maxSpeed;
    hasExceeded = maxSpeed > 0 && currentSpeed > maxSpeed;
    this.type = TABLE_ROW;
  }
  
  public Speed(String listSpeed) {
  	this.listSpeed = listSpeed;
  	this.type = CHART;
	}
	
	public Speed(List<String> listLabel) {
  	this.listLabel = listLabel;
  	this.type = TABLE_LABEL;
	}
  
  public long getDateTimeMillis()
  {
    return time;
  }
  
  @Bindable
  public String getTime()
  {
    String date = new SimpleDateFormat(Values.XAXIS_LABEL_DATE, Locale.ENGLISH)
				.format(new Date(getDateTimeMillis()));
    String time = new SimpleDateFormat(Values.XAXIS_LABEL_TIME, Locale.ENGLISH)
				.format(new Date(getDateTimeMillis()));
    return String.format(Locale.ENGLISH, "%s, %s", date, time);
  }
  
  @Bindable
  public int getCurrentSpeed()
  {
    return currentSpeed;
  }
  
  @Bindable
  public int getMaxSpeed()
  {
    return maxSpeed;
  }
	
	public String getListSpeed() {
		return listSpeed;
	}
	
	public List<String> getListLabel() {
		return listLabel;
	}
	
	public int getType() {
		return type;
	}
	
	@Bindable
  public boolean isHasExceeded()
  {
    return hasExceeded;
  }
}
