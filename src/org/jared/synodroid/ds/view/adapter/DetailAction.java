/**
 * 
 */
package org.jared.synodroid.ds.view.adapter;

/**
 * A simple interface which define an action which can be executed when the user click on a detail
 * 
 * @author Eric Taix
 */
public interface DetailAction {

	/**
	 * Exececute an action on a particular detail instance
	 * 
	 * @param detailsP
	 */
	public void execute(Detail detailsP);

}
