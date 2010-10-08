/**
 * 
 */
package org.jared.synodroid.ds.view.wizard;

import org.jared.synodroid.common.ui.wizard.WizardInterface;
import org.jared.synodroid.common.ui.wizard.WizardStep;
import org.jared.synodroid.ds.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This list displays the server list
 * @author Eric Taix
 */
public class ServerListStep implements WizardStep {

  private WizardInterface wizard;
  // For test purpose only
  static final String[] NAS = new String[] { "Nas-1", "Nas-video", "Nas-Internet" };
  
  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#getView()
   */
  public View getView() {
    // Create the main inflater
    LayoutInflater inflater = (LayoutInflater) wizard.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    LinearLayout ll = (LinearLayout)inflater.inflate(R.layout.wizard_serverlist, null, false);
    ListView lv = (ListView)ll.findViewById(R.id.server_listview);
    lv.setAdapter(new ArrayAdapter<String>(wizard.getContext(),R.layout.wizard_row, R.id.label, NAS) {
      @Override
      public View getView(int positionP, View convertViewP, ViewGroup parentP) {
        View v = super.getView(positionP, convertViewP, parentP);
        TextView tv = (TextView)v.findViewById(R.id.label);
        tv.setTextColor(Color.BLACK);
        return v;
      }
      
    });
    // When the user click on an item, then go to the next step
    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parentP, View viewP, int positionP, long idP) {
        wizard.next();
      }
    });
    return ll;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#reset()
   */
  public void reset() {
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardStep#setContext(android.content.Context)
   */
  public void init(WizardInterface wizP) {
    wizard = wizP;
  }

}
