package il.co.rotstein.server.common;

public class StringPatterns {
	
	public static String MESSAGE_TITLE = "<-- זוהי הודעה אוטומטית ממערכת שיט! -->\n\n"; 
	
	public static String MESSAGES_REMOVED_SUBJECT = "%s הודעות הוסרו בהצלחה מרשימת ההמתנה";
	public static String MESSAGE_REMOVED_SUBJECT = "ההודעה הוסרה בהצלחה מרשימת ההמתנה";
	
	public static String MESSAGES_REMOVED_BODY = MESSAGE_TITLE + "%s הודעות הוסרו בהצלחה מרשימת ההמתנה על פי בקשך\n\n" +
																	"שורות הנושא של ההודעות:\n" +
																	"%s";
	public static String MESSAGE_REMOVED_BODY = MESSAGE_TITLE + "ההודעה הוסרה בהצלחה מרשימת ההמתנה על פי בקשך\n\n" +
																	"שורת הנושא של ההודעה:\n" +
																	"%s";
	
	public static String MESSAGE_NOT_FOUND_SUBJECT = "לא נמצאו הודעות ברשימת ההמתנה";
	public static String MESSAGE_NOT_FOUND_BODY = MESSAGE_TITLE + 
													"התקבלה בקשת הסרה מהכתובת\n" +
													"%s\n" +
													"אך לא נמצאה אף הודעה מכתובת זו ברשימת ההמתנה.\n\n" +
													"במידה ואכן שלחת הודעה לפני זמן קצר ייתכן והיא עדיין איננה נכנסה לרשימת ההמתנה, תוכל/י לנסות לשלוח שוב בקשת הסרה.\n\n" +
													"לשליחת בקשת הסרה נוספת ניתן לענות להודעה זו.";
	
	public static String MANUAL_MODERATION_SUBJECT = "A message from %s is waiting for manual moderation ";
	public static String MANUAL_MODERATION_BODY = "https://groups.google.com/forum/?hl=en&fromgroups#!pendingmsg/%s";
	
	public static String MISTAKE_ALERT_SUBJECT = "מערכת שיט! זיהתה הודעה שיתכן ונשלחה בטעות לקבוצה";
	public static String MISTAKE_ALERT_BODY = MESSAGE_TITLE +
												"המערכת זיהתה הודעה שנשלחה על ידך ויתכן ונשלחה בטעות לקבוצה.\n" +
												"שורת הנושא של ההודעה:\n\n" +
												"%s\n\n" +
												"במידה ואכן מדובר בטעות, ניתן לבטל את העברת ההודעה לקבוצה באמצעות מענה מיידי למייל זה ( השב + שלח ).\n" +
												"להזכירך - המערכת מעכבת את ההודעה לפרק זמן של בין 5 ל -8  דקות בלבד.\n\n" +
												"במידה ולא מדובר בטעות - שיט! מתנצלת על ההטרדה.";
	
}
