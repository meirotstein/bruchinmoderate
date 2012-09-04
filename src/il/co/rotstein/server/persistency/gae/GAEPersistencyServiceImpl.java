package il.co.rotstein.server.persistency.gae;

import il.co.rotstein.server.ModeratedMessage;
import il.co.rotstein.server.exception.PersistencyServiceException;
import il.co.rotstein.server.mail.MailUtils;
import il.co.rotstein.server.persistency.PersistencyService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

public class GAEPersistencyServiceImpl implements PersistencyService {

	private static final Logger log = Logger.getLogger(GAEPersistencyServiceImpl.class.getName());
	
	private static final String MAIL_CATEGORY = "mail_kind";
	private static final String MAIL_CONTENT_PROP = "mailContent";
	private static final String MAIL_DATE_PROP = "mailDate";
	private static final String MAIL_REPLY_TO = "mail_reply_to";
	private static final String MAIL_SUBJECT = "mail_subject";
	private static final String MAIL_RECIPIENT = "mail_recipient";
	
	private DatastoreService datastore;
	private Session session;
	
	public void init( Session session ) throws PersistencyServiceException {
		
		ensureDatastore();
		this.session = session;
		
	}
	
	@Override
	public void store(Message message) throws PersistencyServiceException {
		
		ensureDatastore();
		
		try {

			String contentStr = MailUtils.contentToString( message );
			
			Text content = new Text( contentStr );
			Date rDate = message.getReceivedDate();

			Entity messageEnt = new Entity( MAIL_CATEGORY , rDate.getTime() );
			messageEnt.setProperty( MAIL_CONTENT_PROP , content );
			messageEnt.setProperty( MAIL_DATE_PROP , rDate );
			messageEnt.setProperty( MAIL_REPLY_TO , message.getReplyTo()[0].toString() );
			messageEnt.setProperty( MAIL_SUBJECT , message.getSubject() );
			messageEnt.setProperty( MAIL_RECIPIENT , message.getRecipients(RecipientType.TO)[0].toString() );

			datastore.put( messageEnt );
			
		} catch (IOException e) {
			
			throw new PersistencyServiceException( e.getMessage() );

		} catch (MessagingException e) {

			throw new PersistencyServiceException( e.getMessage() );

		} 

		log.log(Level.FINE, "Message stored successfuly" );
	}

	@Override
	public void remove(Message message) throws PersistencyServiceException {
		
		ensureDatastore();

		try {

			Key key = KeyFactory.createKey( MAIL_CATEGORY , message.getReceivedDate().getTime() );
			datastore.delete( key );

		} catch (MessagingException e) {

			log.log( Level.SEVERE, "Deletion of message from store failed with message: " + e.getMessage() , e);
		}


	}

	@Override
	public void remove(Collection<Message> messages)  throws PersistencyServiceException {
		
		ensureDatastore();

		for ( Message message : messages ) {

			remove( message );

		}

	}

	@Override
	public List<Message> loadAll()  throws PersistencyServiceException {
		
		ensureDatastore();
		
		List<Message> ret = new ArrayList<Message>();
		
		Query q = new Query( MAIL_CATEGORY );
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable()) {

			Text content = (Text) result.getProperty( MAIL_CONTENT_PROP );
			Date rDate = (Date) result.getProperty( MAIL_DATE_PROP );

			InputStream contentIs = new ByteArrayInputStream( content.getValue().getBytes() );

			try {

				ModeratedMessage msg = new ModeratedMessage( session , contentIs );
				msg.setReceivedDate( rDate );
				msg.setReplyTo( new Address[] { new InternetAddress( ( String ) result.getProperty( MAIL_REPLY_TO ) ) } );
				msg.setSubject( (String) result.getProperty( MAIL_SUBJECT ) );
				msg.setRecipient( RecipientType.TO , new InternetAddress( ( String ) result.getProperty( MAIL_RECIPIENT ) ) );
				msg.setContentAsString( content.getValue() );

				ret.add( msg );

				log.log(Level.FINE, "successfuly retrieved message from date: " + rDate );

			} catch ( MessagingException e ) {

				throw new PersistencyServiceException( e.getMessage() );

			}

		}
		
		return ret;
		
	}
	
	private void ensureDatastore() throws PersistencyServiceException {
		
		//fallback
		if( datastore == null ){
			datastore = DatastoreServiceFactory.getDatastoreService();
		}
		
		if( datastore == null ){
			throw new PersistencyServiceException( "Datastore cannot be reached" );
		}
	}

}
