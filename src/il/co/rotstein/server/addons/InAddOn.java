package il.co.rotstein.server.addons;

import javax.mail.Message;

public abstract class InAddOn implements AddOn {
	
	protected String[] parameters;
	
	public void init( String[] parameters ) {
		this.parameters = parameters;
	}
	
	public abstract AddOnResult manipulate( Message message );

}
