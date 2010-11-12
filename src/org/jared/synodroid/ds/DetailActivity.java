/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.ds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jared.synodroid.Synodroid;
import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.action.DetailTaskAction;
import org.jared.synodroid.common.action.DownloadOriginalLinkAction;
import org.jared.synodroid.common.action.GetFilesAction;
import org.jared.synodroid.common.action.SynoAction;
import org.jared.synodroid.common.action.UpdateTaskAction;
import org.jared.synodroid.common.data.OriginalFile;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskDetail;
import org.jared.synodroid.common.data.TaskFile;
import org.jared.synodroid.common.data.TaskStatus;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.common.ui.SynodroidActivity;
import org.jared.synodroid.common.ui.Tab;
import org.jared.synodroid.common.ui.TabListener;
import org.jared.synodroid.common.ui.TabWidgetManager;
import org.jared.synodroid.ds.view.adapter.Detail;
import org.jared.synodroid.ds.view.adapter.Detail2Progress;
import org.jared.synodroid.ds.view.adapter.Detail2Text;
import org.jared.synodroid.ds.view.adapter.DetailAction;
import org.jared.synodroid.ds.view.adapter.DetailAdapter;
import org.jared.synodroid.ds.view.adapter.DetailProgress;
import org.jared.synodroid.ds.view.adapter.DetailText;
import org.jared.synodroid.ds.view.adapter.FileDetailAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity displays a task's details
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class DetailActivity extends SynodroidActivity implements TabListener {

	private static final String TAB_FILES = "FILES";
	private static final String TAB_TRANSFERT = "TRANSFERT";
	private static final String TAB_GENERAL = "GENERAL";
	private static final String PREFERENCE_FULLSCREEN = "general_cat.fullscreen";
	private static final String PREFERENCE_GENERAL = "general_cat";

	private static final int TASK_PARAMETERS_DIALOG = 1;

	// The "Not yet implemented" dialog
	@SuppressWarnings("unused")
	private AlertDialog notYetImplementedDialog;
	// The tab manager
	private TabWidgetManager tabManager;
	// The title contains the file's name
	private TextView title;
	// The adapter for general informations
	private DetailAdapter genAdapter;
	// The adapter for transfert informations
	private DetailAdapter transAdapter;
	// The task to retrieve details from
	private Task task;
	// The adapter for task's files
	private FileDetailAdapter fileAdapter;
	// The file ListView
	private ListView filesListView;
	// Flag to know if files tab must be shown
	private boolean showFileTab = false;
	// The seeding ratio
	private int seedingRatio;
	// The seeding time
	private int seedingTime;
	// Flag to know of the user changed seeding parameters
	private boolean seedingChanged = false;
	// The values of seeding time
	private int[] seedingTimes;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);      
		// Create the seeding time int array
		String[] timesArray = getResources().getStringArray(R.array.seeding_time_array_values);
		seedingTimes = new int[timesArray.length];
		for (int iLoop = 0; iLoop < timesArray.length; iLoop++) {
			seedingTimes[iLoop] = Integer.parseInt(timesArray[iLoop]);
		}

		// Get the details intent
		Intent intent = getIntent();
		task = (Task) intent.getSerializableExtra("org.jared.synodroid.ds.Details");

		// Build the general tab
		ListView genListView = new ListView(this);
		genAdapter = new DetailAdapter(this);
		genListView.setAdapter(genAdapter);
		genListView.setOnItemClickListener(genAdapter);

		// Build the transfer tab
		ListView transListView = new ListView(this);
		transAdapter = new DetailAdapter(this);
		transListView.setAdapter(transAdapter);
		transListView.setOnItemClickListener(transAdapter);

		filesListView = new ListView(this);
		fileAdapter = new FileDetailAdapter(this, task);
		filesListView.setAdapter(fileAdapter);

		if (savedInstanceState != null) {
			showFileTab = savedInstanceState.getBoolean("showFileTab", false);
		}
		// Build the TabManager
		tabManager = new TabWidgetManager(this, R.drawable.ic_tab_slider);
		Tab genTab = new Tab(TAB_GENERAL, R.drawable.ic_tab_general, R.drawable.ic_tab_general_selected);
		tabManager.addTab(genTab, genListView);
		Tab transTab = new Tab(TAB_TRANSFERT, R.drawable.ic_tab_transfer, R.drawable.ic_tab_transfer_selected);
		tabManager.addTab(transTab, transListView);
		if (showFileTab) {
			Tab filesTab = new Tab(TAB_FILES, R.drawable.ic_tab_files, R.drawable.ic_tab_files_selected);
			tabManager.addTab(filesTab, filesListView);
		}
		// Call super onCreate after the tab intialization
		super.setTabmanager(tabManager);
		super.onCreate(savedInstanceState);

		genListView.setOnTouchListener(gestureListener);
		transListView.setOnTouchListener(gestureListener);
		filesListView.setOnTouchListener(gestureListener);
		
		// Create a "Not yet implemented" dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_information)).setMessage(getString(R.string.not_yet_implemented)).setCancelable(false).setPositiveButton(R.string.button_ok, null);
		notYetImplementedDialog = builder.create();

		// Add a tab listener
		tabManager.setTabListener(this);

		// Set the the title (the filename)
		title.setText(task.fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		// Create the task's parameters dialog
		case TASK_PARAMETERS_DIALOG:
			// Create the view
			View container = inflater.inflate(R.layout.seeding_parameters, null, false);
			final EditText seedRatio = (EditText) container.findViewById(R.id.seedingPercentage);
			final Spinner seedTime = (Spinner) container.findViewById(R.id.seedingTime);
			// Create the dialog
			builder.setTitle(getString(R.string.seeding_parameters_time));
			builder.setView(container);
			builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogP, int whichP) {
					seedingChanged = true;
					try {
						int seedR = Integer.parseInt(seedRatio.getText().toString());
						seedingRatio = seedR;
						int pos = seedTime.getSelectedItemPosition();
						seedingTime = seedingTimes[pos];
						// At the end, update the task.
						updateTask(true);
					}
					// The ratio is not an integer
					catch (NumberFormatException ex) {
						// NTD: the input method does not allow to set a float or
						// a string
					}
				}
			});
			builder.setNegativeButton(getString(R.string.button_cancel), null);
			return builder.create();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		// Prepare the task's parameters dialog
		case TASK_PARAMETERS_DIALOG:
			final EditText seedRatio = (EditText) dialog.findViewById(R.id.seedingPercentage);
			final Spinner seedTime = (Spinner) dialog.findViewById(R.id.seedingTime);
			seedRatio.setText("" + seedingRatio);
			// Try to find the right value
			int pos = 0;
			for (int iLoop = 0; iLoop < seedingTimes.length; iLoop++) {
				if (seedingTimes[iLoop] == seedingTime) {
					pos = iLoop;
					break;
				}
			}
			seedTime.setSelection(pos);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.ui.SynodroidActivity#handleMessage(android.os
	 * .Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message msgP) {
		switch (msgP.what) {
		// Details updated
		case ResponseHandler.MSG_DETAILS_RETRIEVED:
			TaskDetail details = (TaskDetail) msgP.obj;
			task.isTorrent = details.isTorrent;
			task.isNZB = details.isNZB;
			// If torrent or NZB then add the file's tab
			if (task.isTorrent || task.isNZB) {
				// Build the file tab
				fileAdapter = (FileDetailAdapter) filesListView.getAdapter();
				Tab filesTab = new Tab(TAB_FILES, R.drawable.ic_tab_files, R.drawable.ic_tab_files_selected);
				// If the tab does not already exist !
				if (tabManager.getTab(filesTab) == null) {
					tabManager.addTab(filesTab, filesListView);
				}
				showFileTab = true;
			}
			else {
				showFileTab = false;
			}
			genAdapter.updateDetails(buildGeneralDetails(details));
			transAdapter.updateDetails(buildTransferDetails(details));
			break;
		case ResponseHandler.MSG_ERROR:
			SynoServer server = ((Synodroid) getApplication()).getServer();
			if (server != null)
				server.setLastError((String) msgP.obj);
			showError(server.getLastError(), null);
			break;
		case ResponseHandler.MSG_DETAILS_FILES_RETRIEVED:
			List<TaskFile> files = (List<TaskFile>) msgP.obj;
			fileAdapter.updateFiles(files);
			break;
		case ResponseHandler.MSG_ORIGINAL_FILE_RETRIEVED:
			OriginalFile oriFile = (OriginalFile) msgP.obj;
			File path = Environment.getExternalStorageDirectory();
			path = new File(path, "download");
			File file = new File(path, oriFile.fileName);
			try {
				// Make sure the Pictures directory exists.
				path.mkdirs();
				StringBuffer rawData = oriFile.rawData;
				OutputStream os = new FileOutputStream(file);
				os.write(rawData.toString().getBytes());
				os.close();
				Toast toast = Toast.makeText(DetailActivity.this, getString(R.string.action_download_original_saved), Toast.LENGTH_SHORT);
				toast.show();
			}
			catch (IOException e) {
				// Unable to create file, likely because external storage is
				// not currently mounted.
				Log.w("ExternalStorage", "Error writing " + file, e);
				Toast toast = Toast.makeText(this, getString(R.string.action_download_original_failed), Toast.LENGTH_LONG);
				toast.show();
			}
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// Try to update the details
		updateTask(false);
		Synodroid app = (Synodroid) getApplication();
		app.pauseServer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		//Check for fullscreen
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
		if (preferences.getBoolean(PREFERENCE_FULLSCREEN, false)){
			//Set fullscreen or not
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  	
		}
		else{
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		// Launch the gets task's details recurrent action
		Synodroid app = (Synodroid) getApplication();
		SynoAction detailAction = new DetailTaskAction(task);
		app.executeAsynchronousAction(this, detailAction, false);
		app.setRecurrentAction(this, detailAction);
		app.resumeServer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.ui.SynodroidActivity#attachMainContentView
	 * (android .view.ViewGroup)
	 */
	@Override
	public void attachMainContentView(LayoutInflater inflaterP, ViewGroup parentP) {
		parentP.addView(tabManager.getContentView());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.ui.SynodroidActivity#attachStatusView(android
	 * .view.ViewGroup)
	 */
	@Override
	public void attachStatusView(LayoutInflater inflaterP, ViewGroup parentP) {
		parentP.addView(tabManager.getTabView());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.ui.SynodroidActivity#attachTitleView(android
	 * .view.ViewGroup)
	 */
	@Override
	public void attachTitleView(LayoutInflater inflaterP, ViewGroup parentP) {
		RelativeLayout titleContainer = (RelativeLayout) inflaterP.inflate(R.layout.detail_title, parentP, true);
		title = (TextView) titleContainer.findViewById(R.id.id_title);
	}

	/**
	 * Return a sub detail list for the general's tab
	 */
	private List<Detail> buildGeneralDetails(TaskDetail details) {
		ArrayList<Detail> result = new ArrayList<Detail>();
		// FileName
		result.add(new DetailText(getString(R.string.detail_filename), details.fileName));
		setTitle(details.fileName);
		// Destination
		DetailText destDetail = new DetailText(getString(R.string.detail_destination), details.destination);
		result.add(destDetail);
		// File size
		result.add(new DetailText(getString(R.string.detail_filesize), Utils.bytesToFileSize(details.fileSize, getString(R.string.detail_unknown))));
		// Creation time
		result.add(new DetailText(getString(R.string.detail_creationtime), Utils.computeDate(details.creationDate)));
		// URL
		final String originalLink = details.url;
		DetailText urlDetail = new DetailText(getString(R.string.detail_url), originalLink);
		urlDetail.setAction(new DetailAction() {
			public void execute(Detail detailsP) {
				if ((task.isTorrent || task.isNZB) && tabManager.getSlideToTabName().equals(TAB_GENERAL)){
					Synodroid app = (Synodroid) getApplication();
					task.originalLink = originalLink;
					app.executeAsynchronousAction(DetailActivity.this, new DownloadOriginalLinkAction(task), false);
				}
			}
		});
		result.add(urlDetail);
		// Username
		result.add(new DetailText(getString(R.string.detail_username), details.userName));
		return result;
	}

	/**
	 * Return a sub detail list for the general's tab
	 */
	private List<Detail> buildTransferDetails(TaskDetail details) {
		ArrayList<Detail> result = new ArrayList<Detail>();

		// Set the result to be returned to the previous activity
		Intent previous = new Intent();
		previous.putExtra("org.jared.synodroid.ds.Details", details);
		setResult(RESULT_OK, previous);
		
		// ------------ Status
		result.add(new DetailText(getString(R.string.detail_status), TaskStatus.getLabel(this, details.status)));
		// ------------ Transfered
		String transfered = getString(R.string.detail_progress_download) + " " + Utils.bytesToFileSize(details.bytesDownloaded, getString(R.string.detail_unknown));
		if (details.isTorrent) {
			transfered = transfered + " - " + getString(R.string.detail_progress_upload) + " " + Utils.bytesToFileSize(details.bytesUploaded, getString(R.string.detail_unknown))+" (" + details.bytesRatio + " %)";
		}
		result.add(new DetailText(getString(R.string.detail_transfered), transfered));
		// ------------- Progress
		long downloaded = details.bytesDownloaded;
		long filesize = details.fileSize;
		String downPerStr = getString(R.string.detail_unknown);
		int downPer = 0;
		if (filesize != -1) {
			downPer = (int) ((downloaded * 100) / filesize);
			downPerStr = "" + downPer + "%";
		}

		int upPerc = 0;
		String upPercStr = getString(R.string.detail_unknown);
		Integer uploadPercentage = Utils.computeUploadPercent(details);
		if (uploadPercentage != null) {
			upPerc = uploadPercentage.intValue();
			upPercStr = "" + upPerc + "%";
		}
		// If it is a torrent
		Detail proDetail = null;
		if (details.isTorrent) {
			Detail2Progress progDetail = new Detail2Progress(getString(R.string.detail_progress));
			proDetail = progDetail;
			progDetail.setProgress1(getString(R.string.detail_progress_download) + " " + downPerStr, downPer);
			progDetail.setProgress2(getString(R.string.detail_progress_upload) + " " + upPercStr, upPerc);
			progDetail.setAction(new DetailAction() {
				public void execute(Detail detailsP) {
					if (tabManager.getSlideToTabName().equals(TAB_TRANSFERT)){
					showDialog(TASK_PARAMETERS_DIALOG);
				}
				}
			});
		}
		else {
			DetailProgress progDetail = new DetailProgress(getString(R.string.detail_progress), R.layout.details_progress_template1);
			proDetail = progDetail;
			progDetail.setProgress(getString(R.string.detail_progress_download) + " " + downPerStr, downPer);
		}
		result.add(proDetail);
		// ------------ Speed
		String speed = getString(R.string.detail_progress_download) + " " + details.speedDownload + " KB/s";
		if (details.isTorrent) {
			speed = speed + " - " + getString(R.string.detail_progress_upload) + " " + details.speedUpload + " KB/s";
		}
		result.add(new DetailText(getString(R.string.detail_speed), speed));
		// ------------ Peers
		if (details.isTorrent) {
			String peers = details.peersCurrent + " / " + details.peersTotal;
			DetailProgress peersDetail = new DetailProgress(getString(R.string.detail_peers), R.layout.details_progress_template2);
			int pProgress = 0;
			if (details.peersTotal != 0) {
				pProgress = (int) ((details.peersCurrent * 100) / details.peersTotal);
			}
			peersDetail.setProgress(peers, pProgress);
			result.add(peersDetail);
		}
		// ------------ Seeders / Leechers
		if (details.isTorrent) {
			String seedStr = getString(R.string.detail_unvailable);
			String leechStr = getString(R.string.detail_unvailable);
			if (details.seeders != null)
				seedStr = details.seeders.toString();
			if (details.leechers != null)
				leechStr = details.leechers.toString();
			String seeders = seedStr + " - " + leechStr;
			result.add(new DetailText(getString(R.string.detail_seeders_leechers), seeders));
		}
		// ------------ ETAs
		String etaUpload = getString(R.string.detail_unknown);
		String etaDownload = getString(R.string.detail_unknown);
		if (details.speedDownload != 0) {
			long sizeLeft = filesize - downloaded;
			long timeLeft = (long) (sizeLeft / (details.speedDownload * 1000));
			etaDownload = Utils.computeTimeLeft(timeLeft);
		}
		else {
			if (downPer == 100) {
				etaDownload = getString(R.string.detail_finished);
			}
		}
		Long timeLeftSize = null;
		long uploaded = details.bytesUploaded;
		double ratio = ((double) (details.seedingRatio)) / 100.0d;
		if (details.speedUpload != 0 && details.seedingRatio != 0) {
			long sizeLeft = (long) ((filesize * ratio) - uploaded);
			timeLeftSize = (long) (sizeLeft / (details.speedUpload * 1000));
		}
		// If the user defined a minimum seeding time AND we are in seeding
		// mode
		Long timeLeftTime = null;
		if (details.seedingInterval != 0 && TaskStatus.valueOf(details.status) == TaskStatus.TASK_SEEDING) {
			timeLeftTime = (details.seedingInterval * 60) - details.seedingElapsed;
			if (timeLeftTime < 0) {
				timeLeftTime = null;
			}
		}
		// At least one time has been computed
		if (timeLeftTime != null || timeLeftSize != null) {
			// By default take the size time
			Long time = timeLeftSize;
			// Except if it is null
			if (timeLeftSize == null) {
				time = timeLeftTime;
			}
			else {
				// If time is not null
				if (timeLeftTime != null) {
					// Get the higher value
					if (timeLeftTime > timeLeftSize) {
						time = timeLeftTime;
					}
				}
			}
			etaUpload = Utils.computeTimeLeft(time);
		}
		else if (upPerc == 100) {
			etaUpload = getString(R.string.detail_finished);
		}
		// In case the user set the seedin time to forever
		if (details.seedingInterval == -1) {
			etaUpload = getString(R.string.detail_forever);
		}
		Detail etaDet = null;
		// If it is a torrent then show the upload ETA
		if (details.isTorrent) {
			Detail2Text etaDetail = new Detail2Text(getString(R.string.detail_eta));
			etaDet = etaDetail;
			etaDetail.setValue1(getString(R.string.detail_progress_download) + " " + etaDownload);
			etaDetail.setValue2(getString(R.string.detail_progress_upload) + " " + etaUpload);
			etaDetail.setAction(new DetailAction() {
				public void execute(Detail detailsP) {
					if (tabManager.getSlideToTabName().equals(TAB_TRANSFERT)){
					showDialog(TASK_PARAMETERS_DIALOG);
				}
				}
			});
		}
		// Otherwise only show the download ETA
		else {
			DetailText etaDetail = new DetailText(getString(R.string.detail_eta));
			etaDet = etaDetail;
			etaDetail.setValue(getString(R.string.detail_progress_download) + " " + etaDownload);
		}
		result.add(etaDet);
		// ------------ Pieces
		if (details.isTorrent) {
			String pieces = details.piecesCurrent + " / " + details.piecesTotal;
			DetailProgress piecesDetail = new DetailProgress(getString(R.string.detail_pieces), R.layout.details_progress_template2);
			int piProgress = 0;
			if (details.piecesTotal != 0) {
				piProgress = (int) ((details.piecesCurrent * 100) / details.piecesTotal);
			}
			piecesDetail.setProgress(pieces, piProgress);
			result.add(piecesDetail);
		}
		// Update seeding parameters
		seedingRatio = details.seedingRatio;
		seedingTime = details.seedingInterval;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.ui.TabListener#selectedTabChanged(java.lang.
	 * String, java.lang.String)
	 */
	public void selectedTabChanged(String oldTabId, String newTabIdP) {
		// If showing the Files tab then retrieve task's files
		if (newTabIdP.equals(TAB_FILES)) {
			Synodroid application = (Synodroid) getApplication();
			SynoServer server = application.getServer();
			if (server != null) {
				// Clear the list just before updating items
				fileAdapter.updateFiles(new ArrayList<TaskFile>());
				server.executeAsynchronousAction(this, new GetFilesAction(task), false);
			}
		}
		// If the user comes from the Files tab then save the modifications
		if (oldTabId.equals(TAB_FILES)) {
			updateTask(false);
		}
	}

	/**
	 * Update the current task
	 */
	private void updateTask(boolean forceRefreshP) {
		List<TaskFile> modifiedTaskFiles = fileAdapter.getModifiedTaskList();
		if ((modifiedTaskFiles != null && modifiedTaskFiles.size() > 0) || (seedingChanged)) {
			Synodroid app = (Synodroid) getApplication();
			UpdateTaskAction update = new UpdateTaskAction(task, modifiedTaskFiles, seedingRatio, seedingTime);
			app.getServer().executeAsynchronousAction(this, update, forceRefreshP);
			seedingChanged = false;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putInt("tabID", tabManager.getCurrentTabIndex());
		savedInstanceState.putBoolean("showFileTab", showFileTab);
		// etc.
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		int curTabId = savedInstanceState.getInt("tabID");
		tabManager.slideTo(tabManager.getNameAtId(curTabId));
	}

}
