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
import org.jared.synodroid.common.data.TaskProperties;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.ds.R;

/**
 * Update a task (files and parameters)
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class GetTaskPropertiesAction implements SynoAction {

	// The task to update
	private Task task;

	/**
	 * Constructor
	 * 
	 * @param taskP
	 * @param filesP
	 * @param seedingRatioP
	 * @param seedingIntervalP
	 */
	public GetTaskPropertiesAction(Task taskP) {
		task = taskP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.action.SynoAction#execute(org.jared.synodroid.common.protocol.ResponseHandler, org.jared.synodroid.common.SynoServer)
	 */
	public void execute(ResponseHandler handlerP, SynoServer serverP) throws Exception {
		TaskProperties tp = serverP.getDSMHandlerFactory().getDSHandler().getTaskProperty(task);
		serverP.fireMessage(handlerP, ResponseHandler.MSG_PROPERTIES_RECEIVED, tp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.action.SynoAction#getName()
	 */
	public String getName() {
		return "Gathering properties for task " + task.taskId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.action.SynoAction#getTask()
	 */
	public Task getTask() {
		return task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.action.SynoAction#getToastId()
	 */
	public int getToastId() {
		return R.string.action_task_prop;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.action.SynoAction#isToastable()
	 */
	public boolean isToastable() {
		return true;
	}

}
