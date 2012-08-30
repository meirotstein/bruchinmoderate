package il.co.rotstein.server.queue;

import il.co.rotstein.server.ModeratedMessage;
import il.co.rotstein.server.mail.MailUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
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


public class MessagesQueue {

	private static final String MAIL_CATEGORY = "mail_kind";
	private static final String MAIL_CONTENT_PROP = "mailContent";
	private static final String MAIL_DATE_PROP = "mailDate";
	private static final String MAIL_REPLY_TO = "mail_reply_to";
	private static final String MAIL_SUBJECT = "mail_subject";
	private static final String MAIL_RECIPIENT = "mail_recipient";


	private DatastoreService datastore;

	private final Logger log = Logger.getLogger(MessagesQueue.class.getName());

	private LinkedList<Message> messages = new LinkedList<Message>();
	private Session session;

	public void init( Session session ) {

		this.session = session;

		if( datastore == null ){
			datastore = DatastoreServiceFactory.getDatastoreService();
		}

		if( messages.isEmpty() ) {
			loadAll();
			log.info( size() + " Messages loaded from queue" );
		}

	}


	public void push( Message msg ) {
		messages.addLast( msg );
		store( msg );
	}

	public Message pop() {
		if( messages.isEmpty() )
			return null;

		Message msg = messages.removeFirst();
		removeFromStore( msg );

		return msg;
	}

	public List<Message> popAll() {
		if( messages.isEmpty() )
			return null;

		ArrayList<Message> ret = new ArrayList<Message>();
		while ( !messages.isEmpty() ){
			Message msg =  messages.removeFirst();
			removeFromStore( msg );

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
			removeFromStore( ret );
		}

		return ret;

	}


	public boolean isEmpty() {
		return messages.isEmpty();
	}

	public int size() {
		return messages.size();
	}

	private void store( Message message ) {
		
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

			log.log(Level.SEVERE, "Message store failed: " + e.getMessage() , e );

		} catch (MessagingException e) {

			log.log(Level.SEVERE, "Message store failed: " + e.getMessage() , e );

		} 

		log.log(Level.FINE, "Message stored successfuly" );

	}

	private void removeFromStore( Message message ){

		try {

			Key key = KeyFactory.createKey( MAIL_CATEGORY , message.getReceivedDate().getTime() );
			datastore.delete( key );

		} catch (MessagingException e) {

			log.log( Level.SEVERE, "Deletion of message from store failed with message: " + e.getMessage() , e);
		}

	}

	private void removeFromStore( Collection<Message> messages ){

		for ( Message message : messages ) {

			removeFromStore( message );

		}

	}

	private void loadAll() {

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

				messages.push( msg );

				log.log(Level.FINE, "successfuly retrieved message from date: " + rDate );

			} catch ( MessagingException e ) {

				log.log( Level.SEVERE, "Load of message from store failed with message: " + e.getMessage() , e);

			}

		}

	}

}
