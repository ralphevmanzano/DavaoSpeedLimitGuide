package com.dslg.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.dslg.app.api.setup.ApiInterface;
import com.dslg.app.api.setup.ApiManager;
import com.dslg.app.api.setup.CheckNetworkInterface;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class Utils {
	
	private static final String TAG = Utils.class.getSimpleName();
	
	/*
	try {
				HttpURLConnection urlc = (HttpURLConnection)
						(new URL("http://clients3.google.com/generate_204")
								.openConnection());
				urlc.setRequestProperty("User-Agent", "Android");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(1500);
				urlc.connect();
				return (urlc.getResponseCode() == 204 &&
						urlc.getContentLength() == 0);
			} catch (IOException e) {
				Log.e(TAG, "Error checking internet connection", e);
			}
	 */
	public static void isNetworkAvailable (Context context, CheckNetworkInterface listener) {
		if (isConnectedToNetwork(context)) {
			ApiManager manager = new ApiManager();
			
			manager.get("http://clients3.google.com/generate_204", listener);
		}
		else {
			listener.onFailure();
		}
	}
	
	private static boolean isConnectedToNetwork(Context context) {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = null;
		if (connectivityManager != null) {
			activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		}
		Log.d(TAG, "isConnectedToNetwork: " + String.valueOf(activeNetworkInfo != null));
		
		return activeNetworkInfo != null;
	}
}
