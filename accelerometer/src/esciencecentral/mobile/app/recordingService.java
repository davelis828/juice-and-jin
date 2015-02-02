package esciencecentral.mobile.app;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class recordingService extends Service implements SensorEventListener {

	private static final String TAG = recordingService.class.getName();
	private final IBinder mBinder = new MyBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "created");

	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	public class MyBinder extends Binder {
		recordingService getService() {
			return recordingService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

	}

}
