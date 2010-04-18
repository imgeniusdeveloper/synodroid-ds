/**
 * Copyright 2010 Eric Taix
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 */
package org.jared.synodroid.ds;

import java.util.ArrayList;
import java.util.List;

import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.TaskStatus;
import org.jared.synodroid.ds.action.DeleteTaskAction;
import org.jared.synodroid.ds.action.TaskAction;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;

/**
 * The application (single instance) which implements utility methods to access
 * to the current server
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class DownloadApplication extends Application {

	// The current server
	private SynoServer currentServer = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	/**
	 * Set the current server
	 * 
	 * @param activityP
	 * @param serverP
	 */
	public void setServer(DownloadActivity activityP, SynoServer serverP, List<TaskAction> actionQueueP) {
		// First disconnect the old server
		if (currentServer != null) {
			currentServer.disconnect();
		}
		// Then connect the new one
		currentServer = serverP;
		currentServer.connect(activityP, actionQueueP);
	}

	/**
	 * Get the current server
	 * 
	 * @return currentServer
	 */
	public SynoServer getServer(){
		return currentServer;
	}
	
	/**
	 * Bind an activity to the current server
	 * 
	 * @param activityP
	 */
	public boolean bindActivity(DownloadActivity activityP) {
		if (currentServer != null) {
			currentServer.bindActivity(activityP);
			return true;
		}
		return false;
	}

	/**
	 * Force a refresh
	 */
	public void forceRefresh() {
		if (currentServer != null) {
			currentServer.forceRefresh();
		}
	}

	/**
	 * Pause the current server if exist
	 */
	public void pauseServer() {
		if (currentServer != null) {
			currentServer.pause();
		}
	}

	/**
	 * Resume the current server if exist
	 */
	public void resumeServer() {
		if (currentServer != null) {
			currentServer.resume();
		}
	}

	/**
	 * Execute an action and connect to the server or display the connection dialog if needed
	 * @param activityP
	 * @param actionP
	 * @param forceRefreshP
	 */
	public void executeAction(final DownloadActivity activityP, final TaskAction actionP, final boolean forceRefreshP) {
		if (currentServer != null) {
			// First verify if it is a DeleteTaskAction and if the task is not finished
			TaskStatus status = null;
			if (actionP.getTask() != null && actionP.getTask().status!=null) {
				status = TaskStatus.valueOf(actionP.getTask().status);
			}
			if ((actionP instanceof DeleteTaskAction) && (status != TaskStatus.TASK_FINISHED)) {
				Dialog d = new AlertDialog.Builder(activityP).setTitle(R.string.dialog_title_confirm).setMessage(R.string.dialog_message_confirm).setNegativeButton(android.R.string.no, null).setPositiveButton(android.R.string.yes,
				    new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int which) {
						    currentServer.executeAction(activityP, actionP, forceRefreshP);
					    }
				    }).create();
				// d.setOwnerActivity(this); // why can't the builder do this?
				d.show();
			}
			// Ok no problem do it
			else {
				currentServer.executeAction(activityP, actionP, forceRefreshP);
			}
		}
		// If an action have to be executed but with no current connection
		else {
			ArrayList<TaskAction> actionQueue = new ArrayList<TaskAction>();
			actionQueue.add(actionP);
			activityP.showDialogToConnect(true, actionQueue);
		}
	}

	/**
	 * Change the sort
	 * 
	 * @param sorAttrP
	 * @param ascendingP
	 */
	public void setServerSort(String sorAttrP, boolean ascendingP) {
		if (currentServer != null) {
			currentServer.setSortAttribute(sorAttrP);
			currentServer.setAscending(ascendingP);
			currentServer.forceRefresh();
		}
	}


}
