package il.co.rotstein.server;

import java.io.InputStream;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class ModeratedMessage extends MimeMessage {

	private Date receivedDate;
	private String contentAsString;
	
	public ModeratedMessage(Session session, InputStream in)
			throws MessagingException {
		super(session, in);
	}
	
	public ModeratedMessage( Session session ) {
		super( session );
	}
	
	
	public void setReceivedDate( Date date ) {
		receivedDate = date;
	}
	
	@Override
	public Date getReceivedDate() throws MessagingException {
		
		return receivedDate;
		
	}
	
	public void setContentAsString( String content ){
		
		this.contentAsString = content;
		
	}
	
	public String getContentAsString() {
		
		return this.contentAsString;
		
	}

}
