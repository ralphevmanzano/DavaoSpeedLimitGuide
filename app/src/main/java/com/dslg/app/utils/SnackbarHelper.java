package com.dslg.app.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.ViewGroup;

import com.dslg.app.R;

public class SnackbarHelper {
	public static void configSnackbar(Context context, Snackbar snack) {
		addMargins(snack);
		setRoundBordersBg(context, snack);
		ViewCompat.setElevation(snack.getView(), 6f);
	}
	
	private static void addMargins(Snackbar snack) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snack.getView().getLayoutParams();
		params.setMargins(12, 12, 12, 12);
		snack.getView().setLayoutParams(params);
	}
	
	private static void setRoundBordersBg(Context context, Snackbar snackbar) {
		snackbar.getView().setBackground(ContextCompat.getDrawable(context, R.drawable.snackbar_bg));
	}
}
