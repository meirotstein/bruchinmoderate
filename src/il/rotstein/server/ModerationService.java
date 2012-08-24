package il.rotstein.server;

import il.rotstein.server.mail.MailHandler;
import il.rotstein.server.mail.MailUtils;
import il.rotstein.server.queue.MessagesQueue;
import il.rotstein.server.queue.OlderThenSatisfier;
import il.rotstein.server.queue.SenderSatisfier;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
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

	//should be externlized to a config file
	private static final int MIN_WAITING_PERIOD = 3; 
	private static final String MODERATE_TARGET = "moderate@mybruchin.appspotmail.com";
	private static final String REMOVE_TARGET = "remove@mybruchin.appspotmail.com";
	private static final String MODERATOR_SENDER = "bruchin+msgappr@googlegroups.com";
	private static final String DO_NOT_REPLY = "DO-NOT-REPLY@mybruchin.appspotmail.com";
	private static final String SUPER_ADMIN = "meir@rotstein.co.il";
	private static final String DO_NOT_MODERATE_STR = "@";

	private static final long serialVersionUID = 8934486967870817021L;
	private Session session;
	private MailHandler mhandler;
	private MessagesQueue mQueue = new MessagesQueue(); 

	private OlderThenSatisfier olderThenSatsfier = new OlderThenSatisfier();

	private final Logger log = Logger.getLogger(ModerationService.class.getName());

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		log.log( Level.FINE , "Service gets up, queue size: " + mQueue.size() );

		Properties props = new Properties(); 
		session = Session.getDefaultInstance(props, null);
		mhandler = new MailHandler(session);
		mQueue.init( session );

		//		try {
		//			
		//			InputStream configJsonRes = ModerationService.class.getResourceAsStream( Configurations.CONFIGURATION_RESOURSE );
		//			
		//			StringWriter writer = new StringWriter();
		//			IOUtils.copy(configJsonRes, writer);
		//			String cnfgString = writer.toString();
		//			
		//			Gson gson = new Gson();
		//			gson.fromJson( cnfgString , Configurations.class );
		//			
		//		} catch (JsonSyntaxException e) {
		//			log.log( Level.FINE , "Fail to load configuration due to Json syntax error - " +
		//									"default configurations will be taken into account" , e);
		//		} catch (IOException e) {
		//			log.log( Level.FINE , "Fail to load configuration due to resource loading error - " +
		//					"default configurations will be taken into account" , e);
		//		}

	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException { 
		try {


			ModeratedMessage message = new ModeratedMessage(session, req.getInputStream());
			String target = ((InternetAddress) message.getRecipients(RecipientType.TO)[0]).getAddress();
			String sender = message.getFrom()[0].toString();

			log.info( "sender address: "  + sender );

			if( sender != null && sender.contains( MODERATOR_SENDER )){
				
				String subject = MailUtils.fetchSubjectFromInnerMessage( message );
				
				//check for do-not-moderate char - if presents - free immediately
				if( subject != null && subject.startsWith( DO_NOT_MODERATE_STR )) {
					
					mhandler.replyblank( message , MODERATE_TARGET );
					return;
				}

				message.setReceivedDate( Calendar.getInstance().getTime() );				
				mQueue.push( message );

				log.info( "message put on hold with subject: "  + message.getSubject() + " decoded: " );

			}else if( REMOVE_TARGET.equals( target ) ){

				log.info( "remove request received from " + sender );

				if( sender != null ) {

					SenderSatisfier ss = new SenderSatisfier( sender );
					List<Message> msgs = mQueue.pop( ss );

					if( msgs.size() > 0 ) {
						
						StringBuilder sb = new StringBuilder();
						
						sb.append( msgs.size() );
						sb.append( " " );
						sb.append( "הודעות הוסרו בהצלחה מרשימת ההמתנה על פי בקשך" );
						sb.append( "\n" );
						sb.append( "שורות הנושא בהודעות שהוסרו:" );
						sb.append( "\n" );
						
						for( Message msg : msgs ) {
							sb.append( MailUtils.fetchSubjectFromInnerMessage( msg ) );
							sb.append( "\n" );
						}
							

						mhandler.send( 	sender , 
										null, 
										SUPER_ADMIN,
										DO_NOT_REPLY, 
										null, 
										msgs.size() + " הודעות הוסרו מרשימת ההמתנה ", 
										sb.toString(),
										"UTF-16" );
						
					} else {
						
						mhandler.send( 	sender , 
										null, 
										SUPER_ADMIN,
										DO_NOT_REPLY, 
										null, 
										"לא נמצאה אף הודעה ברשימת ההמתנה" ,
										"שלחת בקשת הסרה של הודעות מהכתובת\n"
										+ sender + "\n" +
										"אך לא נמצאה אף הודעה מכתובת זו ברשימת ההמתנה\n" + 
										"ייתכן וההודעה ששלחת אינה הגיעה עדיין, תוכל/י לנסות לשלוח שוב בקשת הסרה בעוד כחצי דקה.",
										"UTF-16" );
						
						
					}

				}

			}




		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException { 

		String path = req.getRequestURI();
		
		if( FREE_PENDING_REQ.equals( path )) {

			Calendar cal = Calendar.getInstance();

			//adjust the calender for minus MIN_WAITING_PERIOD
			cal.add(Calendar.MINUTE, - MIN_WAITING_PERIOD);			 

			if( !mQueue.isEmpty() ){

				olderThenSatsfier.setFrom( cal.getTime() );
				List<Message> msgs = mQueue.pop( olderThenSatsfier );

				for ( Message msg : msgs ){

					mhandler.replyblank( msg , MODERATE_TARGET );
				}				
			} else {
				log.info( "message queue returned no messages, queue size: " + mQueue.size() );
			}

		}

	}

	@Override
	public void destroy() {
		log.log( Level.FINE , "Service went down, queue size: " + mQueue.size() );

		super.destroy();

	}

}
