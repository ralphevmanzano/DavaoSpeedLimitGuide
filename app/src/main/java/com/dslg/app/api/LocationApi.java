package com.dslg.app.api;

import android.content.Context;


import com.dslg.app.api.setup.ApiInterface;
import com.dslg.app.api.setup.ApiManager;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class LocationApi extends ApiManager
{
  
  public static LocationApi call()
  {
    return new LocationApi();
  }
  
  private LocationApi() {
    super();
  }
  
  public void postLocation(JSONObject requestBody, ApiInterface listener) {
    this.post(this.URL, requestBody, listener);
  }
}
