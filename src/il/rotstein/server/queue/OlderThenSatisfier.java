package il.rotstein.server.queue;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;

public class OlderThenSatisfier implements Satisfier {
	
	private final Logger log = Logger.getLogger(OlderThenSatisfier.class.getName());

	private Date from;
	
	public void setFrom( Date from ) {
		
		this.from = from;
		
	}
	
	@Override
	public Boolean isSatisfied( Message msg ) {

		try {

			log.log( Level.FINE , "Date of queue msg: " + msg.getReceivedDate() + " from Date: " + from );

			if (msg.getReceivedDate().before(from)) {
				return true;
			}

		} catch (MessagingException e) {
			// should never happen
		}
		
		return false;
		
	}

}
