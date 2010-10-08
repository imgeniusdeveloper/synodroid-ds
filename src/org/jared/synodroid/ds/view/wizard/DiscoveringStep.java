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
import android.widget.TextView;

/**
 * The first step which shows a simple infinite progressbar while searching
 * @author Eric Taix
 */
public class DiscoveringStep implements WizardStep {

  // The current context
  private WizardInterface wizard;
  
  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#setContext(android.content.Context)
   */
  public void init(WizardInterface wizP) {
    wizard = wizP;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#getView()
   */
  public View getView() {
    // Create the main inflater
    LayoutInflater inflater = (LayoutInflater) wizard.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.wizard_discover_server, null, false);
    TextView text = (TextView)view.findViewById(R.id.searching_text_id);
    text.setText(wizard.getContext().getText(R.string.wizard_searching_text).toString());
    return view;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#isOptionnal()
   */
  public boolean isOptionnal() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#reset()
   */
  public void reset() {
  }

}
