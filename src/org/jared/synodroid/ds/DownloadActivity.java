/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.ds;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.jared.synodroid.Synodroid;
import org.jared.synodroid.common.Eula;
import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.action.AddTaskAction;
import org.jared.synodroid.common.action.ClearAllTaskAction;
import org.jared.synodroid.common.action.EnumShareAction;
import org.jared.synodroid.common.action.GetAllAndOneDetailTaskAction;
import org.jared.synodroid.common.action.ResumeAllAction;
import org.jared.synodroid.common.action.SetShared;
import org.jared.synodroid.common.action.StopAllAction;
import org.jared.synodroid.common.action.SynoAction;
import org.jared.synodroid.common.data.SharedDirectory;
import org.jared.synodroid.common.data.SynoProtocol;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskContainer;
import org.jared.synodroid.common.data.TaskDetail;
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
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * This activity list all current tasks
 * 
 * @author eric.taix at gmail.com
 */
public class DownloadActivity extends SynodroidActivity implements Eula.OnEulaAgreedTo, TitleClicklistener, TabListener {

	private static final String TAB_ABOUT = "ABOUT";
	private static final String TAB_SEARCH = "SEARCH";
	private static final String TAB_TASKS = "TASKS";
	private static final String PREFERENCE_AUTO = "auto";
	private static final String PREFERENCE_AUTO_CREATENOW = "auto.createnow";
	private static final String PREFERENCE_FULLSCREEN = "general_cat.fullscreen";
	private static final String PREFERENCE_GENERAL = "general_cat";
	private static final String PREFERENCE_SEARCH_SOURCE = "general_cat.search_source";
    private static final String PREFERENCE_SEARCH_ORDER = "general_cat.search_order";
	

	// The connection dialog ID
	private static final int CONNECTION_DIALOG_ID = 1;
	// No server configured
	private static final int NO_SERVER_DIALOG_ID = 2;

	// Menu parameters
	public static final int MENU_PARAMETERS = 1;
	// Menu Clear All
	public static final int MENU_CLEAR_ALL = 2;
	// Menu Shared directory
	public static final int MENU_DESTINATION = 3;
	// Menu Pause All
	public static final int MENU_PAUSE_ALL = 4;
	// Menu Resume All
	public static final int MENU_RESUME_ALL = 5;

	// The torrent listview
	private ListView taskView, resList;
	// The total upload rate view
	private TextView totalUpView;
	// The total download rate view
	private TextView totalDownView;
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

	private final String[] from = new String[] { "NAME", "SIZE", "ADDED", "LEECHERS", "SEEDERS", "TORRENTURL" };
	private final int[] to = new int[] { R.id.result_title, R.id.result_size, R.id.result_date, R.id.result_leechers, R.id.result_seeds, R.id.result_url };

	private TextView emptyText;
	private ScrollView sv;
	private Button btnInstall;

	private Spinner SpinnerSource, SpinnerSort;
	private ArrayAdapter<CharSequence> AdapterSource, AdapterSort;

	private String[] SortOrder = { "Combined", "BySeeders" };
	private String lastSearch = "";
	private Tab searchTab, torrentTab, aboutTab;
	private int curTabId = 0;
	private boolean tabsNeedInit = false;
	public boolean alreadyCanceled = false;
	private boolean slide = false;

	@Override
	public boolean onSearchRequested(){
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		return super.onSearchRequested();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//ignore orientation change
		super.onConfigurationChanged(newConfig);
	}
	
