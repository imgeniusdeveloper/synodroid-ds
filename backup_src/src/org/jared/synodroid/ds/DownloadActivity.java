/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.ds;

import java.util.ArrayList;
import java.util.List;

import org.jared.synodroid.Synodroid;
import org.jared.synodroid.common.Eula;
import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.action.AddTaskAction;
import org.jared.synodroid.common.action.ClearAllTaskAction;
import org.jared.synodroid.common.action.EnumShareAction;
import org.jared.synodroid.common.action.GetAllTaskAction;
import org.jared.synodroid.common.action.SetShared;
import org.jared.synodroid.common.action.SynoAction;
import org.jared.synodroid.common.data.SharedDirectory;
import org.jared.synodroid.common.data.SynoProtocol;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskContainer;
import org.jared.synodroid.common.preference.PreferenceFacade;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.common.ui.SynodroidActivity;
import org.jared.synodroid.common.ui.Tab;
import org.jared.synodroid.common.ui.TabListener;
import org.jared.synodroid.common.ui.TabWidgetManager;
import org.jared.synodroid.common.ui.TitleClicklistener;
import org.jared.synodroid.ds.action.ShowDetailsAction;
import org.jared.synodroid.ds.action.TaskActionMenu;
import org.jared.synodroid.ds.view.adapter.ActionAdapter;
import org.jared.synodroid.ds.view.adapter.TaskAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This activity list all current tasks
 * 
 * @author eric.taix at gmail.com
 */
public class DownloadActivity extends SynodroidActivity implements Eula.OnEulaAgreedTo, TitleClicklistener, TabListener {

  private static final String TAB_ABOUT = "ABOUT";
  private static final String TAB_EMULE = "EMULE";
  private static final String TAB_TASKS = "TASKS";
  private static final String PREFERENCE_AUTO = "auto";
  private static final String PREFERENCE_AUTO_CREATENOW = "auto.createnow";
  
  // The connection dialog ID
  private static final int CONNECTION_DIALOG_ID = 1;
  // No server configured
  private static final int NO_SERVER_DIALOG_ID = 2;

  // Menu parameters
  public static final int MENU_PARAMETERS = 1;
  // Menu Clear
  public static final int MENU_CLEAR = 2;
  // Menu Shared directory
  public static final int MENU_DESTINATION = 3;

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
  // Current tab
  private String cur_tab = TAB_TASKS;

