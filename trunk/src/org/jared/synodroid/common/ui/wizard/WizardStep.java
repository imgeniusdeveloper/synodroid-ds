/**
 * 
 */
package org.jared.synodroid.common.ui.wizard;

import android.content.Context;
import android.view.View;

/**
 * Represents a single Wizard step
 * 
 * @author Eric Taix
 */
public interface WizardStep {

  /**
   * Define the current context. An implementation can store this reference for future use if needed.
   * @param ctxP
   */
  public void setContext(Context ctxP);
  
  /**
   * Return the view associated with this step
   * 
   * @return
   */
  public View getView();

  /**
   * Ask the step to reset its current values: the GUI must reflect this change
   */
  public void reset();
}
