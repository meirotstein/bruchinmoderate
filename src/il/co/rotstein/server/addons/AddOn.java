package il.co.rotstein.server.addons;

import javax.mail.Message;

/**
 * @author meirotstein
 *
 */
public interface AddOn {
	
	public static enum AddOnResult { Continue , Abort };
	
	
	/**
	 * Initialization method, automatically called by {@link AddOnsManager} upon add on load
	 * @param parameters as defined in the config.json file
	 */
	public void init ( String[] parameters );
	
	/**
	 * Execution of the add on logic 
	 * @param message
	 * @return {@link AddOnResult}
	 */
	public abstract AddOnResult manipulate( Message message );	

}
