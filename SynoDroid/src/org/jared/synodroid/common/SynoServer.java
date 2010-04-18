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
package org.jared.synodroid.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;

import org.jared.synodroid.common.data.DSMVersion;
import org.jared.synodroid.common.data.SynoProtocol;
import org.jared.synodroid.common.data.TaskContainer;
import org.jared.synodroid.common.protocol.DSMException;
import org.jared.synodroid.common.protocol.DSMHandlerFactory;
import org.jared.synodroid.common.protocol.MultipartBuilder;
import org.jared.synodroid.common.protocol.https.AcceptAllHostNameVerifier;
import org.jared.synodroid.common.protocol.https.AcceptAllTrustManager;
import org.jared.synodroid.ds.DownloadActivity;
import org.jared.synodroid.ds.R;
import org.jared.synodroid.ds.action.TaskAction;
import org.json.JSONObject;

import android.os.Message;
import android.util.Log;

/**
 * This class represents a Synology server. It manages the connection and also
 * the automatic refresh to retrieve the torrent list.
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class SynoServer {

	// The nickname of the server
	private String nickname = "";
	// The protocol used to communicate with the server
	private SynoProtocol protocol = SynoProtocol.HTTP;
	// The hostname or ip address
	private String host;
	// The port
	private Integer port = 5000;
	// The version of DSM
	private DSMVersion dsmVersion = DSMVersion.VERSION2_2;
	// The user
	private String user;
	// The password
	private String password;
	// The refresh interval in seconds
	private Integer refreshInterval = 10;
	// The resfresh state (enable or disable autorefresh)
	private boolean autoRefresh = true;
	// The sort atttribut
	private String sortAttribute = "task_id";
	// Is the sort ascending
	private boolean ascending = true;

	// Are we connected with the server: login+passwd?
	private boolean connected = false;
	// Flag to pause the thread until it is interrupted
	private boolean pause = false;
	// The DSM protocol handler
	private DSMHandlerFactory dsmFactory;
	// The data's collector thread
	private Thread collector;
	// Cookies
	private List<String> cookies;

	// Binded DownloadActivity
	private DownloadActivity bindedActivity;

	/**
	 * Static intialization of the SSL factory to accept each certificate, even if
	 * a certificate is self signed
	 */
	static {
		SSLContext sc;
		try {
			sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new AcceptAllTrustManager() }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new AcceptAllHostNameVerifier());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * A new SynoServer with no informations
	 */
	public SynoServer() {
	}

	/**
	 * Constructor which set all server's informations. No connection are made
	 * when calling the constructor.
	 */
	public SynoServer(String nicknameP, SynoProtocol protocolP, String hostP, int portP, String userP, String passwordP) {
		nickname = nicknameP;
		protocol = protocolP;
		host = hostP;
		port = portP;
		user = userP;
		password = passwordP;
		// Create the appropriated factory
		dsmFactory = DSMHandlerFactory.getFactory(dsmVersion, this);
	}

	/**
	 * Connect to the server. It is a requirement to connect to the NAS server
	 * before any attempt to call a method of this class.
	 * 
	 * @return
	 * @throws DSMException
	 */
	public void connect(final DownloadActivity activityP, final List<TaskAction> actionQueueP) {
		bindActivity(activityP);

		// If we are not already connected
		if (!connected) {
			// Everything is OK, so launch the thread
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						// Send a connecting message
						fireMessage(SynoServer.this.bindedActivity, DownloadActivity.MSG_CONNECTING);
						// Connect: try to...
						dsmFactory.connect();
						// Here we are connected
						connected = true;
						
						// If the action's queue is not empty
						if (actionQueueP != null) {
							for (TaskAction taskAction : actionQueueP) {
								executeAction(activityP, taskAction, false);
							}
						}
						// Send a connected message
						fireMessage(SynoServer.this.bindedActivity, DownloadActivity.MSG_CONNECTED);
						// If everything is fine then start to collect informations
						while (connected) {
							try {
								Log.d(DownloadActivity.DS_TAG, "Refreshing torrents");
								// Update the progressbar
								fireMessage(SynoServer.this.bindedActivity, DownloadActivity.MSG_OPERATION_PENDING);
								
								// Retrieve update torrents list
								TaskContainer container = dsmFactory.getDSHandler().getAllTorrent(sortAttribute, ascending);
								// In case we are disconnected before the response is received
								if (connected) {
									// If everything is fine then send new torrent's list
									fireMessage(SynoServer.this.bindedActivity, DownloadActivity.MSG_TASKS_UPDATED, container);
									// If auto refresh
									synchronized (this) {
										if (autoRefresh) {
											// Sleep
											wait(refreshInterval * 1000);
										}
										else {
											wait();
										}
									}
									// If the thread is paused
									synchronized (this) {
										if (pause) {
											wait();
										}
									}
								}
							}
							// Nothing to do. It may be a force refresh after an action!
							catch (InterruptedException iex) {
								Log.d(DownloadActivity.DS_TAG, "Been interrupted while sleeping...");
							}
						}
					}
					// Connection error
					catch (DSMException e) {
						// If the user didn't change the server
						if (connected) {
							fireMessage(SynoServer.this.bindedActivity, DownloadActivity.MSG_ERROR, translateError(SynoServer.this.bindedActivity, e));
						}
					}
					// Programmation exception
					catch (Exception e) {
						// If the user didn't change the server
						if (connected) {
							fireMessage(SynoServer.this.bindedActivity, DownloadActivity.MSG_ERROR, e.getMessage());
						}
					}
					// Set the connection to null to force connection next time
					finally {
						connected = false;
					}
				}
			};
			collector = new Thread(runnable, "Synodroid DS collector");
			collector.start();
		}
	}

	/**
	 * Bind an activity with this current server
	 * 
	 * @param activityP
	 */
	public void bindActivity(DownloadActivity activityP) {
		bindedActivity = activityP;
	}

	/**
	 * Disconnect from the server
	 */
	public void disconnect() {
		connected = false;
		collector.interrupt();
	}

	/**
	 * Send a message
	 */
	public void fireMessage(DownloadActivity activityP, int msgP) {
		fireMessage(activityP, msgP, null);
	}

	/**
	 * Send a message
	 */
	public void fireMessage(DownloadActivity activityP, int msgP, Object objP) {
		// Send the connecting message
		Message msg = new Message();
		msg.what = msgP;
		msg.obj = objP;
		activityP.handleMessage(msg);

	}

	/**
	 * Translate an error (JSON or technical exception) to a end-user message
	 * 
	 * @param Log
	 */
	private String translateError(DownloadActivity activityP, DSMException dsmExP) {
		String msg = "Can't display error";
		msg = activityP.getString(R.string.connect_unknwon_reason);
		// Get the reason
		String jsoReason = dsmExP.getJsonReason();
		// If no JSON reason, try to find the reason in the root DSMException
		if (jsoReason == null && dsmExP.getRootException() != null && dsmExP.getRootException() instanceof DSMException) {
			jsoReason = ((DSMException) dsmExP.getRootException()).getJsonReason();
		}
		// If there's is a wellknown reason
		if (jsoReason != null) {
			// Wrong user or password
			if (jsoReason.equals("error_noprivilege")) {
				msg = activityP.getString(R.string.connect_wrong_userpassword);
			}
			else if (jsoReason.equals("error_interrupt")) {
				msg = activityP.getString(R.string.connect_already_connected);
			}
			else if (jsoReason.equals("error_cantlogin")) {
				msg = activityP.getString(R.string.connect_cant);
			}
			else {
				msg += ": " + jsoReason;
				Log.d(DownloadActivity.DS_TAG, "JSON's error not trapped: " + jsoReason);
			}

		}
		// Or if there's a wellknown exception
		else if (dsmExP.getRootException() != null) {
			if (dsmExP.getRootException() instanceof SocketException) {
				msg = activityP.getString(R.string.connect_nohost);
			}
			else if (dsmExP.getRootException() instanceof SSLException) {
				msg = MessageFormat.format(activityP.getString(R.string.connect_ssl_error), new Object[] { dsmExP.getCause().getMessage() });
			}
			else {
				msg = dsmExP.getRootException().getMessage();
			}
		}

		// Return the message
		return msg;
	}

	/**
	 * Return the string representation of a Synology server
	 */
	@Override
	public String toString() {
		return (protocol.name() + "://" + host + ":" + port).toLowerCase();
	}

	/**
	 * Return the handler factory
	 * 
	 * @return
	 */
	public DSMHandlerFactory getDSMHandlerFactory() {
		return dsmFactory;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return (protocol.name() + "://" + host + ":" + port).toLowerCase();
	}

	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * @return the protocol
	 */
	public SynoProtocol getProtocol() {
		return protocol;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @return the refreshInterval
	 */
	public int getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @return the dsmVersion
	 */
	public DSMVersion getDsmVersion() {
		return dsmVersion;
	}

	/**
	 * @param nickname
	 *          the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
		connected = false;
	}

	/**
	 * @param protocol
	 *          the protocol to set
	 */
	public void setProtocol(SynoProtocol protocol) {
		this.protocol = protocol;
		connected = false;
	}

	/**
	 * @param host
	 *          the host to set
	 */
	public void setHost(String host) {
		this.host = host;
		connected = false;
	}

	/**
	 * @param port
	 *          the port to set
	 */
	public void setPort(int port) {
		this.port = port;
		connected = false;
	}

	/**
	 * @param refreshInterval
	 *          the refreshInterval to set
	 */
	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
		connected = false;
	}

	/**
	 * @param user
	 *          the user to set
	 */
	public void setUser(String user) {
		this.user = user;
		connected = false;
	}

	/**
	 * @param password
	 *          the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
		connected = false;
	}

	/**
	 * @param dsmVersion
	 *          the dsmVersion to set
	 */
	public void setDsmVersion(DSMVersion dsmVersion) {
		this.dsmVersion = dsmVersion;
		connected = false;
		// Create the appropriated factory
		dsmFactory = DSMHandlerFactory.getFactory(dsmVersion, this);
	}

	/**
	 * @return the autoRefresh
	 */
	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	/**
	 * @param autoRefresh
	 *          the autoRefresh to set
	 */
	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}

	/**
	 * @param port
	 *          the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * @param refreshInterval
	 *          the refreshInterval to set
	 */
	public void setRefreshInterval(Integer refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	/**
	 * @param sortAttribute
	 *          the sortAttribute to set
	 */
	public void setSortAttribute(String sortAttribute) {
		this.sortAttribute = sortAttribute;
	}

	/**
	 * @param ascending
	 *          the ascending to set
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	/**
	 * Execute an asynchronous action on this server
	 * 
	 * @param actionP
	 *          The action to execute
	 * @param forceRefreshP
	 *          Flag to set if a refresh is needed after the completion of the
	 *          action
	 */
	public void executeAction(final DownloadActivity activityP, final TaskAction actionP, final boolean forceRefreshP) {
		Runnable runnable = new Runnable() {
			public void run() {
				// An operation is pending
				fireMessage(activityP, DownloadActivity.MSG_OPERATION_PENDING);
				Log.d(DownloadActivity.DS_TAG, "Executing action: " + actionP.getName());
				try {
					// If a Toast must be shown
					if (actionP.isToastable()) {
						int resId = actionP.getToastId();
						String text = activityP.getString(resId, new Object[] { actionP.getTask().fileName });
						fireMessage(activityP, DownloadActivity.MSG_TOAST, text);
					}
					actionP.execute(activityP, SynoServer.this);
				}
				// Doesn't matter: just log it. Is it enough ?
				catch (Exception e) {
					Log.e("SynoDroid DS", "Unexpected error", e);
				}
				finally {
					fireMessage(activityP, DownloadActivity.MSG_OPERATION_DONE);
					// Interrup the collector's thread so it will refresh immediatelty
					if (forceRefreshP) {
						collector.interrupt();
					}
				}
			}
		};
		new Thread(runnable, "Synodroid DS action").start();
	}

	/**
	 * Send a request to the server.
	 * 
	 * @param uriP
	 *          The part of the URI ie: /webman/doit.cgi
	 * @param requestP
	 *          The query in the form 'param1=foo&param2=yes'
	 * @param methodP
	 *          The method to send this request
	 * @return A JSONObject containing the response of the server
	 * @throws DSMException
	 */
	public JSONObject sendJSONRequest(String uriP, String requestP, String methodP) throws Exception {
		HttpURLConnection con = null;

		try {
			// Prepare the connection
			con = (HttpURLConnection) new URL(getUrl() + uriP).openConnection();
			// con.setConnectTimeout(20000);
			// Add cookies if exist
			if (cookies != null) {
				for (String cookie : cookies) {
					con.addRequestProperty("Cookie", cookie);
				}
			}
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod(methodP);
			Log.d(DownloadActivity.DS_TAG, methodP + ": " + uriP + "?" + requestP);
			// Add the parameters
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(requestP);
			// Send the request
			wr.flush();

			// Try to retrieve the session cookie
			Map<String, List<String>> headers = con.getHeaderFields();
			List<String> newCookie = headers.get("set-cookie");
			if (newCookie != null) {
				cookies = newCookie;
			}
			// Now read the reponse and build a string with it
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();

			Log.d(DownloadActivity.DS_TAG, "Response is: " + sb.toString());
			JSONObject respJSO = new JSONObject(sb.toString());
			return respJSO;
		}
		// Unexpected exception
		catch (Exception ex) {
			Log.e(DownloadActivity.DS_TAG, "Unexpected error", ex);
			throw ex;
		}
		// Finally close everything
		finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	/**
	 * Upload a file which is located on the mobile
	 */
	public void sendMultiPart(String uriP, MultipartBuilder multiPartP) {
		HttpURLConnection conn;
		try {
			// Open a HTTP connection to the URL
			URL url = new URL(getUrl() + uriP);
			conn = (HttpURLConnection) url.openConnection();
			// Add cookies if exist
			if (cookies != null) {
				for (String cookie : cookies) {
					conn.addRequestProperty("Cookie", cookie);
				}
			}
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + multiPartP.getBoundary());

			// Write the multipart
			multiPartP.writeData(conn.getOutputStream());

			// Get the response
			int code = conn.getResponseCode();
			String resp = conn.getResponseMessage();
			// Now read the reponse and build a string with it
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();

			Log.d(DownloadActivity.DS_TAG, "Response is: " + sb.toString());
			JSONObject respJSO = new JSONObject(sb.toString());
			Log.d(DownloadActivity.DS_TAG, "Multipart response is: " + code + "/" + resp + "/" + respJSO);
		}
		catch (Exception e) {
			Log.e(DownloadActivity.DS_TAG, "Error while sending multipart", e);
		}
	}

	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Force a refresh by interrupting the sleep
	 */
	public void forceRefresh() {
		collector.interrupt();
	}

	/**
	 * Pause the server's thread
	 */
	public void pause() {
		pause = true;
	}

	/**
	 * Resume the server's thread
	 */
	public void resume() {
		pause = false;
		collector.interrupt();
	}
}