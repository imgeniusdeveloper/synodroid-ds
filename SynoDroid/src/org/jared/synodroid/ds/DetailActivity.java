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
package org.jared.synodroid.ds;

import java.util.ArrayList;
import java.util.List;

import org.jared.synodroid.common.data.TaskDetail;
import org.jared.synodroid.common.data.TaskStatus;
import org.jared.synodroid.common.ui.SynodroidActivity;
import org.jared.synodroid.common.ui.Tab;
import org.jared.synodroid.common.ui.TabWidgetManager;
import org.jared.synodroid.ds.view.adapter.Detail;
import org.jared.synodroid.ds.view.adapter.Detail2Progress;
import org.jared.synodroid.ds.view.adapter.Detail2Text;
import org.jared.synodroid.ds.view.adapter.DetailAction;
import org.jared.synodroid.ds.view.adapter.DetailAdapter;
import org.jared.synodroid.ds.view.adapter.DetailProgress;
import org.jared.synodroid.ds.view.adapter.DetailText;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * This activity displays a task's details
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class DetailActivity extends SynodroidActivity {

  // The "Not yet implemented" dialog
  private AlertDialog notYetImplementedDialog;
  // The tab manager
  private TabWidgetManager tabManager;

  
  
  /* (non-Javadoc)
   * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
   */
  @Override
  public boolean dispatchTouchEvent(MotionEvent evP) {
    // TODO Auto-generated method stub
    return super.dispatchTouchEvent(evP);
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {

    // Get the details intent
    Intent intent = getIntent();
    TaskDetail details = (TaskDetail) intent.getSerializableExtra("org.jared.synodroid.ds.Details");

    // Build the general tab
    ListView genListView = new ListView(this);
    DetailAdapter genAdapter = new DetailAdapter(this);
    genListView.setAdapter(genAdapter);
    genListView.setOnItemClickListener(genAdapter);
    genAdapter.updateDetails(buildGeneralDetails(details));

    // Build the transfer tab
    ListView transListView = new ListView(this);
    DetailAdapter transAdapter = new DetailAdapter(this);
    transListView.setAdapter(transAdapter);
    transListView.setOnItemClickListener(transAdapter);
    transAdapter.updateDetails(buildTransferDetails(details));

    // Build the file tab
    ListView filesListView = new ListView(this);

    // Build the TabManager
    tabManager = new TabWidgetManager(this, R.drawable.ic_tab_slider);
    Tab genTab = new Tab("GENERAL", "General", R.drawable.ic_tab_general, R.drawable.ic_tab_general_focused);
    tabManager.addTab(genTab, genListView);
    Tab transTab = new Tab("TRANSFERT", "Transfert", R.drawable.ic_tab_transfer, R.drawable.ic_tab_general_focused);
    tabManager.addTab(transTab, transListView);
    Tab filesTab = new Tab("FILES", "Files", R.drawable.ic_tab_files, R.drawable.ic_tab_general_focused);
    tabManager.addTab(filesTab, filesListView);

    // Call super onCreate after the tab intialization
    super.onCreate(savedInstanceState);

    // Create a "Not yet implemented" dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.title_information)).setMessage(getString(R.string.not_yet_implemented))
            .setCancelable(false).setPositiveButton(R.string.button_ok, null);
    notYetImplementedDialog = builder.create();

    // Get the tabContent
    // FrameLayout frameLayout = (FrameLayout)
    // findViewById(R.id.id_tab_content);
    // frameLayout.bringChildToFront(genListView);

    final ImageView sliderGen = (ImageView) findViewById(R.id.id_tab_slider_general);
    final ImageView sliderTransf = (ImageView) findViewById(R.id.id_tab_slider_transfert);

//    ImageView geneImg = (ImageView) findViewById(R.id.id_tab_transfert);
//    geneImg.setOnClickListener(new View.OnClickListener() {
//      public void onClick(View v) {
//        runAnimation(sliderGen, sliderTransf);
//      }
//    });
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jared.synodroid.common.ui.SynodroidActivity#attachMainContentView
   * (android .view.ViewGroup)
   */
  @Override
	public void attachMainContentView(LayoutInflater inflaterP, ViewGroup parentP) {
	  parentP.addView(tabManager.getContentView());
	}

  /*
   * (non-Javadoc)
   * @see
   * org.jared.synodroid.common.ui.SynodroidActivity#attachStatusView(android
   * .view.ViewGroup)
   */
  @Override
  public void attachStatusView(LayoutInflater inflaterP, ViewGroup parentP) {
    parentP.addView(tabManager.getTabView());
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jared.synodroid.common.ui.SynodroidActivity#attachTitleView(android
   * .view.ViewGroup)
   */
  @Override
  public void attachTitleView(LayoutInflater inflaterP, ViewGroup parentP) {
  }

  public Animation runAnimation(final View source, final View dest) {
    Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_right);
    animation.setFillEnabled(true);
    animation.setAnimationListener(new AnimationListener() {

      public void onAnimationStart(Animation animation) {
      }

      public void onAnimationRepeat(Animation animation) {
      }

      public void onAnimationEnd(Animation animation) {
        source.setVisibility(View.INVISIBLE);
        dest.setVisibility(View.VISIBLE);
      }
    });
    // ((ImageView)target).sets
    source.startAnimation(animation);
    return animation;
  }

  /**
   * Return a sub detail list for the general's tab
   */
  private List<Detail> buildGeneralDetails(TaskDetail details) {
    ArrayList<Detail> result = new ArrayList<Detail>();
    // FileName
    result.add(new DetailText(getString(R.string.detail_filename), details.fileName));
    setTitle(details.fileName);
    // Destination
    DetailText destDetail = new DetailText(getString(R.string.detail_destination), details.destination);
    destDetail.setAction(new DetailAction() {
      public void execute(Detail detailsP) {
        notYetImplementedDialog.show();
      }
    });
    result.add(destDetail);
    // File size
    result.add(new DetailText(getString(R.string.detail_filesize), Utils.bytesToFileSize(details.fileSize)));
    // Creation time
    result.add(new DetailText(getString(R.string.detail_creationtime), Utils.computeDate(details.creationDate)));
    // URL
    DetailText urlDetail = new DetailText(getString(R.string.detail_url), details.url);
    urlDetail.setAction(new DetailAction() {
      public void execute(Detail detailsP) {
        notYetImplementedDialog.show();
      }
    });
    result.add(urlDetail);
    // Username
    result.add(new DetailText(getString(R.string.detail_username), details.userName));
    return result;
  }

  /**
   * Return a sub detail list for the general's tab
   */
  private List<Detail> buildTransferDetails(TaskDetail details) {
    ArrayList<Detail> result = new ArrayList<Detail>();

    // ------------ Status
    result.add(new DetailText(getString(R.string.detail_status), TaskStatus.getLabel(this, details.status)));
    // ------------ Transfered
    String transfered = getString(R.string.detail_progress_upload) + " " + Utils.bytesToFileSize(details.bytesUploaded);
    transfered += " - " + getString(R.string.detail_progress_download) + " "
            + Utils.bytesToFileSize(details.bytesDownloaded);
    result.add(new DetailText(getString(R.string.detail_transfered), transfered));
    // ------------- Progress
    long downloaded = details.bytesDownloaded;
    long filesize = details.fileSize;
    int downPer = (int) ((downloaded * 100) / filesize);
    long uploaded = details.bytesUploaded;
    double ratio = (double) (details.seedingRatio / 100);
    int upPerc = 100;
    if (ratio != 0) {
      upPerc = (int) ((uploaded * 100) / (filesize * ratio));
    }
    Detail2Progress progDetail = new Detail2Progress(getString(R.string.detail_progress));
    progDetail.setProgress1(getString(R.string.detail_progress_upload) + " " + upPerc + "%", upPerc);
    progDetail.setProgress2(getString(R.string.detail_progress_download) + " " + downPer + "%", downPer);
    progDetail.setAction(new DetailAction() {
      public void execute(Detail detailsP) {
        notYetImplementedDialog.show();
      }
    });
    result.add(progDetail);
    // ------------ Speed
    String speed = getString(R.string.detail_progress_upload) + " " + details.speedUpload + " KB/s";
    speed += " - " + getString(R.string.detail_progress_download) + " " + details.speedDownload + " KB/s";
    result.add(new DetailText(getString(R.string.detail_speed), speed));
    // ------------ Peers
    if (details.isTorrent) {
      String peers = details.peersCurrent + " / " + details.peersTotal;
      DetailProgress peersDetail = new DetailProgress(getString(R.string.detail_peers));
      int pProgress = 0;
      if (details.peersTotal != 0) {
        pProgress = (int) ((details.peersCurrent * 100) / details.peersTotal);
      }
      peersDetail.setProgress(peers, pProgress);
      result.add(peersDetail);
    }
    // ------------ Seeders / Leechers
    if (details.isTorrent) {
      String seedStr = getString(R.string.detail_unvailable);
      String leechStr = getString(R.string.detail_unvailable);
      if (details.seeders != null) seedStr = details.seeders.toString();
      if (details.leechers != null) leechStr = details.leechers.toString();
      String seeders = seedStr + " - " + leechStr;
      result.add(new DetailText(getString(R.string.detail_seeders_leechers), seeders));
    }
    // ------------ ETAs
    Detail2Text etaDetail = new Detail2Text(getString(R.string.detail_eta));
    String etaUpload = "?";
    String etaDownload = "?";
    if (details.speedDownload != 0) {
      long sizeLeft = filesize - downloaded;
      long timeLeft = (long) (sizeLeft / (details.speedDownload * 1000));
      etaDownload = Utils.computeTimeLeft(timeLeft);
    }
    else {
      if (downPer == 100) {
        etaDownload = getString(R.string.detail_finished);
      }
    }
    Long timeLeftSize = null;
    if (details.speedUpload != 0 && details.seedingRatio != 0) {
      long sizeLeft = (long) ((filesize * ratio) - uploaded);
      timeLeftSize = (long) (sizeLeft / (details.speedUpload * 1000));
    }
    // If the user defined a minimum seeding time AND we are in seeding
    // mode
    Long timeLeftTime = null;
    if (details.seedingInterval != 0 && TaskStatus.valueOf(details.status) == TaskStatus.TASK_SEEDING) {
      timeLeftTime = (details.seedingInterval * 60) - details.seedingElapsed;
    }
    // At least one time has been computed
    if (timeLeftTime != null || timeLeftSize != null) {
      // By default take the size time
      Long time = timeLeftSize;
      // Except if it is null
      if (timeLeftSize == null) {
        time = timeLeftTime;
      }
      else {
        // If time is not null
        if (timeLeftTime != null) {
          // Get the higher value
          if (timeLeftTime > timeLeftSize) {
            time = timeLeftTime;
          }
        }
      }
      etaUpload = Utils.computeTimeLeft(time);
    }
    else if (upPerc == 100) {
      etaUpload = getString(R.string.detail_finished);
    }

    etaDetail.setValue1(getString(R.string.detail_progress_upload) + " " + etaUpload);
    etaDetail.setValue2(getString(R.string.detail_progress_download) + " " + etaDownload);
    etaDetail.setAction(new DetailAction() {
      public void execute(Detail detailsP) {
        notYetImplementedDialog.show();
      }
    });
    result.add(etaDetail);
    // ------------ Pieces
    String pieces = details.piecesCurrent + " / " + details.piecesTotal;
    DetailProgress piecesDetail = new DetailProgress(getString(R.string.detail_pieces));
    int piProgress = 0;
    if (details.piecesTotal != 0) {
      piProgress = (int) ((details.piecesCurrent * 100) / details.piecesTotal);
    }
    piecesDetail.setProgress(pieces, piProgress);
    result.add(piecesDetail);

    return result;
  }

}
