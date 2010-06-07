/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.common.protocol;

import java.util.List;

import org.jared.synodroid.common.data.SharedDirectory;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskContainer;
import org.jared.synodroid.common.data.TaskDetail;
import org.jared.synodroid.common.data.TaskFile;

import android.net.Uri;

/**
 * This interface defines the Download Station protocol
 * 
 * @author Eric Taix (eric.taix at gmail dot com)
 */
public interface DSHandler {

  /**
   * Return the base URL to request the DownloadStation without '/' at the end
   * @return
   */
  public String getBaseURL();
  
  /**
   * Return all torrent
   * 
   * @return
   */
  public TaskContainer getAllTask(String sortAttrP, boolean ascendingP) throws Exception;

  /**
   * Stop a torrent
   * 
   * @param taskP
   * @throws DSMException
   */
  public void stop(final Task taskP) throws Exception;

  /**
   * Resume a torrent
   * 
   * @param torrentP
   * @throws DSMException
   */
  public void resume(final Task taskP) throws Exception;

  /**
   * delete a torrent
   * 
   * @param torrentP
   * @throws DSMException
   */
  public void delete(final Task taskP) throws Exception;

  /**
   * Clear all finished tasks
   * @throws Exception
   */
  public void clearAll() throws Exception;
  
  /**
   * Upload a file defined by an Uri
   * 
   * @param uriP
   */
  public void upload(final Uri uriP) throws Exception;

  /**
   * Upload a file defined by an Uri
   * 
   * @param uriP
   */
  public void uploadUrl(final Uri uriP) throws Exception;

  /**
   * Get task's raw details
   * 
   * @param uriP
   */
  public TaskDetail getDetails(final Task taskP) throws Exception;

  /**
   * Get the file list of the specified task
   * 
   * @param taskP
   * @return
   * @throws Exception
   */
  public List<TaskFile> getFiles(final Task taskP) throws Exception;

  /**
   * Update a task
   * 
   * @param taskP The task to update
   * @param filesP the file list (only modified files)
   * @param seedingRatioP The new seeding ratio in %
   * @param seedingIntervalP The new seeding interval (in minutes)
   */
  public void updateTask(final Task taskP, List<TaskFile> filesP, int seedingRatioP, int seedingIntervalP)
          throws Exception;

  /**
   * Retrieve all shared directories according to the user's autorizations
   * 
   * @return
   * @throws Exception
   */
  public List<SharedDirectory> enumSharedDirectory() throws Exception;

  /**
   * Set the new shared directory for all non finished downloads
   * 
   * @param directoryP
   * @throws Exception
   */
  public void setSharedDirectory(Task taskP, String directoryP) throws Exception;
  
  /**
   * Download a specific URL
   * @param urlP
   */
  public StringBuffer getOriginalFile(Task taskP) throws Exception;  
  
}
