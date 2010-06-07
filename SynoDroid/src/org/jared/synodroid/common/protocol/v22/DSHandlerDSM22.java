/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.common.protocol.v22;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.SharedDirectory;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskContainer;
import org.jared.synodroid.common.data.TaskDetail;
import org.jared.synodroid.common.data.TaskFile;
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

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#getBaseURL()
   */
  public String getBaseURL() {
    return server.getUrl()+"/"+DM_URI;
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.Protocol#getAllTask()
   */
  public TaskContainer getAllTask(String sortAttrP, boolean ascendingP) throws Exception {
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
          // Create the task item
          Task task = new Task();
          task.fileName = item.getString("filename").trim();
          task.taskId = item.getInt("task_id");
          task.downloadRate = item.getString("current_rate").trim();
          task.downloadSize = item.getString("current_size").trim();
          String prog = item.getString("progress").trim();
          int index = prog.indexOf("%");
          // If a value could be found ('%' found)
          if (index != -1) {
            prog = prog.substring(0, index);
            try {
              task.progress = (int) Float.parseFloat(prog);
            }
            catch(NumberFormatException ex) {
              // Set to unknown
              task.progress = -1;
            }
          }
          // Set to unknown
          else {
            task.progress = -1;
          }
          task.status = item.getString("status");
          task.eta = Utils.computeTimeLeft(item.getInt("timeleft"));
          task.totalSize = item.getString("total_size").trim();
          task.uploadRate = item.getString("upload_rate").trim();
          task.creator = item.getString("username").trim();
          if (task.creator == "") {
            task.creator = server.getUser();
          }
          task.server = server;
          result.add(task);
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
   * @see org.jared.synodroid.common.protocol.DSHandler#delete(org.jared.synodroid .common.data.Task)
   */
  public void delete(Task taskP) throws Exception {
    // If we are logged on
    if (server.isConnected()) {
      try {
        String tID = "";
        String action = "clear";
        if (taskP.taskId != -1) {
          tID += taskP.taskId;
          action = "delete";
        }
        QueryBuilder getAllRequest = new QueryBuilder().add("action", action).add("idList", tID);
        // Execute
        synchronized (server) {
          server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
        }
      }
      catch(Exception e) {
        Log.e("SynoDroid DS", "Not expected exception occured while deleting id:" + taskP.taskId, e);
        throw e;
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#resume(org.jared.synodroid .common.data.Task)
   */
  public void resume(Task taskP) throws Exception {
    // If we are logged on
    if (server.isConnected()) {
      QueryBuilder getAllRequest = new QueryBuilder().add("action", "resume").add("idList", "" + taskP.taskId);
      // Execute
      synchronized (server) {
        server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#stop(org.jared.synodroid. common.data.Task)
   */
  public void stop(Task taskP) throws Exception {
    // If we are logged on
    if (server.isConnected()) {
      QueryBuilder getAllRequest = new QueryBuilder().add("action", "stop").add("idList", "" + taskP.taskId);
      // Execute
      synchronized (server) {
        server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#getFiles(org.jared.synodroid .common.data.Task)
   */
  public List<TaskFile> getFiles(Task taskP) throws Exception {
    ArrayList<TaskFile> result = new ArrayList<TaskFile>();
    // If we are logged on
    if (server.isConnected()) {
      QueryBuilder getAllRequest = new QueryBuilder().add("action", "getfilelist").add("taskid", "" + taskP.taskId);
      // Execute
      JSONObject json = null;
      synchronized (server) {
        json = server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
      }
      boolean success = json.getBoolean("success");
      // If request succeded
      if (success) {
        JSONArray array = json.getJSONArray("items");
        for (int iLoop = 0; iLoop < array.length(); iLoop++) {
          JSONObject obj = array.getJSONObject(iLoop);
          // Create the file
          TaskFile file = new TaskFile();
          file.name = obj.getString("name");
          file.filesize = obj.getString("size");
          file.download = obj.getBoolean("dl");
          result.add(file);
        }
        array.length();
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#getDetails(org.jared.synodroid .common.data.Task)
   */
  public TaskDetail getDetails(Task taskP) throws Exception {
    TaskDetail result = new TaskDetail();
    // If we are logged on
    if (server.isConnected()) {
      QueryBuilder getAllRequest = new QueryBuilder().add("action", "getone").add("taskid", "" + taskP.taskId).add(
              "update", "1");
      // Execute
      JSONObject json = null;
      synchronized (server) {
        json = server.sendJSONRequest(DM_URI, getAllRequest.toString(), "GET");
      }
      boolean success = json.getBoolean("success");
      // If successful then build details list
      if (success) {
        JSONObject data = json.getJSONObject("data");
        if (data.has("stime")) result.seedingDate = data.getString("stime");
        if (data.has("totalpeer")) result.peersTotal = Utils.toLong(data.getString("totalpeer"));
        if (data.has("currpeer")) result.peersCurrent = Utils.toLong(data.getString("currpeer"));
        if (data.has("istorrent")) result.isTorrent = data.getBoolean("istorrent");
        if (data.has("speed")) {
          Pattern p = Pattern.compile("(((\\d)*\\.(\\d)*) KB/s)");
          Matcher m = p.matcher(data.getString("speed"));
          if (m.find() && m.groupCount() >= 2) {
            result.speedUpload = Utils.toDouble(m.group(2));
          }
          if (m.find() && m.groupCount() >= 2) {
            result.speedDownload = Utils.toDouble(m.group(2));
          }
          else {
            result.speedDownload = result.speedUpload;
          }
        }
        if (data.has("filename")) result.fileName = data.getString("filename");
        if (data.has("username")) result.userName = data.getString("username");
        if (data.has("totalpieces")) result.piecesTotal = Utils.toLong(data.getString("totalpieces"));
        if (data.has("transfered")) {
          Pattern p = Pattern.compile("((\\d*\\.\\d*)\\s[KMGT]B)");
          Matcher m = p.matcher(data.getString("transfered"));
          if (m.find() && m.groupCount() >= 1) {
            result.bytesUploaded = Utils.fileSizeToBytes(m.group(1));
          }
          if (m.find() && m.groupCount() >= 1) {
            result.bytesDownloaded = Utils.fileSizeToBytes(m.group(1));
          }
          else {
            result.bytesDownloaded = result.bytesUploaded;
          }
        }
        if (data.has("seedelapsed")) result.seedingElapsed = data.getInt("seedelapsed");
        if (data.has("isnzb")) result.isNZB = data.getBoolean("isnzb");
        if (data.has("destination")) result.destination = data.getString("destination");
        if (data.has(("url"))) result.url = data.getString("url");
        if (data.has("ctime")) result.creationDate = data.getString("ctime");
        if (data.has("status")) result.status = data.getString("status");
        if (data.has("seeding_interval")) result.seedingInterval = data.getInt("seeding_interval");
        if (data.has("currpieces")) result.piecesCurrent = Utils.toLong(data.getString("currpieces"));
        if (data.has("id")) result.taskId = data.getInt("id");
        if (data.has("seeding_ratio")) result.seedingRatio = data.getInt("seeding_ratio");
        if (data.has("filesize")) result.fileSize = Utils.fileSizeToBytes(data.getString("filesize"));
        if (data.has("seeders_leechers")) {
          Pattern p = Pattern.compile("(\\d+)(/)(\\d+)");
          String v = data.getString("seeders_leechers");
          Matcher m = p.matcher(v);
          if (m.find()) {
            if (m.groupCount() >= 1) result.seeders = Utils.toLong(m.group(1));
            if (m.groupCount() >= 3) result.leechers = Utils.toLong(m.group(3));
          }
        }
      }
      // Otherwise throw a exception
      else {
        String reason = "";
        if (json.has("reason")) {
          reason = json.getString("reason");
        }
        else if (json.has("errno")) {
          JSONObject err = json.getJSONObject("errno");
          reason = err.getString("key");
        }
        throw new DSMException(reason);
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
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

  public void uploadUrl(Uri uriP) throws Exception {
    // If we are logged on
    if (server.isConnected()) {
      if (uriP.toString() != null) {
        // Create the multipart
        MultipartBuilder builder = new MultipartBuilder("-----------7dabb2d41348");

        // The field's part
        builder.addPart(new Part("field").setContent("task_id".getBytes()));
        // The direction's part
        builder.addPart(new Part("direction").setContent("ASC".getBytes()));

        if (uriP.toString().toLowerCase().startsWith("https:")) {
          // The url_http's part
          builder.addPart(new Part("url_http").setContent("".getBytes()));
          // The url_https's part
          builder.addPart(new Part("url_https").setContent(uriP.toString().getBytes()));
          // The url_ftp's part
          builder.addPart(new Part("url_ftp").setContent("".getBytes()));
        }
        else if (uriP.toString().toLowerCase().startsWith("http:")) {
          // The url_http's part
          builder.addPart(new Part("url_http").setContent(uriP.toString().getBytes()));
          // The url_https's part
          builder.addPart(new Part("url_https").setContent("".getBytes()));
          // The url_ftp's part
          builder.addPart(new Part("url_ftp").setContent("".getBytes()));
        }
        else if (uriP.toString().toLowerCase().startsWith("ftp:")) {
          // The url_http's part
          builder.addPart(new Part("url_http").setContent("".getBytes()));
          // The url_https's part
          builder.addPart(new Part("url_https").setContent("".getBytes()));
          // The url_ftp's part
          builder.addPart(new Part("url_ftp").setContent(uriP.toString().getBytes()));
        }
        else {
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

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#updateTask(org.jared.synodroid .common.data.Task,
   * java.util.List, int, int)
   */
  public void updateTask(Task taskP, List<TaskFile> filesP, int seedingRatioP, int seedingIntervalP) throws Exception {
    // Create the JSON request
    QueryBuilder updateTaskRequest = new QueryBuilder().add("action", "applytask").add("taskid", "" + taskP.taskId)
            .add("update", "1");
    // If files update is needed
    if (filesP != null && filesP.size() > 0) {
      JSONObject data = new JSONObject();
      JSONArray datas = new JSONArray();
      for (TaskFile taskFile : filesP) {
        JSONObject file = new JSONObject();
        file.put("name", taskFile.name);
        file.put("dl", taskFile.download);
        datas.put(file);
      }
      data.put("data", datas);
      updateTaskRequest.add("fsel", data.toString());
    }
    updateTaskRequest.add("seeding_ratio", "" + seedingRatioP);
    updateTaskRequest.add("seeding_interval", "" + seedingIntervalP);
    // Execute it to the server
    JSONObject json = null;
    synchronized (server) {
      json = server.sendJSONRequest(DM_URI, updateTaskRequest.toString(), "POST");
    }
    boolean success = json.getBoolean("success");
    // If not successful then throw an exception
    if (!success) {
      String reason = "";
      if (json.has("reason")) {
        reason = json.getString("reason");
      }
      else if (json.has("errno")) {
        JSONObject err = json.getJSONObject("errno");
        reason = err.getString("key");
      }
      throw new DSMException(reason);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#enumSharedDirectory()
   */
  public List<SharedDirectory> enumSharedDirectory() throws Exception {
    List<SharedDirectory> result = new ArrayList<SharedDirectory>();
    // Create the JSON request
    QueryBuilder updateTaskRequest = new QueryBuilder().add("action", "enumshares");
    // Execute it to the server
    JSONObject json = null;
    synchronized (server) {
      json = server.sendJSONRequest(DM_URI, updateTaskRequest.toString(), "GET");
    }
    boolean success = json.getBoolean("success");
    // If request succeded
    if (success) {
      JSONArray array = json.getJSONArray("items");
      for (int iLoop = 0; iLoop < array.length(); iLoop++) {
        JSONObject obj = array.getJSONObject(iLoop);
        // Create the file
        SharedDirectory dir = new SharedDirectory(obj.getString("name"));
        dir.description = obj.getString("description");
        result.add(dir);
      }
    }
    // If not successful then throw an exception
    else {
      String reason = "";
      if (json.has("reason")) {
        reason = json.getString("reason");
      }
      else if (json.has("errno")) {
        JSONObject err = json.getJSONObject("errno");
        reason = err.getString("key");
      }
      throw new DSMException(reason);
    }
    return result;
  }

	/* (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#setSharedDirectory(java.lang.String)
   */
  public void setSharedDirectory(Task taskP, String directoryP) throws Exception {
    // If we are logged on
    if (server.isConnected()) {
      QueryBuilder setShared = new QueryBuilder().add("action", "shareset").add("share", directoryP);
      if (taskP != null) {
      	setShared.add("taskid", "" + taskP.taskId);
      }
      // Execute
      synchronized (server) {
        server.sendJSONRequest(DM_URI, setShared.toString(), "POST");
      }
    }
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.protocol.DSHandler#getOriginalLink(org.jared.synodroid.common.data.Task)
   */
  public String getOriginalFile(String linkP) throws Exception {
    String result = null;
    // If we are logged on
    if (server.isConnected()) {
      QueryBuilder setShared = new QueryBuilder().add("torrent", linkP);
      // Execute
      synchronized (server) {
        result = server.download(DM_URI, setShared.toString());
      }
    }
    return result;
  }

  
  
}
