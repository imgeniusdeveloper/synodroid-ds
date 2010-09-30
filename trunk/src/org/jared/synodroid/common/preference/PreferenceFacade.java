/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.common.preference;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.jared.synodroid.Synodroid;
import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.DSMVersion;
import org.jared.synodroid.common.data.SynoProtocol;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * A facade pattern which implements utilities methods to read/write preferences
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class PreferenceFacade {

  public static final String REFRESHVALUE_SUFFIX = ".refreshvalue";
  public static final String SHOWUPLOAD_SUFFIX = ".showupload";
  public static final String REFRESHSTATE_SUFFIX = ".refreshstate";
  public static final String PASSWORD_SUFFIX = ".password";
  public static final String USER_SUFFIX = ".user";
  public static final String DSM_SUFFIX = ".dsm";
  public static final String PORT_SUFFIX = ".port";
  public static final String HOST_SUFFIX = ".host";
  public static final String PROTOCOL_SUFFIX = ".protocol";
  public static final String NICKNAME_SUFFIX = ".nickname";

  public static final String WLAN_SUFFIX = ".wlan";
  public static final String WLANSSID_SUFFIX = ".wlanssid";

  public static final String SERVER_PREFIX = "server.";

  /**
   * Load servers from the SharedPreference
   */
  public static void processLoadingServers(SharedPreferences sharedPreferencesP, PreferenceProcessor processorP) {
    // Read all preferences
    Map<String, ?> prefs = sharedPreferencesP.getAll();
    for (String key : prefs.keySet()) {
      // If the key starts with 'server.' and then an integer
      if (key.startsWith(SERVER_PREFIX)) {
        String idStr = key.substring(SERVER_PREFIX.length());
        try {
          int idx = idStr.indexOf(".");
          if (idx != -1) {
            // Get the sub key
            String subKey = idStr.substring(idx);
            // Create the server if only the subkey is 'nickname'
            if (subKey != null && subKey.equals(NICKNAME_SUFFIX)) {
              idStr = idStr.substring(0, idx);
              int id = Integer.parseInt(idStr);
              // Retreive server's informations
              Properties props = new Properties();
              props.setProperty(NICKNAME_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + NICKNAME_SUFFIX)));
              props.setProperty(PROTOCOL_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + PROTOCOL_SUFFIX)));
              props.setProperty(HOST_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + HOST_SUFFIX)));
              props.setProperty(PORT_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + PORT_SUFFIX)));
              props.setProperty(DSM_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + DSM_SUFFIX)));
              props.setProperty(USER_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + USER_SUFFIX)));
              props.setProperty(PASSWORD_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + PASSWORD_SUFFIX)));
              props.setProperty(REFRESHSTATE_SUFFIX,
                      convert2String(prefs.get(SERVER_PREFIX + id + REFRESHSTATE_SUFFIX)));
              props.setProperty(REFRESHVALUE_SUFFIX,
                      convert2String(prefs.get(SERVER_PREFIX + id + REFRESHVALUE_SUFFIX)));
              props.setProperty(SHOWUPLOAD_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + SHOWUPLOAD_SUFFIX)));
              props.setProperty(WLAN_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + WLAN_SUFFIX)));
              props.setProperty(WLANSSID_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + WLANSSID_SUFFIX)));
              // Process the current server's item
              processorP.process(id, SERVER_PREFIX + id, props);
            }
          }
        }
        // Not a number: don't care
        catch(NumberFormatException ex) {
        }
      }
    }
  }

  /**
   * Cast to a String instance but NEVER return null
   * 
   * @param objP
   * @return
   */
  private static String convert2String(Object objP) {
    if (objP != null) {
      return "" + objP;
    }
    return "";
  }

  /**
   * Load all servers from the shared preference
   */
  public static ArrayList<SynoServer> loadServers(Context contextP, final SharedPreferences sharedPreferencesP) {
    // Determine the current network access
    WifiManager wifiMgr = (WifiManager) contextP.getSystemService(Context.WIFI_SERVICE);
    boolean wifiOn = wifiMgr.isWifiEnabled();
    final WifiInfo currentWifi = wifiMgr.getConnectionInfo();
    final boolean wifiConnected = (wifiOn && currentWifi.getNetworkId() != -1);

    // Create the servers list
    final ArrayList<SynoServer> result = new ArrayList<SynoServer>();
    processLoadingServers(sharedPreferencesP, new PreferenceProcessor() {
      public void process(int idP, String keyP, Properties propertiesP) {
        try {
          // Minimal informations
          SynoProtocol protocol = SynoProtocol.valueOf(propertiesP.getProperty(PreferenceFacade.PROTOCOL_SUFFIX));
          int port = Integer.parseInt(propertiesP.getProperty(PreferenceFacade.PORT_SUFFIX));
          SynoServer server = new SynoServer(propertiesP.getProperty(PreferenceFacade.NICKNAME_SUFFIX), protocol,
                  propertiesP.getProperty(PreferenceFacade.HOST_SUFFIX), port, propertiesP
                          .getProperty(PreferenceFacade.USER_SUFFIX), propertiesP
                          .getProperty(PreferenceFacade.PASSWORD_SUFFIX));
          // DSM version
          DSMVersion vers = DSMVersion.titleOf(propertiesP.getProperty(PreferenceFacade.DSM_SUFFIX));
          server.setDsmVersion(vers);
          // Show upload
          boolean showUpload = Boolean.parseBoolean(propertiesP.getProperty(PreferenceFacade.SHOWUPLOAD_SUFFIX));
          server.setShowUpload(showUpload);
          // Refresh
          Integer refreshInterval = Integer.parseInt(propertiesP.getProperty(PreferenceFacade.REFRESHVALUE_SUFFIX));
          server.setRefreshInterval(refreshInterval);
          boolean autoRefresh = Boolean.parseBoolean(propertiesP.getProperty(PreferenceFacade.REFRESHSTATE_SUFFIX));
          server.setAutoRefresh(autoRefresh);
          // Sort informations
          String sortAttr = sharedPreferencesP.getString("sort", "task_id");
          boolean asc = sharedPreferencesP.getBoolean("asc", true);
          server.setSortAttribute(sortAttr);
          server.setAscending(asc);
          // Wlan
          boolean useInWlan = Boolean.parseBoolean(propertiesP.getProperty(PreferenceFacade.WLAN_SUFFIX));
          server.setWlan(useInWlan);
          String ssid = propertiesP.getProperty(PreferenceFacade.WLANSSID_SUFFIX);
          server.setWifiSSID(ssid);
          // 
          // Add this server according to the following rules :
          // 
          // the server is configured to be used in a wlan AND wifi is activated AND ssids are equals
          // OR
          // the server is not configured to be used in a wlan AND wifi is disabled
          // 
          if ((useInWlan && wifiConnected && currentWifi.getSSID().equals(ssid))
                  || (!useInWlan && !wifiConnected)) {
            result.add(server);
          }
        }
        catch(Exception ex) {
          Log.d(Synodroid.DS_TAG, "An exception occured while loading servers from preference", ex);
        }
      }
    });
    // If at
    return result;
  }
}
