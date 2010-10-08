package org.jared.synodroid.common.ui.wizard;

import android.content.Context;

/**
 * An interface which can be used by instance which need to interact with a Wizard
 * @author Eric Taix
 *
 */
public interface WizardInterface {

  /**
   * Go to the next view
   */
  public abstract void next();

  /**
   * Go to the previous view
   */
  public abstract void previous();

  /**
   * Reset all previous state
   */
  public abstract void reset();

  /**
   * Return the current context in which this Wizard is running
   * @return
   */
  public Context getContext();
  
}