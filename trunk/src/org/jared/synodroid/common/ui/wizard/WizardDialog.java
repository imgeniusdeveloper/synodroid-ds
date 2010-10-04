/**
 * 
 */
package org.jared.synodroid.common.ui.wizard;

import org.jared.synodroid.ds.R;

import android.app.Dialog;
import android.content.Context;

/**
 * This is a Wizard diaog
 * @author Eric Taix
 */
public class WizardDialog extends Dialog {

  /**
   * Constructor which initialiaz the context
   * @param contextP
   */
  public WizardDialog(Context contextP, String titleP) {
    super(contextP);
    initUI(titleP);
  }

  /**
   * Constructor which initialiaz the context and the thme
   * @param contextP
   * @param themeP
   */
  public WizardDialog(Context contextP, int themeP, String titleP) {
    super(contextP, themeP);
    initUI(titleP);
  }
  
  /**
   * Initialize the UI
   */
  private void initUI(String titleP) {
    setTitle(titleP);
    setContentView(R.layout.wizard);
  }
}
