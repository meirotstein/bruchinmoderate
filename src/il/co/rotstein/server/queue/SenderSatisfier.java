package il.co.rotstein.server.queue;

import il.co.rotstein.server.exception.MailOperationException;
import il.co.rotstein.server.mail.MailUtils;

import javax.mail.Message;

public class SenderSatisfier implements Satisfier {

	private String sender;
	
	public SenderSatisfier( String sender ) {
		
		this.sender = sender;
		
	}
	
	@Override
	public Boolean isSatisfied( Message msg ) {

		try {
			
			String innerSender = MailUtils.fetchSenderFromInnerMessage( msg );
			
			return this.sender.contains( innerSender );
			
		} catch (MailOperationException e) {
			
			return false;
			
		}
		
	}

}
