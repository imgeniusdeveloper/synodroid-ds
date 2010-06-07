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
package org.jared.synodroid.common.action;

import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.ds.R;

/**
 * Download the original file
 * 
 * @author Eric Taix (eric.taix at gmail dot com)
 */
public class DownloadOriginalLinkAction implements SynoAction {

	// The original file's link
	private String link;
	
	public DownloadOriginalLinkAction(String linkP) {
		link = linkP;
	}

	/* (non-Javadoc)
   * @see org.jared.synodroid.common.SynoAction#execute(org.jared.synodroid.ds.TorrentListActivity, org.jared.synodroid.common.SynoServer)
   */
  public void execute(ResponseHandler handlerP, SynoServer serverP) throws Exception {
  	String data = serverP.getDSMHandlerFactory().getDSHandler().getOriginalFile(link);
    serverP.fireMessage(handlerP, ResponseHandler.MSG_ORIGINAL_FILE_RETRIEVED, data);
  }

	/* (non-Javadoc)
   * @see org.jared.synodroid.common.SynoAction#getName()
   */
  public String getName() {
	  return "Download original file: "+link;
  }

	/* (non-Javadoc)
   * @see org.jared.synodroid.common.SynoAction#getToastId()
   */
  public int getToastId() {
	  return R.string.action_download_original_file;
  }

	/* (non-Javadoc)
   * @see org.jared.synodroid.common.SynoAction#isToastable()
   */
  public boolean isToastable() {
	  return true;
  }

	/* (non-Javadoc)
   * @see org.jared.synodroid.ds.action.TaskAction#getTask()
   */
  public Task getTask() {
	  return null;
  }
	
}