  /**
   * Handle the message
   */
  @SuppressWarnings("unchecked")
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
      taskView.setOnItemLongClickListener(taskAdapter);
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
          if (server != null) {
            if (!server.isConnected()) {
              showDialogToConnect(false, null);
            }
          }
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
    // Show task's details
    else if (msg.what == ResponseHandler.MSG_SHOW_DETAILS) {
      Intent next = new Intent();
      next.setClass(DownloadActivity.this, DetailActivity.class);
      next.putExtra("org.jared.synodroid.ds.Details", (Task) msg.obj);
      DownloadActivity.this.startActivity(next);
    }
    // Shared directories have been retrieved
    else if (msg.what == ResponseHandler.MSG_SHARED_DIRECTORIES_RETRIEVED) {
      List<SharedDirectory> newDirs = (List<SharedDirectory>) msg.obj;
      final String[] dirNames = new String[newDirs.size()];
      int selected = -1;
      for (int iLoop = 0; iLoop < newDirs.size(); iLoop++) {
        SharedDirectory sharedDir = newDirs.get(iLoop);
        dirNames[iLoop] = sharedDir.name;
        if (sharedDir.isCurrent) {
          selected = iLoop;
        }
      }
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.shared_dir_title));
      builder.setSingleChoiceItems(dirNames, selected, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          dialog.dismiss();
          Synodroid app = (Synodroid) getApplication();
          app.executeAsynchronousAction(DownloadActivity.this, new SetShared(null, dirNames[item]), true);
        }
      });

      AlertDialog alert = builder.create();
      alert.show();
    }

  }

  /**
   * Activity creation
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {

    licenceAccepted = Eula.show(this, false);

    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    // Retrieve the listview
    RelativeLayout downloadContent = (RelativeLayout) inflater.inflate(R.layout.download_list, null, false);
    taskView = (ListView) downloadContent.findViewById(R.id.id_task_list);
    totalUpView = (TextView) downloadContent.findViewById(R.id.id_total_upload);
    totalDownView = (TextView) downloadContent.findViewById(R.id.id_total_download);

    // Create the tab maanger
    tabManager = new TabWidgetManager(this, R.drawable.ic_tab_slider);
    Tab torrentTab = new Tab(TAB_TASKS, R.drawable.ic_tab_download, R.drawable.ic_tab_download_selected);
    torrentTab.setLogo(R.drawable.download_logo, R.string.logo_download);
    tabManager.addTab(torrentTab, downloadContent);
    Tab emuleTab = new Tab(TAB_EMULE, R.drawable.ic_tab_emule, R.drawable.ic_tab_emule_selected);
    emuleTab.setLogo(R.drawable.emule_logo, R.string.logo_emule);
    tabManager.addTab(emuleTab, null);
    Tab aboutTab = new Tab(TAB_ABOUT, R.drawable.ic_tab_about, R.drawable.ic_tab_about_selected);
    aboutTab.setLogo(R.drawable.about_logo, R.string.logo_about);
    View about = inflater.inflate(R.layout.about, null, false);
    Button eulaBtn = (Button) about.findViewById(R.id.id_eula_view);
    eulaBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // Diplay the EULA
        Eula.show(DownloadActivity.this, true);
      }
    });
    TextView message = (TextView) about.findViewById(R.id.about_code);
    message.setText(Html
            .fromHtml("<a href=\"http://code.google.com/p/synodroid-ds/\">http://code.google.com/p/synodroid-ds/</a>"));
    message.setMovementMethod(LinkMovementMethod.getInstance());
    tabManager.addTab(aboutTab, about);

    tabManager.setTabListener(this);

    super.onCreate(savedInstanceState);

    // Retrieve title's text, icon and progress for future uses
    titleText = (TextView) findViewById(R.id.id_title);
    titleIcon = (ImageView) findViewById(R.id.id_https);

    // Create the task adapter
    TaskAdapter taskAdapter = new TaskAdapter(this);
    taskView.setAdapter(taskAdapter);
    taskView.setOnItemClickListener(taskAdapter);
    taskView.setOnItemLongClickListener(taskAdapter);

	Intent intent = getIntent();
	String action = intent.getAction();
	// Show the dialog only if the intent's action is not to view a
	// content -> add a new file
	if (action != null && (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND))) {
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0){	
			handleIntent(intent);
		}
	}
    

    // The user is able to click on the title bar to connect to a server
    setTitleClickListener(this);
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.ui.TitleClicklistener#onTitleClicked(android .view.View)
   */
  public void onTitleClicked(View viewP) {
    showDialogToConnect(false, null);
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.ui.SynodroidActivity#getMainContentView()
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
	if ((intentP.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0){
		Log.d(Synodroid.DS_TAG, "New intent: " + intentP);
	    handleIntent(intentP);	
	}
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
        if (uriString == null) {
          return;
        }
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
        dialog.setCancelable(false);
        ((ProgressDialog) dialog).setMessage(getString(R.string.connect_connecting2));
        ((ProgressDialog) dialog).setIndeterminate(true);
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
            final SharedPreferences preferences = getSharedPreferences(PREFERENCE_AUTO, Activity.MODE_PRIVATE);
            preferences.edit().putBoolean(PREFERENCE_AUTO_CREATENOW, true).commit();
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
   * Create the option menu of this activity
   */
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, MENU_PARAMETERS, 0, getString(R.string.menu_parameter)).setIcon(android.R.drawable.ic_menu_preferences);
    menu.add(0, MENU_CLEAR, 0, getString(R.string.menu_clearall))
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    menu.add(0, MENU_DESTINATION, 0, getString(R.string.menu_destination)).setIcon(android.R.drawable.ic_menu_share);
    return true;
  }

  /**
   * Interact with the user when a menu is selected
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    Synodroid app = (Synodroid) getApplication();
    switch (item.getItemId()) {
      case MENU_CLEAR:
        app.executeAction(DownloadActivity.this, new ClearAllTaskAction(), false);
        return true;
      case MENU_PARAMETERS:
        showPreferenceActivity();
        return true;
      case MENU_DESTINATION:
        app.executeAsynchronousAction(this, new EnumShareAction(), false);
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
          connectDialogOpened = true;
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
              ((Synodroid) getApplication()).connectServer(DownloadActivity.this, server, actionQueueP);
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
        }
        else {
          // Auto connect to the first server
          if (servers.size() > 0) {
            server = servers.get(0);
            // Change the server
            ((Synodroid) getApplication()).connectServer(DownloadActivity.this, server, actionQueueP);
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
    super.onResume();
    // There are some case where the connected server does not show up in
    // the title bar on top. This fixes thoses cases.
    server = ((Synodroid) getApplication()).getServer();
    if (server != null && server.isConnected()) {
      titleText.setText(server.getNickname());
      titleIcon.setVisibility(server.getProtocol() == SynoProtocol.HTTPS ? View.VISIBLE : View.GONE);
      
      // Launch the gets task's details recurrent action
      Synodroid app = (Synodroid) getApplication();
      app.setRecurrentAction(this, new GetAllTaskAction(server.getSortAttribute(), server.isAscending()));
      app.resumeServer();
    }
    // No server then display the connection dialog
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
    Synodroid app = (Synodroid) getApplication();
    app.executeAction(DownloadActivity.this, new ShowDetailsAction(taskP), true);
  }

  /**
   * A task as been long clicked by the user
   * 
   * @param taskP
   */
  public void onTaskLongClicked(final Task taskP) {
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
   * The Eula has just been accepted
   */
  public void onEulaAgreedTo() {
    licenceAccepted = true;
    showDialogToConnect(true, null);
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.ui.TabListener#selectedTabChanged(java.lang. String, java.lang.String)
   */
  public void selectedTabChanged(String oldTabIdP, String newTabIdP) {
    if (newTabIdP != null && newTabIdP.equals(TAB_TASKS)) {
      ((Synodroid) getApplication()).forceRefresh();
    }
    cur_tab = newTabIdP;
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    // Save UI state changes to the savedInstanceState.
    // This bundle will be passed to onCreate if the process is
    // killed and restarted.
    savedInstanceState.putString("tabID", cur_tab);
    // etc.
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    // Restore UI state from the savedInstanceState.
    // This bundle has also been passed to onCreate.
    cur_tab = savedInstanceState.getString("tabID");
    tabManager.slideTo(cur_tab);
  }
}