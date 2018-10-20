package com.dslg.app;

import android.support.multidex.MultiDexApplication;

import com.dslg.app.storage.Pref;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class BaseApplication extends MultiDexApplication
{
  @Override
  public void onCreate()
  {
    super.onCreate();
    Fabric.with(this, new Crashlytics());
    Pref.init(this);
  }
}
