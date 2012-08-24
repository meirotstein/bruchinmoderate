package il.rotstein.server.queue;

import il.rotstein.server.mail.MailUtils;

import javax.mail.Message;

public class SenderSatisfier implements Satisfier {

	private String sender;
	
	public SenderSatisfier( String sender ) {
		
		this.sender = sender;
		
	}
	
	@Override
	public Boolean isSatisfied( Message msg ) {

		String innerSender = MailUtils.fetchSenderFromInnerMessage( msg );
		
		return this.sender.contains( innerSender );
		
	}

}
