package il.rotstein.server.queue;

import javax.mail.Message;

interface Satisfier {
	
	public Boolean isSatisfied( Message msg );

}
