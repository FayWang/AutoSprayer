package com.anlab.autosprayer;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

public class ArduinoHelper {

	private Activity mActivity;

	public static final byte LED_SERVO_COMMAND = 2;

	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileOutputStream mOutputStream;

	public ArduinoHelper(Activity activity) {
		mActivity = activity;
		mUsbManager = (UsbManager) activity
				.getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		activity.registerReceiver(mUsbReceiver, filter);

		if (activity.getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) activity
					.getLastNonConfigurationInstance();
			openAccessoryInternal(mAccessory);
		}

	}

	public void onResume() {

		if (mOutputStream != null) {
			return;
		}

		openAccessory();
	}

	public void onPause() {
		closeAccessory();
	}

	public void onDestroy() {
		mActivity.unregisterReceiver(mUsbReceiver);

	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent
							.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessoryInternal(accessory);
					} else {
						mLoger.info("permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = (UsbAccessory) intent
						.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
			
		}
	};

	private void openAccessory() {
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessoryInternal(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			mLoger.info("mAccessory is null");
		}
	}

	private void openAccessoryInternal(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mOutputStream = new FileOutputStream(fd);
			mLoger.info("accessory opened");

		} else {
			mLoger.info("accessory open fail");
		}
	}

	private void closeAccessory() {

		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
				mOutputStream.close();

			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
			mOutputStream = null;

		}
	}

	public void startMotorControlPin() {
	
		sendCommand(LED_SERVO_COMMAND, (byte) 0x0, 250);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendCommand(LED_SERVO_COMMAND, (byte) 0x0, 0);
		
	}

	public void stopMotorControlPin() {
		
		sendCommand(LED_SERVO_COMMAND, (byte) 0x1, 250);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendCommand(LED_SERVO_COMMAND, (byte) 0x1, 0);
		
	}

	public void sendCommand(byte command, byte target, int value) {
		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = target;
		buffer[2] = (byte) value;
		if (mOutputStream != null && buffer[1] != -1) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				mLoger.info("write failed" + e.getMessage());
			}
		}
	}

	private static Logger mLoger = Logger.getLogger(ArduinoHelper.class
			.getName());
}
