/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jared.synodroid.ds;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.jared.synodroid.Synodroid;
import org.jared.synodroid.common.Eula;
import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.SynoProtocol;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskContainer;
import org.jared.synodroid.common.data.TaskDetail;
import org.jared.synodroid.common.preference.PreferenceFacade;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.common.ui.SynodroidActivity;
import org.jared.synodroid.common.ui.Tab;
import org.jared.synodroid.common.ui.TabWidgetManager;
import org.jared.synodroid.common.ui.TitleClicklistener;
import org.jared.synodroid.ds.action.AddTaskAction;
import org.jared.synodroid.ds.action.SynoAction;
import org.jared.synodroid.ds.action.TaskActionMenu;
import org.jared.synodroid.ds.view.adapter.ActionAdapter;
import org.jared.synodroid.ds.view.adapter.TaskAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This activity list all current tasks
 * 
 * @author eric.taix at gmail.com
 */
public class DownloadActivity extends SynodroidActivity implements Eula.OnEulaAgreedTo, TitleClicklistener {

  // The connection dialog ID
  private static final int CONNECTION_DIALOG_ID = 1;
  // The contributors dialog
  private static final int CONTRIBUTORS_DIALOG_ID = 3;
  // No server configured
  private static final int NO_SERVER_DIALOG_ID = 4;

  // Menu connection
  public static final int MENU_CONNECT = 1;
  // Menu search
  public static final int MENU_SEARCH = 2;
  // Menu actions
  public static final int MENU_ACTIONS = 3;
  // Menu parameters
  public static final int MENU_PARAMETERS = 4;
  // Menu refresh
  public static final int MENU_REFRESH = 5;
  // Menu about
  public static final int MENU_ABOUT = 6;

  // The torrent listview
  private ListView taskView;
  // The total upload rate view
  private TextView totalUpView;
  // The total download rate view
  private TextView totalDownView;
  // The synology server
  private SynoServer server;
  // Flag to know is the EULA has been accepted
  private boolean licenceAccepted = false;
  // Flag to tell app that the connect dialog is opened
  private boolean connectDialogOpened = false;
  // The title text
  private TextView titleText;
  // The title icon
  private ImageView titleIcon;
  // The tab manager
  private TabWidgetManager tabManager;

  /**
   * Handle the message
   */
  public void handleMessage(Message msg) {
    // Update torrent
    if (msg.what == ResponseHandler.MSG_TASKS_UPDATED) {
      TaskContainer container = (TaskContainer) msg.obj;
      List<Task> tasks = container.getTasks();
      // Get the adapter
      TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
      taskAdapter.updateTasks(tasks);
      // Dissmiss the connection dialog
      try {
        dismissDialog(CONNECTION_DIALOG_ID);
      }
      // Nothing to do because it can occured when a new instance is
      // created
      // if a SynoCollector thread is already running
      catch(IllegalArgumentException ex) {
      }
      // Update total rates
      totalUpView.setText(container.getTotalUp());
      totalDownView.setText(container.getTotalDown());
    }
    // An error message
    else if (msg.what == ResponseHandler.MSG_ERROR) {
      // Change the title
      titleText.setText(getString(R.string.app_name));
      titleIcon.setVisibility(View.GONE);
      // No tasks
      ArrayList<Task> tasks = new ArrayList<Task>();
      // Get the adapter AND update datas
      TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
      taskView.setOnItemClickListener(taskAdapter);
      taskAdapter.updateTasks(tasks);
      // Dissmiss the connection dialog
      try {
        dismissDialog(CONNECTION_DIALOG_ID);
      }
      // Nothing to do because it can occured when a new instance is
      // created
      // if a SynoCollector thread is already running
      catch(IllegalArgumentException ex) {
      }
      // Show the error
      // Save the last error inside the server to surive UI rotation and
      // pause/resume.
      if (server == null) server = ((Synodroid) getApplication()).getServer();
      if (server != null) server.setLastError((String) msg.obj);
      showError(server.getLastError(), new Dialog.OnClickListener() {
        public void onClick(DialogInterface dialogP, int whichP) {
          // Ask to reconnect when connection is lost.
          showDialogToConnect(false, null);
        }
      });
    }
    // Connection is done
    else if (msg.what == ResponseHandler.MSG_CONNECTED) {
      // Change the title
      titleText.setText(server.getNickname());
      titleIcon.setVisibility(server.getProtocol() == SynoProtocol.HTTPS ? View.VISIBLE : View.GONE);
    }
    // Connecting to the server
    else if (msg.what == ResponseHandler.MSG_CONNECTING) {
      // Clear the prevous task list
      TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
      taskAdapter.updateTasks(new ArrayList<Task>());
      // Show the connection dialog
      showDialog(CONNECTION_DIALOG_ID);
    }
    else if (msg.what == ResponseHandler.MSG_DETAILS_RETRIEVED) {
      Intent next = new Intent();
      next.setClass(DownloadActivity.this, DetailActivity.class);
      next.putExtra("org.jared.synodroid.ds.Details", (TaskDetail) msg.obj);
      DownloadActivity.this.startActivity(next);
    }
  }

