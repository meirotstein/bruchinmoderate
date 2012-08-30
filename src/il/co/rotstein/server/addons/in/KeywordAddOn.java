package il.co.rotstein.server.addons.in;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import il.co.rotstein.server.addons.InAddOn;
import il.co.rotstein.server.common.StringPatterns;
import il.co.rotstein.server.config.Configurations;
import il.co.rotstein.server.exception.MailOperationException;
import il.co.rotstein.server.mail.MailHandler;
import il.co.rotstein.server.mail.MailUtils;

public class KeywordAddOn extends InAddOn {
	
	private final Logger log = Logger.getLogger(KeywordAddOn.class.getName());
	
	private static int FIRST_CHARS_TO_CHECK = 35;

	@Override
	public AddOnResult manipulate(Message message) {
		
		try {
			
			String subject = MailUtils.fetchSubjectFromInnerMessage( message );
			String content = MailUtils.fetchContentFromInnerMessage( message );
			
			if( content.length() < FIRST_CHARS_TO_CHECK ) {
				FIRST_CHARS_TO_CHECK = content.length() - 1;
			}
			
			log.log( Level.FINE , "Keyword Add On.\nSubject: " + subject + "\nContent: " + content.substring( 0 ,  FIRST_CHARS_TO_CHECK ) );
			
			for( String keyword : parameters ) {
				
				log.log( Level.FINE , "Addon: check matching for " + keyword );
				
				if( subject.contains( keyword ) || content.substring( 0 , FIRST_CHARS_TO_CHECK ).contains( keyword ) ) {
					
					String sender = MailUtils.fetchSenderFromInnerMessage( message );
					
					log.info( "Match! Keyword Add On recognized suspicious message, alerting " + sender );
					
					MailHandler.send( 	sender , 
										null, 
										Configurations.getSuperAdmin(), 
										"remove@" + Configurations.getMailHost(), 
										"remove@" + Configurations.getMailHost(), 
										StringPatterns.MISTAKE_ALERT_SUBJECT, 
										String.format( StringPatterns.MISTAKE_ALERT_BODY , subject ),
										"UTF-16" );
					
				}
				
			}
			
		} catch (UnsupportedEncodingException e) {
			
			log.log( Level.WARNING , "Fail to fetch details from message - add on operation aborted" , e );
			
		} catch (MailOperationException e) {
			
			log.log( Level.WARNING , "Fail to fetch details from message - add on operation aborted" , e );
			
		} catch (AddressException e) {
			
			log.log( Level.WARNING , "Fail to alert sender - add on operation aborted" , e );
			
		} catch (MessagingException e) {
			
			log.log( Level.WARNING , "Fail to alert sender - add on operation aborted" , e );
			
		}
		
		return AddOnResult.Continue;
	}

}
