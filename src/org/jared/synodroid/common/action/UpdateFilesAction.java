/**
 * Copyright 2010 Steve Garon
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

import java.util.List;

import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskFile;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.ds.R;

/**
 * Retrieve task's files
 * 
 * @author Steve Garon (steve.garon at gmail dot com)
 */
public class UpdateFilesAction implements SynoAction {

	// The torrent to resume
	private Task task;
	private List<TaskFile> files;

	public UpdateFilesAction(Task taskP, List<TaskFile> filesP) {
		task = taskP;
		files = filesP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.SynoAction#execute(org.jared.synodroid.ds.TorrentListActivity, org.jared.synodroid.common.SynoServer)
	 */
	public void execute(ResponseHandler handlerP, SynoServer serverP) throws Exception {
		serverP.getDSMHandlerFactory().getDSHandler().setFilePriority(task, files);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.SynoAction#getName()
	 */
	public String getName() {
		return "Updating task " + task.taskId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.SynoAction#getToastId()
	 */
	public int getToastId() {
		return R.string.action_updating;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.SynoAction#isToastable()
	 */
	public boolean isToastable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.ds.action.TaskAction#getTask()
	 */
	public Task getTask() {
		return task;
	}

}
