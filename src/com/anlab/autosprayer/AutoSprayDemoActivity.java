package com.anlab.autosprayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AutoSprayDemoActivity extends Activity {
	private static final String TAG ="SprayerDemo";
	private static final int MIN_INTERVAL = 2; // 单位是分钟
	private static final int MAX_INTERVAL = 20;
	private static final int MIN_TIMES = 1;
	private static final int MAX_TIMES = 15;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo);

		Button b = (Button) findViewById(R.id.btn_start);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText interval = (EditText) findViewById(R.id.interval);
				EditText times = (EditText) findViewById(R.id.times);

				if (checkParametersAvailable(interval, times)) {
					//发送指令
					android.util.Log.d(TAG,"start send commond");
				}else{
					android.util.Log.d(TAG,"parameter not available");
				}

			}
		});
	}

	@SuppressLint("ShowToast")
	private Boolean checkParametersAvailable(EditText interval, EditText times) {
		if (TextUtils.isEmpty(interval.getText())) {
			Toast.makeText(this, "请设置喷水间隔", Toast.LENGTH_LONG);
			return false;
		} else {
			int input_interval = Integer.valueOf(interval.getText().toString());
			if (input_interval < MIN_INTERVAL || input_interval > MAX_INTERVAL) {
				Toast.makeText(this, "喷水间隔设置不正确，需大于" + MIN_INTERVAL + "分钟小于"
						+ MAX_INTERVAL + "分钟。", Toast.LENGTH_LONG);
				return false;
			}
		}

		if (TextUtils.isEmpty(times.getText())) {
			Toast.makeText(this, "请设置喷水次数", Toast.LENGTH_LONG);
			return false;
		} else {
			int input_times = Integer.valueOf(times.getText().toString());
			if (input_times < MIN_TIMES || input_times > MAX_TIMES) {
				Toast.makeText(this, "喷水次数设置不正确，需大于" + MIN_TIMES + "小于"
						+ MAX_TIMES + "。", Toast.LENGTH_LONG);
				return false;
			}
		}
		return true;
	}
}