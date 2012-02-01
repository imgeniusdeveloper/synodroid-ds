package org.jared.synodroid.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;
import org.jared.synodroid.Synodroid;
import org.jared.synodroid.common.action.AddTaskAction;
import org.jared.synodroid.ds.DownloadActivity;
import org.jared.synodroid.ds.R;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class TorrentDownloadAndAdd extends AsyncTask<String, Void, Uri> {
	private Activity a;
	
	public TorrentDownloadAndAdd (Activity activityP){
		a = activityP;
	}
	
	@Override
	protected void onPreExecute() {
		Toast toast = Toast.makeText(a, a.getString(R.string.wait_for_download), Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	protected Uri doInBackground(String... params) {
		try {
			Uri uri = Uri.parse(params[0]);
			return fixUri(uri);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Uri uri) {
		boolean out_url = false;
		if (uri != null) {
			if (!uri.toString().startsWith("file:")) {
				out_url = true;
			}
			AddTaskAction addTask = new AddTaskAction(uri, out_url);
			Synodroid app = (Synodroid) a.getApplication();
			app.executeAction((DownloadActivity)a, addTask, true);
		}
	}
	

	private Uri fixUri(Uri uri) {
		try {
			URL url = new URL(uri.toString()); // you can write here any link
			File path = Environment.getExternalStorageDirectory();
			path = new File(path, "Android/data/org.jared.synodroid/");
			path.mkdirs();
			String temp[] = uri.toString().split("/");
			String fname = temp[(temp.length) - 1];
			if (!fname.toLowerCase().endsWith(".torrent") && !fname.toLowerCase().endsWith(".nzb")) {
				fname += ".torrent";
			}
			File file = new File(path, fname);

			long startTime = System.currentTimeMillis();
			try{
				if (((Synodroid)a.getApplication()).DEBUG) Log.d(Synodroid.DS_TAG, "Downloading " + uri.toString() + " to temp folder...");
			}catch (Exception ex){/*DO NOTHING*/}
			try{
				if (((Synodroid)a.getApplication()).DEBUG) Log.d(Synodroid.DS_TAG, "Temp file destination: " + file.getAbsolutePath());
			}catch (Exception ex){/*DO NOTHING*/}
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
			try{
				if (((Synodroid)a.getApplication()).DEBUG) Log.d(Synodroid.DS_TAG, "Download completed. Elapsed time: " + ((System.currentTimeMillis() - startTime) / 1000) + " sec(s)");
			}catch (Exception ex){/*DO NOTHING*/}
			uri = Uri.fromFile(file);
		} catch (Exception e) {
			try{
				if (((Synodroid)a.getApplication()).DEBUG) Log.d(Synodroid.DS_TAG, "Download Error: " + e);
			}catch (Exception ex){/*DO NOTHING*/}
			try{
				if (((Synodroid)a.getApplication()).DEBUG) Log.d(Synodroid.DS_TAG, "Letting the NAS do the heavy lifting...");
			}catch (Exception ex){/*DO NOTHING*/}
		}
		return uri;
	}
}

