package com.dslg.app.api.setup;

import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiHandler
{
  @GET
  Call<ResponseBody> get(@Url String url, @HeaderMap Map<String, String> headers);
  
  @POST
  Call<ResponseBody> post(@Url String url, @HeaderMap Map<String, String> headers,
      @Body JsonObject requestBody);
  
  @DELETE
  Call<ResponseBody> delete(@Url String url, @HeaderMap Map<String, String> headers);
}
