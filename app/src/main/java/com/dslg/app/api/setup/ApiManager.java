package com.dslg.app.api.setup;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiManager {
	private static ApiConnector mConn;
	private         String language = "";
	//  protected boolean debug = SoloApi.getInstance().isDebug();
	//  protected final String URL = SoloApi.getInstance().getBaseUrl();
	//  protected final String URL = "http://dslg.futuristech.ph/";
	protected final String URL      = "http://dslg2.futuristech.ph/api/area";
	private Call<ResponseBody> mCall;
	private boolean deleting = false;
	
	public static ApiManager init() {
		return new ApiManager();
	}
	
	public ApiManager() {
		mConn = ApiConnector.init();
	}
	
	public void post(String url, JSONObject requestBody, ApiInterface listener) {
		if (!this.cannotCall()) {
			try {
				if (StringUtils.isBlank(url)) {
					throw new Exception("url is empty");
				}
				
				if (requestBody.length() < 0) {
					throw new Exception("requestBody is empty");
				}
			}
			catch (Exception var7) {
				listener.onThrownException(var7);
				return;
			}
			
			JsonParser jsonParser = new JsonParser();
			JsonObject body = (JsonObject) jsonParser.parse(requestBody.toString());
			ApiHandler handler = mConn.createService(ApiHandler.class);
			Log.d("constructLocationData", url);
			Log.d("constructLocationData", body.toString());
			Map<String, String> header = new HashMap<>();
			header.put("Content-Type", "application/json");
			Log.d("constructLocationData", " header size " + header.size());
			this.mCall = handler.post(url, header, body);
			this.makeCall(listener);
		}
	}
	
	public void get(String url, ApiInterface listener) {
		if (!this.cannotCall()) {
			try {
				if (StringUtils.isBlank(url)) {
					throw new Exception("url is empty");
				}
			}
			catch (Exception var4) {
				listener.onThrownException(var4);
				return;
			}
			
			ApiHandler handler = mConn.createService(ApiHandler.class);
			Map<String, String> header = new HashMap<>();
			this.mCall = handler.get(url, header);
			this.makeCall(listener);
		}
	}
	
	public void get(String url, CheckNetworkInterface listener) {
		if (!this.cannotCall()) {
			try {
				if (StringUtils.isBlank(url)) {
					throw new Exception("url is empty");
				}
			}
			catch (Exception var4) {
				var4.printStackTrace();
				return;
			}
			
			ApiHandler handler = mConn.createService(ApiHandler.class);
			Map<String, String> header = new HashMap<>();
			this.mCall = handler.get(url, header);
			this.makeCall(listener);
		}
	}
	
	public void forceStop() {
		if (this.mCall != null && this.mCall.isExecuted()) {
			this.mCall.cancel();
		}
	}
	
	private void makeCall(final ApiInterface listener) {
		if (listener == null) {
			try {
				throw new Exception("ApiInterface is null");
			}
			catch (Exception var3) {
				var3.printStackTrace();
			}
		}
		else {
			try {
				if (this.mCall == null) {
					throw new Exception("ApiConnector has not been initialized use method init");
				}
				
				this.mCall.enqueue(new Callback<ResponseBody>() {
					public void onResponse(@NonNull Call<ResponseBody> call,
							@NonNull Response<ResponseBody> response) {
						try {
							if (response != null) {
								if (response.isSuccessful()) {
									JSONObject jsonObject = new JSONObject();
									if (ApiManager.this.deleting) {
										ApiManager.this.deleting = false;
									}
									else {
										String serverResponsex = response.body().string();
										jsonObject = new JSONObject(serverResponsex);
									}
									
									listener.onSuccess(jsonObject);
								}
								else {
									String serverResponse = response.errorBody()
																									.string();
									JSONObject errorObject = new JSONObject(serverResponse);
									listener.onError(errorObject);
								}
							}
						}
						catch (JSONException | NullPointerException | IOException var5) {
							listener.onThrownException(var5);
						}
						
					}
					
					public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
						listener.onFailure(call, t);
					}
				});
			}
			catch (Exception var4) {
				listener.onThrownException(var4);
			}
			
		}
	}
	private void makeCall(final CheckNetworkInterface listener) {
		if (listener == null) {
			try {
				throw new Exception("ApiInterface is null");
			}
			catch (Exception var3) {
				var3.printStackTrace();
			}
		}
		else {
			try {
				if (this.mCall == null) {
					throw new Exception("ApiConnector has not been initialized use method init");
				}
				
				this.mCall.enqueue(new Callback<ResponseBody>() {
					public void onResponse(@NonNull Call<ResponseBody> call,
							@NonNull Response<ResponseBody> response) {
								if (response.code() == 204) {
									if (response.body() == null){
										listener.onSuccess();
									}
									else {
										listener.onFailure();
									}
								}
								else {
									listener.onFailure();
								}
					}
					
					public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
						listener.onFailure();
					}
				});
			}
			catch (Exception var4) {
				var4.printStackTrace();
			}
		}
	}
	
	private boolean cannotCall() {
		if (mConn == null) {
			try {
				throw new Exception("ApiConnector has not been initialized use method init");
			}
			catch (Exception var2) {
				var2.printStackTrace();
				return true;
			}
		}
		else {
			return false;
		}
	}
}
