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

import java.util.ArrayList;
import java.util.List;

import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskStatus;
import org.jared.synodroid.ds.DownloadActivity;
import org.jared.synodroid.ds.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * An adaptor for torrents list. This adaptor aims to create a view for each
 * item in the listView
 * 
 * @author eric.taix at gmail.com
 */
public class TaskAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

	// List of torrent
	private List<Task> tasks = new ArrayList<Task>();
	// The XML view inflater
	private final LayoutInflater inflater;
	// The main activity
	private DownloadActivity activity;

	/**
	 * Constructor
	 * 
	 * @param activityP
	 *          The current activity
	 * @param torrentsP
	 *          List of torrent
	 */
	public TaskAdapter(DownloadActivity activityP) {
		activity = activityP;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Update the torrents list
	 * 
	 * @param torrentsP
	 */
	public void updateTasks(List<Task> tasksP) {
		tasks = tasksP;
		notifyDataSetChanged();
	}

	/**
	 * Return the count of element
	 * 
	 * @return The number of torrent in the list
	 */
	public int getCount() {
		if (tasks != null) {
			return tasks.size();
		}
		else {
			return 0;
		}
	}

	/**
	 * Return the torrent at the defined index
	 * 
	 * @param indexP
	 *          The index to use starting from 0
	 * @return Instance of Torrent
	 */
	public Object getItem(int indexP) {
		if (tasks != null) {
			if (indexP < tasks.size()) {
				return tasks.get(indexP);
			}
		}
		return null;
	}

	/**
	 * Return the item id of the item at index X
	 * 
	 * @param indexP
	 */
	public long getItemId(int indexP) {
		return tasks.get(indexP).taskId;
	}

	/**
	 * Return the view used for the item at position indexP. Always try to reuse
	 * an old view
	 */
	public View getView(int positionP, View convertViewP, ViewGroup parentP) {
		LinearLayout view = null;
		if (convertViewP != null) {
			view = (LinearLayout) convertViewP;
		}
		else {
			view = (LinearLayout) inflater.inflate(R.layout.task_template, parentP, false);
		}
		bindView(view, tasks.get(positionP));
		return view;
	}

	/**
	 * Bind torrent's data with widget
	 * 
	 * @param viewP
	 * @param torrentP
	 */
	private void bindView(LinearLayout viewP, final Task torrentP) {
		// Torrent's status icon
		ImageView image = (ImageView) viewP.findViewById(R.id.id_torrent_icon);
		IconFacade.bindTorrentStatus(activity, image, torrentP);

		// The name of the torrent
		TextView torrentName = (TextView) viewP.findViewById(R.id.id_torrent_name);
		torrentName.setText(torrentP.fileName);

		// The torrent size
		TextView torrentSize = (TextView) viewP.findViewById(R.id.id_torrent_total_size);
		torrentSize.setText(torrentP.totalSize);

		// The torrent's owner
		TextView torrentCurrentSize = (TextView) viewP.findViewById(R.id.id_torrent_username);
		torrentCurrentSize.setText(torrentP.creator);

		// The progress bar 
		ProgressBar progress = (ProgressBar) viewP.findViewById(R.id.id_torrent_progress);
		// If a known value
		if (torrentP.progress != -1 && (torrentP.progress != 100 || torrentP.status.equals(TaskStatus.TASK_SEEDING.name()))) {
			progress.setProgress(torrentP.progress);
			progress.setVisibility(View.VISIBLE);
		}
		// Otherwise hide the progress bar
		else {
			progress.setVisibility(View.INVISIBLE);
		}

		// The current rates
		TextView torrentRates = (TextView) viewP.findViewById(R.id.id_torrent_speed);
		String rates = "";
		if (torrentP.downloadRate.length() > 0) {
			rates += "D:" + torrentP.downloadRate + "    ";
		}
		if (torrentP.uploadRate.length() > 0) {
			rates += "U:" + torrentP.uploadRate;
		}
		torrentRates.setText(rates);

		// The estimated time left
		TextView torrentETA = (TextView) viewP.findViewById(R.id.id_torrent_eta);
		torrentETA.setText(torrentP.eta);
	}

	/**
	 * Click on a item
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Task task = tasks.get(position);
		if (task != null) {
			activity.onTaskClicked(task);
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Task task = tasks.get(position);
		if (task != null) {
			activity.onTaskLongClicked(task);
			return true;
		}
		return false;
	}

}
