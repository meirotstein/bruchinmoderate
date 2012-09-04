package il.co.rotstein.server.queue;

import il.co.rotstein.server.exception.PersistencyServiceException;
import il.co.rotstein.server.persistency.PersistencyService;
import il.co.rotstein.server.persistency.PersistencyServiceFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;


public class MessagesQueue {

	PersistencyService persistService;

	private final Logger log = Logger.getLogger(MessagesQueue.class.getName());

	private LinkedList<Message> messages = new LinkedList<Message>();

	public void init( Session session ) {

		try {
			
			persistService = PersistencyServiceFactory.getService( session );
			
		} catch ( PersistencyServiceException e ) {
			
			log.log( Level.WARNING , "Persistency service fail to load" , e );
			
		}

		if( messages.isEmpty() ) {
			loadAll();
			log.info( size() + " Messages loaded from queue" );
		}

	}


	public void push( Message msg ) {
		messages.addLast( msg );
		
		if( persistService != null ) {
			
			try {
				
				persistService.store( msg );
				
			} catch ( PersistencyServiceException e ) {
				
				log.log( Level.WARNING , "Fail to store message in persistency" , e );
				
			}
			
		}		
	}

	public Message pop() {
		if( messages.isEmpty() )
			return null;

		Message msg = messages.removeFirst();
		
		if( persistService != null ) {
			
			try {
				
				persistService.remove( msg );
				
			} catch ( PersistencyServiceException e ) {
				
				log.log( Level.WARNING , "Fail to remove message from persistency" , e );
				
			}
			
		}	
		
		return msg;
	}

	public List<Message> popAll() {
		if( messages.isEmpty() )
			return null;

		ArrayList<Message> ret = new ArrayList<Message>();
		while ( !messages.isEmpty() ){
			Message msg =  messages.removeFirst();
			
			if( persistService != null ) {
				
				try {
					
					persistService.remove( msg );
					
				} catch ( PersistencyServiceException e ) {
					
					log.log( Level.WARNING , "Fail to remove message from persistency" , e );
					
				}
				
			}	
			
			ret.add( msg );
		}

		return ret;

	}

	public List<Message> pop( Satisfier satisfier ) {
		
		ArrayList<Message> ret = new ArrayList<Message>();

		for (Message msg : messages){

			if( satisfier.isSatisfied( msg ) ) {
				
				ret.add( msg );
				
			}
			
		}

		if( ret.size() > 0 ){ 
			messages.removeAll( ret );
			
			if( persistService != null ) {
				
				try {
					
					persistService.remove( ret );
					
				} catch ( PersistencyServiceException e ) {
					
					log.log( Level.WARNING , "Fail to remove messages from persistency" , e );
					
				}
				
			}	
		}

		return ret;

	}


	public boolean isEmpty() {
		return messages.isEmpty();
	}

	public int size() {
		return messages.size();
	}

	private void loadAll() {
		
		List<Message> msgs = null;
		
		if( persistService != null ) {
			
			try {
				
				msgs = persistService.loadAll();
				
			} catch ( PersistencyServiceException e ) {
				
				log.log( Level.WARNING , "Fail to load messages from persistency" , e );
				
			}
			
		}
		
		if( msgs != null ){
			messages.addAll( msgs );
		}
				
	}

}
