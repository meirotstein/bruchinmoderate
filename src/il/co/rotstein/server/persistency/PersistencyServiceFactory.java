package il.co.rotstein.server.persistency;

import il.co.rotstein.server.config.Configurations;
import il.co.rotstein.server.exception.PersistencyServiceException;

import java.util.logging.Logger;

import javax.mail.Session;

public class PersistencyServiceFactory {
	
	private static final Logger log = Logger.getLogger(PersistencyServiceFactory.class.getName());
	
	public static PersistencyService getService( Session session ) throws PersistencyServiceException {
		
		try {
			
			String clzName = Configurations.getPersistencyServiceClassName();
			
			ClassLoader cLoader = PersistencyServiceFactory.class.getClassLoader();
			
			Class<?> aClass = cLoader.loadClass( clzName );
			PersistencyService service = ( PersistencyService ) aClass.newInstance();
			service.init( session );
			
			log.info( "PersistencyService loaded successfuly" );
			
			return service;
			
		} catch (ClassNotFoundException e) {
			
			throw new PersistencyServiceException( e.getMessage() );
			
		} catch (InstantiationException e) {
			 
			throw new PersistencyServiceException( e.getMessage() );
			
		} catch (IllegalAccessException e) {

			throw new PersistencyServiceException( e.getMessage() );
			
		}
		
	}

}
