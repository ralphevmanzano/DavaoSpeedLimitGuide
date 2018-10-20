package com.dslg.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.dslg.app.api.setup.ApiManager;
import com.dslg.app.api.setup.CheckNetworkInterface;
import com.dslg.app.dialog.CustomDialog;
import com.dslg.app.storage.Pref;
import com.dslg.app.utils.Utils;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private boolean isBackPressed = true;
    private CheckNetworkInterface listener =
            new CheckNetworkInterface() {
                @Override
                public void onSuccess() {
                    if (checkLocationPermissions()) {
                        checkLocationProvider();
                    } else {
                        requestLocationPermissions();
                    }
                }

                @Override
                public void onFailure() {

                    CustomDialog customDialog = new CustomDialog(SplashScreen.this);
                    customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    customDialog.setCancelable(true);
                    customDialog.setOnPositiveClickListener(
                            new CustomDialog.OnPositiveButtonClickedListener() {
                                @Override
                                public void onClick(final Dialog dialog) {
                                    Utils.isNetworkAvailable(SplashScreen.this, new CheckNetworkInterface() {
                                        @Override
                                        public void onSuccess() {
                                            isBackPressed = false;
                                            dialog.dismiss();
                                            if (checkLocationPermissions()) {
                                                checkLocationProvider();
                                            } else {
                                                requestLocationPermissions();
                                            }
                                        }

                                        @Override
                                        public void onFailure() {
                                        }
                                    });
                                }
                            });
                    customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (isBackPressed) {
                                finish();
                            }
                        }
                    });
                    customDialog.show();
			
			/*AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
			builder.setCancelable(false);
			builder.setIcon(R.drawable.ic_error);
			builder.setTitle(getString(R.string.oops));
			builder.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(final DialogInterface dialog, int which) {
					Utils.isNetworkAvailable(SplashScreen.this, new CheckNetworkInterface() {
						@Override
						public void onSuccess() {
							dialog.dismiss();
							if (checkLocationPermissions()) {
								checkLocationProvider();
							}
							else {
								requestLocationPermissions();
							}
						}
						
						@Override
						public void onFailure() {
						}
					});
				}
			});
			AlertDialog alert = builder.create();
			alert.show();*/
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiManager.init();

        Log.d(TAG, "onCreate: Splash Screen");

        @SuppressLint("HardwareIds") String udid =
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Pref.getInstance()
                .setUdid(udid);
        Log.d(TAG, "onCreate: " + udid);
		
		/*timer = new Thread() {
			public void run() {
				try {
					sleep(1000);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
					startActivity(intent);
					finish();
				}
			}
		};*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.isNetworkAvailable(this, listener);
    }

    private boolean checkLocationPermissions() {
        int permissionState =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this).setTitle("")
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,
                                                        int i) {
                                        //Prompt the user once explanation has been shown
                                        ActivityCompat.requestPermissions(SplashScreen.this,
                                                new String[]{
                                                        Manifest.permission.ACCESS_FINE_LOCATION},
                                                LOCATION_PERMISSION_REQUEST_CODE);
                                    }
                                })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Do nothing, onResume() will be called again
                    Log.d(TAG, "onRequestPermissionsResult: granted");
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //Close app no, permission
                        finish();
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", SplashScreen.this.getPackageName(), null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                break;
        }
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
            dialog.setCancelable(false);
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
        } else {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            finish();
        }
    }


}
