package com.dslg.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dslg.app.adapters.RecentSpeedsAdapter;
import com.dslg.app.constants.Values;
import com.dslg.app.databinding.ActivityRecentSpeedsBinding;
import com.dslg.app.models.Speed;
import com.dslg.app.service.LocationUpdatesService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecentSpeedActivity extends AppCompatActivity {
	private static final String TAG = "RecentSpeedActivity";
	
	@BindView(R.id.txt_no_data)
	TextView                          txtNotes;
	@BindView(R.id.my_toolbar)
	android.support.v7.widget.Toolbar toolbar;
	
	private LinkedList<Speed>           speedList;
	private RecyclerView                mRecyclerView;
	
	int i = 0;
	private boolean mBound = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityRecentSpeedsBinding binding =
				DataBindingUtil.setContentView(this, R.layout.activity_recent_speeds);
		mRecyclerView = binding.rvRecentSpeeds;
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		if (getSupportActionBar() != null) {
			Log.d(TAG, "setsuppoertactionbar");
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setTitle(R.string.recent_speed_record);
		
		updateUI();
	}
	
	private void updateUI() {
		Intent intent = getIntent();
		if (intent.hasExtra(Values.RECENT_SPEED_LIST)) {
			String jsonData = intent.getStringExtra(Values.RECENT_SPEED_LIST);
			Type type = new TypeToken<LinkedList<Speed>>() {
			}.getType();
			Gson gson = new Gson();
			speedList = gson.fromJson(jsonData, type);
		}
		
		if (speedList != null && speedList.size() > 0) {
			txtNotes.setVisibility(View.GONE);
			initRecyclerView();
			mRecyclerView.setVisibility(View.VISIBLE);
		}
		else {
			mRecyclerView.setVisibility(View.GONE);
			txtNotes.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onStop() {
		onBackPressed();
		unbindService();
		super.onStop();
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void initRecyclerView() {
		Gson gson = new Gson();
		String stringList = gson.toJson(speedList);
		
		Collections.reverse(speedList);
		
		ArrayList<String> tableLabels = new ArrayList<>();
		tableLabels.add(getResources().getString(R.string.time));
		tableLabels.add(getResources().getString(R.string.driver_s_speed));
		tableLabels.add(getResources().getString(R.string.limit));
		
		speedList.add(0, new Speed(tableLabels));
		speedList.add(0, new Speed(stringList));
		
		RecentSpeedsAdapter adapter = new RecentSpeedsAdapter(speedList, this);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.getRecycledViewPool()
								 .setMaxRecycledViews(Speed.CHART, 0);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setAdapter(adapter);
	}
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
		}
	};
}
