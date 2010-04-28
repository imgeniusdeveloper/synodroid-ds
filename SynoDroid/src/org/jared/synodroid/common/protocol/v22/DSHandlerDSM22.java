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
package org.jared.synodroid.common.protocol.v22;

import java.util.ArrayList;
import java.util.HashMap;

import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskContainer;
import org.jared.synodroid.common.protocol.DSHandler;
import org.jared.synodroid.common.protocol.DSMException;
import org.jared.synodroid.common.protocol.MultipartBuilder;
import org.jared.synodroid.common.protocol.Part;
import org.jared.synodroid.common.protocol.QueryBuilder;
import org.jared.synodroid.common.protocol.StreamFactory;
import org.jared.synodroid.ds.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

/**
 * @author Eric Taix (eric.taix at gmail dot com)
 * 
 */
class DSHandlerDSM22 implements DSHandler {

	// DownloadManager constant declaration
	private static final String DM_URI = "/download/downloadman.cgi";

	/* The Synology's server */
	private SynoServer server;

	/**
	 * The constructor
	 * 
	 * @param serverP
	 */
	public DSHandlerDSM22(SynoServer serverP) {
		server = serverP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.Protocol#getAllTorrent()
	 */
	public TaskContainer getAllTorrent(String sortAttrP, boolean ascendingP) throws Exception {
		ArrayList<Task> result = new ArrayList<Task>();
		TaskContainer container = new TaskContainer(result);
		// If we are logged on
		if (server.isConnected()) {
			QueryBuilder getAllRequest = new QueryBuilder().add("action", "getall");
			getAllRequest.add("sort", sortAttrP.toLowerCase());
			String asc = (ascendingP ? "ASC" : "DESC");
			getAllRequest.add("dir", asc);
			// Execute
			JSONObject jso = null;
			synchronized (server) {
				jso = server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
			}
			boolean success = jso.getBoolean("success");
			// If request succeded
			if (success) {
				// Get the totals rates
				String totalUp = jso.getString("total_up");
				String totalDown = jso.getString("total_down");
				container.setTotalUp(totalUp);
				container.setTotalDown(totalDown);

				JSONArray items = jso.getJSONArray("items");
				for (int iLoop = 0; iLoop < items.length(); iLoop++) {
					JSONObject item = items.getJSONObject(iLoop);
					// Create the torrent item
					Task torrent = new Task();
					torrent.fileName = item.getString("filename").trim();
					torrent.taskId = item.getInt("task_id");
					torrent.downloadRate = item.getString("current_rate").trim();
					torrent.downloadSize = item.getString("current_size").trim();
					String prog = item.getString("progress").trim();
					int index = prog.indexOf("%");
					// If a value could be found ('%' found)
					if (index != -1) {
						prog = prog.substring(0, index);
						try {
							torrent.progress = (int) Float.parseFloat(prog);
						}
						catch (NumberFormatException ex) {
							// Set to unknown
							torrent.progress = -1;
						}
					}
					// Set to unknown
					else {
						torrent.progress = -1;
					}
					torrent.status = item.getString("status");
					torrent.eta = Utils.computeTimeLeft(item.getInt("timeleft"));
					torrent.totalSize = item.getString("total_size").trim();
					torrent.uploadRate = item.getString("upload_rate").trim();
					torrent.creator = item.getString("username").trim();
					if (torrent.creator == ""){
						torrent.creator = server.getUser();
					}
					torrent.server = server;
					result.add(torrent);
				}
			}
			// Try to do something
			else {
				String reason = jso.getJSONObject("errno").getString("key");
				throw new DSMException(reason);
			}
		}
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.protocol.DSHandler#delete(org.jared.synodroid
	 * .common.data.Torrent)
	 */
	public void delete(Task torrentP) throws Exception {
		// If we are logged on
		if (server.isConnected()) {
			try {
				QueryBuilder getAllRequest = new QueryBuilder().add("action", "delete").add("idList", "" + torrentP.taskId);
				// Execute
				synchronized (server) {
					server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
				}
			}
			catch (Exception e) {
				Log.e("SynoDroid DS", "Not expected exception occured while deleting id:" + torrentP.taskId, e);
				throw e;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.protocol.DSHandler#resume(org.jared.synodroid
	 * .common.data.Torrent)
	 */
	public void resume(Task torrentP) throws Exception {
		// If we are logged on
		if (server.isConnected()) {
			QueryBuilder getAllRequest = new QueryBuilder().add("action", "resume").add("idList", "" + torrentP.taskId);
			// Execute
			synchronized (server) {
				server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.protocol.DSHandler#stop(org.jared.synodroid.
	 * common.data.Torrent)
	 */
	public void stop(Task torrentP) throws Exception {
		// If we are logged on
		if (server.isConnected()) {
			QueryBuilder getAllRequest = new QueryBuilder().add("action", "stop").add("idList", "" + torrentP.taskId);
			// Execute
			synchronized (server) {
				server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.protocol.DSHandler#getDetails(org.jared.synodroid
	 * .common.data.Task)
	 */
	public HashMap<String, String> getDetails(Task taskP) throws Exception {
		HashMap<String, String> result = new HashMap<String, String>();
		// If we are logged on
		if (server.isConnected()) {
			QueryBuilder getAllRequest = new QueryBuilder().add("action", "getone").add("taskid", "" + taskP.taskId).add("update", "1");
			// Execute
			synchronized (server) {
				JSONObject json = server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
				boolean success = json.getBoolean("success");
				// If successful then build details list
				if (success) {
					JSONObject data = json.getJSONObject("data");
					JSONArray arrayNames = data.names();
					for(int iLoop=0; iLoop<arrayNames.length(); iLoop++) {
						result.put(arrayNames.getString(iLoop), data.getString(arrayNames.getString(iLoop)));
					}
				}
				// Otherwise throw a exception
				else {
					throw new DSMException(json.getString("reason"));
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.protocol.DSHandler#upload(android.net.Uri)
	 */
	public void upload(Uri uriP) throws Exception {
		// If we are logged on
		if (server.isConnected()) {
			if (uriP.getPath() != null) {
				// Create the multipart
				MultipartBuilder builder = new MultipartBuilder("-----------7dabb2d41348");

				// The field's part
				builder.addPart(new Part("field").setContent("task_id".getBytes()));
				// The direction's part
				builder.addPart(new Part("direction").setContent("ASC".getBytes()));
				// The url_http's part
				builder.addPart(new Part("url_http").setContent("".getBytes()));
				// The url_https's part
				builder.addPart(new Part("url_https").setContent("".getBytes()));
				// The url_ftp's part
				builder.addPart(new Part("url_ftp").setContent("".getBytes()));

				// The upload_type's part
				builder.addPart(new Part("upload_type").setContent("torrent".getBytes()));
				// The torrent's part
				Part filePart = new Part("torrent").addExtra("filename", uriP.getPath());
				filePart.setContentType("application/octet-stream");
				// Get the stream according to the Uri
				byte[] buffer = StreamFactory.getStream(uriP);

				// Set the content
				filePart.setContent(buffer);
				builder.addPart(filePart);
				// Execute
				synchronized (server) {
					server.sendMultiPart(DM_URI, builder);
				}
			}
		}
	}
	
	public void upload_url(Uri uriP) throws Exception {
		// If we are logged on
		if (server.isConnected()) {
			if (uriP.toString() != null) {
				// Create the multipart
				MultipartBuilder builder = new MultipartBuilder("-----------7dabb2d41348");
				
				// The field's part
				builder.addPart(new Part("field").setContent("task_id".getBytes()));
				// The direction's part
				builder.addPart(new Part("direction").setContent("ASC".getBytes()));
				
				
				if (uriP.toString().toLowerCase().startsWith("https:")){
					// The url_http's part
					builder.addPart(new Part("url_http").setContent("".getBytes()));
					// The url_https's part
					builder.addPart(new Part("url_https").setContent(uriP.toString().getBytes()));
					// The url_ftp's part
					builder.addPart(new Part("url_ftp").setContent("".getBytes()));
				}
				else if (uriP.toString().toLowerCase().startsWith("http:")){
					// The url_http's part
					builder.addPart(new Part("url_http").setContent(uriP.toString().getBytes()));
					// The url_https's part
					builder.addPart(new Part("url_https").setContent("".getBytes()));
					// The url_ftp's part
					builder.addPart(new Part("url_ftp").setContent("".getBytes()));
				}
				else if (uriP.toString().toLowerCase().startsWith("ftp:")){
					// The url_http's part
					builder.addPart(new Part("url_http").setContent("".getBytes()));
					// The url_https's part
					builder.addPart(new Part("url_https").setContent("".getBytes()));
					// The url_ftp's part
					builder.addPart(new Part("url_ftp").setContent(uriP.toString().getBytes()));	
				}
				else{
					return;
				}
				// The url_ftp's part
				builder.addPart(new Part("url").setContent(uriP.toString().getBytes()));
				// The upload_type's part
				builder.addPart(new Part("upload_type").setContent("url".getBytes()));
				
				// Execute
				synchronized (server) {
					server.sendMultiPart(DM_URI, builder);
				}
			}
		}
	}
}
