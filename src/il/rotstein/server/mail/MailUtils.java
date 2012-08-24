package il.rotstein.server.mail;

import il.rotstein.server.ModeratedMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;

public class MailUtils {

	private final static Logger log = Logger.getLogger(MailUtils.class.getName());

	private static String streamToString( InputStream is ) throws IOException {

		if (is != null) {

			Writer writer = new StringWriter();

			char[] buffer = new char[ 1024 ];
			try {
				Reader reader = new BufferedReader( new InputStreamReader( is ) );

				int n;

				while( ( n = reader.read( buffer ) ) > -1 ){
					writer.write( buffer, 0, n );
				}

			} finally {
				is.close();
			}

			return writer.toString();

		} else {
			return null;
		}

	}
	
	private static String fetchLineByPattern( Message message , String pattern ) {
		
		String result = null;		

		try {

			String body =  contentToString( message );

			log.log( Level.FINE , "Body is " + body );

			if( body != null && body != "" ){

				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher( body );
				
				if( m.find() ) {
					result = body.substring( m.start(), body.indexOf( "\n", m.start() ) );
					
					return result;
				}
			}

		} catch (IOException e) {
			log.log( Level.FINE , "Error parsing message" ,e );
		} catch (MessagingException e) {
			log.log( Level.FINE , "Error parsing message" ,e );
		}
		
		return null;
		
	}
	
	public static String contentToString( Message message ) throws IOException, MessagingException {
		
		String content;
		
		if( message instanceof ModeratedMessage ) {
			
			content = ( ( ModeratedMessage )  message ).getContentAsString();
			
			if( content == null ) {
				//cache content str
				content = streamToString( message.getInputStream() );
				( ( ModeratedMessage )  message ).setContentAsString( content );
				
			}
			
		} else {
			
			content = streamToString( message.getInputStream() );
			
		}
		
		return content;
		
	}

	public static String fetchSenderFromInnerMessage( Message message ) {

		String from = fetchLineByPattern( message , "(From:)(.*)(.+@.+\\.[a-z]+)" );
		
		if( from != null ){
			
			int emailStart = from.lastIndexOf("<");
			int emailEnd = from.lastIndexOf(">");
			
			if( emailEnd != -1 && emailStart != -1 && emailEnd > emailStart){
				String email = from.substring( emailStart + 1 , emailEnd );
				log.log( Level.FINE , "'From' str is " + email );
				return email;
			}
		}
		
		return null;

	}
	
	public static String fetchSubjectFromInnerMessage( Message message ) {

		String subject = fetchLineByPattern( message , "(Subject:)(.*)" );
		
		if( subject != null ){
			return subject.substring( 8 );
		}
		
		return null;

	}

}
