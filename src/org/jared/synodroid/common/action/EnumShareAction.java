/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.common.action;

import java.util.List;

import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.SharedDirectory;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.ds.R;

/**
 * Enum all shared directories
 * 
 * @author Eric Taix (eric.taix at gmail dot com)
 */
public class EnumShareAction implements SynoAction {

	public EnumShareAction() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.SynoAction#execute(org.jared.synodroid.ds.TorrentListActivity, org.jared.synodroid.common.SynoServer)
	 */
	public void execute(ResponseHandler handlerP, SynoServer serverP) throws Exception {
		List<SharedDirectory> dirs = serverP.getDSMHandlerFactory().getDSHandler().enumSharedDirectory();
		String dir = serverP.getDSMHandlerFactory().getDSHandler().getSharedDirectory();
		if (dir != null) {
			SharedDirectory foo = new SharedDirectory(dir);
			int index = dirs.indexOf(foo);
			if (index != -1) {
				dirs.get(index).isCurrent = true;
			}
		}
		serverP.fireMessage(handlerP, ResponseHandler.MSG_SHARED_DIRECTORIES_RETRIEVED, dirs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.SynoAction#getName()
	 */
	public String getName() {
		return "Enum shared directories";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.SynoAction#getToastId()
	 */
	public int getToastId() {
		return R.string.action_enum_shared;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.SynoAction#isToastable()
	 */
	public boolean isToastable() {
		return true;
	}

	/**
	 * @return the task
	 */
	public Task getTask() {
		return null;
	}

}
