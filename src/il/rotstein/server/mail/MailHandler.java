package il.rotstein.server.mail;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailHandler {

	private final Logger log = Logger.getLogger(MailHandler.class.getName());
	
	private Session session;

	public MailHandler( Session session) {

		this.session = session;

	}

	public void replyblank( Message message ) {
		
		replyblank( message , null );

	}
	
	public void replyblank( Message message , String from ) {


		try {

			String to = ((InternetAddress) message.getReplyTo()[0]).getAddress();
			
			if( from == null ) {
				from =  ((InternetAddress) message.getRecipients(RecipientType.TO)[0]).getAddress();
			}

			String subject = message.getSubject();

			Message msg = message.reply(false);
			
			if( message instanceof MimeMessage ) {
				((MimeMessage) msg).setSubject( subject , "UTF-16");
			}
			
			msg.setFrom(new InternetAddress( from ));
			
			msg.setText("");
			
			log.info( "Replying " + to + " from " + msg.getFrom()[0]);

			Transport.send(msg);

		} catch (MessagingException e) {
			log.log(Level.WARNING, "Reply failed with exception: " + e.getMessage() , e );
		}

	}
	
	public void send( 	String to ,
						String cc,
						String bcc,
						String from, 
						String replyTo, 
						String subject, 
						String body, 
						String encoding ) throws AddressException, MessagingException {
		
		MimeMessage msg = new MimeMessage( session );
		
		if( to != null && to !="" )
			msg.setRecipient( RecipientType.TO , new InternetAddress( to ) );
		if( cc != null && cc !="" )
			msg.setRecipient( RecipientType.CC , new InternetAddress( cc ) );
		if( bcc != null && bcc !="" )
			msg.setRecipient( RecipientType.BCC , new InternetAddress( bcc ) );
		if( from != null && from !="" )
			msg.setFrom( new InternetAddress( from ) );
		if( replyTo != null && replyTo !="" )
			msg.setReplyTo( new InternetAddress[] { new InternetAddress( replyTo ) } );
		if( subject != null && subject !="" )
			msg.setSubject( subject , encoding );
		if( body != null && body !="" )
			msg.setText( body , encoding );
		
		Transport.send(msg);
		
	}


}
