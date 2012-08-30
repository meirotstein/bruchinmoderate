package il.co.rotstein.server.mail;

import il.co.rotstein.server.ModeratedMessage;
import il.co.rotstein.server.exception.MailOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

public class MailUtils {

	private final static Logger log = Logger.getLogger(MailUtils.class.getName());

	private final static int BUFFER_SIZE = 1024; 

	/**
	 * Serialize stream to string
	 * @param is InputStream
	 * @param limit in Bytes of the string size to be fetched, should be bigger then BUFFER_SIZE (best is to use multiplicity of it, e.g. BUFFER_SIZE * 6), smaller parameters will be ignored and the complete stream will be fetched
	 * @return
	 * @throws IOException
	 */
	private static String streamToString( InputStream is , long limit ) throws IOException {

		if (is != null) {

			Writer writer = new StringWriter();

			char[] buffer = new char[ BUFFER_SIZE ];
			try {
				Reader reader = new BufferedReader( new InputStreamReader( is ) );

				int n;

				if( limit > BUFFER_SIZE ) {

					int cycles = ( int ) ( limit / BUFFER_SIZE );

					while( ( n = reader.read( buffer ) ) > -1 && cycles > 0 ){
						writer.write( buffer, 0, n );
						--cycles;
					} 

				} else {

					while( ( n = reader.read( buffer ) ) > -1 ){
						writer.write( buffer, 0, n );
					} 

				}

			} finally {
				is.close();
			}

			return writer.toString();

		} else {
			return null;
		}

	}

	private static String fetchLineByPattern( Message message , Pattern pattern ) {

		String result = null;	

		try {

			String body = contentToString( message );

			log.log( Level.FINE , "Body is " + body );

			if( body != null && body != "" ){

				Matcher m = pattern.matcher( body );

				if( m.find() ) {

					result = body.substring( m.start(), body.indexOf( "\n", m.start() ) );

					return MimeUtility.decodeText( result );

				}
			}

		} catch (IOException e) {
			log.log( Level.FINE , "Error parsing message" ,e );
		} catch (MessagingException e) {
			log.log( Level.FINE , "Error parsing message" ,e );
		}

		return null;

	}

	/**
	 * Returns text section between two matches
	 * 
	 * @param message
	 * @param startPattern upper border of the section (pattern text not included)  
	 * @param endPattern lower border of the section (pattern text not included), if null / no matches the lower border will be the end of text
	 * @return
	 */
	private static String fetchSectionFromContent( Message message , Pattern startPattern , Pattern endPattern ) {

		String result = null;		

		try {

			String body = contentToString( message );

			//log.log( Level.FINE , "Body is " + body );

			if( body != null && body != "" ){

				Matcher sm = startPattern.matcher( body );

				Matcher em = endPattern.matcher( body );

				if( sm.find() ) {

					int startPos = sm.end();
					int endPos;

					if( em.find( startPos ) ) {

						endPos = em.start();

						if( endPos <= startPos ){

							endPos = body.length() - 1;

						}

					} else {

						endPos = body.length() - 1;

					}

					result = body.substring( startPos , endPos );

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
				content = streamToString( message.getInputStream() , BUFFER_SIZE * 500 );
				( ( ModeratedMessage )  message ).setContentAsString( content );

			}

		} else {

			content = streamToString( message.getInputStream() , BUFFER_SIZE * 500 );

		}

		return content;

	}

	public static String fetchSenderFromInnerMessage( Message message ) throws MailOperationException {

		String from = fetchLineByPattern( message , Pattern.compile( "(From:)(.*)(.+@.+\\.[a-z]+)" ) );

		if( from != null ){

			int emailStart = from.lastIndexOf("<");
			int emailEnd = from.lastIndexOf(">");

			if( emailEnd != -1 && emailStart != -1 && emailEnd > emailStart){
				String email = from.substring( emailStart + 1 , emailEnd );
				log.log( Level.FINE , "'From' str is " + email );
				return email;
			}
		}

		throw new MailOperationException( "fail to fetch Sender from inner message" );

	}

	public static String fetchContentFromInnerMessage( Message message ) throws MailOperationException, UnsupportedEncodingException {

		String section = fetchSectionFromContent( message , Pattern.compile( "(From:)(.*)(Content-Type: text/plain;)" , Pattern.DOTALL ) , Pattern.compile( "(--)(.*)(Content-Type:)" , Pattern.DOTALL ) );

		if( section != null ) {
			
			String decodedSection = decodeContent( section );
			
			log.log( Level.FINE , "Fetched content: " + decodedSection );
			
			return decodedSection;
			
		} else {
			
			throw new MailOperationException( "Content does not contains enougth data for decoding" );
			
		}		

	}

	public static String fetchSubjectFromInnerMessage( Message message ) throws MailOperationException {

		String subject = fetchLineByPattern( message , Pattern.compile( "(Subject:)(.*)" ) );

		if( subject != null ){
			
			log.log( Level.FINE , "Fetched subject: " + subject );
			
			return subject.substring( 8 );
		}

		throw new MailOperationException( "fail to fetch Subject from inner message" );

	}

	private static String decodeContent( String content ) throws UnsupportedEncodingException {

		try {

			if( content == null )
				throw new RuntimeException( "Content is null" );

			String charset;
			String encoing;

			Pattern charsetP = Pattern.compile( "(charset=)(.*)" );
			Matcher charsetM = charsetP.matcher( content );

			if( charsetM.find() ) {
				charset = content.substring( charsetM.start() + 8 , charsetM.end() ).replace( "\"", "" ).trim();
			} else {
				throw new RuntimeException( "Cannot find charset expression" );
			}

			Pattern enocdeP = Pattern.compile( "(Content-Transfer-Encoding: )(.*)" );
			Matcher enocdeM = enocdeP.matcher( content );

			if( enocdeM.find() ) {
				encoing = content.substring( enocdeM.start() + 27 , enocdeM.end() ).trim();
			} else {
				throw new RuntimeException( "Cannot find encoding expression" );
			}

			if( "".equals( encoing ) || "".equals( charset ) )
				throw new RuntimeException( "no charset or encoding" );



			return MimeUtility.decodeText( "=?" + charset + "?" + 
					encoing.toUpperCase().charAt( 0 ) + "?" + 
					content.substring( enocdeM.end() ).trim().replaceAll(" " ,  "" ).replaceAll( "\n" , "" ) + "?=" );

		} catch (RuntimeException e) {

			log.warning( "Content was not decoded, Error: " + e.getMessage() );
			return content;
			
		}


	}
	
	

}
