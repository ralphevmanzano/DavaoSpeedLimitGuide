package com.dslg.app.storage;

import com.dslg.app.models.Speed;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.LinkedList;

public class CacheHandler {
	private static final String SPEED_LISTS_FILE = "SpeedListsFile";
	private static volatile CacheHandler ourInstance;
	private                 CacheManager manager;
	
	private CacheHandler(CacheManager manager) {
		this.manager = manager;
		if (ourInstance != null) {
			throw new RuntimeException("Use getInstance() method to get the singleton instance");
		}
	}
	
	public static CacheHandler getInstance(CacheManager manager) {
		if (ourInstance == null) { //if there is no instance available... create new one
			synchronized (CacheHandler.class) {
				if (ourInstance == null) {
					ourInstance = new CacheHandler(manager);
				}
			}
		}
		
		return ourInstance;
	}
	
	public void setSpeedList(LinkedList<Speed> speedLists) {
		Gson gson = new Gson();
		String data = gson.toJson(speedLists);
		manager.writeToFile(data, SPEED_LISTS_FILE);
	}
	
	public LinkedList<Speed> getSpeedList() {
		LinkedList<Speed> speedList = new LinkedList<>();
		try {
			String data = manager.readFromFile(SPEED_LISTS_FILE);
			if (StringUtils.isNotBlank(data))
			{
				Type type = new TypeToken<LinkedList<Speed>>(){}.getType();
				Gson gson = new Gson();
				return gson.fromJson(data, type);
			}
		}
		catch (NullPointerException | JsonParseException e) {
			e.printStackTrace();
		}
		return speedList;
	}
}
