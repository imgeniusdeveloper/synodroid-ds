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

import java.util.List;

import org.jared.synodroid.common.data.Detail;
import org.jared.synodroid.ds.view.adapter.DetailAdapter;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TabHost;

/**
 * This activity displays a task's details 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class DetailActivity extends TabActivity {

	/* (non-Javadoc)
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
    tabHost.addTab(tabHost.newTabSpec("tab_test2").setIndicator(getString(R.string.detail_tab_transfer), getResources().getDrawable(R.drawable.icon_transfert)).setContent(R.id.textview2));
    tabHost.addTab(tabHost.newTabSpec("tab_test3").setIndicator(getString(R.string.detail_tab_files), getResources().getDrawable(R.drawable.icon_files)).setContent(R.id.textview3));
    // Set the default tab
    tabHost.setCurrentTab(0);
    
    // Build the general tab
    ListView listView = (ListView)findViewById(R.id.general_layout);
    DetailAdapter detailAdapter = new DetailAdapter(this);
    listView.setAdapter(detailAdapter);
    
    // Set the details
    Intent intent = getIntent();
    List<Detail> details = (List<Detail>)intent.getSerializableExtra("org.jared.synodroid.ds.Details");
    detailAdapter.updateDetails(details);
  }

}
