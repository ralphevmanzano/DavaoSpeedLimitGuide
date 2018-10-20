package com.dslg.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.dslg.app.R;

public class CustomDialog extends Dialog {
	
	private Button positive;
	
	private OnPositiveButtonClickedListener positiveListener;
	//  public static CustomDialog build(Context context)
	//  {
	//
	//  }
	
	public CustomDialog(@NonNull Context context) {
		super(context);
//		Context context1 = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_dialog);
		positive = findViewById(R.id.btn_positive);
		positive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (positiveListener != null) {
					positiveListener.onClick(CustomDialog.this);
				}
			}
		});
	}
	
	public void setOnPositiveClickListener(final OnPositiveButtonClickedListener positiveListener) {
		if (positiveListener != null) {
			this.positiveListener = positiveListener;
		}
		if (positiveListener != null && positive != null) {
			positive.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					positiveListener.onClick(CustomDialog.this);
				}
			});
		}
	}
	
	public interface OnPositiveButtonClickedListener {
		void onClick(Dialog dialog);
	}
	
}
