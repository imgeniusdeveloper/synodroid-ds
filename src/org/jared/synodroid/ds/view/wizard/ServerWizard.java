package org.jared.synodroid.ds.view.wizard;

import javax.jmdns.ServiceInfo;

import org.jared.synodroid.ds.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The wizard which try to find server on a local network
 * 
 * @author Eric Taix
 */
public class ServerWizard implements DialogInterface.OnClickListener {

	private static final int MSG_SERVER_SELECTED = 1;
	private static final int MSG_USER_EDITED = 2;

	// ====================================================================
	// The message handler
	private DiscoveringHandler handler = new DiscoveringHandler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// A server was found
			case MSG_SERVER_FOUND:
				searchDialog.dismiss();
				searchDialog = null;
				selectServer((ServiceInfo[]) msg.obj);
				break;
		  // A server was selected
			case MSG_SERVER_SELECTED :
				serverDialog.dismiss();
				serverDialog = null;
				editUser();
				break;
			// User informations has been edited
			case MSG_USER_EDITED :
				userDialog.dismiss();
				userDialog = null;
				break;
			default:
				break;
			}
		}
	};

	// The cancel listener available for every dialogs
	private DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			// If we want to add a message when the user cancel the wizard: it's here !
    }
	};
	
	// The current context in which this wizard is executed
	private Context context;
	// The view inflater
	private LayoutInflater inflater;
	// The search server dialog
	private Dialog searchDialog;
	// The server list dialog
	private AlertDialog serverDialog;
	// Often used label
	private CharSequence cancelSeq;
	// The user dialog
	private Dialog userDialog;
	/**
	 * Constructor
	 * 
	 * @param ctxP
	 */
	public ServerWizard(Context ctxP) {
		context = ctxP;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		cancelSeq = context.getText(R.string.button_cancel);
	}

	/**
	 * Start the wizard
	 * 
	 * @param contextP
	 * @return
	 */
	public void start() {
		discoverServer();
	}

	/**
	 * Show and edit user informations (username & password)
	 */
	private void editUser() {
		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.wizard_user_pass_form, null);
		userDialog = new WizardBuilder(context).
		  setTitle(context.getText(R.string.wizard_user_title)).
		  setView(ll).
		  setPositiveButton(context.getText(R.string.button_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Message msg = new Message();
					msg.what = MSG_USER_EDITED;
					handler.sendMessage(msg);
				}
			}).
		  create();
		userDialog.show();
	}

	/**
	 * Select the server to create
	 */
	private void selectServer(ServiceInfo[] infos) {
		serverDialog = new WizardBuilder(context).
		  setTitle(context.getText(R.string.wizard_selectserver_title)).
		  setAdapter(new ServerAdapter(context, infos), this).
		  create();
		serverDialog.show();
	}

	/**
	 * The user clicked on a ListView item
	 */
	public void onClick(DialogInterface dialog, int which) {
		if (dialog.equals(serverDialog)) {
			ServiceInfo si = (ServiceInfo) serverDialog.getListView().getSelectedItem();
			Message msg = new Message();
			msg.what = MSG_SERVER_SELECTED;
			msg.obj = si;
			handler.sendMessage(msg);			
		}
	}

	/**
	 * Discover servers which are on the current WLAN
	 */
	private void discoverServer() {
		// Create or show the search dialog
		if (searchDialog == null) {
			LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.wizard_discover_server, null);
			TextView tv = (TextView) ll.findViewById(R.id.searching_text_id);
			tv.setText(context.getText(R.string.wizard_searching_msg));
			searchDialog = new WizardBuilder(context).
			  setTitle(context.getText(R.string.wizard_searching_title)).
			  setView(ll).
			  create();
			searchDialog.show();
		}
		else {
			searchDialog.show();
		}
		// Launch the thead to search for servers
		DiscoveringThread thread = new DiscoveringThread(context, handler);
		thread.start();
	}

	/* ======================================================================== */
	/**
	 * An AlertDailog builder which add some default behaviour
	 */
	private class WizardBuilder extends AlertDialog.Builder {
		/**
		 * Default constructor
		 * @param ctxP
		 */
		public WizardBuilder(Context ctxP) {
	    super(ctxP);
    }

		/* (non-Javadoc)
     * @see android.app.AlertDialog.Builder#create()
     */
    @Override
    public AlertDialog create() {
    	// First add default values
    	setCancelable(false);
  	  setNegativeButton(cancelSeq, cancelListener);
  	  // Then create the dialog
	    return super.create();
    }
	}
}
