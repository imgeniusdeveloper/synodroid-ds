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

import org.jared.synodroid.common.data.Detail;
import org.jared.synodroid.ds.DetailActivity;
import org.jared.synodroid.ds.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * An adaptor for task's details. This adaptor aims to create a view for each
 * detail in the listView
 * 
 * @author eric.taix at gmail.com
 */
public class DetailAdapter extends BaseAdapter {

	// List of detail
	private List<Detail> details = new ArrayList<Detail>();
	// The XML view inflater
	private final LayoutInflater inflater;
	// The main activity
	private DetailActivity activity;

	/**
	 * Constructor
	 * 
	 * @param activityP
	 *          The current activity
	 * @param torrentsP
	 *          List of torrent
	 */
	public DetailAdapter(DetailActivity activityP) {
		activity = activityP;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Update the torrents list
	 * 
	 * @param torrentsP
	 */
	public void updateDetails(List<Detail> detailsP) {
		details = detailsP;
		notifyDataSetChanged();
	}

	/**
	 * Return the count of element
	 * 
	 * @return The number of torrent in the list
	 */
	public int getCount() {
		if (details != null) {
			return details.size();
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
		if (details != null) {
			if (indexP < details.size()) {
				return details.get(indexP);
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
		return indexP;
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
			view = (LinearLayout) inflater.inflate(R.layout.details_template, parentP, false);
		}
		bindView(view, details.get(positionP));
		return view;
	}

	/**
	 * Bind torrent's data with widget
	 * 
	 * @param viewP
	 * @param torrentP
	 */
	private void bindView(LinearLayout viewP, final Detail detailP) {
  	// The name of the detail
		TextView name = (TextView) viewP.findViewById(R.id.id_detail_name);
		name.setText(detailP.getName());

		// The value of the detail
		TextView value = (TextView) viewP.findViewById(R.id.id_detail_value);
		value.setText(detailP.getValue());
	}
}