  /**
   * Activity creation
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    
    // Retrieve the listview
    RelativeLayout downloadContent = (RelativeLayout) inflater.inflate(R.layout.download_list, null, false);
    taskView = (ListView) downloadContent.findViewById(R.id.id_task_list);
    totalUpView = (TextView) downloadContent.findViewById(R.id.id_total_upload);
    totalDownView = (TextView) downloadContent.findViewById(R.id.id_total_download);
    
    // Create the tab maanger
    tabManager = new TabWidgetManager(this, R.drawable.ic_tab_slider);
    Tab refreshTab = new Tab("REFRESH", R.drawable.ic_tab_download, R.drawable.ic_tab_download_selected);
    tabManager.addTab(refreshTab, downloadContent);
    Tab paramTab = new Tab("PARAMS", R.drawable.ic_tab_parameters, R.drawable.ic_tab_parameters_selected);
    tabManager.addTab(paramTab, new View(this));
    Tab aboutTab = new Tab("ABOUT", R.drawable.ic_tab_about, R.drawable.ic_tab_about_selected);
    tabManager.addTab(aboutTab, new View(this));
    
    super.onCreate(savedInstanceState);

    licenceAccepted = Eula.show(this, false);



    // Retrieve title's text, icon and progress for future uses
    titleText = (TextView) findViewById(R.id.id_title);
    titleIcon = (ImageView) findViewById(R.id.id_https);

    // Create the task adapter
    TaskAdapter taskAdapter = new TaskAdapter(this);
    taskView.setAdapter(taskAdapter);
    taskView.setOnItemClickListener(taskAdapter);

    // First bind the current activity to the current server if exist
    if (!((Synodroid) getApplication()).bindActivity(this)) {
      Intent intent = getIntent();
      String action = intent.getAction();
      // Show the dialog only if the intent's action is not to view a
      // content -> add a new file
      if (action != null && !(action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND))) {
        showDialogToConnect(true, null);
      }
      else {
        handleIntent(intent);
      }
    }
    
    // The user is able to click on the title bar to connect to a server
    setTitleClickListener(this);
    

  }

  /*
   * (non-Javadoc)
   * @see
   * org.jared.synodroid.common.ui.TitleClicklistener#onTitleClicked(android
   * .view.View)
   */
  public void onTitleClicked(View viewP) {
    showDialogToConnect(false, null);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jared.synodroid.common.ui.SynodroidActivity#getMainContentView()
   */
  @Override
  public void attachMainContentView(LayoutInflater inflaterP, ViewGroup parentP) {
    parentP.addView(tabManager.getContentView());
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.ui.SynodroidActivity#getStatusView()
   */
  @Override
  public void attachStatusView(LayoutInflater inflaterP, ViewGroup parentP) {
    parentP.addView(tabManager.getTabView());
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.ui.SynodroidActivity#getTitleView()
   */
  @Override
  public void attachTitleView(LayoutInflater inflaterP, ViewGroup parentP) {
    inflaterP.inflate(R.layout.download_title, parentP, true);
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onStart()
   */
  @Override
  protected void onStart() {
    super.onStart();
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onNewIntent(android.content.Intent)
   */
  @Override
  protected void onNewIntent(Intent intentP) {
    super.onNewIntent(intentP);
    Log.d(Synodroid.DS_TAG, "New intent: " + intentP);
    handleIntent(intentP);
  }

  /**
   * Handle all new intent
   * 
   * @param intentP
   */
  private void handleIntent(Intent intentP) {
    String action = intentP.getAction();
    if (action != null) {
      Uri uri = null;
      boolean out_url = false;
      if (action.equals(Intent.ACTION_VIEW)) {
        uri = intentP.getData();
      }
      else if (action.equals(Intent.ACTION_SEND)) {
        String uriString = (String) intentP.getExtras().get(Intent.EXTRA_TEXT);
        uri = Uri.parse(uriString);
        out_url = true;
      }
      else {
        return;
      }

      if (uri != null) {
        AddTaskAction addTask = new AddTaskAction(uri, out_url);
        Synodroid app = (Synodroid) getApplication();
        app.executeAction(this, addTask, true);
      }
    }
  }

  /**
   * Create the connection and error dialog
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    super.onCreateDialog(id);

    Dialog dialog = null;
    switch (id) {
      // The connection dialog
      case CONNECTION_DIALOG_ID:
        dialog = new ProgressDialog(this);
        dialog.setTitle("");
        ((ProgressDialog) dialog).setMessage("Connecting. Please wait...");
        ((ProgressDialog) dialog).setIndeterminate(true);
        break;
      // The contributors dialog
      case CONTRIBUTORS_DIALOG_ID:
        AlertDialog.Builder builderCont = new AlertDialog.Builder(this);
        builderCont.setIcon(R.drawable.icon_phone).setTitle(R.string.app_name);
        String text = getString(R.string.app_contributors) + "\n\n";
        text += "- Eric Taix\n" + "- Steve Garon\n";
        builderCont.setMessage(text);
        builderCont.setCancelable(true).setPositiveButton(getString(R.string.button_ok), null);
        builderCont.setNegativeButton(getString(R.string.eula_view), new OnClickListener() {
          public void onClick(DialogInterface dialogP, int whichP) {
            // Diplay the EULA
            Eula.show(DownloadActivity.this, true);
          }
        });
        dialog = builderCont.create();
        break;
      // No server have been yet configured
      case NO_SERVER_DIALOG_ID:
        AlertDialog.Builder builderNoServer = new AlertDialog.Builder(this);
        builderNoServer.setTitle(R.string.dialog_title_information);
        builderNoServer.setMessage(getString(R.string.no_server_configured));
        builderNoServer.setCancelable(true);
        builderNoServer.setPositiveButton(getString(R.string.button_yesplease), new OnClickListener() {
          // Launch the Preference activity
          public void onClick(DialogInterface dialogP, int whichP) {
            showPreferenceActivity();
          }
        });
        builderNoServer.setNegativeButton(getString(R.string.button_nothanks), null);
        dialog = builderNoServer.create();
        break;

    }
    return dialog;
  }

  /**
   * Update the dialog according to the internal state of the activity
   */
  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
    switch (id) {
      // The connection dialog
      case CONNECTION_DIALOG_ID:
        // On UI rotation this.server is set to null. Verifying is the
        // server is null fixes the force close on UI rotate.
        if (server != null) {
          String msg = MessageFormat.format(getString(R.string.connect_connecting), new Object[] { server.toString() });
          ((ProgressDialog) dialog).setMessage(msg);
        }
        break;
    }
  }

  /**
   * Create the option menu of this activity
   */
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, MENU_CONNECT, 0, getString(R.string.menu_connect)).setIcon(android.R.drawable.ic_menu_share);
    menu.add(0, MENU_REFRESH, 0, getString(R.string.menu_refresh)).setIcon(R.drawable.menu_refresh);
    menu.add(0, MENU_PARAMETERS, 0, getString(R.string.menu_parameter)).setIcon(android.R.drawable.ic_menu_preferences);
    menu.add(0, MENU_ABOUT, 0, getString(R.string.menu_about)).setIcon(android.R.drawable.ic_menu_info_details);
    return true;
  }

  /**
   * Interact with the user when a menu is selected
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONNECT:
        showDialogToConnect(false, null);
        return true;
      case MENU_SEARCH:
        return true;
      case MENU_REFRESH:
        ((Synodroid) getApplication()).forceRefresh();
        return true;
      case MENU_ACTIONS:
        return true;
        // Launch the parameters activity
      case MENU_PARAMETERS:
        showPreferenceActivity();
        return true;
        // Launch the about dialog
      case MENU_ABOUT:
        showDialog(CONTRIBUTORS_DIALOG_ID);
        return true;
    }
    return false;
  }

  /**
   * Show the preference activity
   */
  private void showPreferenceActivity() {
    Intent next = new Intent();
    next.setClass(this, DownloadPreferenceActivity.class);
    startActivity(next);
  }

  /**
   * Show the dialog to connect to a server
   */
  public void showDialogToConnect(boolean autoConnectIfOnlyOneServerP, final List<SynoAction> actionQueueP) {
    if (!connectDialogOpened) {
      final ArrayList<SynoServer> servers = PreferenceFacade.loadServers(PreferenceManager
              .getDefaultSharedPreferences(this));
      // If at least one server
      if (servers.size() != 0) {
        // If more than 1 server OR if we don't want to autoconnect then
        // show
        // the dialog
        if (servers.size() > 1 || !autoConnectIfOnlyOneServerP) {
          String[] serversTitle = new String[servers.size()];
          for (int iLoop = 0; iLoop < servers.size(); iLoop++) {
            serversTitle[iLoop] = servers.get(iLoop).getNickname();
          }
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle(getString(R.string.menu_connect));
          // When the user select a server
          builder.setItems(serversTitle, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
              server = servers.get(item);
              // Change the server
              ((Synodroid) getApplication()).setServer(DownloadActivity.this, server, actionQueueP);
              dialog.dismiss();
            }
          });
          AlertDialog connectDialog = builder.create();
          connectDialog.show();
          connectDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
              connectDialogOpened = false;
            }
          });
          connectDialogOpened = true;
        }
        else {
          // Auto connect to the first server
          if (servers.size() > 0) {
            server = servers.get(0);
            // Change the server
            ((Synodroid) getApplication()).setServer(DownloadActivity.this, server, actionQueueP);
          }
        }
      }
      // No server then show the dialog to configure a server
      else {
        // Only if the EULA has been accepted. If the EULA has not been
        // accepted, it means that the EULA is currenlty being displayed so
        // don't show the "Wizard" dialog
        if (licenceAccepted) {
          showDialog(NO_SERVER_DIALOG_ID);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onPause()
   */
  @Override
  protected void onPause() {
    super.onPause();
    ((Synodroid) getApplication()).pauseServer();
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onResume()
   */
  @Override
  protected void onResume() {
    ((Synodroid) getApplication()).resumeServer();
    super.onResume();
    // There are some case where the connected server does not show up in
    // the title bar on top. This fixes thoses cases.
    server = ((Synodroid) getApplication()).getServer();
    if (server != null) {
      if (server.isConnected()) {
        titleText.setText(server.getNickname());
        titleIcon.setVisibility(server.getProtocol() == SynoProtocol.HTTPS ? View.VISIBLE : View.INVISIBLE);
      }
    }
    // No server then display the connect to dialog
    else {
      showDialogToConnect(true, null);
    }
  }

  /**
   * A task as been clicked by the user
   * 
   * @param taskP
   */
  public void onTaskClicked(final Task taskP) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.dialog_title_action));
    final ActionAdapter adapter = new ActionAdapter(this, taskP);
    builder.setAdapter(adapter, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        TaskActionMenu taskAction = (TaskActionMenu) adapter.getItem(which);
        // Only if TaskActionMenu is enabled: it seems that even if the
        // item is
        // disable the user can tap it
        if (taskAction.isEnabled()) {
          Synodroid app = (Synodroid) getApplication();
          app.executeAction(DownloadActivity.this, taskAction.getAction(), true);
        }
      }
    });
    AlertDialog connectDialog = builder.create();
    connectDialog.show();
  }

  /**
   * The Eual has just been accepted
   */
  public void onEulaAgreedTo() {
    licenceAccepted = true;
    showDialogToConnect(true, null);
  }

}
