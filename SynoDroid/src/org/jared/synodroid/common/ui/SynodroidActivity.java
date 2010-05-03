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
package org.jared.synodroid.common.ui;

import org.jared.synodroid.ds.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * The base class of an activity in Synodroid
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public abstract class SynodroidActivity extends Activity {

	// The inflater
	private LayoutInflater inflater;
	
	/**
	 * Activity creation
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Create the main view of this activity
		setContentView(R.layout.base_activity);

		// Create the main inflater
    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Retrieve the title bar
		RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.id_title_bar);
		attachTitleView(inflater, titleBar);

		// Retrieve the status bar
		RelativeLayout statusBar = (RelativeLayout) findViewById(R.id.id_status_bar);
		attachStatusView(inflater, statusBar);

		// Retrieve the main content
		RelativeLayout mainContent = (RelativeLayout) findViewById(R.id.id_main_content);
		attachMainContentView(inflater, mainContent);
	}

	/**
	 * Return the view which will be added to the titleBar
	 * 
	 * @return
	 */
	public abstract void attachTitleView(LayoutInflater inflaterP, ViewGroup parentP);

	/**
	 * Return the view which will be added to the statusBar
	 * 
	 * @return
	 */
	public abstract void attachStatusView(LayoutInflater inflaterP, ViewGroup parentP);

	/**
	 * Return the view which will be added to the main content
	 * 
	 * @return
	 */
	public abstract void attachMainContentView(LayoutInflater inflaterP, ViewGroup parentP);

}
