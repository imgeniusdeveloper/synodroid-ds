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
package org.jared.synodroid.common.data;

import org.jared.synodroid.common.SynoServer;

/**
 * A simple data container for a torrent. This class is used to display
 * 'general' information about a torrent file.<br/>
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class Task {

	// The server from which is torrent is retrieved
	public SynoServer server;
	
	// The unique ID of the torrent
	public int taskId;
	// The filename
	public String fileName;	
	// Total size
	public String totalSize;
	// Creator's name
	public String creator;
	// Upload rate
	public String uploadRate;
	// Download rate
	public String downloadRate;
	// Current size downloaded
	public String downloadSize;
	// Progress
	public int progress;
	// Status
	public String status;
	// Time left
	public String eta;

}
