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

import org.jared.synodroid.ds.R;

import android.content.Context;

/**
 * Here are defined all torrent'status
 * @author Eric Taix (eric.taix at gmail.com)
 */
public enum TaskStatus {
  TASK_WAITING, TASK_DOWNLOADING, TASK_SEEDING, TASK_PAUSED, TASK_FINISHED, TASK_HASH_CHECKING, TASK_ERROR, TASK_ERROR_BROKEN_LINK, TASK_ERROR_DISK_FULL;


  /**
   * Return a localized status label
   * @param ctxP
   * @param statusP
   * @return
   */
  public static String getLabel(Context ctxP, String statusP) {
  	TaskStatus status = TaskStatus.valueOf(statusP);
  	switch(status) {
  	case TASK_WAITING: return ctxP.getString(R.string.detail_status_waiting);
  	case TASK_DOWNLOADING: return ctxP.getString(R.string.detail_status_downloading);
  	case TASK_SEEDING: return ctxP.getString(R.string.detail_status_seeding);
  	case TASK_PAUSED: return ctxP.getString(R.string.detail_status_paused);
  	case TASK_FINISHED: return ctxP.getString(R.string.detail_status_finished);
  	case TASK_HASH_CHECKING: return ctxP.getString(R.string.detail_status_hash_checking);
  	case TASK_ERROR: 
  	case TASK_ERROR_BROKEN_LINK:
  	case TASK_ERROR_DISK_FULL:
  	default: return ctxP.getString(R.string.detail_status_error);
  	}
  }
}