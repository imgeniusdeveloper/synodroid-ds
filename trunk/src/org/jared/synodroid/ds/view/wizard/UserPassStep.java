/**
 * 
 */
package org.jared.synodroid.ds.view.wizard;

import org.jared.synodroid.common.ui.wizard.WizardInterface;
import org.jared.synodroid.common.ui.wizard.WizardStep;
import org.jared.synodroid.ds.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * In this step the user edit his username and password
 * @author Eric Taix
 */
public class UserPassStep implements WizardStep {

  private WizardInterface wizard;
  
  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#getView()
   */
  public View getView() {
    // Create the main inflater
    LayoutInflater inflater = (LayoutInflater) wizard.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.wizard_user_pass_form, null, false);
    return view;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#init(org.jared.synodroid.common.ui.wizard.WizardInterface)
   */
  public void init(WizardInterface wizP) {
    wizard = wizP;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#reset()
   */
  public void reset() {
  }

}
