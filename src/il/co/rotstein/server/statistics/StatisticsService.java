package il.co.rotstein.server.statistics;

import il.co.rotstein.server.exception.StatisticsServiceException;

import java.util.Date;

import javax.mail.Session;


/**
 * @author I058533
 *
 */
public interface StatisticsService {
	
	/**
	 * @param session
	 * @throws StatisticsServiceException
	 */
	public void init( Session session ) throws StatisticsServiceException;
	
	/**
	 * @param when		date of remove request has been initiated 
	 * @param address	address of initiator
	 * @param subject	subject of the removed message
	 */
	public void logRemoveRequest( Date when , String address , String subject) throws StatisticsServiceException;
	
	/**
	 * log request for skip moderation (@ as subject prefix)
	 * 
	 * @param when		date of skip request has been initiated 
	 * @param address	address of initiator
	 * @param subject	subject of the skipped message
	 */
	public void logSkipModerationRequest( Date when , String address , String subject) throws StatisticsServiceException;
	
	
	/**
	 * @param when		date of moderated message has been initiated 
	 * @param address	address of initiator
	 * @param subject	subject of the moderated message
	 */
	public void logManualModeration( Date when , String address , String subject ) throws StatisticsServiceException;
	
	/**
	 * General method for add record to statistics (maily for addons usage) 
	 * 
	 * @param when		date of moderated message has been initiated 
	 * @param address	address of initiator
	 * @param subject	subject of the moderated message
	 */
	public void logCustom( String statId , Date when , String address , String subject ) throws StatisticsServiceException;
	
	/**
	 * @param when	date of raised error
	 * @param errorMsg	
	 */
	public void logError( Date when , String errorMsg ) throws StatisticsServiceException;
	
	/**
	 * @param from earliest date of logged messages, if null - count all 	
	 */
	public int countRemoveRequests( Date from ) throws StatisticsServiceException;
	
	/**
	 * @param from earliest date of logged messages, if null - count all 	
	 */
	public int countSkipRequests( Date from ) throws StatisticsServiceException;
	
	/**
	 * @param from earliest date of logged messages, if null - count all 	
	 */
	public int countManualModerations( Date from ) throws StatisticsServiceException;
	
	/**
	 * @param from earliest date of logged messages, if null - count all 	
	 */
	public int countCustom( String statId , Date from ) throws StatisticsServiceException;

}
