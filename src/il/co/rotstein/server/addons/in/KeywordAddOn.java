package il.co.rotstein.server.addons.in;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;


import il.co.rotstein.server.addons.InAddOn;
import il.co.rotstein.server.common.StringPatterns;
import il.co.rotstein.server.config.Configurations;
import il.co.rotstein.server.exception.MailOperationException;
import il.co.rotstein.server.exception.StatisticsServiceException;
import il.co.rotstein.server.mail.MailHandler;
import il.co.rotstein.server.mail.MailUtils;
import il.co.rotstein.server.statistics.StatisticsService;
import il.co.rotstein.server.statistics.StatisticsServiceFactory;

public class KeywordAddOn extends InAddOn {
	
	public static final String ID = "KeywordAddOn"; 
	
	private final Logger log = Logger.getLogger( KeywordAddOn.class.getName() );
	
	//private static int FIRST_CHARS_TO_CHECK = 35;

	private Map<String, Integer> keywords;
	private StatisticsService statSrvs = null;
	
	@Override
	public void init( String[] parameters ) {
		super.init( parameters );
		try {
			statSrvs = StatisticsServiceFactory.getService( null );
		} catch (StatisticsServiceException e) {
			// do nothing
		}		
	}

	@Override
	public AddOnResult manipulate( Message message ) {

		try {

			String subject = "";

			try {

				subject = MailUtils.fetchSubjectFromInnerMessage( message );

			} catch (MailOperationException e) {

				log.log( Level.WARNING , "Fail to fetch subject from message - considering empty subject" , e );

			} 

			String content = "";

			try {

				content = MailUtils.fetchContentFromInnerMessage( message );

			} catch (MailOperationException e) {

				log.log( Level.WARNING , "Fail to fetch content for message - considering empty content " , e );

			} catch (UnsupportedEncodingException e) {

				log.log( Level.WARNING , "Fail to fetch content for message - considering empty content " , e );

			}
			
			if( ( content == null && subject == null ) || ( "".equals( content ) && "".equals( subject ) ) ) {
				
				return AddOnResult.Continue;
				
			}

			if( content == null ) 
				content = "";
			if( subject == null ) 
				subject = "";

			log.log( Level.FINE , "Keyword Add On.\nSubject: " + subject + "\nContent: " + content );

			if( keywords == null ) {

				keywords = new HashMap<String, Integer>();

				for( String param : parameters ) {					

					String[] values = param.split(",");

					keywords.put( values[0] , ( values.length > 1 ) ? new Integer( values[1] ) : new Integer( Integer.MAX_VALUE ) );

				}

			}

			for( String key : keywords.keySet() ) {

				String keyword = key;
				Integer numOfChars = keywords.get( key );

				log.log( Level.FINE , "Addon: check matching for " + keyword );

				if( subject.contains( keyword ) || 
						( ( ( content.length() > numOfChars ) ? 
								content.substring( 0 ,  numOfChars ).contains( keyword ) : 
									content.contains( keyword ) ) ) ) {

					String sender = MailUtils.fetchSenderFromInnerMessage( message );

					log.info( "Match! Keyword Add On recognized suspicious message, alerting " + sender );

					MailHandler.send( 	sender , 
							null, 
							Configurations.getSuperAdmin(), 
							"remove@" + Configurations.getMailHost(), 
							"remove@" + Configurations.getMailHost(), 
							StringPatterns.MISTAKE_ALERT_SUBJECT, 
							String.format( StringPatterns.MISTAKE_ALERT_BODY , subject ),
							"UTF-16",
							false);
					
					if( statSrvs != null ) {
						try {
							
							statSrvs.logCustom( ID , new Date() , sender , subject );
							
						} catch (StatisticsServiceException e) {
							log.log( Level.WARNING , "Fail to collect statistics for KeywordAddOn" , e );
						}
					}
					
					break;

				}

			}

		}  catch (MailOperationException e) {

			log.log( Level.WARNING , "Fail to fetch details from message - add on operation aborted" , e );

		} catch (AddressException e) {

			log.log( Level.WARNING , "Fail to alert sender - add on operation aborted" , e );

		} catch (MessagingException e) {

			log.log( Level.WARNING , "Fail to alert sender - add on operation aborted" , e );

		}

		return AddOnResult.Continue;
	}

}
