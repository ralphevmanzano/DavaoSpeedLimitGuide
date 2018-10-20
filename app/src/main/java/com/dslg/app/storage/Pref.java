package com.dslg.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;


public class Pref {
	private static final String FILE                        = "DslgPreferences";
	private static final String UDID                        = "DeviceUdid";
	private static final String LOCATION_LABEL              = "LocationLabel";
	private static final String SOUNDON                     = "SoundOn";
	private static final String REQUESTING_LOCATION_UPDATES = "RequestingLocationUpdates";
	private static final String LAST_LAT_LNG                = "LastLatLng";
	public static final  String MAX_SPEED                   = "DslgMaxSpeed";
	public static final  String HAS_NETWORK                 = "HasNetwork";
	public static final  double DAVAO_LAT                   = 7.0818875;
	private static final double DAVAO_LONG                  = 125.5513889;
	
	private        SharedPreferences mPref;
	private static Pref              ourInstance;
	
	public static void init(Context context) {
		ourInstance = new Pref(context);
	}
	
	public static Pref getInstance() {
		return ourInstance;
	}
	
	private Pref(Context context) {
		mPref = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
	}
	
	public SharedPreferences getPref() {
		return mPref;
	}
	
	public void setMaxSpeed(long maxSpeed) {
		SharedPreferences pref = mPref;
		if (maxSpeed > 0) {
			if (maxSpeed != getMaxSpeed()) {
				SharedPreferences.Editor editor = pref.edit();
				editor.putLong(MAX_SPEED, maxSpeed);
				editor.apply();
			}
		}
		else {
			if (pref.contains(MAX_SPEED)) {
				SharedPreferences.Editor editor = pref.edit();
				editor.remove(MAX_SPEED);
				editor.apply();
			}
		}
	}
	
	public long getMaxSpeed() {
		SharedPreferences pref = mPref;
		return pref.getLong(MAX_SPEED, -1);
	}
	
	public void setLocationLabel(String locationLabel) {
		SharedPreferences pref = mPref;
		if (StringUtils.isNotBlank(locationLabel)) {
			if (!locationLabel.equals(getLocationLabel())) {
				SharedPreferences.Editor editor = pref.edit();
				editor.putString(LOCATION_LABEL, locationLabel);
				editor.apply();
			}
		}
		else {
			if (pref.contains(LOCATION_LABEL)) {
				SharedPreferences.Editor editor = pref.edit();
				editor.remove(LOCATION_LABEL);
				editor.apply();
			}
		}
	}
	
	public String getLocationLabel() {
		SharedPreferences pref = mPref;
		return pref.getString(LOCATION_LABEL, "");
	}
	
	public void setUdid(String uuid) {
		SharedPreferences pref = mPref;
		if (StringUtils.isNotBlank(uuid)) {
			SharedPreferences.Editor editor = pref.edit();
			editor.putString(UDID, uuid);
			editor.apply();
		}
		else {
			if (pref.contains(UDID)) {
				SharedPreferences.Editor editor = pref.edit();
				editor.remove(UDID);
				editor.apply();
			}
		}
	}
	
	public String getUdid() {
		SharedPreferences pref = mPref;
		return pref.getString(UDID, "");
	}
	
	public void setSoundOn(boolean soundOn) {
		SharedPreferences pref = mPref;
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(SOUNDON, soundOn);
		editor.apply();
	}
	
	public boolean getSoundOn() {
		SharedPreferences pref = mPref;
		return pref.getBoolean(SOUNDON, true);
	}
	
	public void setRequestingLocationUpdates(boolean requestingLocationUpdates) {
		SharedPreferences pref = mPref;
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(REQUESTING_LOCATION_UPDATES, requestingLocationUpdates);
		editor.apply();
	}
	
	public boolean getRequestingLocationUpdates() {
		SharedPreferences pref = mPref;
		return pref.getBoolean(REQUESTING_LOCATION_UPDATES, false);
	}
	
	public void setLastLatLng(LatLng latLng) {
		SharedPreferences pref = mPref;
		if (latLng != null) {
			Gson gson = new Gson();
			String data = gson.toJson(latLng, LatLng.class);
			if (StringUtils.isNotBlank(data)) {
				SharedPreferences.Editor editor = pref.edit();
				editor.putString(LAST_LAT_LNG, data);
				editor.apply();
			}
			else {
				if (pref.contains(LAST_LAT_LNG)) {
					SharedPreferences.Editor editor = pref.edit();
					editor.remove(LAST_LAT_LNG);
					editor.apply();
				}
			}
		}
	}
	
	public LatLng getLastLatLng() {
		SharedPreferences pref = mPref;
		Gson gson = new Gson();
		String json = pref.getString(LAST_LAT_LNG, "");
		if (StringUtils.isNotBlank(json)) {
			return gson.fromJson(json, LatLng.class);
		}
		return new LatLng(DAVAO_LAT, DAVAO_LONG);
	}
	
	public void setHasNetwork(boolean hasNetwork) {
		if (getHasNetwork() != hasNetwork) {
			SharedPreferences pref = mPref;
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean(HAS_NETWORK, hasNetwork);
			editor.apply();
		}
	}
	
	public boolean getHasNetwork() {
		SharedPreferences pref = mPref;
		return pref.getBoolean(HAS_NETWORK, true);
	}
}
