package com.dslg.app.api.setup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiConnector {
	private Retrofit.Builder             builder;
	private Retrofit                     retrofit;
	private HttpLoggingInterceptor       logging;
	private okhttp3.OkHttpClient.Builder httpClient;
	
	public static ApiConnector init() {
		return new ApiConnector();
	}
	
	public static ApiConnector init(long connectionTimeout, long writeTimeout, long readTimeout) {
		return new ApiConnector(connectionTimeout, writeTimeout, readTimeout);
	}
	
	private ApiConnector() {
		//		String baseUrl = "http://dslg.futuristech.ph/";
		String baseUrl = "http://dslg2.futuristech.ph/api/area/";
		Gson gson = (new GsonBuilder()).setPrettyPrinting()
																	 .create();
		this.builder = (new Retrofit.Builder()).baseUrl(baseUrl)
																					 .addConverterFactory(GsonConverterFactory.create(gson));
		this.retrofit = this.builder.build();
		this.logging = (new HttpLoggingInterceptor()).setLevel(HttpLoggingInterceptor.Level.BODY);
		this.httpClient = (new okhttp3.OkHttpClient.Builder()).connectTimeout(5L, TimeUnit.MINUTES)
																													.writeTimeout(5L, TimeUnit.MINUTES)
																													.readTimeout(5L, TimeUnit.MINUTES);
	}
	
	private ApiConnector(long connectionTimeout, long writeTimeout, long readTimeout) {
		//		String baseUrl = "http://dslg.futuristech.ph/";
		String baseUrl = "http://dslg2.futuristech.ph/api/area/";
		Gson gson = (new GsonBuilder()).setPrettyPrinting()
																	 .create();
		this.builder = (new Retrofit.Builder()).baseUrl(baseUrl)
																					 .addConverterFactory(GsonConverterFactory.create(gson));
		this.retrofit = this.builder.build();
		this.logging = (new HttpLoggingInterceptor()).setLevel(HttpLoggingInterceptor.Level.BODY);
		this.httpClient =
				(new okhttp3.OkHttpClient.Builder()).connectTimeout(connectionTimeout, TimeUnit.MINUTES)
																						.writeTimeout(writeTimeout, TimeUnit.MINUTES)
																						.readTimeout(readTimeout, TimeUnit.MINUTES);
	}
	
	public <S> S createService(Class<S> serviceClass) {
		if (!this.httpClient.interceptors()
												.contains(this.logging)) {
			this.httpClient.addInterceptor(this.logging);
			this.builder.client(this.httpClient.build());
			this.retrofit = this.builder.build();
		}
		
		return this.retrofit.create(serviceClass);
	}
}
