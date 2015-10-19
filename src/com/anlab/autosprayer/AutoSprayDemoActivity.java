package com.anlab.autosprayer;

import java.util.HashMap;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AutoSprayDemoActivity extends Activity {
	private static final String TAG = "SprayerDemo";
	private static final int MIN_INTERVAL = 2; // 单位是分钟
	private static final int MAX_INTERVAL = 20;
	private static final int MIN_TIMES = 1;
	private static final int MAX_TIMES = 15;

	protected ArduinoHelper mArduinoHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo);
		mArduinoHelper = new ArduinoHelper(this);

		Button b = (Button) findViewById(R.id.btn_start);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText interval = (EditText) findViewById(R.id.interval);
				EditText times = (EditText) findViewById(R.id.times);

				if (checkParametersAvailable(interval, times)) {
					// 发送指令
					r.run();
					android.util.Log.d(TAG, "start send commond");
				} else {
					android.util.Log.d(TAG, "parameter not available");
				}

			}
		});

	}

	@SuppressLint("ShowToast")
	private Boolean checkParametersAvailable(EditText interval, EditText times) {
		if (TextUtils.isEmpty(interval.getText())) {
			Toast toast = Toast.makeText(this, "请设置喷水间隔", Toast.LENGTH_LONG);
			toast.show();
			return false;
		} else {
			int input_interval = Integer.valueOf(interval.getText().toString());
			if (input_interval < MIN_INTERVAL || input_interval > MAX_INTERVAL) {
				Toast toast = Toast.makeText(this, "喷水间隔设置不正确，需大于" + MIN_INTERVAL + "分钟小于"
						+ MAX_INTERVAL + "分钟。", Toast.LENGTH_LONG);
				toast.show();
				return false;
			}
		}

		if (TextUtils.isEmpty(times.getText())) {
			Toast toast = Toast.makeText(this, "请设置喷水次数", Toast.LENGTH_LONG);
			toast.show();
			return false;
		} else {
			int input_times = Integer.valueOf(times.getText().toString());
			if (input_times < MIN_TIMES || input_times > MAX_TIMES) {
				Toast toast = Toast.makeText(this, "喷水次数设置不正确，需大于" + MIN_TIMES + "小于"
						+ MAX_TIMES + "。", Toast.LENGTH_LONG);
				toast.show();
				return false;
			}
		}
		return true;
	}

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			UsbManager manager = (UsbManager) AutoSprayDemoActivity.this
					.getApplicationContext().getSystemService(
							Context.USB_SERVICE);
			UsbDeviceConnection connection = null;
			HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();// 获取所有设备
			while (deviceIterator.hasNext()) {
				UsbDevice device = deviceIterator.next();
				if (manager.hasPermission(device)) {// 如果已经拥有该设备的连接权限
					connection = manager.openDevice(device);// 打开一个UsbDeviceConnection
					UsbInterface intf = device.getInterface(0);
					UsbEndpoint epOut = intf.getEndpoint(1);// 结束点，一般index为1的为输入，0的为输入
					UsbEndpoint epIn = intf.getEndpoint(0);
					connection.claimInterface(intf, true); // 在发送和接收数据前 要进行申明
					connection.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0,
							0);// 这是设置波特率的代码。没有这个行代码，会出现一些问题。
					int result = connection.bulkTransfer(epOut, "R".getBytes(),
							"R".getBytes().length, 3000);// 发送数据，如果返回值大于0，表示发送成功
					Log.i(TAG,"result:" + result);
					// 接收数据的方法
					byte[] buffer = new byte[1024];
					int ret = connection.bulkTransfer(epIn, buffer,
							buffer.length, 3000);
					if (ret > 0) {
						StringBuilder sb = new StringBuilder();
						for (int j = 2; j < 1024; j++) {
							if (buffer[j] != 0) {
								sb.append((char) buffer[j]);
							} else {
								Log.i(TAG, sb.toString());
								break;
							}
						}
						// view.setText("ret:" + ret + "数据内容-->" + sb.toString());
						// 在一个view中显示出来
					}
				}
			}
		}
	};
}