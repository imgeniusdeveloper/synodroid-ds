/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid;

import java.util.ArrayList;
import java.util.List;

import org.acra.CrashReportingApplication;
import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.SynoServerConnection;
import org.jared.synodroid.common.action.DeleteTaskAction;
import org.jared.synodroid.common.action.GetAllAndOneDetailTaskAction;
import org.jared.synodroid.common.action.SynoAction;
import org.jared.synodroid.common.data.TaskStatus;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.ds.DetailActivity;
import org.jared.synodroid.ds.DownloadActivity;
import org.jared.synodroid.ds.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

/**
 * The application (single instance) which implements utility methods to access to the current server
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class Synodroid extends CrashReportingApplication {

  private static final String GOOGLEDOC_FORM_ID = "dENyc3VzSFNwdzZScVJ0T3RPNk9tbVE6MQ";

  public static final String DS_TAG = "Synodroid DS";

  // The current server
  private SynoServer currentServer = null;

  
  
  /* (non-Javadoc)
   * @see org.acra.CrashReportingApplication#getFormId()
   */
  @Override
  public String getFormId() {
    return GOOGLEDOC_FORM_ID;
  }

  /* (non-Javadoc)
   * @see org.acra.CrashReportingApplication#getCrashResources()
   */
  @Override
  public Bundle getCrashResources() {
    Bundle result = new Bundle();
    result.putInt(RES_NOTIF_TICKER_TEXT, R.string.crash_notif_ticker_text);
    result.putInt(RES_NOTIF_TITLE, R.string.crash_notif_title);
    result.putInt(RES_NOTIF_TEXT, R.string.crash_notif_text);
    result.putInt(RES_NOTIF_ICON, android.R.drawable.stat_notify_error); // optional. default is a warning sign
    result.putInt(RES_DIALOG_TEXT, R.string.crash_dialog_text);
    result.putInt(RES_DIALOG_ICON, android.R.drawable.ic_dialog_info); //optional. default is a warning sign
    result.putInt(RES_DIALOG_TITLE, R.string.crash_dialog_title); // optional. default is your application name 
    result.putInt(RES_DIALOG_COMMENT_PROMPT, R.string.crash_dialog_comment_prompt); // optional. when defined, adds a user text field input with this text resource as a label
    result.putInt(RES_DIALOG_OK_TOAST, R.string.crash_dialog_ok_toast); // optional. Displays a Toast when the user accepts to send a report ("Thank you !" for example)
    return result;
  }

  /*
   * (non-Javadoc)
   * @see android.app.Application#onCreate()
   */
  @Override
  public void onCreate() {
    super.onCreate();
  }

  /*
   * (non-Javadoc)
   * @see android.app.Application#onTerminate()
   */
  @Override
  public void onTerminate() {
    super.onTerminate();
  }

  /**
   * Set the current server. An attempt to connect to the server is only done if this is different server
   * 
   * @param activityP
   * @param serverP
   */
  public synchronized void connectServer(DownloadActivity activityP, SynoServer serverP, List<SynoAction> actionQueueP) {
    if (currentServer == null || !currentServer.isAlive() || !currentServer.equals(serverP)) {
      // First disconnect the old server
      if (currentServer != null) {
        currentServer.disconnect();
      }
      // Set the recurrent action
      GetAllAndOneDetailTaskAction recurrentAction = new GetAllAndOneDetailTaskAction(serverP.getSortAttribute(), serverP.isAscending(), activityP.getTaskAdapter());
      serverP.setRecurrentAction(activityP, recurrentAction);
      // Then connect the new one
      currentServer = serverP;
      
      // Determine the current network access
      WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
      boolean wifiOn = wifiMgr.isWifiEnabled();
      final WifiInfo currentWifi = wifiMgr.getConnectionInfo();
      final boolean wifiConnected = (wifiOn && currentWifi.getNetworkId() != -1);
      // If we are connected to a WIFI network, verify if SSID match
      boolean pub  = true; 
      String cur_ssid = currentWifi.getSSID();
      if (wifiConnected && cur_ssid != null) {
    	SynoServerConnection sc = currentServer.getLocalConnection();
    	if (sc != null){
	        List<String> ssids = sc.wifiSSID;
	        if (ssids != null) {
	        	for (String ssid : ssids) {
	        		if (cur_ssid.equals(ssid)) {
	        			pub = false;
	        			break;
	        		}
	          }
	        }
    	}
      }      
      currentServer.connect(activityP, actionQueueP, pub);
    }
  }

  /**
   * Get the current server
   * 
   * @return currentServer
   */
  public SynoServer getServer() {
    return currentServer;
  }

  /**
   * Bind an activity to the current server
   * 
   * @param handlerP
   */
  public boolean bindResponseHandler(ResponseHandler handlerP) {
    if (currentServer == null) {
      return false;
    }
    else{
      currentServer.bindResponseHandler(handlerP);
      return true;
    }
  }

  /**
   * Change the recurrent action
   * 
   * @param actionP
   */
  public void setRecurrentAction(ResponseHandler handlerP, SynoAction actionP) {
    if (currentServer != null) {
      currentServer.setRecurrentAction(handlerP, actionP);
    }
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
   * 
   * @param activityP
   * @param actionP
   * @param forceRefreshP
   */
  public void executeAction(final DetailActivity activityP, final SynoAction actionP, final boolean forceRefreshP) {
    if (currentServer != null) {
      // First verify if it is a DeleteTaskAction and if the task is not finished
      TaskStatus status = null;
      if (actionP.getTask() != null && actionP.getTask().status != null) {
        status = actionP.getTask().getStatus();
      }
      if ((actionP instanceof DeleteTaskAction) && (status != TaskStatus.TASK_FINISHED)) {
        Dialog d = new AlertDialog.Builder(activityP).setTitle(R.string.dialog_title_confirm).setMessage(
                R.string.dialog_message_confirm).setNegativeButton(android.R.string.no, null).setPositiveButton(
                android.R.string.yes, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    currentServer.executeAsynchronousAction(activityP, actionP, forceRefreshP);
                    activityP.finish();
                  }
                }).create();
        // d.setOwnerActivity(this); // why can't the builder do this?
        d.show();
      }
      else if (actionP instanceof DeleteTaskAction){
    	  currentServer.executeAsynchronousAction(activityP, actionP, forceRefreshP);
    	  activityP.finish();
      }
      // Ok no problem do it
      else {
        currentServer.executeAsynchronousAction(activityP, actionP, forceRefreshP);
      }
    }
    // If an action have to be executed but with no current connection
    else {
      ArrayList<SynoAction> actionQueue = new ArrayList<SynoAction>();
      actionQueue.add(actionP);
    }
  }
  
  /**
   * Execute an action and connect to the server or display the connection dialog if needed
   * 
   * @param activityP
   * @param actionP
   * @param forceRefreshP
   */
  public void executeAction(final DownloadActivity activityP, final SynoAction actionP, final boolean forceRefreshP) {
	if (currentServer != null && currentServer.isConnected()) {
      // First verify if it is a DeleteTaskAction and if the task is not finished
      TaskStatus status = null;
      if (actionP.getTask() != null && actionP.getTask().status != null) {
    	  status = actionP.getTask().getStatus();
      }
      if ((actionP instanceof DeleteTaskAction) && (status != TaskStatus.TASK_FINISHED)) {
        Dialog d = new AlertDialog.Builder(activityP).setTitle(R.string.dialog_title_confirm).setMessage(
                R.string.dialog_message_confirm).setNegativeButton(android.R.string.no, null).setPositiveButton(
                android.R.string.yes, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    currentServer.executeAsynchronousAction(activityP, actionP, forceRefreshP);
                  }
                }).create();
        // d.setOwnerActivity(this); // why can't the builder do this?
        d.show();
      }
      // Ok no problem do it
      else {
    	currentServer.executeAsynchronousAction(activityP, actionP, forceRefreshP);
      }
    }
    // If an action have to be executed but with no current connection
    else {
      ArrayList<SynoAction> actionQueue = new ArrayList<SynoAction>();
      actionQueue.add(actionP);
      activityP.alreadyCanceled = false;
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
  
  /**
   * Execute an asynchronous action if the server is currently connected
   * @param handlerP
   * @param actionP
   * @param forceRefreshP
   * @param showToastP
   */
  
  public void executeAsynchronousAction(ResponseHandler handlerP, SynoAction actionP, final boolean forceRefreshP, final boolean showToastP) {
	  if (currentServer != null) {
	      currentServer.executeAsynchronousAction(handlerP, actionP, forceRefreshP, showToastP);
	  }
  }
	  
  /**
   * Execute an asynchronous action if the server is currently connected
   * @param handlerP
   * @param actionP
   * @param forceRefreshP
   */
  public void executeAsynchronousAction(ResponseHandler handlerP, SynoAction actionP, final boolean forceRefreshP) {
    if (currentServer != null) {
      currentServer.executeAsynchronousAction(handlerP, actionP, forceRefreshP);
    }
  }
  
}
