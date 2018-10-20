package com.dslg.app.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dslg.app.MapsActivity;
import com.dslg.app.R;
import com.dslg.app.api.LocationApi;
import com.dslg.app.api.setup.ApiInterface;
import com.dslg.app.api.setup.ApiManager;
import com.dslg.app.models.Area;
import com.dslg.app.models.Data;
import com.dslg.app.models.Speed;
import com.dslg.app.storage.CacheHandler;
import com.dslg.app.storage.CacheManager;
import com.dslg.app.storage.Pref;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class LocationUpdatesService extends Service {
	private static final String PACKAGE_NAME                    = "com.dslg.app.service";
	private static final String TAG                             =
			LocationUpdatesService.class.getSimpleName();
	private static final String CHANNEL_ID                      = "channel_01";
	private static final String EXTRA_STARTED_FROM_NOTIFICATION =
			PACKAGE_NAME + ".started_from_notification";
	public static final  String ACTION_BROADCAST_LOCATION       = PACKAGE_NAME + ".broadcast";
	public static final  String ACTION_BROADCAST_EXIT           = PACKAGE_NAME + ".exit";
	public static final  String EXTRA_LOCATION                  = PACKAGE_NAME + ".location";
	
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS         = 1000;
	private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
			UPDATE_INTERVAL_IN_MILLISECONDS / 2;
	private static final int  NOTIFICATION_ID                         = 1;
	
	
	/**
	 * Used to check whether the bound activity has really gone away and not unbound as part of an
	 * orientation change. We create a foreground service notification only if the former takes
	 * place.
	 */
	private boolean mChangingConfiguration = false;
	
	private LocationApi mLocationApi;
	private final IBinder mBinder          = new LocalBinder();
	private       Handler mPostDataHandler = new Handler();
	
	private NotificationManager         mNotificationManager;
	private FusedLocationProviderClient mFusedLocationClient;
	private LocationRequest             mLocationRequest;
	private LocationCallback            mLocationCallback;
	private Location                    mLocation;
	private MediaPlayer                 mPlayer;
	private Handler                     mServiceHandler;
	private LinkedList<Speed>           speedList;
	
	private boolean isRunnableRunning  = false;
	private boolean shouldStopRunnable = false;
	
	public LocationUpdatesService() {
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		mPlayer = MediaPlayer.create(this, R.raw.beep_sound);
		mPlayer.setLooping(true);
		
		mLocationApi = LocationApi.call();
		
		createLocationCallback();
		createLocationRequest();
		
		HandlerThread handlerThread = new HandlerThread(TAG);
		handlerThread.start();
		
		mServiceHandler = new Handler(handlerThread.getLooper());
		speedList = CacheHandler.getInstance(CacheManager.getInstance(this))
														.getSpeedList();
		
		if (!isRunnableRunning) {
			mPostDataHandler.postDelayed(postDataRunnable, 5000);
			isRunnableRunning = true;
		}
		
		getLastLocation();
		initNotification();
		requestLocationUpdates();
	}
	
	private void initNotification() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		// Android O requires a Notification Channel.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = getString(R.string.app_name);
			// Create the channel for the notification
			NotificationChannel mChannel =
					new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
			mChannel.setVibrationPattern(new long[]{0});
			mChannel.enableVibration(true);
			
			// Set the Notification Channel for the Notification Manager.
			mNotificationManager.createNotificationChannel(mChannel);
		}
	}
	
	private void createLocationCallback() {
		mLocationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				super.onLocationResult(locationResult);
				onNewLocation(locationResult.getLastLocation());
			}
		};
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			int startedFromNotificationCode = bundle.getInt(EXTRA_STARTED_FROM_NOTIFICATION, 0);
			Log.i(TAG, "Service Started: startedFromNotificationCode " + startedFromNotificationCode);
			switch (startedFromNotificationCode) {
				case 1:
					startForeground(NOTIFICATION_ID, getNotification());
					break;
				
				case 2:
					onExitAppFromNotification();
					break;
				
				case 3:
					Pref pref = Pref.getInstance();
					pref.setSoundOn(false);
					if (!pref.getSoundOn() && mPlayer.isPlaying()) {
						mPlayer.stop();
						try {
							mPlayer.prepare();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
			}
		}
		
		//Tells the system to try to recreate the service after it has been killed
		return START_STICKY;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mChangingConfiguration = true;
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		// Called when a client (MainActivity in case of this sample) comes to the foreground
		// and binds with this service. The service should stop to be a foreground service
		// when that happens.
		Log.i(TAG, "in onBind()");
		
		//removes the notification
		stopForeground(true);
		mChangingConfiguration = false;
		return mBinder;
	}
	
	@Override
	public void onRebind(Intent intent) {
		// Called when a client (MainActivity in case of this sample) returns to the foreground
		// and binds once again with this service. The service should cease to be a foreground
		// service when that happens.
		Log.i(TAG, "in onRebind()");
		
		//removes the notification
		stopForeground(true);
		mChangingConfiguration = false;
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "Last client unbound from service");
		// Called when the last client (MainActivity in case of this sample) unbinds from this
		// service. If this method is called due to a configuration change in MainActivity, we
		// do nothing. Otherwise, we make this service a foreground service.
		if (!mChangingConfiguration && Pref.getInstance()
																			 .getRequestingLocationUpdates()) {
			Log.i(TAG, "Starting foreground service");
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
				Intent intent1 = new Intent(getApplicationContext(), LocationUpdatesService.class);
				intent1.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, 1);
				ContextCompat.startForegroundService(this, intent1);
			}
			else {
				startForeground(NOTIFICATION_ID, getNotification());
			}
		}
		return true; // Ensures onRebind() is called when a client re-binds.
	}
	
	@Override
	public void onDestroy() {
		shouldStopRunnable = true;
		CacheHandler ch = CacheHandler.getInstance(CacheManager.getInstance(this));
		ch.setSpeedList(speedList);
		if (mLocation != null) {
			Pref.getInstance()
					.setLastLatLng(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
		}
		if (mLocationApi != null) {
			mLocationApi.forceStop();
		}
		mServiceHandler.removeCallbacksAndMessages(null);
		stopBeep();
		super.onDestroy();
	}
	
	public void requestLocationUpdates() {
		Log.i(TAG, "Requesting location updates");
		Pref.getInstance()
				.setRequestingLocationUpdates(true);
		startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
		try {
			mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
					Looper.myLooper());
		}
		catch (SecurityException unlikely) {
			Pref.getInstance()
					.setRequestingLocationUpdates(false);
			Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
		}
	}
	
	public void removeLocationUpdates() {
		Log.i(TAG, "Removing location updates");
		try {
			mFusedLocationClient.removeLocationUpdates(mLocationCallback);
			Pref.getInstance()
					.setRequestingLocationUpdates(false);
			stopSelf();
		}
		catch (SecurityException unlikely) {
			Pref.getInstance()
					.setRequestingLocationUpdates(true);
			Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
		}
	}
	
	final Runnable postDataRunnable = new Runnable() {
		@Override
		public void run() {
			sendLocationData(constructLocationData(mLocation));
			if (!shouldStopRunnable) {
				mPostDataHandler.postDelayed(this, 5000);
			}
		}
	};
	
	private JSONObject constructLocationData(Location currentLocation) {
		if (currentLocation != null) {
			int speed = (int) (currentLocation.getSpeed() * 3600);
			try {
				JSONObject jsonObject = new JSONObject();
				JSONObject jsonCoordinates = new JSONObject();
				jsonCoordinates.put("latitude", currentLocation.getLatitude());
				jsonCoordinates.put("longitude", currentLocation.getLongitude());
				jsonObject.put("device-udid", Pref.getInstance()
																					.getUdid());
				jsonObject.put("coordinates", jsonCoordinates);
				jsonObject.put("speed", speed);
				Log.d(TAG, "constructLocationData: jsonObject " + jsonObject.toString());
				
				return jsonObject;
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private void sendLocationData(final JSONObject jsonObject) {
		final Pref pref = Pref.getInstance();
		if (jsonObject != null) {
			mLocationApi
								 .postLocation(jsonObject, new ApiInterface() {
									 @Override
									 public void onSuccess(JSONObject var1) {
										 pref.setHasNetwork(true);
										 Log.d(TAG, "constructLocationData: Success POST");
										 Pref pref = Pref.getInstance();
										 Log.d(TAG, "constructLocationData: " + var1.toString());
					
										 Area area = Area.getInstance();
					
										 JSONObject jsonData = var1.optJSONObject("data");
										 if (jsonData != null) {
											 JSONObject jsonArea = jsonData.optJSONObject("area");
											 if (jsonArea != null) {
												 int id = jsonArea.optInt("id");
												 String label = jsonArea.optString("label");
												 long maxSpeed = jsonArea.optLong("max-speed");
							
												 area.setId(id);
												 area.setLabel(label);
												 area.setMaxSpeed(maxSpeed);
											 }
						
											 String status = jsonData.optString("status");
						
											 Data data = Data.getInstance();
											 data.setArea(area);
											 data.setStatus(status);
						
//											 pref.setMaxSpeed(3000);
											 pref.setMaxSpeed(data.getArea()
																						.getMaxSpeed());
											 pref.setLocationLabel(data.getArea()
																								 .getLabel());
										 }
										 else {
											 JSONObject json = var1.optJSONObject("status");
											 if (json != null) {
												 String code = json.optString("code");
												 if ("error".equals(code)) {
													 pref.setMaxSpeed(0);
												 }
											 }
										 }
										 //										 onPostData();
									 }
				
									 @Override
									 public void onError(JSONObject var1) {
										 Log.e(TAG, "onError: " + var1.toString(), null);
									 }
				
									 @Override
									 public void onThrownException(Exception var1) {
										 Log.e(TAG, "onThrownException: " + var1.getLocalizedMessage(), null);
									 }
				
									 @Override
									 public void onFailure(Call<ResponseBody> var1, Throwable var2) {
										 Log.e(TAG, "onFailure: " + var2.getLocalizedMessage(), var2);
										 pref.setHasNetwork(false);
									 }
								 });
		}
		
	}
	
	/**
	 * Returns the {@link NotificationCompat} used as part of the foreground service.
	 */
	private Notification getNotification() {
		Intent muteIntent = new Intent(this, LocationUpdatesService.class);
		muteIntent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, 3);
		PendingIntent servicePendingIntent =
				PendingIntent.getService(this, 0, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent exitIntent = new Intent(this, LocationUpdatesService.class);
		exitIntent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, 2);
		PendingIntent exitPendingIntent = PendingIntent.getService(this, 1, exitIntent, 0);
		
		CharSequence text = getStatusText();
		
		Intent toActivityIntent = new Intent(this, MapsActivity.class);
		PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 2, toActivityIntent, 0);
		
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(this, CHANNEL_ID).addAction(R.drawable.ic_volume_off,
						getString(R.string.mute), servicePendingIntent)
																												.addAction(R.drawable.ic_exit,
																														getString(R.string.exit),
																														exitPendingIntent)
																												.setContentIntent(activityPendingIntent)
																												.setContentText(text)
																												.setContentTitle(getLocationTitle())
																												.setOngoing(true)
																												.setPriority(Notification.PRIORITY_HIGH)
																												.setSmallIcon(
																														R.drawable.ic_notif_transparent)
																												.setTicker(text)
																												.setColor(ContextCompat.getColor(this,
																														R.color.colorPrimary))
																												.setWhen(System.currentTimeMillis());
		
		// Set the Channel ID for Android O.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder.setChannelId(CHANNEL_ID); // Channel ID
		}
		
		return builder.build();
	}
	
	private void getLastLocation() {
		try {
			mFusedLocationClient.getLastLocation()
													.addOnCompleteListener(new OnCompleteListener<Location>() {
														@Override
														public void onComplete(@NonNull Task<Location> task) {
															if (task.isSuccessful() && task.getResult() != null) {
																mLocation = task.getResult();
															}
															else {
																Log.w(TAG, "Failed to get location.");
															}
														}
													});
		}
		catch (SecurityException unlikely) {
			Log.e(TAG, "Lost location permission." + unlikely);
		}
	}
	
	private void onNewLocation(Location location) {
		Log.i(TAG, "New location: " + location);
		
		mLocation = location;
		
		handleLogging();
		handleBeep();
		
		// Notify anyone listening for broadcasts about the new location.
		Intent intent = new Intent(ACTION_BROADCAST_LOCATION);
		intent.putExtra(EXTRA_LOCATION, location);
		LocalBroadcastManager.getInstance(getApplicationContext())
												 .sendBroadcast(intent);
		
		// Update notification content if running as a foreground service.
		if (serviceIsRunningInForeground(this)) {
			mNotificationManager.notify(NOTIFICATION_ID, getNotification());
		}
	}
	
	private void onExitAppFromNotification() {
		Intent intent = new Intent(ACTION_BROADCAST_EXIT);
		LocalBroadcastManager.getInstance(getApplicationContext())
												 .sendBroadcast(intent);
	}
	
	private void handleBeep() {
		Pref pref = Pref.getInstance();
		long speed = (Math.round(mLocation.getSpeed() * 3.6));
		
		if (pref.getMaxSpeed() > 0) {
			if (speed * 1000 > pref.getMaxSpeed()) {
				if (mPlayer != null) {
					if (!mPlayer.isPlaying()) {
						playBeep();
					}
				}
			}
			else {
				stopBeep();
			}
		}
		else {
			stopBeep();
		}
	}
	
	private void handleLogging() {
		Pref pref = Pref.getInstance();
		int speed = (int) Math.round(mLocation.getSpeed() * 3.6);
		long dateTimeMillis = System.currentTimeMillis();
		int maxSpeed;
		
		if (pref.getMaxSpeed() >= 0) {
			maxSpeed = (int) (Pref.getInstance()
														.getMaxSpeed() / 1000);
		}
		else {
			maxSpeed = 0;
		}
		if (speed != 0) {
			if (speedList.size() < 60) {
				speedList.add(new Speed(dateTimeMillis, speed, maxSpeed));
			}
			else {
				speedList.removeFirst();
				speedList.add(new Speed(dateTimeMillis, speed, maxSpeed));
			}
		}
	}
	
	public LinkedList<Speed> getSpeedList() {
		return speedList;
	}
	
	private void playBeep() {
		if (mPlayer != null) {
			if (Pref.getInstance()
							.getSoundOn()) {
				if (!mPlayer.isPlaying()) {
					mPlayer.start();
				}
			}
		}
	}
	
	public void stopBeep() {
		Pref pref = Pref.getInstance();
		if (mPlayer != null) {
			if (!pref.getSoundOn() || mPlayer.isPlaying()) {
				mPlayer.stop();
				try {
					mPlayer.prepare();
				}
				catch (IOException e) {
					e.printStackTrace();
					
				}
			}
		}
	}
	
	/**
	 * Class used for the client Binder.  Since this service runs in the same process as its
	 * clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public LocationUpdatesService getService() {
			return LocationUpdatesService.this;
		}
	}
	
	/**
	 * Returns true if this is a foreground service.
	 *
	 * @param context The {@link Context}.
	 */
	public boolean serviceIsRunningInForeground(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (manager != null) {
			for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
					Integer.MAX_VALUE)) {
				if (getClass().getName()
											.equals(service.service.getClassName())) {
					if (service.foreground) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}
	
	private String getStatusText() {
		if (mLocation != null) {
			long speed = (Math.round(mLocation.getSpeed() * 3600));
			long maxSpeed = Pref.getInstance()
													.getMaxSpeed();
			if (maxSpeed > 0) {
				if (speed > maxSpeed) {
					return getString(R.string.slow_down);
				}
			}
		}
		return "";
	}
	
	private String getLocationTitle() {
		long temp = Pref.getInstance()
										.getMaxSpeed();
		String maxSpeed = temp > 0 ? String.valueOf(temp / 1000) : getString(R.string.dash);
		return getString(R.string.speed_limit) + ": " + maxSpeed + " " + getString(R.string.km_h);
	}
}
