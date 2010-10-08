package org.jared.synodroid.ds.view.wizard;

import org.jared.synodroid.common.ui.wizard.WizardDialog;
import org.jared.synodroid.ds.R;

import android.app.Dialog;
import android.content.Context;


/**
 * A factory to search for servers
 * @author Eric Taix
 */
public class WizardServerFactory {

  /**
   * Create and return a discovery dialog
   * @param contextP
   * @return
   */
  public static Dialog createDialog(Context contextP) {
    WizardDialog dialog = new WizardDialog(contextP, contextP.getText(R.string.wizard_server_title).toString());
    dialog.addStep(new DiscoveringStep());
    dialog.addStep(new ServerListStep());
    dialog.addStep(new UserPassStep());
    return dialog;
  }
  
}
