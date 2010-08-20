/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.common.ui;

import java.util.ArrayList;

import org.jared.synodroid.ds.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A tab manager which is able to manage multiple tabs.
 * 
 * @author Eric Taix
 */
public class TabWidgetManager implements View.OnClickListener {

  private static final String TAG_SELECTED_SUFFIX = "_selected";
  private static final String TAG_NORMAL_SUFFIX = "_normal";
  private static final String TAG_FRAME = "FRAME";

  // The current visible frame
  private int currentIndex = -1;
  // The tabs list
  private ArrayList<Tab> tabs = new ArrayList<Tab>();
  // The view list
  private ArrayList<View> views = new ArrayList<View>();
  // The associated activity
  private Activity activity;
  // The drawable which is used to show the selected state
  private int sliderDrawable;
  // The inflater
  private LayoutInflater inflater;
  // The content's frame
  private WorkspaceView contentFrame;
  // The sliders frame
  private LinearLayout selectedTabFrame;
  // The normal tabs frame
  private LinearLayout normalTabFrame;
  // The main content view
  private LinearLayout mainContentView;
  // The main tabs view
  private LinearLayout mainTabsView;
  // Flag to know if a animation is playing
  private boolean animPlaying = false;
  // The tablistener
  private TabListener tabListener;

  /**
   * The default constructor
   */
  public TabWidgetManager(Activity activityP, int sliderDrawableP) {
    activity = activityP;
    sliderDrawable = sliderDrawableP;
    // Get the main inflater
    inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mainContentView = (LinearLayout) inflater.inflate(R.layout.tabs_content, null, false);
    contentFrame = (WorkspaceView) mainContentView.findViewById(R.id.id_tab_content);
    contentFrame.setTouchSlop(32);
    // Load background image (for test purpose only)
    Bitmap backGd = BitmapFactory.decodeResource(activityP.getResources(), R.drawable.background1);
    contentFrame.loadWallpaper(backGd);
    mainTabsView = (LinearLayout) inflater.inflate(R.layout.tabs_tab, null, false);
    selectedTabFrame = (LinearLayout) mainTabsView.findViewById(R.id.id_selected_tabs);
    normalTabFrame = (LinearLayout) mainTabsView.findViewById(R.id.id_normal_tabs);
  }

  /**
   * Add a new tab with an associated View
   * 
   * @param tabP
   * @param viewP The associated view
   */
  public void addTab(Tab tabP, View viewP) {
    tabs.add(tabP);

    // Add the associated view to the frame
    if (viewP != null) {
      views.add(viewP);
      contentFrame.addView(viewP);
      viewP.setTag("" + tabP.getId() + TAG_FRAME);
    }
    // Add images to the appropriated frame
    ImageView normal = new ImageView(activity);
    normal.setTag("" + tabP.getId() + TAG_NORMAL_SUFFIX);
    normal.setVisibility(View.VISIBLE);
    normal.setImageResource(tabP.getIconNormal());
    normal.setOnClickListener(this);
    normalTabFrame.addView(normal);
    ImageView selected = new ImageView(activity);
    selected.setTag("" + tabP.getId() + TAG_SELECTED_SUFFIX);
    selected.setOnClickListener(this);
    selected.setVisibility(View.INVISIBLE);
    selected.setImageResource(sliderDrawable);
    selectedTabFrame.addView(selected);
    // If this is the first tab
    if (tabs.size() == 1) {
      setTab(tabP.getId());
    }
  }

