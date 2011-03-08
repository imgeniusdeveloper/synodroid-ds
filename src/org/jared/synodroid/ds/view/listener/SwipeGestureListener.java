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
package org.jared.synodroid.ds.view.listener;

import org.jared.synodroid.common.ui.TabWidgetManager;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

/**
 * A gesture listener which listen to swipe left/right
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class SwipeGestureListener extends SimpleOnGestureListener {

	private static final int SWIPE_MIN_X_DISTANCE = 120;
	private static final int SWIPE_MAX_Y_DISTANCE = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	// The tab manager
	private TabWidgetManager tabManager;

	/**
	 * The constructor
	 * 
	 * @param tabManagerP
	 */
	public SwipeGestureListener(TabWidgetManager tabManagerP) {
		tabManager = tabManagerP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view
	 * .MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_Y_DISTANCE) {
				return true;
			}
			// Swipe right
			if (e1.getX() - e2.getX() > SWIPE_MIN_X_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				tabManager.nextTab();
				return true;
			}
			// Swipe left
			else if (e2.getX() - e1.getX() > SWIPE_MIN_X_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				tabManager.previousTab();
				return true;
			} else {
				return true;
			}
		} catch (Exception e) {
			// nothing
			System.out.println("No");
			return true;
		}
	}
}
