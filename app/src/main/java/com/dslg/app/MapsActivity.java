package com.dslg.app;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dslg.app.animations.SlideX;
import com.dslg.app.animations.SlideY;
import com.dslg.app.constants.Values;
import com.dslg.app.models.Speed;
import com.dslg.app.service.LocationUpdatesService;
import com.dslg.app.storage.Pref;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity
		implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
		GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {
	private static final String TAG = "MapsActivity";
	
	private SupportMapFragment mapFragment;
	private GoogleMap          mMap;
	private Location           mCurrentLocation;
	private Marker             mMarker;
	
	private static final int  TIME_INTERVAL = 2000;
	private              long mBackPressed;
	
	private Handler btnHandler = new Handler();
	
	@BindView(R.id.container)
	ConstraintLayout     activityContainer;
	@BindView(R.id.current_speed_widget)
	ConstraintLayout     currentSpeedWidget;
	@BindView(R.id.speed_limit_widget)
	ConstraintLayout     speedLimitWidget;
	@BindView(R.id.txt_long)
	TextView             txtLong;
	@BindView(R.id.txt_lat)
	TextView             txtLat;
	@BindView(R.id.txt_label)
	TextView             txtLabel;
	@BindView(R.id.btn_recent_speed)
	FloatingActionButton btnRecentSpeed;
	@BindView(R.id.btn_sound_on)
	FloatingActionButton btnSoundOn;
	@BindView(R.id.btn_my_location)
	FloatingActionButton btnMyLocation;
	@BindView(R.id.txt_network_status)
	TextView             txtNetworkStatus;
	@BindView(R.id.preloader)
	ImageView            imgPreloader;
	@BindView(R.id.fade_bg)
	View                 fadeBg;
	@BindView(R.id.cont_pre_loader)
	ConstraintLayout     preloaderLayout;
	
	private TextView txtCurrentSpeed;
	private TextView txtSpeedLimit;
	
	private boolean isExitAppReceiverRegistered = false;
	private boolean isMovedToInitLoc            = false;
	private boolean btnRunnableRunning;
	
	private boolean                mBound = false;
	private LocationUpdatesService mService;
	private LocationReceiver       locationReceiver;
	private ExitAppReceiver        exitAppReceiver;
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mBound = false;
		}
	};
	
	private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =
			new SharedPreferences.OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					switch (key) {
						case Pref.MAX_SPEED:
							updateSpeedLimitUI();
							break;
						
						case Pref.HAS_NETWORK:
							showNetworkStatus(Pref.getInstance()
																		.getHasNetwork());
							break;
						
					}
				}
			};
	
	private SharedPreferences mSharedPreferences;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		showDisclaimer();
		init();
	}
	
	private void init() {
		ButterKnife.bind(this);
		mSharedPreferences = Pref.getInstance()
														 .getPref();
		locationReceiver = new LocationReceiver();
		exitAppReceiver = new ExitAppReceiver();
		
		if (checkLocationPermissions()) {
			initMap();
		}
		
		setupViews();
	}
	
	@Override
	public void onBackPressed() {
		if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
			super.onBackPressed();
			return;
		}
		else {
			Toast.makeText(getBaseContext(), "Tap back button again to exit", Toast.LENGTH_SHORT)
					 .show();
		}
		
		mBackPressed = System.currentTimeMillis();
	}
	
	private void initMap() {
		mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}
	
	
	@SuppressLint("MissingPermission")
	@Override
	public void onMapReady(GoogleMap googleMap) {
		//		Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
		mMap = googleMap;
		mMap.setOnMapClickListener(this);
		
		if (checkLocationPermissions()) {
			mMap.getUiSettings()
					.setMyLocationButtonEnabled(true);
		}
		moveCamera(Pref.getInstance()
									 .getLastLatLng());
	}
	
	private void onLocationUpdates() {
		LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
		float angle = (float) getAngleFromBearing();
		
		if (mMarker == null) {
			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.position(latLng);
			markerOptions.title("Your location");
			markerOptions.flat(true);
			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car));
			if (mCurrentLocation.getSpeed() != 0) {
				markerOptions.rotation(angle);
			}
			mMarker = mMap.addMarker(markerOptions);
		}
		else {
			if (mCurrentLocation.getSpeed() != 0) {
				mMarker.setRotation(angle);
			}
			mMarker.setPosition(latLng);
		}
		
		moveCamera(latLng);
	}
	
	private double getAngleFromBearing() {
		double bearing = mCurrentLocation.getBearing();
		return (bearing + 360) % 360;
	}
	
	@OnClick(R.id.btn_sound_on)
	void btnSoundOn() {
		Pref pref = Pref.getInstance();
		pref.setSoundOn(!pref.getSoundOn());
		handleSoundButton();
		if (mBound) {
			mService.stopBeep();
		}
	}
	
	@OnClick(R.id.btn_recent_speed)
	void btnRecentSpeed() {
		Intent myIntent = new Intent(MapsActivity.this, RecentSpeedActivity.class);
		Gson gson = new Gson();
		if (mBound) {
			LinkedList<Speed> speedList = mService.getSpeedList();
			if (speedList != null) {
				String data = gson.toJson(speedList);
				myIntent.putExtra(Values.RECENT_SPEED_LIST, data);
				startActivity(myIntent);
			}
		}
	}
	
	@OnClick(R.id.btn_my_location)
	void btnMyLocation() {
		if (mCurrentLocation != null) {
			LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
			moveCamera(latLng);
		}
	}
	
	private void moveCamera(LatLng latLng) {
    /*if (latLng != null)
    {
      Log.d(TAG, "moveCamera: moving camera to " + latLng.latitude + " " + latLng.longitude);
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }*/
		
		
		if (mMap != null && latLng != null) {
			Log.d(TAG, "moveCamera: lat " + latLng.latitude + "\tlong " + latLng.longitude);
			CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng)
																																	.zoom(18)
																																	.build();
			if (!isMovedToInitLoc) {
				boolean hasLoc = latLng.latitude != Pref.DAVAO_LAT;
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, hasLoc ? 18 : 11));
				showPreloader(!hasLoc);
				isMovedToInitLoc = true;
			}
			else {
				showPreloader(false);
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
		}
		else {
			Log.e(TAG, "cameraToMarker: Null object mMap", null);
		}
	}
	
	private void setupViews() {
		currentSpeedWidget.setBackground(ContextCompat.getDrawable(this, R.drawable.speed_widget_bg));
		speedLimitWidget.setBackground(
				ContextCompat.getDrawable(this, R.drawable.speed_limit_widget_bg));
		TextView txtCurrentSpeedLabel = currentSpeedWidget.findViewById(R.id.txtSpeedLbl);
		txtCurrentSpeed = currentSpeedWidget.findViewById(R.id.txtSpeedVal);
		TextView txtSpeedLimitLabel = speedLimitWidget.findViewById(R.id.txtSpeedLbl);
		txtSpeedLimit = speedLimitWidget.findViewById(R.id.txtSpeedVal);
		
		txtCurrentSpeedLabel.setText(R.string.speed);
		txtSpeedLimitLabel.setText(R.string.limit);
		imgPreloader.bringToFront();
		Glide.with(this)
				 .load(R.drawable.pre_loader)
				 .into(imgPreloader);
		
		handleSoundButton();
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private Transition createButtonsTransition() {
		TransitionSet set = new TransitionSet();
		set.setOrdering(TransitionSet.ORDERING_TOGETHER);
		
		Transition tRecentSpeeds = new SlideX();
		tRecentSpeeds.setDuration(150);
		tRecentSpeeds.addTarget(btnRecentSpeed);
		
		Transition tSoundOn = new SlideX();
		tSoundOn.setDuration(150);
		tSoundOn.addTarget(btnSoundOn);
		
		set.addTransition(tRecentSpeeds);
		set.addTransition(tSoundOn);
		
		return set;
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private Transition createNetworkStatusTransition() {
		TransitionSet set = new TransitionSet();
		set.setOrdering(TransitionSet.ORDERING_TOGETHER);
		
		Transition tNetworkStatus = new SlideY();
		tNetworkStatus.setDuration(150);
		tNetworkStatus.addTarget(txtNetworkStatus);
		
		set.addTransition(tNetworkStatus);
		
		return set;
	}
	
	private void handleSoundButton() {
		Pref pref = Pref.getInstance();
		if (pref.getSoundOn()) {
			btnSoundOn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_volume_up));
		}
		else {
			btnSoundOn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_volume_off));
		}
	}
	
	private void updateUI() {
		if (mCurrentLocation != null) {
			
			txtLat.setText(
					String.format(Locale.ENGLISH, "%s: %f", "Lat: ", mCurrentLocation.getLatitude()));
			txtLong.setText(
					String.format(Locale.ENGLISH, "%s: %f", "Long: ", mCurrentLocation.getLongitude()));
			
			long speed = (Math.round(mCurrentLocation.getSpeed() * 3.6));
			txtCurrentSpeed.setText(String.valueOf(speed));
		}
	}
	
	
	private void showNetworkStatus(boolean hasNetwork) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Transition transition = createNetworkStatusTransition();
			TransitionManager.beginDelayedTransition(activityContainer, transition);
		}
		if (!hasNetwork) {
			txtNetworkStatus.setVisibility(View.VISIBLE);
		}
		else {
			txtNetworkStatus.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public void onResume() {
		if (mapFragment != null) {
			mapFragment.onResume();
		}
		super.onResume();
		LocalBroadcastManager.getInstance(this)
												 .registerReceiver(locationReceiver,
														 new IntentFilter(LocationUpdatesService.ACTION_BROADCAST_LOCATION));
		if (!isExitAppReceiverRegistered) {
			LocalBroadcastManager.getInstance(this)
													 .registerReceiver(exitAppReceiver,
															 new IntentFilter(LocationUpdatesService.ACTION_BROADCAST_EXIT));
			isExitAppReceiverRegistered = true;
		}
		mSharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
		
		checkLocationProvider();
		handleSoundButton();
		updateUI();
		updateSpeedLimitUI();
		showNetworkStatus(Pref.getInstance()
													.getHasNetwork());
	}
	
	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(this)
												 .unregisterReceiver(locationReceiver);
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
		super.onPause();
		if (mapFragment != null) {
			mapFragment.onPause();
		}
		//		CacheHandler ch = CacheHandler.getInstance(CacheManager.getInstance(this));
		//		ch.setSpeedList(speedList);
	}
	
	@Override
	protected void onStop() {
		unbindService();
		
		super.onStop();
		Log.i(TAG, "btnRecentSpeed: on stop starts");
		
	}
	
	public void unbindService() {
		if (mBound) {
			// Unbind from the service. This signals to the service that this activity is no longer
			// in the foreground, and the service can respond by promoting itself to a foreground
			// service.
			unbindService(mServiceConnection);
			mBound = false;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (mapFragment != null) {
			mapFragment.onDestroy();
		}
		
		LocalBroadcastManager.getInstance(this)
												 .unregisterReceiver(exitAppReceiver);
		mService.removeLocationUpdates();
	}
	
	/**
	 * Receiver for broadcasts sent by {@link LocationUpdatesService}.
	 */
	private class LocationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
			if (location != null) {
				Log.d(LocationUpdatesService.class.getSimpleName(),
						"onReceive: " + "(" + location.getLatitude() + ", " + location.getLongitude() + ")");
				mCurrentLocation = location;
				
				updateUI();
				onLocationUpdates();
			}
		}
	}
	
	/**
	 * Receiver for broadcasts sent by {@link LocationUpdatesService}.
	 */
	private class ExitAppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if (intent.getAction() != null) {
					if (intent.getAction()
										.equals(LocationUpdatesService.ACTION_BROADCAST_EXIT)) {
						finish();
					}
				}
			}
		}
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mapFragment != null) {
			mapFragment.onLowMemory();
		}
	}
	
	private void updateSpeedLimitUI() {
		Pref pref = Pref.getInstance();
		
		long maxSpeed = pref.getMaxSpeed() / 1000;
		String label = pref.getLocationLabel();
		if (maxSpeed > 0) {
			txtSpeedLimit.setText(String.valueOf(maxSpeed));
		}
		else {
			txtSpeedLimit.setText(getResources().getString(R.string.dash));
		}
		
		if (StringUtils.isNotBlank(pref.getLocationLabel())) {
			txtLabel.setText(label);
		}
	}
	
	private boolean checkLocationPermissions() {
		int permissionState =
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
		return permissionState == PackageManager.PERMISSION_GRANTED;
	}
	
	private void checkLocationProvider() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enabled = false;
		boolean network_enabled = false;
		
		if (lm != null) {
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		
		if (!gps_enabled && !network_enabled) {
			// notify user
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(getResources().getString(R.string.location_not_found));
			dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
			
			dialog.setPositiveButton(getResources().getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface paramDialogInterface, int paramInt) {
							Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(myIntent);
							//get gps
						}
					});
			dialog.show();
		}
		else {
			bindService(new Intent(MapsActivity.this, LocationUpdatesService.class), mServiceConnection,
					Context.BIND_AUTO_CREATE);
		}
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) {
	
	}
	
	@Override
	public void onMapClick(LatLng latLng) {
		handleOnMapClick();
	}
	
	private void handleOnMapClick() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Transition transition = createButtonsTransition();
			TransitionManager.beginDelayedTransition(activityContainer, transition);
		}
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "runnable: called");
				delayedHide();
			}
		};
		
		if (btnSoundOn.getVisibility() == View.INVISIBLE &&
				btnRecentSpeed.getVisibility() == View.INVISIBLE) {
			btnSoundOn.setVisibility(View.VISIBLE);
			btnRecentSpeed.setVisibility(View.VISIBLE);
			
			if (!btnRunnableRunning) {
				btnHandler.postDelayed(runnable, 10000);
				btnRunnableRunning = true;
			}
		}
		else {
			btnHandler.removeCallbacksAndMessages(runnable);
			btnSoundOn.setVisibility(View.INVISIBLE);
			btnRecentSpeed.setVisibility(View.INVISIBLE);
			btnRunnableRunning = false;
		}
	}
	
	private void delayedHide() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Transition transition = createButtonsTransition();
			TransitionManager.beginDelayedTransition(activityContainer, transition);
		}
		btnSoundOn.setVisibility(View.INVISIBLE);
		btnRecentSpeed.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		return false;
	}
	
	private void showDisclaimer() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setMessage(getResources().getString(R.string.disclaimer));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
				getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}
	
	private void showPreloader(boolean show) {
		if (show) {
			preloaderLayout.setVisibility(View.VISIBLE);
			fadeBg.setVisibility(View.VISIBLE);
			fadeBg.animate()
						.alpha(0.5f);
		}
		else {
			preloaderLayout.setVisibility(View.GONE);
			fadeBg.animate()
						.alpha(0.0f)
						.setListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animation) {
					
							}
				
							@Override
							public void onAnimationEnd(Animator animation) {
								fadeBg.setVisibility(View.GONE);
							}
				
							@Override
							public void onAnimationCancel(Animator animation) {
					
							}
				
							@Override
							public void onAnimationRepeat(Animator animation) {
					
							}
						});
			
		}
	}
}
