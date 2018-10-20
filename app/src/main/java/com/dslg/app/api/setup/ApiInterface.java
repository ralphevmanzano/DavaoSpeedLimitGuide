package com.dslg.app.api.setup;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;

public interface ApiInterface
{
  void onSuccess(JSONObject var1);
  
  void onError(JSONObject var1);
  
  void onThrownException(Exception var1);
  
  void onFailure(Call<ResponseBody> var1, Throwable var2);
}

