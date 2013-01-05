package il.so.rotstein.shit.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import il.co.rotstein.server.ModeratedMessage;
import il.co.rotstein.server.exception.MailOperationException;
import il.co.rotstein.server.mail.MailUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.junit.Test;

public class ContentTest {
	
	private static String SEPERATOR = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>RESULTS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
	

	@Test
	public void test() {
		
		EmailsIterator ei = new EmailsIterator();
		
		while( ei.hasNext() ){
			String text = ei.next();
			
			String[] parts = text.split( SEPERATOR );
			
			String content = parts[ 0 ];
			String results = parts[ 1 ].replace("\r\n", "\n");
			
			Properties props = new Properties(); 
			Session session = Session.getDefaultInstance( props, null );
			
			try {
				
				ModeratedMessage msg = new ModeratedMessage(session, new ByteArrayInputStream( content.getBytes() ) );
				msg.setContentAsString(content);
				
				Pattern subjectPtrn = Pattern.compile("(Subject:)(.*)(\n)");
				Matcher subjectMtchr = subjectPtrn.matcher( results );
				
				String expectedSubject = "";
				
				if( subjectMtchr.find() ) {
				
					expectedSubject = results.substring( subjectMtchr.start() + 8 , subjectMtchr.end() ).trim();
				
				} else {
					fail("Test #  " + ei.getCurrenIndex() + " cannot find result match");
				}
				
				Pattern contentPtrn = Pattern.compile("(Content:)(.*)" , Pattern.DOTALL );
				Matcher contentMtchr = contentPtrn.matcher( results );
				
				String expectedContent = "";
				
				if( contentMtchr.find() ) {
					
					expectedContent = results.substring( contentMtchr.start() + 8 , contentMtchr.end() );
					
				} else {
					fail("Test #  " + ei.getCurrenIndex() + " cannot find content match");
				}
				
				Pattern senderPtrn = Pattern.compile("(Sender:)(.*)(\n)");
				Matcher senderMtchr = senderPtrn.matcher( results );
				
				String expectedSender = "";
				
				if( senderMtchr.find() ) {
					
					expectedSender = results.substring( senderMtchr.start() + 7 , senderMtchr.end() ).trim();
					
				} else {
					fail("Test #  " + ei.getCurrenIndex() + " cannot find sender match");
				}
				
				String sender;
				try {
					sender = MailUtils.fetchSenderFromInnerMessage( msg );
				} catch (MailOperationException e) {				
					sender = "";
				}				
				assertTrue( "Test #  " + ei.getCurrenIndex() + " Sender not match, got: " + sender + " , expected: " + expectedSender , 
						expectedSender.equals( sender.trim() ));
				
				String subject;
				try {
					subject = MailUtils.fetchSubjectFromInnerMessage( msg );
				} catch (MailOperationException e) {
					subject = "";
				}				
				assertTrue( "Test #  " + ei.getCurrenIndex() + " Subject not match, got: " + subject + " , expected: " + expectedSubject , 
						expectedSubject.equals( subject.replace("\r\n", "\n").trim() ));
				
				String cont;
				try {
					cont = MailUtils.fetchContentFromInnerMessage( msg );
				} catch (MailOperationException e) {
					cont = "";
				}
				if( !expectedContent.equals("*")) {
				assertTrue( "Test #  " + ei.getCurrenIndex() + " Content not match, got: " + cont + " , expected: " + expectedContent , 
						expectedContent.replaceAll("\r", "").replaceAll("\n", "").equals( cont.replaceAll("\r", "").replaceAll("\n", "") ));
				}
				
				
//				expectedContent.length();
				
			} catch ( Exception e ) {
				
				fail("Test #  " + ei.getCurrenIndex() + " Error while test invocation");
				
			}
			
			
		}
		
	}

}