  /**
   * Return the view corresponding to the tabP parameter or null if the tab does not exist
   * 
   * @param tabP
   * @return
   */
  public View getTab(Tab tabP) {
    int index = tabs.indexOf(tabP);
    if (index != -1) {
      return views.get(index);
    }
    else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  public void onClick(View viewP) {
    String tagID = (String) viewP.getTag();
    // Determine the suffix's length
    int suffixLength = TAG_NORMAL_SUFFIX.length();
    if (tagID.endsWith(TAG_SELECTED_SUFFIX)) {
      suffixLength = TAG_SELECTED_SUFFIX.length();
    }
    tagID = tagID.substring(0, tagID.length() - suffixLength);
    slideTo(tagID);
  }

  /**
   * Return the main content view according to the differents tabs
   * 
   * @param contextP
   * @return
   */
  public View getContentView() {
    return mainContentView;
  }

  /**
   * Return the tab view
   * 
   * @param contextP
   * @return
   */
  public View getTabView() {
    return mainTabsView;
  }

  /**
   * Slide to a new tab.
   * 
   * @param The tagId to select
   * @return
   */
  public void slideTo(String tabIdP) {
    // If there is no current animation
    if (!animPlaying) {
      Tab fake = new Tab(tabIdP);
      int index = tabs.indexOf(fake);
      if (index != -1 /* && index != currentIndex */) {
        // Retrieve tabs
        Tab fromTab = tabs.get(currentIndex);
        final Tab toTab = tabs.get(index);
        // Show the logo
        if (currentIndex != index && (toTab.getLogoId() != 0 || toTab.getLogoTextId() != 0)) {
          setSelected(currentIndex, View.INVISIBLE);
        }

        // Create animation
        float factor = 1.0f * (index - currentIndex);
        Animation animSlider = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                factor, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animSlider.setDuration(400);
        // Retrieve the source slider
        final ImageView slider = (ImageView) selectedTabFrame.findViewWithTag("" + fromTab.getId()
                + TAG_SELECTED_SUFFIX);
        animSlider.setAnimationListener(new AnimationListener() {
          public void onAnimationStart(Animation animation) {
          }

          public void onAnimationRepeat(Animation animation) {
          }

          public void onAnimationEnd(Animation animation) {
            setTab(toTab.getId());
            animPlaying = false;
          }
        });
        // Start the animation
        animPlaying = true;
        slider.startAnimation(animSlider);
      }
    }
  }

  /**
   * Change the current selected tab
   */
  private void setTab(String tabId) {
    // Try to find the associated tab
    Tab fake = new Tab(tabId);
    int newIndex = tabs.indexOf(fake);
    // If the view was found AND if this is a new tab
    String oldId = null;
    if (newIndex != -1 /* && currentIndex != newIndex */) {
      if (currentIndex != -1) {
        Tab oldTab = tabs.get(currentIndex);
        oldId = oldTab.getId();
        setSelected(currentIndex, View.INVISIBLE);
      }
      setSelected(newIndex, View.VISIBLE);
      // If exist fire the tab event
      if (tabListener != null) {
        tabListener.selectedTabChanged(oldId, tabId);
      }
      // Change the current index
      currentIndex = newIndex;
    }
  }

  /**
   * Change the selected state of a tab and of the associated view
   * 
   * @param indexP
   * @param visibilityP
   */
  private void setSelected(int indexP, int visibilityP) {
    Tab tab = tabs.get(indexP);
    ImageView img = (ImageView) selectedTabFrame.findViewWithTag(tab.getId() + TAG_SELECTED_SUFFIX);
    if (img != null) {
      img.setVisibility(visibilityP);
      // If the tab have a different images (normal + selected)
      int imgSelected = tab.getIconSelected();
      if (imgSelected != 0) {
        ImageView icon = (ImageView) normalTabFrame.findViewWithTag(tab.getId() + TAG_NORMAL_SUFFIX);
        icon.setImageResource((visibilityP == View.VISIBLE ? tab.getIconSelected() : tab.getIconNormal()));
      }
    }
  }

  /**
   * Change the selected tab to the next one (on the right)
   */
  public void nextTab() {
    if (currentIndex < tabs.size() - 1) {
      Tab tab = tabs.get(currentIndex + 1);
      slideTo(tab.getId());
    }
  }

  /**
   * Change the selected tab to the previous one (on the left)
   */
  public void previousTab() {
    if (currentIndex > 0) {
      Tab tab = tabs.get(currentIndex - 1);
      slideTo(tab.getId());
    }
  }

  /**
   * @return the tabListener
   */
  public TabListener getTabListener() {
    return tabListener;
  }

  /**
   * @param tabListener the tabListener to set
   */
  public void setTabListener(TabListener tabListenerP) {
    tabListener = tabListenerP;
  }

}
