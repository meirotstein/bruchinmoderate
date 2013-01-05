package il.so.rotstein.shit.test;

import il.co.rotstein.server.mail.MailUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class EmailsIterator {
	
	private String[] resourceList;
	private String rootpath;
	private int index = 0;
	
	public EmailsIterator() {
		
		rootpath = EmailsIterator.class.getResource("/emails").getPath();
		File dir = new File( rootpath );
		resourceList = dir.list();
		
	}
	
	public boolean hasNext() {
		
		if( resourceList != null ) {
			return resourceList.length > index;
		}
		return false;
	}
	public String next() {

		InputStream is = EmailsIterator.class.getResourceAsStream( "/emails/" + resourceList[ index ] );
		
		try {
			
			return MailUtils.streamToString( is , -1 );
			
		} catch (IOException e) {

			throw new RuntimeException( e );
			
		} finally {
			
			++index;
			
		}
	}
	
	public int getCurrenIndex() {
		
		return index;
		
	}

}
