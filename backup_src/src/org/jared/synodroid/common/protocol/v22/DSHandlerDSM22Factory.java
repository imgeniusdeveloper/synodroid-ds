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
package org.jared.synodroid.common.protocol.v22;

import org.jared.synodroid.Synodroid;
import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.protocol.DSHandler;
import org.jared.synodroid.common.protocol.DSMException;
import org.jared.synodroid.common.protocol.DSMHandlerFactory;
import org.jared.synodroid.common.protocol.QueryBuilder;
import org.json.JSONObject;

import android.util.Log;

/**
 * The factory implementation for DSM v2.2
 * 
 * @author Eric Taix (eric.taix at gmail dot com)
 */
public class DSHandlerDSM22Factory extends DSMHandlerFactory {

	/* Login's constants */
	private static final String LOGIN_PASSWORD_KEY = "passwd";
	private static final String LOGIN_USERNAME_KEY = "username";
	private static final String LOGIN_URI = "/webman/modules/login.cgi";
	private static final String LOGIN_RESULT_KEY = "result";
	private static final String LOGIN_ERROR_REASON = "reason";
	private static final String LOGIN_RESULT_SUCCESS = "success";

	// The Synology's server
	private SynoServer server;
	// Download station handler
	private DSHandler dsHandler;

	/**
	 * Constructor for the DSM 2.2 handler
	 * 
	 * @param serverP
	 *          The synology server
	 */
	public DSHandlerDSM22Factory(SynoServer serverP) {
		server = serverP;
		dsHandler = new DSHandlerDSM22(serverP);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jared.synodroid.common.protocol.DSMHandlerFactory#connect(org.jared
	 * .synodroid.common.SynoServer)
	 */
	@Override
	public void connect() throws Exception {
		String result = null;
		String reason = null;
		String pass = server.getPassword();
		QueryBuilder builder = new QueryBuilder().add(LOGIN_USERNAME_KEY, server.getUser()).add(LOGIN_PASSWORD_KEY, pass);
		JSONObject respJSO = server.sendJSONRequest(LOGIN_URI, builder.toString(), "POST");
		Log.d(Synodroid.DS_TAG, "JSON response is:" + respJSO);
		result = respJSO.getString(LOGIN_RESULT_KEY);
		// If no success or not login success
		if (result == null || !result.equals(LOGIN_RESULT_SUCCESS)) {
			reason = respJSO.getString(LOGIN_ERROR_REASON);
			throw new DSMException(reason);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jared.synodroid.common.protocol.DSMHandlerFactory#getDSHandler()
	 */
	@Override
	public DSHandler getDSHandler() {
		return dsHandler;
	}

}
