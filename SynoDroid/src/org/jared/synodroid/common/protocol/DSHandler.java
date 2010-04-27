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
package org.jared.synodroid.common.protocol;

import java.util.List;

import org.jared.synodroid.common.data.Detail;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskContainer;

import android.net.Uri;

/**
 * This interface defines the Download Station protocol 
 * @author Eric Taix (eric.taix at gmail dot com)
 */
public interface DSHandler {
	/**
	 * Return all torrent
	 * @return
	 */
	public TaskContainer getAllTorrent(String sortAttrP, boolean ascendingP) throws Exception; 

	/**
	 * Stop a torrent
	 * @param taskP
	 * @throws DSMException
	 */
  public void stop(final Task taskP) throws Exception;
  
  /**
   * Resume a torrent
   * @param torrentP
   * @throws DSMException
   */
  public void resume(final Task taskP) throws Exception;
  
  /**
   * delete a torrent
   * @param torrentP
   * @throws DSMException
   */
  public void delete(final Task taskP) throws Exception;
  
  /**
   * Upload a file defined by an Uri
   * @param uriP
   */
  public void upload(final Uri uriP) throws Exception ;
  
  /**
   * Upload a file defined by an Uri
   * @param uriP
   */
  public void upload_url(final Uri uriP) throws Exception ;
  
  /**
   * Get task's details
   * @param uriP
   */
  public List<Detail> getDetails(final Task taskP) throws Exception ;
}
