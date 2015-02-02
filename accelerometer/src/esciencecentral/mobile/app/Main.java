package esciencecentral.mobile.app;

/** Author: Chrysanthos Lianos chrysanthoslianos@gmail.com **/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class Main extends Activity implements OnClickListener,
		SensorEventListener {

	// variables
	Button ba, bc, bd,be = null; // ba dialog, bc stop, bd website, be exit
	TextView tx,ty,tz = null;
	Button bnorm,bxyz,para = null;
	Button stopnorm,stopxyz,stopall,disDialog = null;
	// dialogs
	Dialog dialog,stopDialog,introD,aboutD = null;
	// preferences
	SharedPreferences preferences;
	Boolean showNextTime = false;
	CheckBox cb = null;
	// sensor manager and sensor
	private SensorManager sm;
	private Sensor accelerometer;
	// custom SensorEventListeners
	List<FileRecorder> recorders;
	private FileRecorder fnormalized,fxyz;
	// tags for notifier
	public static final String WRITEALL = "writeall";
	public static final String WRITE = "write";
	public static final String STOPWRITE = "stop";
	public static final String STOPWRITEALL = "stopall";
	// tags for different types of recording
	public static final String XYZ = "xyz";
	public static final String NORM = "norm";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		recorders = new ArrayList<FileRecorder>();

		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

		ba = (Button) findViewById(R.id.Button01);
		ba.setText("Start logging");
		ba.setOnClickListener((OnClickListener) this);

		bc = (Button) findViewById(R.id.Button03);
		bc.setText("Stop logging");
		bc.setOnClickListener((OnClickListener) this);

		bd = (Button) findViewById(R.id.Button04);
		bd.setText("e-Science website");
		bd.setOnClickListener((OnClickListener) this);

		be = (Button) findViewById(R.id.Button05);
		be.setText("Exit program");
		be.setOnClickListener((OnClickListener) this);

		tx = (TextView) findViewById(R.id.textView1);
		ty = (TextView) findViewById(R.id.textView2);
		tz = (TextView) findViewById(R.id.textView3);

		// preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean showNextTime = preferences.getBoolean("showIntro", true);
		if (showNextTime) {
			// intro dialog
			introD = new Dialog(this);
			introD.setContentView(R.layout.introscreen);
			introD.show();
			Button disDiag = (Button) introD.findViewById(R.id.intbut);
			CheckBox cb = (CheckBox) introD.findViewById(R.id.checkBox1);
			disDiag.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					introD.dismiss();
				}
			}
			);
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub
					SharedPreferences.Editor editor = preferences.edit();
					editor.putBoolean("showIntro", false);
					editor.commit();
				}
			});
		}
		// start dialog
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.recordingdialog);
		Button bnorm = (Button) dialog.findViewById(R.id.ButtonNorm);
		Button bxyz = (Button) dialog.findViewById(R.id.ButtonXYZ);
		Button para = (Button) dialog.findViewById(R.id.ButtonParallel);
		para.setOnClickListener(new parallelListener(dialog));
		bnorm.setOnClickListener(new NormListener(dialog));
		bxyz.setOnClickListener(new XYZListener(dialog));

		// stop dialog
		stopDialog = new Dialog(this);
		stopDialog.setContentView(R.layout.stopdialog);
		Button stopNorm = (Button) stopDialog.findViewById(R.id.ButtonNormS);
		Button stopxyz = (Button) stopDialog.findViewById(R.id.ButtonXYZS);
		Button stopall = (Button) stopDialog.findViewById(R.id.ButtonStopAll);
		stopNorm.setOnClickListener(new snl(stopDialog));
		stopxyz.setOnClickListener(new sxyz(stopDialog));
		stopall.setOnClickListener(new sall(stopDialog));
		
		//about dialog
		aboutD = new Dialog(this);
		aboutD.setContentView(R.layout.aboutdialog);
		//Button closeD = (Button) findViewById(R.id.closeaboutdialog);
	}

	// stop normalized logging
	protected class snl implements OnClickListener {
		private Dialog dialog;

		public snl(Dialog dialog) {
			this.dialog = dialog;
		}

		public void onClick(View arg0) {
			if (fnormalized != null) {
				notifier(STOPWRITE);
				sm.unregisterListener(fnormalized);
				fnormalized.stopListener();
			}
			dialog.dismiss();
		}
	}

	// stop xyz logging
	protected class sxyz implements OnClickListener {
		private Dialog dialog;

		public sxyz(Dialog dialog) {
			this.dialog = dialog;
		}

		public void onClick(View arg0) {
			if (fxyz != null) {
				notifier(STOPWRITE);
				sm.unregisterListener(fxyz);
				fxyz.stopListener();
			}
			dialog.dismiss();
		}
	}

	// stop all
	protected class sall implements OnClickListener {
		private Dialog dialog;

		public sall(Dialog dialog) {
			this.dialog = dialog;
		}

		public void onClick(View arg0) {
			notifier(STOPWRITEALL);
			for (FileRecorder f : recorders) {
				sm.unregisterListener(f);
				f.stopListener();
			}
			recorders.clear();
			dialog.dismiss();
		}
	}

	// DIALOG LISTENER THAT STARS BOTH TYPES OF LOGGING
	protected class parallelListener implements OnClickListener {

		private Dialog dialog;

		public parallelListener(Dialog dialog) {
			this.dialog = dialog;
		}

		public void onClick(View v) {
			notifier(WRITEALL);
			try {
				fnormalized = new FileRecorder(accelerometer, NORM);
				fxyz = new FileRecorder(accelerometer, XYZ);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sm.registerListener(fnormalized, accelerometer,
					SensorManager.SENSOR_DELAY_UI);
			sm.registerListener(fxyz, accelerometer,
					SensorManager.SENSOR_DELAY_UI);
			recorders.add(fnormalized);
			recorders.add(fxyz);
			dialog.dismiss();
		}
	}

	// DIALOG LISTENER FOR NORMALIZED LOGGING
	protected class NormListener implements OnClickListener {
		private Dialog dialog;

		public NormListener(Dialog dialog) {
			this.dialog = dialog;
		}

		public void onClick(View v) {
			notifier(WRITE);
			try {
				fnormalized = new FileRecorder(accelerometer, NORM);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sm.registerListener(fnormalized, accelerometer,
					SensorManager.SENSOR_DELAY_UI);
			recorders.add(fnormalized);
			dialog.dismiss();
		}
	}

	// DIALOG LISTENER FOR 3-AXIS LOGGING
	protected class XYZListener implements OnClickListener {
		private Dialog dialog;

		public XYZListener(Dialog dialog) {
			this.dialog = dialog;
		}

		public void onClick(View v) {
			notifier(WRITE);
			try {
				fxyz = new FileRecorder(accelerometer, XYZ);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sm.registerListener(fxyz, accelerometer,
					SensorManager.SENSOR_DELAY_UI);
			recorders.add(fxyz);
			dialog.dismiss();
		}
	}

	// MAIN MENU BUTTONS
	public void onClick(View v) {
		if (v == ba) { // display dialog
			dialog.show();
		} else if (v == bc) { // stop logging
			stopDialog.show();
		} else if (v == bd) { // e-science website
			Intent i = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://www.esciencecentral.co.uk/"));
			startActivity(i);
		} else if (v == be) { // exit
			System.exit(0);
		}
	}

	// OVERRIDEN METHODS
	@Override
	public void onRestart() {
		super.onRestart();
		sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		super.onPause();
		for (FileRecorder f : recorders) {
			sm.registerListener(f, accelerometer, SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		for (FileRecorder f : recorders) {
			sm.registerListener(f, accelerometer, SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		for (FileRecorder f : recorders) {
			sm.registerListener(f, accelerometer, SensorManager.SENSOR_DELAY_UI);
		}
	}

	// SensorEventListener
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		tx.setText("X-axis: " + event.values[0] + " m/s^2");
		ty.setText("Y-axis: " + event.values[1] + " m/s^2");
		tz.setText("Z-axis: " + event.values[2] + " m/s^2");
	}

	// DISPLAYS NOTIFICATION ALERTS
	public void notifier(String tag) {
		if (tag == WRITE) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification(
					R.drawable.ic_launcher, "Beginning data loggging",
					System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent intent = new Intent(this, Main.class);
			PendingIntent activity = PendingIntent.getActivity(this, 0, intent,
					0);
			notification.setLatestEventInfo(this, "eScience mobile",
					"Logging accelerometer data", activity);
			notificationManager.notify(0, notification);
		} else if (tag == STOPWRITE) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification(
					R.drawable.ic_launcher, "Stopping data logging",
					System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent intent = new Intent(this, Main.class);
			PendingIntent activity = PendingIntent.getActivity(this, 0, intent,
					0);
			notification.setLatestEventInfo(this, "eScience mobile",
					"Data logging finished", activity);
			notificationManager.notify(0, notification);
			notificationManager.cancel(0);
		} else if (tag == STOPWRITEALL) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification(
					R.drawable.ic_launcher, "Stopping all loggers",
					System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent intent = new Intent(this, Main.class);
			PendingIntent activity = PendingIntent.getActivity(this, 0, intent,
					0);
			notification.setLatestEventInfo(this, "eScience mobile",
					"Data logging finished", activity);
			notificationManager.notify(0, notification);
			notificationManager.cancel(0);
		} else if (tag == WRITEALL) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification(
					R.drawable.ic_launcher, "Beginning dual logging",
					System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent intent = new Intent(this, Main.class);
			PendingIntent activity = PendingIntent.getActivity(this, 0, intent,
					0);
			notification.setLatestEventInfo(this, "eScience mobile",
					"Logging accelerometer data", activity);
			notificationManager.notify(0, notification);
			notificationManager.cancel(0);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.item1:
			aboutD.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}