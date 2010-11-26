/**
 * 
 */
package org.jared.synodroid.common;

import java.util.Properties;

import org.jared.synodroid.common.data.SynoProtocol;
import org.jared.synodroid.common.preference.PreferenceFacade;

/**
 * A technical connection definition to a SynoServer 
 * @author Eric Taix
 */
public class SynoServerConnection {

  // The protocol used to communicate with the server
  public SynoProtocol protocol = SynoProtocol.HTTP;
  // The hostname or ip address
  public String host;
  // The port
  public Integer port = 5000;
  // The refresh interval in seconds
  public Integer refreshInterval = 10;
  // The resfresh state (enable or disable autorefresh)
  public boolean autoRefresh = true;
  // Show (or not) the upload progress in the main activity
  public boolean showUpload = true;
  // Wifi SSID allowed for this server (empty when used for a public connection)
  public String wifiSSID = "";
  

  /**
   * Create an instance of SynoServerConnection 
   * @param props
   * @return
   */
  public static SynoServerConnection createFromProperties(boolean local, Properties props) {
    SynoServerConnection result = null;
    
    String radical = "";
    if (local) {
      radical += PreferenceFacade.WLAN_RADICAL;
    }

    SynoProtocol protocol = SynoProtocol.valueOf(props.getProperty(radical + PreferenceFacade.PROTOCOL_SUFFIX));
    int port = Integer.parseInt(props.getProperty(radical + PreferenceFacade.PORT_SUFFIX));
    String host = props.getProperty(radical + PreferenceFacade.HOST_SUFFIX);
    if (protocol!=null && port!=0 && host!=null && host.length()>0) {
      result = new SynoServerConnection();
      result.protocol = protocol;
      result.host = host;
      result.port = port;
      result.showUpload = Boolean.parseBoolean(props.getProperty(radical + PreferenceFacade.SHOWUPLOAD_SUFFIX));
      result.refreshInterval = Integer.parseInt(props.getProperty(radical + PreferenceFacade.REFRESHVALUE_SUFFIX));
      result.autoRefresh = Boolean.parseBoolean(props.getProperty(radical + PreferenceFacade.REFRESHSTATE_SUFFIX));
      result.wifiSSID = props.getProperty(radical + PreferenceFacade.SSID_SUFFIX);    
    }
    return result;
  }  
}
