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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.jared.synodroid.Synodroid;
import org.jared.synodroid.common.data.DSMVersion;
import org.jared.synodroid.common.data.SynoProtocol;
import org.jared.synodroid.common.data.TaskSort;
import org.jared.synodroid.common.preference.EditTextPreferenceWithValue;
import org.jared.synodroid.common.preference.ListPreferenceWithValue;
import org.jared.synodroid.common.preference.PreferenceFacade;
import org.jared.synodroid.common.preference.PreferenceProcessor;
import org.jared.synodroid.ds.view.wizard.ServerWizard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;

/**
 * The preference activity
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class DownloadPreferenceActivity extends PreferenceActivity implements PreferenceProcessor {

	// Menu Create server
	public static final int MENU_CREATE = 1;
	// Menu Delete
	public static final int MENU_DELETE = 2;
	// Menu Wizard
	public static final int MENU_WIZARD = 3;

	private static final String PREFERENCE_AUTO = "auto";
	private static final String PREFERENCE_AUTO_CREATENOW = "auto.createnow";
	private static final String PREFERENCE_FULLSCREEN = "general_cat.fullscreen";
	private static final String PREFERENCE_GENERAL = "general_cat";
	// Store the current max server id
	private int maxServerId = 0;
	// The dynamic servers category
	private PreferenceCategory serversCategory;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Create the UI
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Add the preference screen
		addPreferencesFromResource(R.xml.preference);

		// Retreive the preference screen
		PreferenceScreen prefScreen = getPreferenceScreen();
		// The general category
		PreferenceCategory generalCategory = (PreferenceCategory) prefScreen.getPreferenceManager().findPreference(PREFERENCE_GENERAL);
		final ListPreferenceWithValue orderPref = ListPreferenceWithValue.create(this, "sort", R.string.label_process_sort, R.string.hint_process_sort, null);
		orderPref.setOrder(0);
		generalCategory.addPreference(orderPref);
		// Build the sort list
		String[] sortLabels = new String[TaskSort.values().length];
		String[] sortValues = new String[TaskSort.values().length];
		for (int iLoop = 0; iLoop < TaskSort.values().length; iLoop++) {
			sortLabels[iLoop] = getString(TaskSort.values()[iLoop].getResId());
			sortValues[iLoop] = TaskSort.values()[iLoop].name();
		}
		orderPref.setEntries(sortLabels);
		orderPref.setEntryValues(sortValues);
		// Strange behaviour: I was unable to create the CheckBoxPreference at
		// runtime. Well it ran and the state was correclty saved, but the checkbox
		// was unable to reflect (checked, unchecked) to correct state. So I decided
		// to create the CheckBoxPreference in the XML layout and use it at runtime
		// rather than create it at runtime
		final CheckBoxPreference asc = (CheckBoxPreference) generalCategory.findPreference("asc");
		// Set listeners to update the server sort
		final Synodroid app = (Synodroid) getApplication();
		orderPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				app.setServerSort((String) newValue, asc.isChecked());
				return true;
			}
		});
		asc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				app.setServerSort(orderPref.getCurrentValue(), (Boolean) newValue);
				return true;
			}
		});

		// Fullscreen preference
		final CheckBoxPreference fullPref = new CheckBoxPreference(this);
		fullPref.setKey(PREFERENCE_FULLSCREEN);
		fullPref.setTitle(R.string.fullscreen_preference);
		fullPref.setSummary(R.string.summary_fullscreen_preference);
		generalCategory.addPreference(fullPref);
		fullPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
				if (newValue.toString().equals("true")) {
					preferences.edit().putBoolean(PREFERENCE_FULLSCREEN, true).commit();
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
				else {
					preferences.edit().putBoolean(PREFERENCE_FULLSCREEN, false).commit();
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
				return true;
			}
		});
		// The dynamic servers category
		serversCategory = (PreferenceCategory) prefScreen.getPreferenceManager().findPreference("servers_cat");
		// Load currents servers
		reloadCurrentServers();
	}

	private void reloadCurrentServers() {
		// Load current servers
		serversCategory.removeAll();
		maxServerId = 0;
		PreferenceFacade.processLoadingServers(getPreferenceScreen().getSharedPreferences(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// Check for fullscreen
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
		if (preferences.getBoolean(PREFERENCE_FULLSCREEN, false)) {
			// Set fullscreen or not
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			SharedPreferences preferences = getSharedPreferences(PREFERENCE_AUTO, Activity.MODE_PRIVATE);
			if (preferences.getBoolean(PREFERENCE_AUTO_CREATENOW, false)) {
				openOptionsMenu();
				preferences.edit().putBoolean(PREFERENCE_AUTO_CREATENOW, false).commit();
			}
		}
	}

	/**
	 * Create the option menu of this activity
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_WIZARD, 0, getString(R.string.wizard_menu)).setIcon(R.drawable.ic_menu_wizard);
		menu.add(0, MENU_CREATE, 1, getString(R.string.menu_add_server)).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_DELETE, 2, getString(R.string.menu_delete_server)).setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem wizardItem = menu.getItem(0);
		if (wizardItem != null) {
			WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			boolean wifiOn = wifiMgr.isWifiEnabled();
			final WifiInfo currentWifi = wifiMgr.getConnectionInfo();
			boolean wifiConnected = (wifiOn && currentWifi.getNetworkId() != -1);
			wizardItem.setEnabled(wifiConnected);
		}
		return true;
	}

	/**
	 * Interact with the user
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_WIZARD:
			WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			boolean wifiOn = wifiMgr.isWifiEnabled();
			final WifiInfo currentWifi = wifiMgr.getConnectionInfo();
			boolean wifiConnected = (wifiOn && currentWifi.getNetworkId() != -1);
			if (wifiConnected) {
				ServerWizard wiz = new ServerWizard(this, wifiMgr.getConnectionInfo().getSSID());
				wiz.start();
			}
			break;
		// Create a new server
		case MENU_CREATE:
			maxServerId = maxServerId + 1;
			// Create the create new server screen
			PreferenceScreen screen = createServerPreference(maxServerId, serversCategory, PreferenceFacade.SERVER_PREFIX + maxServerId, getString(R.string.label_default_server_prefix) + maxServerId, getString(R.string.hint_default_server));
			showServerDialog(screen);
			break;

		// Delete one or more servers
		case MENU_DELETE:
			// Load servers list
			final ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
			PreferenceFacade.processLoadingServers(getPreferenceScreen().getSharedPreferences(), new PreferenceProcessor() {
				public void process(int idP, String keyP, Properties propsP) {
					ServerInfo deletion = new ServerInfo();
					deletion.id = idP;
					String title = propsP.getProperty(PreferenceFacade.NICKNAME_SUFFIX);
					title += " (" + propsP.getProperty(PreferenceFacade.SSID_SUFFIX) + ")";
					deletion.title = title;
					deletion.delete = false;
					deletion.key = keyP;
					servers.add(deletion);
				}
			});
			// Sort the list
			Collections.sort(servers, new Comparator<ServerInfo>() {
				public int compare(ServerInfo obj0, ServerInfo obj1) {
					int id0 = obj0.id;
					int id1 = obj1.id;
					if (id0 == id1)
						return 0;
					return (id0 > id1 ? 1 : -1);
				}
			});
			// Build titles
			String[] servsTitle = new String[servers.size()];
			for (int iLoop = 0; iLoop < servers.size(); iLoop++) {
				servsTitle[iLoop] = servers.get(iLoop).title;
			}
			// Create the dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.menu_delete_server));
			builder.setMultiChoiceItems(servsTitle, null, new OnMultiChoiceClickListener() {
				// Change delete state
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					servers.get(which).delete = isChecked;
				}
			});
			// When deleting remove from ServerCategory
			builder.setPositiveButton(getString(R.string.button_delete), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Editor editor = getPreferenceScreen().getEditor();
					// Loop on children
					for (int iLoop = 0; iLoop < serversCategory.getPreferenceCount(); iLoop++) {
						Preference pref = serversCategory.getPreference(iLoop);
						String key = pref.getKey();
						// Try to find the corresponding server
						ServerInfo fake = new ServerInfo();
						fake.key = key;
						int index = servers.indexOf(fake);
						if (index != -1) {
							ServerInfo serv = servers.get(index);
							// If we want to delete it then remove from the ServerCategory
							if (serv.delete) {
								// Deletion is done by hand
								editor.remove(serv.key + PreferenceFacade.NICKNAME_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.PROTOCOL_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.HOST_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.PORT_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.DSM_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.USER_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.PASSWORD_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.REFRESHSTATE_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.REFRESHVALUE_SUFFIX);
								editor.remove(serv.key + PreferenceFacade.SSID_SUFFIX);
							}
						}
					}
					editor.commit();
					// Reload servers preferences
					serversCategory.removeAll();
					maxServerId = 0;
					PreferenceFacade.processLoadingServers(getPreferenceScreen().getSharedPreferences(), DownloadPreferenceActivity.this);
				}
			});
			builder.setNegativeButton(getString(R.string.button_cancel), null);
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.ds.ServerProcessor#process(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public void process(int idP, String keyP, Properties propertiesP) {
		String summary = buildURL(propertiesP.getProperty(PreferenceFacade.PROTOCOL_SUFFIX), propertiesP.getProperty(PreferenceFacade.HOST_SUFFIX), propertiesP.getProperty(PreferenceFacade.PORT_SUFFIX));
		if (idP > maxServerId) {
			maxServerId = idP;
		}
		String title = propertiesP.getProperty(PreferenceFacade.NICKNAME_SUFFIX);
		title += " (" + propertiesP.getProperty(PreferenceFacade.SSID_SUFFIX) + ")";
		createServerPreference(idP, serversCategory, keyP, title, summary);
	}

	/**
	 * Create a preference screen from a SynoServer instance
	 * 
	 * @return The instance of the PreferenceScreen
	 */
	private PreferenceScreen createServerPreference(int idP, PreferenceGroup parentP, String keyP, String titleP, String summaryP) {

		// Create the server preference
		final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
		parentP.addPreference(screen);
		screen.setOrder(idP);
		screen.setKey(keyP);
		screen.setPersistent(true);
		screen.setTitle(titleP);
		screen.setSummary(summaryP);
		// ----------------------------------------------
		// Create a category to show general parameters
		PreferenceCategory generalCategory = new PreferenceCategory(this);
		generalCategory.setTitle(getString(R.string.title_cat_server));
		screen.addPreference(generalCategory);
		// ---- Nickname
		final EditTextPreferenceWithValue nickPref = EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.NICKNAME_SUFFIX, R.string.label_nickname, R.string.hint_nickname);
		nickPref.setText(titleP);
		nickPref.setDefaultValue(titleP);
		generalCategory.addPreference(nickPref);

		// ---- Username
		generalCategory.addPreference(EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.USER_SUFFIX, R.string.label_username, R.string.hint_username));
		// ---- Password
		generalCategory.addPreference(EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.PASSWORD_SUFFIX, R.string.label_password, R.string.hint_password).setInputType(
		    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
		// --- DSM Version
		generalCategory.addPreference(ListPreferenceWithValue.create(this, keyP + PreferenceFacade.DSM_SUFFIX, R.string.label_dsm_version, R.string.hint_dsm_version, DSMVersion.getValues()));

		// Create public connection category
		addConnectionCategory(keyP, screen, false, R.string.title_cat_connection);

		// Create local connection category
		addConnectionCategory(keyP, screen, true, R.string.title_cat_connection_local);

		return screen;
	}

	/**
	 * Add a preference category
	 * 
	 */
	private void addConnectionCategory(String keyP, PreferenceScreen screen, boolean showWifiP, int titleResIdP) {
		// Change the key if it's a wifi connection
		if (showWifiP) {
			keyP += PreferenceFacade.WLAN_RADICAL;
		}

		PreferenceCategory connectionCategory = new PreferenceCategory(this);
		connectionCategory.setTitle(getString(titleResIdP));
		screen.addPreference(connectionCategory);

		final WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// ---- Create Wifi list (ONLY for wifi connection)
		if (showWifiP) {
			List<WifiConfiguration> wifis = wifiMgr.getConfiguredNetworks();
			String[] wifiSSIDs = new String[wifis.size() + 1];
			wifiSSIDs[0] = "None";
			for (int iLoop = 0; iLoop < wifis.size(); iLoop++) {
				String ssid = wifis.get(iLoop).SSID;
				if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
					ssid = ssid.substring(1, ssid.length() - 1);
				}
				wifiSSIDs[iLoop + 1] = ssid;
			}
			final ListPreferenceWithValue wifiSSIDPref = ListPreferenceWithValue.create(this, keyP + PreferenceFacade.SSID_SUFFIX, R.string.label_wifissid, R.string.hint_wifissid, wifiSSIDs);
			connectionCategory.addPreference(wifiSSIDPref);
			if (!wifiMgr.isWifiEnabled()) {
				wifiSSIDPref.setEnabled(false);
			}
		}

		// ---- Protocol
		final ListPreferenceWithValue protocolPref = ListPreferenceWithValue.create(this, keyP + PreferenceFacade.PROTOCOL_SUFFIX, R.string.label_protocol, R.string.hint_protocol, SynoProtocol.getValues());
		connectionCategory.addPreference(protocolPref);
		// ---- Host
		final EditTextPreferenceWithValue hostPref = EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.HOST_SUFFIX, R.string.label_host, R.string.hint_host);
		connectionCategory.addPreference(hostPref);
		// ---- Port
		final EditTextPreferenceWithValue portPref = EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.PORT_SUFFIX, R.string.label_port, R.string.hint_port).setInputType(InputType.TYPE_CLASS_NUMBER);
		connectionCategory.addPreference(portPref);

		// ---- Show upload
		CheckBoxPreference showUpload = new CheckBoxPreference(this);
		showUpload.setKey(keyP + PreferenceFacade.SHOWUPLOAD_SUFFIX);
		showUpload.setTitle(R.string.label_showupload);
		// It looks like by using the set check function, the preference is not save
		// properly. Removing it seems to make default preference better
		// autoRefresh.setChecked(true);
		showUpload.setDefaultValue(false);
		showUpload.setSummaryOn(R.string.hint_showupload_on);
		showUpload.setSummaryOff(R.string.hint_showupload_off);
		// showUpload.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		// {
		// public boolean onPreferenceChange(Preference preference, Object newValue)
		// {
		// app.setServerShowUpload((Boolean) newValue);
		// return true;
		// }
		// });
		connectionCategory.addPreference(showUpload);
		// ---- Auto refresh
		CheckBoxPreference autoRefresh = new CheckBoxPreference(this);
		autoRefresh.setKey(keyP + PreferenceFacade.REFRESHSTATE_SUFFIX);
		autoRefresh.setTitle(R.string.label_autorefresh);
		// It looks like by using the set check function, the preference is not save
		// properly. Removing it seems to make default preference better
		// autoRefresh.setChecked(true);
		autoRefresh.setDefaultValue(true);
		autoRefresh.setSummaryOn(R.string.hint_autorefresh_on);
		autoRefresh.setSummaryOff(R.string.hint_autorefresh_off);
		connectionCategory.addPreference(autoRefresh);
		// -- Refresh value
		final EditTextPreferenceWithValue autoRefreshValue = EditTextPreferenceWithValue.create(this, keyP + PreferenceFacade.REFRESHVALUE_SUFFIX, R.string.label_refreshinterval, R.string.hint_refreshinterval).setInputType(
		    InputType.TYPE_CLASS_NUMBER);
		autoRefreshValue.setDefaultValue("15");
		connectionCategory.addPreference(autoRefreshValue);
		// Add dependencies. DON'T use 'setDependency()' when building Preferences
		// at runtime
		connectionCategory.findPreference(keyP + PreferenceFacade.REFRESHVALUE_SUFFIX).setDependency(keyP + PreferenceFacade.REFRESHSTATE_SUFFIX);

		// // Create listener to update title and summary
		// nickPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		// public boolean onPreferenceChange(Preference preference, Object newValue)
		// {
		// String title = (String) newValue;
		// String summary = buildURL(protocolPref.getValue(), hostPref.getText(),
		// portPref.getText());
		// summary = summary.toLowerCase();
		// updatePrefScreen(screen, title, summary, wlanPref.isChecked(),
		// wifiSSIDPref.getEntry().toString());
		// return true;
		// }
		// });
		// // Create listener to update title and summary
		// protocolPref.setOnPreferenceChangeListener(new
		// OnPreferenceChangeListener() {
		// public boolean onPreferenceChange(Preference preference, Object newValue)
		// {
		// String title = nickPref.getText();
		// String summary = buildURL((String) newValue, hostPref.getText(),
		// portPref.getText());
		// updatePrefScreen(screen, title, summary, wlanPref.isChecked(),
		// wifiSSIDPref.getEntry().toString());
		// return true;
		// }
		// });
		// // Create listener to update title and summary
		// hostPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		// public boolean onPreferenceChange(Preference preference, Object newValue)
		// {
		// String title = nickPref.getText();
		// String summary = buildURL(protocolPref.getValue(), (String) newValue,
		// portPref.getText());
		// updatePrefScreen(screen, title, summary, wlanPref.isChecked(),
		// wifiSSIDPref.getEntry().toString());
		// return true;
		// }
		// });
		// // Create listener to update title and summary
		// portPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		// public boolean onPreferenceChange(Preference preference, Object newValue)
		// {
		// String title = nickPref.getText();
		// String summary = buildURL(protocolPref.getValue(), hostPref.getText(),
		// (String) newValue);
		// updatePrefScreen(screen, title, summary, wlanPref.isChecked(),
		// wifiSSIDPref.getEntry().toString());
		// return true;
		// }
		// });
		// // Create listener to avoid allowing WLAN connection when wifi if OFF
		// wlanPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		// public boolean onPreferenceChange(Preference preferenceP, Object
		// newValueP) {
		// if (!wifiMgr.isWifiEnabled()) {
		// AlertDialog.Builder builder = new
		// AlertDialog.Builder(DownloadPreferenceActivity.this);
		// builder.setTitle(getResources().getText(R.string.title_wifi_disabled)).setMessage(
		// getResources().getText(R.string.message_wifi_disabled)).setPositiveButton(
		// getResources().getText(R.string.button_ok), null);
		// AlertDialog alert = builder.create();
		// alert.show();
		// return false;
		// }
		// else {
		// String summary = buildURL(protocolPref.getValue(), hostPref.getText(),
		// portPref.getText());
		// summary = summary.toLowerCase();
		// String title = nickPref.getText();
		// String wifiSSID = "";
		// if (wifiSSIDPref.getEntry() != null) {
		// wifiSSID = wifiSSIDPref.getEntry().toString();
		// }
		// updatePrefScreen(screen, title, summary, (Boolean) newValueP, wifiSSID);
		// return true;
		// }
		// }
		// });
		// // Create listener to update title and summary
		// wifiSSIDPref.setOnPreferenceChangeListener(new
		// OnPreferenceChangeListener() {
		// public boolean onPreferenceChange(Preference preference, Object newValue)
		// {
		// String title = nickPref.getText();
		// String summary = buildURL(protocolPref.getValue(), hostPref.getText(),
		// portPref.getText());
		// updatePrefScreen(screen, title, summary, wlanPref.isChecked(), (String)
		// newValue);
		// return true;
		// }
		// });
	}

	/**
	 * Update the PreferenceScreen to reflect the current values
	 * 
	 * @param prefScreenP
	 * @param titleP
	 * @param summaryP
	 */
	// private void updatePrefScreen(PreferenceScreen prefScreenP, String titleP,
	// String summaryP, boolean useWifiP, String ssidP) {
	// if (useWifiP) {
	// titleP += " - (" + ssidP + ")";
	// }
	// prefScreenP.setTitle(titleP);
	// prefScreenP.setSummary(summaryP);
	// // Notify the root PreferenceScreen that a child has been updated
	// PreferenceScreen rootScreen = getPreferenceScreen();
	// BaseAdapter adapt = (BaseAdapter) rootScreen.getRootAdapter();
	// adapt.notifyDataSetChanged();
	// }

	/**
	 * Build a end-user String which represents the URL used
	 * 
	 * @param protoP
	 * @param hostP
	 * @param portP
	 * @return
	 */
	private String buildURL(String protoP, String hostP, String portP) {
		String result = "";
		// If at least a non null value
		if ((protoP != null && protoP.length() > 0) || (hostP != null && hostP.length() > 0) || (portP != null && portP.length() > 0)) {
			result = result + (protoP != null ? protoP : "") + "://";
			result = result + (hostP != null ? hostP : "") + ":";
			result = result + (portP != null ? portP : "");
		}
		return result.toLowerCase();
	}

	/**
	 * This method is called when the wizard finished. The metadata should contain
	 * information collected by the wizard. A null paramter means that no
	 * information have been collected.
	 * 
	 * @param metaDataP
	 */
	public void onWizardFinished(HashMap<String, Object> metaDataP) {
		if (metaDataP != null) {
			maxServerId++;
			Editor editor = getPreferenceScreen().getEditor();
			// Write commons datas
			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.NICKNAME_SUFFIX, metaDataP.get(ServerWizard.META_NAME).toString());
			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.USER_SUFFIX, metaDataP.get(ServerWizard.META_USERNAME).toString());
			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.PASSWORD_SUFFIX, metaDataP.get(ServerWizard.META_PASSWORD).toString());
			editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.DSM_SUFFIX, metaDataP.get(ServerWizard.META_DSM).toString());

			// Local connection
			writeConnectionValues(editor, true, metaDataP);
			// If the user also want to access to his server from internet
			if (((Boolean) metaDataP.get(ServerWizard.META_DDNS))) {
				writeConnectionValues(editor, false, metaDataP);
				editor.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + PreferenceFacade.WLAN_RADICAL + PreferenceFacade.SSID_SUFFIX, metaDataP.get(ServerWizard.META_WIFI).toString());
			}
			editor.commit();
			// Reload the servers list
			reloadCurrentServers();
		}
		// Display a message for the end user
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_title_information).setMessage(R.string.wizard_no_server_found).setCancelable(false).setPositiveButton(R.string.button_ok, null).create().show();
		}
	}

	/**
	 * 
	 * @param editorP
	 * @param localP
	 * @param metaDataP
	 */
	private void writeConnectionValues(Editor editorP, boolean localP, HashMap<String, Object> metaDataP) {
		String localRadical = "";
		String host = metaDataP.get(ServerWizard.META_DDNS_NAME).toString();
		boolean showUpload = false;
		String refreshRate = "20";
		// If it is a local connection then adjust some values
		if (localP) {
			localRadical = PreferenceFacade.WLAN_RADICAL;
			host = metaDataP.get(ServerWizard.META_HOST).toString();
			showUpload = true;
			refreshRate = "5";
		}
		// Verify if at least host has been set
		if (host != null && host.length() > 0) {
			editorP.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.PROTOCOL_SUFFIX, ((Boolean) metaDataP.get(ServerWizard.META_HTTPS)) ? "HTTPS" : "HTTP");
			editorP.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.HOST_SUFFIX, host);
	    editorP.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.PORT_SUFFIX, ((Boolean) metaDataP.get(ServerWizard.META_HTTPS)) ? "5001" : "5000");
			editorP.putBoolean(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.SHOWUPLOAD_SUFFIX, showUpload);
			editorP.putBoolean(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.REFRESHSTATE_SUFFIX, true);
			editorP.putString(PreferenceFacade.SERVER_PREFIX + maxServerId + localRadical + PreferenceFacade.REFRESHVALUE_SUFFIX, refreshRate);
		}
	}

	/**
	 * An inner class which provide minimal information about a server
	 */
	class ServerInfo {
		public boolean delete = false;
		public String key = "";
		public String title = "";
		public int id = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ServerInfo other = (ServerInfo) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			}
			else if (!key.equals(other.key))
				return false;
			return true;
		}
	}

	/**
	 * Show the preference screen. It's a very bad implementation
	 * 
	 * @param screenP
	 */
	private void showServerDialog(PreferenceScreen screenP) {
		ListView listView = new ListView(this);
		screenP.bind(listView);

		// Set the title bar if title is available, else no title bar
		final CharSequence title = getTitle();
		Dialog dialog = new Dialog(this, TextUtils.isEmpty(title) ? android.R.style.Theme_NoTitleBar : android.R.style.Theme);
		dialog.setContentView(listView);
		if (!TextUtils.isEmpty(title)) {
			dialog.setTitle(title);
		}
		dialog.setOnDismissListener(screenP);
		dialog.show();
	}
}
