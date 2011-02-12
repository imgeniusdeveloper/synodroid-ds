/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.ds;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.Button;
import android.widget.TextView;

/**
 * This activity displays a help page
 * 
 * @author Steve Garon (synodroid at gmail dot com)
 */
public class HelpActivity extends Activity{
	private static final String PREFERENCE_FULLSCREEN = "general_cat.fullscreen";
	private static final String PREFERENCE_GENERAL = "general_cat";

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//ignore orientation change
		super.onConfigurationChanged(newConfig);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);      
		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View help = inflater.inflate(R.layout.help, null, false);
		Button helpBtn = (Button) help.findViewById(R.id.id_email);
		helpBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try{
					final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"synodroid@gmail.com"});
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Synodroid help");
					startActivity(emailIntent);
				}
				catch (Exception e){
					AlertDialog.Builder builder = new AlertDialog.Builder(HelpActivity.this);
				    builder.setMessage(R.string.err_noemail);
				    builder.setTitle(getString(R.string.connect_error_title)).setCancelable(false).setPositiveButton("OK",
				            new DialogInterface.OnClickListener() {
				              public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				              }
				            });
				    AlertDialog errorDialog = builder.create();
				    try{
				    	errorDialog.show();
				    }
					catch (BadTokenException ex){
						//Unable to show dialog probably because intent has been closed. Ignoring...
					}
				}
			}
		});
		TextView main_web = (TextView) help.findViewById(R.id.syno_main_web);
		main_web.setText(Html.fromHtml("<a href=\"http://www.synology.com\">www.synology.com</a>"));
		main_web.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView buy = (TextView) help.findViewById(R.id.syno_buy_web);
		buy.setText(Html.fromHtml("<a href=\"http://www.synology.com/support/wheretobuy.php\">www.synology.com/support/wheretobuy.php</a>"));
		buy.setMovementMethod(LinkMovementMethod.getInstance());

		
		setContentView(help);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		//Check for fullscreen
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_GENERAL, Activity.MODE_PRIVATE);
		if (preferences.getBoolean(PREFERENCE_FULLSCREEN, false)){
			//Set fullscreen or not
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  	
		}
		else{
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

}
