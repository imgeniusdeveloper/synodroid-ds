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
import org.jared.synodroid.ds.R;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
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
	public static void bindTorrentStatus(Context ctxP, ImageView viewP, Task torrentP) {
		//Trap invalid task status and replace by unknown
		int id = 0;
		Animation animation = null;
		switch (torrentP.getStatus()) {
		case TASK_DOWNLOADING:
			id = R.drawable.dl_download;
			break;
		case TASK_PRE_SEEDING:
		case TASK_SEEDING:
			id = R.drawable.dl_upload;
			break;
		case TASK_PAUSED:
			id = R.drawable.dl_paused;
			break;
		case TASK_WAITING:
			id = R.drawable.dl_wait;
			break;
		case TASK_HASH_CHECKING:
			id = R.drawable.dl_hash;
			animation = AnimationUtils.loadAnimation(ctxP, R.anim.rotate_indefinitely);
			// Can not be set in the XML (LinearInterpolar is not public: arghhh)
			animation.setInterpolator(new LinearInterpolator());
			break;
		case TASK_FINISHING:
		case TASK_FINISHED:
			id = R.drawable.dl_finished;
			break;
		case TASK_UNKNOWN:
		case TASK_ERROR:
		case TASK_ERROR_DEST_NO_EXIST:
	  	case TASK_ERROR_DEST_DENY:
	  	case TASK_ERROR_QUOTA_REACHED:
	  	case TASK_ERROR_TIMEOUT:
	  	case TASK_ERROR_EXCEED_MAX_FS_SIZE:
	  	case TASK_ERROR_BROKEN_LINK:
		case TASK_ERROR_DISK_FULL:
		case TASK_ERROR_EXCEED_MAX_TEMP_FS_SIZE:
		case TASK_ERROR_EXCEED_MAX_DEST_FS_SIZE:
			id = R.drawable.dl_error;
			break;
		}
		if (animation != null) {
			viewP.startAnimation(animation);
		}
		// Stop the previous
		else {
			Animation previousAnimation = viewP.getAnimation();
			if (previousAnimation != null) {
				previousAnimation.setDuration(0);
				viewP.clearAnimation();
			}
		}
		viewP.setImageResource(id);
	}
}
