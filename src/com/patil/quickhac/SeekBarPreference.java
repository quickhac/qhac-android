package com.patil.quickhac;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener, OnClickListener {
	// ------------------------------------------------------------------------------------------
	// Private attributes :
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar;
	private TextView mSplashText, mValueText;
	private Context mContext;

	private String mDialogMessage, mSuffix;
	private int mDefault, mMax, mValue = 0;

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// Constructor :
	public SeekBarPreference(Context context, AttributeSet attrs, int mDefault,
			int mMax, String prefix, String suffix) {

		super(context, attrs);
		mContext = context;

		mDialogMessage = prefix;

		mSuffix = suffix;

		// Get default and max seekbar values :
		this.mDefault = mDefault;
		this.mMax = mMax;
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// DialogPreference methods :
	@Override
	protected View onCreateDialogView() {

		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10, 10, 10, 10);

		mSplashText = new TextView(mContext);
		if (mDialogMessage != null)
			mSplashText.setText(mDialogMessage);
		mSplashText.setPadding(10, 10, 10, 10);
		mSplashText.setTextSize(18);
		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(22);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		if (shouldPersist())
			mValue = getPersistedInt(mDefault);

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);

		return layout;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		else
			mValue = (Integer) defaultValue;
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// OnSeekBarChangeListener methods :
	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		String t = String.valueOf(value);
		mValueText.setText(mSuffix == null ? t : t.concat(" " + mSuffix));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seek) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seek) {
	}

	public void setMax(int max) {
		mMax = max;
	}

	public int getMax() {
		return mMax;
	}

	public void setProgress(int progress) {
		mValue = progress;
		if (mSeekBar != null)
			mSeekBar.setProgress(progress);
	}

	public int getProgress() {
		return mValue;
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// Set the positive button listener and onClick action :
	@Override
	public void showDialog(Bundle state) {

		super.showDialog(state);

		Button positiveButton = ((AlertDialog) getDialog())
				.getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if (shouldPersist()) {
			persistInt(mSeekBar.getProgress());
		}
		callChangeListener(Integer.valueOf(mSeekBar.getProgress()));

		((AlertDialog) getDialog()).dismiss();
	}
	// ------------------------------------------------------------------------------------------
}