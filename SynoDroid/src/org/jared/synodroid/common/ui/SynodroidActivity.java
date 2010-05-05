/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jared.synodroid.common.ui;

import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.ds.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * The base class of an activity in Synodroid
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public abstract class SynodroidActivity extends Activity implements ResponseHandler, OnClickListener {

  // The inflater
  private LayoutInflater inflater;
  // The request view
  private LinearLayout operationPending;
  // The rotate animation
  private Animation animRequest;
  // The title click listener
  private TitleClicklistener titleClickListener = null;
  // The title's button
  private ImageView titleButton;
  
  // A generic Handler which delegate to the activity
  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msgP) {
      // An operation is pending
      if (msgP.what == MSG_OPERATION_PENDING) {
        operationPending.setVisibility(View.VISIBLE);
      }
      // Show a toast
      else if (msgP.what == MSG_OPERATION_DONE) {
        operationPending.setVisibility(View.INVISIBLE);
      }
      // Show a toast
      else if (msgP.what == MSG_TOAST) {
        String text = (String) msgP.obj;
        Toast toast = Toast.makeText(SynodroidActivity.this, text, Toast.LENGTH_LONG);
        toast.show();
      }
      // Delegate to the sub class
      else {
        operationPending.setVisibility(View.INVISIBLE);
        SynodroidActivity.this.handleMessage(msgP);
      }
    }
  };


  /**
   * Activity creation
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create the main view of this activity
    setContentView(R.layout.base_activity);
    // Get the operation pending container (to be able to show/hide)
    operationPending = (LinearLayout) findViewById(R.id.id_operation_pending_container);
    // Get the operation's spinner
    ImageView spinner = (ImageView) findViewById(R.id.id_operation_pending_img);
    animRequest = AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely);
    // Can not be set in the XML (LinearInterpolar is not public: arghhh)
    animRequest.setInterpolator(new LinearInterpolator());
    spinner.startAnimation(animRequest);
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
    
    // Retrieve the top bar
    LinearLayout topBar = (LinearLayout) findViewById(R.id.id_top_bar);
    topBar.setOnClickListener(this);
    
    // Retrieve the title's button 
    titleButton = (ImageView) findViewById(R.id.id_title_click_button);
    titleButton.setVisibility(View.INVISIBLE);
  }

  /* (non-Javadoc)
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  public final void onClick(View viewP) {
    if (titleClickListener != null) {
      titleClickListener.onTitleClicked(viewP);
    }
  }

  /**
   * @param titleClickListenerP the titleClickListener to set
   */
  public void setTitleClickListener(TitleClicklistener titleClickListenerP) {
    titleClickListener = titleClickListenerP;
    titleButton.setVisibility((titleClickListener != null ? View.VISIBLE: View.INVISIBLE));
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jared.synodroid.common.protocol.ResponseHandler#handleReponse(
   * android .os.Message)
   */
  public final void handleReponse(Message msgP) {
    handler.sendMessage(msgP);
  }

  /**
   * Handle the message from a none UI thread. It is safe to interact with
   * the UI in this method
   */
  public abstract void handleMessage(Message msgP);

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
