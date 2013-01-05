package il.co.rotstein.server.statistics;

import il.co.rotstein.server.config.Configurations;
import il.co.rotstein.server.exception.StatisticsServiceException;


import java.util.logging.Logger;

import javax.mail.Session;

/**
 * @author I058533
 *
 */
public class StatisticsServiceFactory {
	
private static final Logger log = Logger.getLogger(StatisticsServiceFactory.class.getName());
	
	public static StatisticsService getService( Session session ) throws StatisticsServiceException {
		
		try {
			
			String clzName = Configurations.getStatisticsServiceClassName();
			
			ClassLoader cLoader = StatisticsServiceFactory.class.getClassLoader();
			
			Class<?> aClass = cLoader.loadClass( clzName );
			StatisticsService service = ( StatisticsService ) aClass.newInstance();
			service.init( session );
			
			log.info( "PersistencyService loaded successfuly" );
			
			return service;
			
		} catch (ClassNotFoundException e) {
			
			throw new StatisticsServiceException( e.getMessage() );
			
		} catch (InstantiationException e) {
			 
			throw new StatisticsServiceException( e.getMessage() );
			
		} catch (IllegalAccessException e) {

			throw new StatisticsServiceException( e.getMessage() );
			
		}
		
	}

}
