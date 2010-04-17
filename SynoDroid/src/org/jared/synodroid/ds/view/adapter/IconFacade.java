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
package org.jared.synodroid.ds.view.adapter;

import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskStatus;
import org.jared.synodroid.ds.R;

import android.widget.ImageView;

/**
 * A simple facade which bind a icon according to a torent status
 * 
 * @author eric.taix @ gmail.com
 * 
 */
public class IconFacade {

	/**
	 * Set the image according to the torrent status
	 * 
	 * @param viewP
	 * @param siteP
	 */
	public static void bindTorrentStatus(ImageView viewP, Task torrentP) {
		TaskStatus status = TaskStatus.valueOf(torrentP.status);
		int id = 0;
		switch (status) {
		case TASK_DOWNLOADING:
			id = R.drawable.dl_download;
			break;
		case TASK_SEEDING:
			id = R.drawable.dl_upload;
			break;
		case TASK_PAUSED:
			id = R.drawable.dl_paused;
			break;
		case TASK_WAITING:
			id = R.drawable.dl_waiting;
			break;
		case TASK_HASH_CHECKING:
			id = R.drawable.dl_waiting;
			break;
		case TASK_FINISHED:
			id = R.drawable.dl_finish;
			break;
		}
		viewP.setImageResource(id);
	}
}