	/**
	 * Handle the message
	 */
	@SuppressWarnings("unchecked")
	public void handleMessage(Message msg) {
		// Update tasks
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
			catch (IllegalArgumentException ex) {
			}
			// Update total rates
			totalUpView.setText(container.getTotalUp());
			totalDownView.setText(container.getTotalDown());
		}
		// Update a task's detail
		else if (msg.what == ResponseHandler.MSG_DETAILS_RETRIEVED) {
			TaskDetail details = (TaskDetail) msg.obj;
			// Get the adapter
			TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
			taskAdapter.updateFromDetail(details);
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
			catch (IllegalArgumentException ex) {
			}
			// Show the error
			// Save the last error inside the server to surive UI rotation and
			// pause/resume.
			final SynoServer server = ((Synodroid) getApplication()).getServer();
			if (server != null) {
				server.setLastError((String) msg.obj);
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
		}
		// Connection is done
		else if (msg.what == ResponseHandler.MSG_CONNECTED) {
			final SynoServer server = ((Synodroid) getApplication()).getServer();
			// Change the title
			String title = server.getNickname();
			if (server.getConnection()==server.getPublicConnection()) {
				title += " (P)";
			}
			titleText.setText(title);
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
			// Starting new intent
			Intent next = new Intent();
			next.setClass(DownloadActivity.this, DetailActivity.class);
			next.putExtra("org.jared.synodroid.ds.Details", (Task) msg.obj);
			startActivityForResult(next, 0);
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
	 * This method is called when a sub activity exits.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				TaskDetail details = (TaskDetail) data.getSerializableExtra("org.jared.synodroid.ds.Details");
				// Get the adapter
				TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
				taskAdapter.updateFromDetail(details);
			}
		}
	}

	private void initTorrentTab(LayoutInflater inflater) {
		RelativeLayout downloadContent = (RelativeLayout) inflater.inflate(R.layout.download_list, null, false);
		taskView = (ListView) downloadContent.findViewById(R.id.id_task_list);
		totalUpView = (TextView) downloadContent.findViewById(R.id.id_total_upload);
		totalDownView = (TextView) downloadContent.findViewById(R.id.id_total_download);
		tabManager.addTab(torrentTab, downloadContent);
		// Create the task adapter
		TaskAdapter taskAdapter = new TaskAdapter(this);
		taskView.setAdapter(taskAdapter);
		taskView.setOnItemClickListener(taskAdapter);
		taskView.setOnItemLongClickListener(taskAdapter);
		// taskView.setOnClickListener(DownloadActivity.this);
		taskView.setOnTouchListener(gestureListener);
	}

	private void initAboutTab(LayoutInflater inflater) {
		View about = inflater.inflate(R.layout.about, null, false);
		Button eulaBtn = (Button) about.findViewById(R.id.id_eula_view);
		eulaBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Diplay the EULA
				Eula.show(DownloadActivity.this, true);
			}
		});

		Button helpBtn = (Button) about.findViewById(R.id.id_help);
		helpBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try{
					final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"synodroid@gmail.com"});
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Synodroid help");
					startActivity(emailIntent);
				}
				catch (Exception e){
					AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.this);
				    builder.setMessage(R.string.err_noemail);
				    builder.setTitle(getString(R.string.connect_error_title)).setCancelable(false).setPositiveButton("OK",
				            new DialogInterface.OnClickListener() {
				              public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				              }
				            });
				    AlertDialog errorDialog = builder.create();
					errorDialog.show();
				}
			}
		});
		
		String vn = "" + getString(R.string.app_name);
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			if (pi != null) {
				vn += " " + pi.versionName;
			}
		}
		catch (Exception e) {
			Log.e(Synodroid.DS_TAG, "Error while retrieving package information", e);
		}
		TextView vname = (TextView) about.findViewById(R.id.app_vers_name_text);
		vname.setText(vn);

		TextView message = (TextView) about.findViewById(R.id.about_code);
		message.setText(Html.fromHtml("<a href=\"http://code.google.com/p/synodroid-ds/\">http://code.google.com/p/synodroid-ds/</a>"));
		message.setMovementMethod(LinkMovementMethod.getInstance());

		ImageView donate = (ImageView) about.findViewById(R.id.ImgViewDonate);
		donate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent("android.intent.action.VIEW", Uri
				    .parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=ABCSFVFDRJEFS&lc=CA&item_name=Synodroid&item_number=synodroid%2dmarket&currency_code=CAD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted"));
				startActivity(i);
			}
		});

		tabManager.addTab(aboutTab, about);
		about.findViewById(R.id.about_scroll).setOnTouchListener(gestureListener);
	}

    private void initSearchTab(LayoutInflater inflater){
		RelativeLayout searchContent = (RelativeLayout) inflater.inflate(R.layout.torrent_search, null, false);
        resList = (ListView)searchContent.findViewById(R.id.resList);
        
        emptyText = (TextView)searchContent.findViewById(R.id.empty);
        
        btnInstall = (Button)searchContent.findViewById(R.id.btnTorSearchInst);
        sv = (ScrollView)searchContent.findViewById(R.id.empty_scroll);
        
        SpinnerSource = (Spinner) searchContent.findViewById(R.id.srcSpinner); 
        SpinnerSort = (Spinner) searchContent.findViewById(R.id.sortSpinner); 

        AdapterSource = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item); 
        AdapterSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        SpinnerSource.setAdapter(AdapterSource); 
        
        AdapterSort = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item); 
        AdapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        SpinnerSort.setAdapter(AdapterSort); 

        SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
        String pref_src = preferences.getString(PREFERENCE_SEARCH_SOURCE, "");
        String pref_order = preferences.getString(PREFERENCE_SEARCH_ORDER, "");
        
        int lastOrder  = 0;
        int lastSource = 0;
        
        for (int i = 0; i < SortOrder.length; i++) {
        	if (pref_order.equals(SortOrder[i])){
        		lastOrder = i;
        	}
        	AdapterSort.add(SortOrder[i]); 
        } 
        
        // Gather the supported torrent sites
        StringBuilder s = new StringBuilder();
        Cursor sites = getSupportedSites();
        if (sites != null) {
	        if (sites.moveToFirst()) {
	        	int i = 0;
		        do {
		        	s.append(sites.getString(1));
		        	s.append("\n");
		        	if (pref_src.equals(sites.getString(1))){
		        		lastSource = i;
		        	}
		        	AdapterSource.add(sites.getString(1));
		        	i++;
		        } while (sites.moveToNext());
	        }
	        emptyText.setText(getString(R.string.sites) + "\n" + s.toString());
	        btnInstall.setVisibility(Button.GONE);
	        resList.setVisibility(ListView.GONE);
	        
        } else {
        	SpinnerSort.setVisibility(Spinner.GONE);
        	SpinnerSource.setVisibility(Spinner.GONE);
        	resList.setVisibility(ListView.GONE);
        	emptyText.setText(R.string.provider_missing);
        	btnInstall.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					Intent goToMarket = null;
					goToMarket = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=org.transdroid.search"));
					try{
						startActivity(goToMarket);	
					}
					catch (Exception e){
						AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.this);
					    // By default the message is "Error Unknown"
					    builder.setMessage(R.string.err_nomarket);
					    builder.setTitle(getString(R.string.connect_error_title)).setCancelable(false).setPositiveButton("OK",
					            new DialogInterface.OnClickListener() {
					              public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					              }
					            });
					    AlertDialog errorDialog = builder.create();
						errorDialog.show();
					}
					
				}
			});
        }
        tabManager.addTab(searchTab, searchContent);
        sv.setOnTouchListener(gestureListener);
        resList.setOnTouchListener(gestureListener);
        
        SpinnerSource.setOnItemSelectedListener(new OnItemSelectedListener(){
        	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		String source = ((TextView) arg1).getText().toString();
				SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
        		preferences.edit().putString(PREFERENCE_SEARCH_SOURCE, source).commit();
        		if (!lastSearch.equals("")){
        			new TorrentSearchTask().execute(lastSearch);
        		}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
        });
        SpinnerSort.setOnItemSelectedListener(new OnItemSelectedListener(){
        	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		String order = ((TextView) arg1).getText().toString();
				SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
        		preferences.edit().putString(PREFERENCE_SEARCH_ORDER, order).commit();
        		if (!lastSearch.equals("")){
        			new TorrentSearchTask().execute(lastSearch);
        		}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
        });
        
        SpinnerSource.setSelection(lastSource);
        SpinnerSort.setSelection(lastOrder);
        resList.setOnItemClickListener(new OnItemClickListener() {			
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final RelativeLayout rl = (RelativeLayout) arg1;
				Dialog d = new AlertDialog.Builder(DownloadActivity.this).setTitle(R.string.dialog_title_confirm).setMessage(
						R.string.dialog_message_confirm_add).setNegativeButton(android.R.string.no, null).setPositiveButton(
		                android.R.string.yes, new DialogInterface.OnClickListener() {
		                	public void onClick(DialogInterface dialog, int which) {
		                		TextView tv = (TextView)rl.findViewById(R.id.result_url);
								String url = tv.getText().toString();
								new TorrentDownloadAndAdd().execute(url);
		                	}
		                }).create();
		        // d.setOwnerActivity(this); // why can't the builder do this?
		        d.show();
			}
		});
	}

	/**
	 * Activity creation
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		licenceAccepted = Eula.show(this, false);
		tabsNeedInit = true;

		// Create the tab manager
		tabManager = new TabWidgetManager(this, R.drawable.ic_tab_slider);

		// Download Tab
		torrentTab = new Tab(TAB_TASKS, R.drawable.ic_tab_download, R.drawable.ic_tab_download_selected);
		torrentTab.setLogo(R.drawable.download_logo, R.string.logo_download);

		// Torrent Search Tab
		searchTab = new Tab(TAB_SEARCH, R.drawable.ic_tab_search, R.drawable.ic_tab_search_selected);
		searchTab.setLogo(R.drawable.search_logo, R.string.logo_search);

		// About Tab
		aboutTab = new Tab(TAB_ABOUT, R.drawable.ic_tab_about, R.drawable.ic_tab_about_selected);
		aboutTab.setLogo(R.drawable.about_logo, R.string.logo_about);

		tabManager.setTabListener(this);
		super.setTabmanager(tabManager);

		super.onCreate(savedInstanceState);

		// Retrieve title's text, icon and progress for future uses
		titleText = (TextView) findViewById(R.id.id_title);
		titleIcon = (ImageView) findViewById(R.id.id_https);

		// The user is able to click on the title bar to connect to a server
		setTitleClickListener(this);
	}

	private Cursor getSupportedSites() {
		// Create the URI of the TorrentSitesProvider
		String uriString = "content://org.transdroid.search.torrentsitesprovider/sites";
		Uri uri = Uri.parse(uriString);
		// Then query all torrent sites (no selection nor projection nor sort):
		return managedQuery(uri, null, null, null, null);
	}

	private class TorrentDownloadAndAdd extends AsyncTask<String, Void, Uri>{

		@Override
		protected Uri doInBackground(String... params) {
			Uri uri = Uri.parse(params[0]);
			return fixUri(uri);
		}
	
		@Override
		protected void onPostExecute(Uri uri) {
			boolean out_url = false;
	        if (!uri.toString().startsWith("file:")){
	        	out_url = true;
	        }
	        AddTaskAction addTask = new AddTaskAction(uri, out_url);
			Synodroid app = (Synodroid) getApplication();
			app.executeAction(DownloadActivity.this, addTask, true);
		}
	}

	private class TorrentSearchTask extends AsyncTask<String, Void, Cursor> {

		@Override
		protected void onPreExecute() {
			emptyText.setVisibility(TextView.VISIBLE);
        	emptyText.setText(getString(R.string.searching) + " " + lastSearch);
        	resList.setVisibility(ListView.GONE);
        	resList.setAdapter(null);
		}
		
		@Override
		protected Cursor doInBackground(String... params) {
			// Create the URI of the TorrentProvider
	        String uriString = "content://org.transdroid.search.torrentsearchprovider/search/"+params[0];
	        Uri uri = Uri.parse(uriString);
	        // Then query for this specific record (no selection nor projection nor sort):
	        SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
	        String pref_src = preferences.getString(PREFERENCE_SEARCH_SOURCE, "");
	        String pref_order = preferences.getString(PREFERENCE_SEARCH_ORDER, "");
	        
	        return managedQuery(uri, null, "SITE = ?", 
	        		new String[] { pref_src }, pref_order);
		}
		
		@Override
		protected void onPostExecute(Cursor cur) {

	        if (cur == null) {
	        	SpinnerSort.setVisibility(Spinner.GONE);
	        	SpinnerSource.setVisibility(Spinner.GONE);
	        	resList.setVisibility(ListView.GONE);
	        	emptyText.setText(R.string.provider_missing);
	        	btnInstall.setOnClickListener(new View.OnClickListener() {			
					public void onClick(View v) {
						Intent goToMarket = null;
						goToMarket = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=org.transdroid.search"));
						startActivity(goToMarket);
					}
				});
	        }
	        
	        // Show results in the list
	        if (cur.getCount() == 0){
	        	emptyText.setVisibility(TextView.VISIBLE);
	        	resList.setVisibility(ListView.GONE);
	        	emptyText.setText(getString(R.string.no_results) + " " + lastSearch);
	        }
	        else{
	        	emptyText.setVisibility(TextView.GONE);
	        	resList.setVisibility(ListView.VISIBLE);
	        	resList.setAdapter(new SimpleCursorAdapter(DownloadActivity.this, R.layout.search_row, cur, from, to));	
	        }
		}
		
    }
		public TaskAdapter getTaskAdapter() {
		TaskAdapter result = null;
		if (taskView != null) {
			result = (TaskAdapter) taskView.getAdapter();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.ui.TitleClicklistener#onTitleClicked(android
	 * .view.View)
	 */
	public void onTitleClicked(View viewP) {
		showDialogToConnect(false, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.ui.SynodroidActivity#getMainContentView()
	 */
	@Override
	public void attachMainContentView(LayoutInflater inflaterP, ViewGroup parentP) {
		parentP.addView(tabManager.getContentView());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.ui.SynodroidActivity#getStatusView()
	 */
	@Override
	public void attachStatusView(LayoutInflater inflaterP, ViewGroup parentP) {
		parentP.addView(tabManager.getTabView());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.ui.SynodroidActivity#getTitleView()
	 */
	@Override
	public void attachTitleView(LayoutInflater inflaterP, ViewGroup parentP) {
		inflaterP.inflate(R.layout.download_title, parentP, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		// Time consuming: DO NOT COMMIT !!!
		// Debug.startMethodTracing("synodroid");
	}

	private Uri fixUri(Uri uri) {
		try {
			URL url = new URL(uri.toString()); // you can write here any link
			File path = Environment.getExternalStorageDirectory();
			path = new File(path, "data/org.jared.synodroid/");
			path.mkdirs();
			String temp[] = uri.toString().split("/");
			String fname = temp[(temp.length) - 1];
			if (!fname.endsWith(".torrent")) {
				fname += ".torrent";
			}
			File file = new File(path, fname);

			long startTime = System.currentTimeMillis();
			Log.d(Synodroid.DS_TAG, "Downloading " + uri.toString() + " to temp folder...");
			Log.d(Synodroid.DS_TAG, "Temp file destination: " + file.getAbsolutePath());
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
			Log.d(Synodroid.DS_TAG, "Download completed. Elapsed time: " + ((System.currentTimeMillis() - startTime) / 1000) + " sec(s)");
			uri = Uri.fromFile(file);
		}
		catch (IOException e) {
			Log.d(Synodroid.DS_TAG, "Download Error: " + e);
			Log.d(Synodroid.DS_TAG, "Letting the NAS do the heavy lifting...");
		}
		return uri;
	}

	/**
	 * Handle all new intent
	 * 
	 * @param intentP
	 */
	private boolean handleIntent(Intent intentP) {
		String action = intentP.getAction();
		Log.d(Synodroid.DS_TAG, "New intent: " + intentP);
		if (action != null) {
			Uri uri = null;
			boolean out_url = false;
			if (action.equals(Intent.ACTION_VIEW)) {
				uri = intentP.getData();
				if (uri.toString().startsWith("http") || uri.toString().startsWith("ftp")) {
					/** Download and fix URL */
					new TorrentDownloadAndAdd().execute(uri.toString());
					return false;
				}
			}
			else if (action.equals(Intent.ACTION_SEND)) {
				String uriString = (String) intentP.getExtras().get(Intent.EXTRA_TEXT);
				if (uriString == null) {
					return true;
				}
				uri = Uri.parse(uriString);
				out_url = true;
				
				AddTaskAction addTask = new AddTaskAction(uri, out_url);
				Synodroid app = (Synodroid) getApplication();
				app.executeAction(this, addTask, true);
			}
			else {
				return true;
			}
		}
		return true;
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
					okToCreateAServer();
				}
			});
			builderNoServer.setNegativeButton(getString(R.string.button_nothanks), new OnClickListener() {
				// Launch the Preference activity
				public void onClick(DialogInterface dialogP, int whichP) {
					alreadyCanceled = true;
				}
			});
			dialog = builderNoServer.create();
			break;

		}
		return dialog;
	}

	/**
	 * Create the option menu of this activity
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_PAUSE_ALL, 0, getString(R.string.menu_pauseall)).setIcon(R.drawable.ic_menu_pause);
		menu.add(0, MENU_RESUME_ALL, 0, getString(R.string.menu_resumeall)).setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0, MENU_CLEAR_ALL, 0, getString(R.string.menu_clearall)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_DESTINATION, 0, getString(R.string.menu_destination)).setIcon(android.R.drawable.ic_menu_share);
		menu.add(0, MENU_PARAMETERS, 0, getString(R.string.menu_parameter)).setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}

	/**
	 * Interact with the user when a menu is selected
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		TaskAdapter taskAdapter = (TaskAdapter) taskView.getAdapter();
		List<Task> tasks = taskAdapter.getTaskList();
		Synodroid app = (Synodroid) getApplication();
		switch (item.getItemId()) {
		case MENU_CLEAR_ALL:
			app.executeAction(DownloadActivity.this, new ClearAllTaskAction(), false);
			return true;
		case MENU_PAUSE_ALL:
			app.executeAction(DownloadActivity.this, new StopAllAction(tasks), false);
			return true;
		case MENU_RESUME_ALL:
			app.executeAction(DownloadActivity.this, new ResumeAllAction(tasks), false);
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
			final ArrayList<SynoServer> servers = PreferenceFacade.loadServers(this, PreferenceManager.getDefaultSharedPreferences(this));
			// If at least one server
			if (servers.size() != 0) {
				// If more than 1 server OR if we don't want to autoconnect then
				// show the dialog
				if (servers.size() > 1 || !autoConnectIfOnlyOneServerP) {
					connectDialogOpened = true;
					String[] serversTitle = new String[servers.size()];
					for (int iLoop = 0; iLoop < servers.size(); iLoop++) {
						SynoServer s = servers.get(iLoop);
						serversTitle[iLoop] = s.getNickname();
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(getString(R.string.menu_connect));
					// When the user select a server
					builder.setItems(serversTitle, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							SynoServer server = servers.get(item);
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
						SynoServer server = servers.get(0);
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
				if (licenceAccepted && !alreadyCanceled) {
					showDialog(NO_SERVER_DIALOG_ID);
				}
			}
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
		((Synodroid) getApplication()).pauseServer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intentP) {
		super.onNewIntent(intentP);
		setIntent(intentP);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);

	    if (hasFocus){
	    	SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
			if (preferences.getBoolean(PREFERENCE_FULLSCREEN, false)){
				//Set fullscreen or not
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  	
			}
			else{
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			
	    }   
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
    
	    /**
	     * Intents are driving me insane.
	     * 
	     * When an intent has been handle by the app I mark the flag activity launched from history on 
	     * so we do not reprocess that intent again. This simplify was more how I was handling intents 
	     * before and is effective in every cases in all android 1.5 up versions...
	     * 
	     * */
		//Init tabmanager tabs
		if (tabsNeedInit){
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			initTorrentTab(inflater);
			initSearchTab(inflater);
	        initAboutTab(inflater);	
	        tabsNeedInit = false;
		}
		if (slide) {
			tabManager.selectTab(tabManager.getNameAtId(curTabId));
			slide = false;
		}
        
		boolean connectToServer = true;
	    //Get the current main intent
	    Intent intent = getIntent();	
	    String action = intent.getAction();
	    //Check if it is a actionable Intent
		if (action != null && (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND))) {
			//REUSE INTENT CHECK: check if the intent is comming out of the history.
			if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0){
				//Not from history -> process intent
				connectToServer = handleIntent(intent);
			}
		}
		else if (Intent.ACTION_SEARCH.equals(action)){
			if (getSupportedSites() != null){
				if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0){
					tabManager.selectTab(TAB_SEARCH);
				}
				String searchKeywords = intent.getStringExtra(SearchManager.QUERY);
				lastSearch = searchKeywords;
				if (!searchKeywords.equals("")) {
					new TorrentSearchTask().execute(searchKeywords);
					SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
							SynodroidSearchSuggestion.AUTHORITY, SynodroidSearchSuggestion.MODE);
			        suggestions.saveRecentQuery(searchKeywords, null);
				}
				else {
					emptyText.setText(R.string.no_keyword);
					emptyText.setVisibility(TextView.VISIBLE);
					resList.setVisibility(TextView.GONE);
				}
			}
			else{
				tabManager.selectTab(TAB_SEARCH);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.this);
			    builder.setMessage(R.string.err_provider_missing);
			    builder.setTitle(getString(R.string.connect_error_title)).setCancelable(false).setPositiveButton("OK",
			            new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			              }
			            });
			    AlertDialog errorDialog = builder.create();
				errorDialog.show();
			}
			
			
		}

		// PREVENT INTENT REUSE: We mark the intent so from now on, the program
		// thinks its from history
		intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		setIntent(intent);
		
	    // There are some case where the connected server does not show up in
		// the title bar on top. This fixes thoses cases.
		SynoServer server = ((Synodroid) getApplication()).getServer();
		if (server != null && server.isConnected()) {
			String title = server.getNickname();
			if (server.getConnection()==server.getPublicConnection()) {
				title += " (P)";
			}
			titleText.setText(title);
			titleIcon.setVisibility(server.getProtocol() == SynoProtocol.HTTPS ? View.VISIBLE : View.GONE);

			// Launch the gets task's details recurrent action
			Synodroid app = (Synodroid) getApplication();
			app.setRecurrentAction(this, new GetAllAndOneDetailTaskAction(server.getSortAttribute(), server.isAscending(), (TaskAdapter) taskView.getAdapter()));

			app.resumeServer();
		}
		// No server then display the connection dialog
		else {
			if (connectToServer) showDialogToConnect(true, null);
		}
	}

	/**
	 * A task as been clicked by the user
	 * 
	 * @param taskP
	 */
	public void onTaskClicked(final Task taskP) {
		if (tabManager.getSlideToTabName().equals(TAB_TASKS)) {
			Synodroid app = (Synodroid) getApplication();
			app.executeAction(DownloadActivity.this, new ShowDetailsAction(taskP), true);
		}
	}

	/**
	 * A task as been long clicked by the user
	 * 
	 * @param taskP
	 */
	public void onTaskLongClicked(final Task taskP) {
		if (tabManager.getSlideToTabName().equals(TAB_TASKS)) {
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
	 * 
	 * @see
	 * org.jared.synodroid.common.ui.TabListener#selectedTabChanged(java.lang.
	 * String, java.lang.String)
	 */
	public void selectedTabChanged(String oldTabIdP, String newTabIdP) {
		if (newTabIdP != null && newTabIdP.equals(TAB_TASKS)) {
			((Synodroid) getApplication()).forceRefresh();
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putInt("tabID", tabManager.getCurrentTabIndex());
		savedInstanceState.putString("lastSearch", lastSearch);
		savedInstanceState.putBoolean("alreadyCanceled", alreadyCanceled);

		// etc.
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		curTabId = savedInstanceState.getInt("tabID");
		lastSearch = savedInstanceState.getString("lastSearch");
		alreadyCanceled = savedInstanceState.getBoolean("alreadyCanceled");
		slide = true;
	}

	/**
	 * The user agree to create a new as no server has been configured or no
	 * server is suitable for the current connection
	 */
	private void okToCreateAServer() {
		final SharedPreferences preferences = getSharedPreferences(PREFERENCE_AUTO, Activity.MODE_PRIVATE);
		preferences.edit().putBoolean(PREFERENCE_AUTO_CREATENOW, true).commit();
		showPreferenceActivity();
	}
}
