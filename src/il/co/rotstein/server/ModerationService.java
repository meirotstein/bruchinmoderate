package il.co.rotstein.server;

import il.co.rotstein.server.addons.AddOnsManager;
import il.co.rotstein.server.addons.InAddOn;
import il.co.rotstein.server.addons.AddOn.AddOnResult;
import il.co.rotstein.server.common.StringPatterns;
import il.co.rotstein.server.config.Configurations;
import il.co.rotstein.server.exception.MailOperationException;
import il.co.rotstein.server.mail.MailHandler;
import il.co.rotstein.server.mail.MailUtils;
import il.co.rotstein.server.queue.MessagesQueue;
import il.co.rotstein.server.queue.OlderThenSatisfier;
import il.co.rotstein.server.queue.SenderSatisfier;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ModerationService extends HttpServlet {

	/**
	 * 
	 */

	private static final String FREE_PENDING_REQ = "/freepending";

	private static String MODERATOR_SENDER = "{0}+msgappr@googlegroups.com";
	private static String SUPER_ADMIN;
	private static int MIN_WAITING_PERIOD;
	private static String MODERATE_TARGET = "moderate@{0}";
	private static String REMOVE_TARGET = "remove@{0}";	
	private static String DO_NOT_REPLY = "no-reply@{0}";
	private static String DO_NOT_MODERATE_KEY;

	private static final long serialVersionUID = 8934486967870817021L;
	private Session session;
	private MessagesQueue mQueue = new MessagesQueue(); 

	private OlderThenSatisfier olderThenSatisfier = new OlderThenSatisfier();

	private final Logger log = Logger.getLogger( ModerationService.class.getName() );

	@Override
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );

		log.log( Level.FINE , "Service gets up, queue size: " + mQueue.size() );
		
		//extract configurations
		Configurations.load( config.getServletContext() );

		Properties props = new Properties(); 
		session = Session.getDefaultInstance( props, null );
		MailHandler.init( session );
		mQueue.init( session );

		MODERATOR_SENDER = MODERATOR_SENDER.replace( "{0}", Configurations.getGroupName() );
		SUPER_ADMIN = Configurations.getSuperAdmin();
		MIN_WAITING_PERIOD = Configurations.getMinimumPendingMinuts();
		MODERATE_TARGET = MODERATE_TARGET.replace( "{0}", Configurations.getMailHost() );
		REMOVE_TARGET = REMOVE_TARGET.replace( "{0}", Configurations.getMailHost() );
		DO_NOT_REPLY = DO_NOT_REPLY.replace( "{0}", Configurations.getMailHost() );
		DO_NOT_MODERATE_KEY = Configurations.getDoNotModerateKey();

		AddOnsManager.loadAddOns( Configurations.getAddOnConfigurations() );

	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException { 
		try {


			ModeratedMessage message = new ModeratedMessage( session, req.getInputStream() );
			String target = ( ( InternetAddress ) message.getRecipients( RecipientType.TO )[0] ).getAddress();
			String sender = message.getFrom()[0].toString();

			log.info( "sender address: "  + sender );

			if( sender != null && sender.contains( MODERATOR_SENDER )){

				try {

					String subject = MailUtils.fetchSubjectFromInnerMessage( message );
					String realSender = MailUtils.fetchSenderFromInnerMessage( message );

					//check for manual moderation
					if( Configurations.getIgnoreSet().contains( realSender ) ) {

						MailHandler.send(	SUPER_ADMIN , 
											null,
											null,
											DO_NOT_REPLY, 
											null, 
											String.format( StringPatterns.MANUAL_MODERATION_SUBJECT , realSender ), 
											String.format( StringPatterns.MANUAL_MODERATION_BODY , Configurations.getGroupName() ),									 
											"UTF-8" );

						return;

					}

					//check for do-not-moderate char - if presents - free immediately
					if( subject != null && subject.trim().startsWith( DO_NOT_MODERATE_KEY )) {

						MailHandler.replyblank( message , MODERATE_TARGET );
						log.info( "Message was redirected immediately as per sender request: "  + sender );
						return;
						
					}

				} catch ( MailOperationException moe ) {
					
					log.warning( "Fail to extract detail from inner message - skipping the pre moderation parts " + moe.getMessage() );
					notifyAdminForError( "Pre moderation was skipped " , moe );
					
				}

				//execute addOns
				List<InAddOn> inAddOns = AddOnsManager.getIncomingAddOns();
				
				log.log( Level.FINE , "Executing " + inAddOns.size() + " addons" );

				for ( InAddOn inAddOn : inAddOns ){
					
					log.log( Level.FINE , "Executing addon " + inAddOn.getClass().getName() );
					
					try {
						
						AddOnResult result = inAddOn.manipulate( message );
	
						if( result == AddOnResult.Abort ){
							return;
						}
					
					} catch ( Throwable t ) {
						
						log.log( Level.WARNING , "Error while add on execution" ,  t );
						notifyAdminForError( "Error while add on execution" , t );
						
					}

				}

				message.setReceivedDate( Calendar.getInstance().getTime() );				
				mQueue.push( message );

				log.info( "message put on hold with subject: "  + message.getSubject() + " decoded: " );

			} else if( REMOVE_TARGET.equals( target ) ){

				log.info( "remove request received from " + sender );

				if( sender != null ) {

					SenderSatisfier ss = new SenderSatisfier( sender );
					List<Message> msgs = mQueue.pop( ss );

					if( msgs.size() > 0 ) {
						
						StringBuilder sb = new StringBuilder();
						
						for( Message msg : msgs ) {
							
							sb.append( MailUtils.fetchSubjectFromInnerMessage( msg ) );
							sb.append( "\n" );
							
						}

						MailHandler.send( 	sender , 
								null, 
								SUPER_ADMIN,
								DO_NOT_REPLY, 
								null, 
								msgs.size() > 1 ? 
										String.format( StringPatterns.MESSAGES_REMOVED_SUBJECT , msgs.size() ) : 
										StringPatterns.MESSAGE_REMOVED_SUBJECT, 
								msgs.size() > 1 ? 
										String.format( StringPatterns.MESSAGES_REMOVED_BODY , msgs.size() , sb.toString() ) : 
										String.format( StringPatterns.MESSAGE_REMOVED_BODY , sb.toString() ),
								"UTF-16" );

					} else {

						MailHandler.send( 	sender , 
											null,
											SUPER_ADMIN,
											REMOVE_TARGET, 
											REMOVE_TARGET, 
											StringPatterns.MESSAGE_NOT_FOUND_SUBJECT,
											String.format( StringPatterns.MESSAGE_NOT_FOUND_BODY , sender ),										
											"UTF-16" );


					}

				}

			}


		} catch ( Exception e ) {

			log.log( Level.SEVERE , e.getMessage() , e );			
			notifyAdminForError( "Fatal Exception" , e );

		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException { 

		String path = req.getRequestURI();

		if( FREE_PENDING_REQ.equals( path )) {

			Calendar cal = Calendar.getInstance();

			//adjust the calendar for minus MIN_WAITING_PERIOD
			cal.add(Calendar.MINUTE, - MIN_WAITING_PERIOD);			 

			if( !mQueue.isEmpty() ){

				olderThenSatisfier.setFrom( cal.getTime() );
				List<Message> msgs = mQueue.pop( olderThenSatisfier );

				for ( Message msg : msgs ){

					MailHandler.replyblank( msg , MODERATE_TARGET );

				}	

			} else {

				log.info( "message queue returned no messages, queue size: " + mQueue.size() );

			}

		}

	}

	private void notifyAdminForError( String message , Throwable t ) {
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		
		try {

			MailHandler.send( 	SUPER_ADMIN , 
					null,
					null,
					DO_NOT_REPLY, 
					DO_NOT_REPLY, 
					"Error in Shit! system: " + message,
					sw.toString(),
					"UTF-8" );

		} catch ( Exception killYourself ) {
			// Another exception? were doomed...
			log.log( Level.SEVERE , killYourself.getMessage() , killYourself );
		} 

	}

}
