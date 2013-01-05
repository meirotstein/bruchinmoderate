package il.co.rotstein.server.statistics.gae;

import il.co.rotstein.server.ModeratedMessage;
import il.co.rotstein.server.exception.PersistencyServiceException;
import il.co.rotstein.server.exception.StatisticsServiceException;
import il.co.rotstein.server.statistics.StatisticsService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

public class GAEStatisticsServiceImpl implements StatisticsService {

	private DatastoreService datastore;
	private Session session;

	private static final String STATISTICS_REMOVE_KIND = "stat_remove";
	private static final String STATISTICS_MANUAL_MODERATE_KIND = "stat_manual_moderate";
	private static final String STATISTICS_ERROR_KIND = "stat_error";
	private static final String STATISTICS_SKIP_KIND = "stat_skip";
	
	private static final String WHEN_PROP = "when";
	private static final String SENDER_PROP = "sender";
	private static final String SUBJECT_PROP = "subject";
	private static final String ERROR_MESSAGE_PROP = "error_message";

	public void init( Session session ) throws StatisticsServiceException {

		ensureDatastore();
		this.session = session;

	}

	@Override
	public void logRemoveRequest(Date when, String address, String subject) throws StatisticsServiceException {

		ensureDatastore();

		Date now = new Date();

		Entity messageEnt = new Entity( STATISTICS_REMOVE_KIND , now.getTime() );
		messageEnt.setProperty( WHEN_PROP , when );
		messageEnt.setProperty( SENDER_PROP , address );
		messageEnt.setProperty( SUBJECT_PROP , subject );

		datastore.put( messageEnt );

	}

	@Override
	public void logManualModeration(Date when, String address, String subject) throws StatisticsServiceException {

		ensureDatastore();

		Date now = new Date();

		Entity messageEnt = new Entity( STATISTICS_MANUAL_MODERATE_KIND , now.getTime() );
		messageEnt.setProperty( WHEN_PROP , when );
		messageEnt.setProperty( SENDER_PROP , address );
		messageEnt.setProperty( SUBJECT_PROP , subject );

		datastore.put( messageEnt );

	}

	public void logSkipModerationRequest( Date when , String address , String subject) throws StatisticsServiceException {

		ensureDatastore();

		Date now = new Date();

		Entity messageEnt = new Entity( STATISTICS_SKIP_KIND , now.getTime() );
		messageEnt.setProperty( WHEN_PROP , when );
		messageEnt.setProperty( SENDER_PROP , address );
		messageEnt.setProperty( SUBJECT_PROP , subject );

		datastore.put( messageEnt );

	}

	@Override
	public void logError(Date when, String errorMsg) throws StatisticsServiceException {

		ensureDatastore();

		Date now = new Date();

		Entity messageEnt = new Entity( STATISTICS_ERROR_KIND , now.getTime() );
		messageEnt.setProperty( WHEN_PROP , when );
		messageEnt.setProperty( ERROR_MESSAGE_PROP , errorMsg );

		datastore.put( messageEnt );

	}

	private void ensureDatastore() throws StatisticsServiceException {

		//fallback
		if( datastore == null ){
			datastore = DatastoreServiceFactory.getDatastoreService();
		}

		if( datastore == null ){
			throw new StatisticsServiceException( "Datastore cannot be reached" );
		}
	}

	@Override
	public int countRemoveRequests(Date from) throws StatisticsServiceException {

		ensureDatastore();
		
		Query q;
		
		if( from != null ){
			
			q = new Query( STATISTICS_REMOVE_KIND )
					.addFilter(
							WHEN_PROP,
							Query.FilterOperator.GREATER_THAN,
							from);
			
		} else {
		
			q = new Query( STATISTICS_REMOVE_KIND );
			
		}
		
		PreparedQuery pq = datastore.prepare(q);
		
		return pq.countEntities( FetchOptions.Builder.withDefaults() );
		
	}

	@Override
	public int countSkipRequests(Date from) throws StatisticsServiceException {
		
		ensureDatastore();
		
		Query q;
		
		if( from != null ){
			
			q = new Query( STATISTICS_SKIP_KIND )
					.addFilter(
							WHEN_PROP,
							Query.FilterOperator.GREATER_THAN,
							from);
			
		} else {
		
			q = new Query( STATISTICS_SKIP_KIND );
			
		}
		
		PreparedQuery pq = datastore.prepare(q);
		
		return pq.countEntities( FetchOptions.Builder.withDefaults() );
		
	}

	@Override
	public int countManualModerations(Date from)
			throws StatisticsServiceException {

		ensureDatastore();
		
		Query q;
		
		if( from != null ){
			
			q = new Query( STATISTICS_MANUAL_MODERATE_KIND )
					.addFilter(
							WHEN_PROP,
							Query.FilterOperator.GREATER_THAN,
							from);
			
		} else {
		
			q = new Query( STATISTICS_MANUAL_MODERATE_KIND );
			
		}
		
		PreparedQuery pq = datastore.prepare(q);
		
		return pq.countEntities( FetchOptions.Builder.withDefaults() );

		
	}

}
