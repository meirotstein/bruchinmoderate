package il.co.rotstein.server.queue;

import javax.mail.Message;

interface Satisfier {
	
	public Boolean isSatisfied( Message msg );

}
