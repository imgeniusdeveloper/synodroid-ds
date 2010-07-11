/**
 * 
 */
package org.jared.synodroid.common.action;

import org.jared.synodroid.common.SynoServer;
import org.jared.synodroid.common.data.Task;
import org.jared.synodroid.common.data.TaskContainer;
import org.jared.synodroid.common.protocol.ResponseHandler;
import org.jared.synodroid.ds.DownloadActivity;

/**
 * Retrieve all task from the servers
 * @author Eric Taix
 *
 */
public class GetAllTaskAction implements SynoAction {

  // The name of the sorted attribut
  private String sortAttr;
  // Is the sort ascending ?
  private boolean ascending = true; 
  
  /**
   * Default constructor
   * @param sortAttrP
   * @param ascendingP
   */
  public GetAllTaskAction(String sortAttrP, boolean ascendingP) {
    sortAttr = sortAttrP;
    ascending = ascendingP;
  }
  
  
  /* (non-Javadoc)
   * @see org.jared.synodroid.ds.action.TaskAction#execute(org.jared.synodroid.common.protocol.ResponseHandler, org.jared.synodroid.common.SynoServer)
   */
  public void execute(ResponseHandler handlerP, SynoServer serverP) throws Exception {
    TaskContainer container = serverP.getDSMHandlerFactory().getDSHandler().getAllTask(sortAttr, ascending);
    serverP.fireMessage(handlerP, DownloadActivity.MSG_TASKS_UPDATED, container);
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.ds.action.TaskAction#getName()
   */
  public String getName() {
    return "Retrieving all tasks";
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.ds.action.TaskAction#getTask()
   */
  public Task getTask() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.ds.action.TaskAction#getToastId()
   */
  public int getToastId() {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.ds.action.TaskAction#isToastable()
   */
  public boolean isToastable() {
    return false;
  }

}
