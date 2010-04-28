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
package org.jared.synodroid.ds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jared.synodroid.common.data.Detail;
import org.jared.synodroid.common.data.TaskStatus;
import org.jared.synodroid.ds.view.adapter.DetailAdapter;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TabHost;

/**
 * This activity displays a task's details
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class DetailActivity extends TabActivity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);

		// Create tabs and associate tab-content
		TabHost tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec("tab_test1").setIndicator(getString(R.string.detail_tab_general), getResources().getDrawable(R.drawable.icon_general)).setContent(R.id.general_layout));
		tabHost.addTab(tabHost.newTabSpec("tab_test2").setIndicator(getString(R.string.detail_tab_transfer), getResources().getDrawable(R.drawable.icon_transfert)).setContent(R.id.transfert_layout));
		tabHost.addTab(tabHost.newTabSpec("tab_test3").setIndicator(getString(R.string.detail_tab_files), getResources().getDrawable(R.drawable.icon_files)).setContent(R.id.files_layout));
		// Set the default tab
		tabHost.setCurrentTab(0);

		// Get the details
		Intent intent = getIntent();
		HashMap<String, String> rawDetails = (HashMap<String, String>) intent.getSerializableExtra("org.jared.synodroid.ds.Details");

		// Build the general tab
		ListView genListView = (ListView) findViewById(R.id.general_layout);
		DetailAdapter genAdapter = new DetailAdapter(this);
		genListView.setAdapter(genAdapter);
		genAdapter.updateDetails(buildGeneralDetails(rawDetails));

		// Build the transfer tab
		ListView transListView = (ListView) findViewById(R.id.transfert_layout);
		DetailAdapter transAdapter = new DetailAdapter(this);
		transListView.setAdapter(transAdapter);
		transAdapter.updateDetails(buildTransferDetails(rawDetails));
	}

	/**
	 * Return a sub detail list for the general's tab
	 */
	private List<Detail> buildGeneralDetails(HashMap<String, String> rawDetails) {
		ArrayList<Detail> result = new ArrayList<Detail>();
		// FileName
		result.add(new Detail(getString(R.string.detail_filename), rawDetails.get("filename")));
		// Destination
		result.add(new Detail(getString(R.string.detail_destination), rawDetails.get("destination")));
		// File size
		result.add(new Detail(getString(R.string.detail_filesize), rawDetails.get("filesize")));
		// Creation time
		result.add(new Detail(getString(R.string.detail_creationtime), Utils.computeDate(rawDetails.get("ctime"))));
		// URL
		result.add(new Detail(getString(R.string.detail_url), rawDetails.get("url")));
		// Username
		result.add(new Detail(getString(R.string.detail_username), rawDetails.get("username")));
		return result;
	}

	/**
	 * Return a sub detail list for the general's tab
	 */
	private List<Detail> buildTransferDetails(HashMap<String, String> rawDetails) {
		ArrayList<Detail> result = new ArrayList<Detail>();

		// Status
		result.add(new Detail(getString(R.string.detail_status), TaskStatus.getLabel(this, rawDetails.get("status"))));
		// Transfered
		String upProgress = "";
		String transfered = rawDetails.get("transfered");
		if (transfered != null) {
			int index = transfered.indexOf("(");
			if (index != -1) {
				upProgress = transfered.substring(index + 1, transfered.length() - 1);
				transfered = transfered.substring(0, index - 1);
			}
			transfered = transfered.replace("/", "-");
		}
		result.add(new Detail(getString(R.string.detail_transfered), transfered));
		// Progress
		result.add(new Detail(getString(R.string.detail_progress), upProgress + " - " + rawDetails.get("progress")));
		// Speed
		String speed = rawDetails.get("speed");
		if (speed != null) {
			speed = speed.replace("KB/s", "KBS");
			speed = speed.replace("/", "-");
			speed = speed.replace("KBS", "KB/s");
		}
		result.add(new Detail(getString(R.string.detail_speed), speed));
		// Peers
		String peers = rawDetails.get("currpeer") + " - " + rawDetails.get("totalpeer");
		result.add(new Detail(getString(R.string.detail_peers), peers));
		// Seeders / Leechers
		String seeders = rawDetails.get("seeders_leechers");
		if (seeders != null) {
			seeders = seeders.replace("/", " - ");
		}
		result.add(new Detail(getString(R.string.detail_seeders_leechers), seeders));
		// Starttime /left time
		String time = Utils.computeDate(rawDetails.get("stime")) + " - " + Utils.computeTimeLeft(rawDetails.get("timeleft"));
		result.add(new Detail(getString(R.string.detail_starttime), time));
		// Pieces
		String pieces = rawDetails.get("currpieces") + " - " + rawDetails.get("totalpieces");
		result.add(new Detail(getString(R.string.detail_pieces), pieces));
		return result;
	}
}
