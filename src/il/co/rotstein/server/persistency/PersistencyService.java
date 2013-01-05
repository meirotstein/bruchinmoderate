package il.co.rotstein.server.persistency;

import il.co.rotstein.server.exception.PersistencyServiceException;

import java.util.Collection;
import java.util.List;

import javax.mail.Message;
import javax.mail.Session;

/**
 * @author I058533
 *
 */
public interface PersistencyService {
	
	/**
	 * @param session
	 * @throws PersistencyServiceException
	 */
	public void init( Session session ) throws PersistencyServiceException;
	
	/**
	 * Store message in persistency
	 * @param message
	 * @throws PersistencyServiceException
	 */
	public void store( Message message ) throws PersistencyServiceException;
	
	/**
	 * remove message from persistency
	 * @param message
	 * @throws PersistencyServiceException
	 */
	public void remove( Message message ) throws PersistencyServiceException;
	
	/**
	 * remove list of messages from persistency
	 * @param messages
	 * @throws PersistencyServiceException
	 */
	public void remove( Collection<Message> messages ) throws PersistencyServiceException;
	
	/**
	 * Load all messages from persistency
	 * @return
	 * @throws PersistencyServiceException
	 */
	public List< Message > loadAll() throws PersistencyServiceException;

}
