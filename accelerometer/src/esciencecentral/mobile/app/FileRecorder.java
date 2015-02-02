package esciencecentral.mobile.app;

/** Author: Chrysanthos Lianos chrysanthos.lianos@ncl.ac.uk **/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Environment;

public class FileRecorder implements SensorEventListener {

	private BufferedWriter out;
	private FileWriter fw;
	private String separator;
	private String enc = null;

	public FileRecorder(Sensor sensor, String encoding) throws IOException {
		super();
		enc = encoding;
		separator = System.getProperty("line.separator");
		File ext = Environment.getExternalStorageDirectory();
		File directory = new File(ext, "eScience");
		directory.mkdir();
		if (!ext.canWrite()) {
			throw new IOException("cannot write to sd");
		}
		if (enc == "xyz") {
			File f = new File(directory, "3axisvalues.csv");
			fw = new FileWriter(f);
			out = new BufferedWriter(fw, 1024);
		} else if (enc == "norm") {
			File f = new File(directory, "normalizedvalues.csv");
			fw = new FileWriter(f);
			out = new BufferedWriter(fw, 1024);
		}
	}

	public void stopListener() {
		try {
			out.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (enc == "xyz") {
			try {
				System.out.print(XYZformat(event.values) + separator);
				out.write(XYZformat(event.values) + separator);
				out.flush();
			} catch (IOException e) {
			}
		} else if (enc == "norm") {
			try {
				System.out.print(normalize(event.values) + separator);
				out.write(normalize(event.values) + separator);
				out.flush();
			} catch (IOException e) {
			}
		}
	}

	public String XYZformat(float[] acceleration) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < acceleration.length; i++) {
			Float acc = acceleration[i];
			sb.append(acc.toString());
			if (i < acceleration.length - 1) {
				sb.append(";");
			}
		}
		return sb.toString();
	}

	public String normalize(float[] acceleration) {
		StringBuilder sb = new StringBuilder();
		double x = acceleration[0];
		double y = acceleration[1];
		double z = acceleration[2];
		double norm = Math.sqrt((x * x) + (y * y) + (z * z));
		sb.append(norm);
		return sb.toString();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
