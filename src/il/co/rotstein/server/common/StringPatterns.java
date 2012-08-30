package il.co.rotstein.server.common;

public class StringPatterns {
	
	public static String MESSAGE_TITLE = "<-- ���� ����� �������� ������ ���! -->\n\n"; 
	
	public static String MESSAGES_REMOVED_SUBJECT = "%s ������ ����� ������ ������ ������";
	public static String MESSAGE_REMOVED_SUBJECT = "������ ����� ������ ������ ������";
	
	public static String MESSAGES_REMOVED_BODY = MESSAGE_TITLE + "%s ������ ����� ������ ������ ������ �� �� ����\n\n" +
																	"����� ����� �� �������:\n" +
																	"%s";
	public static String MESSAGE_REMOVED_BODY = MESSAGE_TITLE + "������ ����� ������ ������ ������ �� �� ����\n\n" +
																	"���� ����� �� ������:\n" +
																	"%s";
	
	public static String MESSAGE_NOT_FOUND_SUBJECT = "�� ����� ������ ������ ������";
	public static String MESSAGE_NOT_FOUND_BODY = MESSAGE_TITLE + 
													"������ ���� ���� �������\n" +
													"%s\n" +
													"�� �� ����� �� ����� ������ �� ������ ������.\n\n" +
													"����� ���� ���� ����� ���� ��� ��� ����� ���� ����� ����� ����� ������ ������, ����/� ����� ����� ��� ���� ����.\n\n" +
													"������ ���� ���� ����� ���� ����� ������ ��.";
	
	public static String MANUAL_MODERATION_SUBJECT = "A message from %s is waiting for manual moderation ";
	public static String MANUAL_MODERATION_BODY = "https://groups.google.com/forum/?hl=en&fromgroups#!pendingmsg/%s";
	
	public static String MISTAKE_ALERT_SUBJECT = "����� ���! ����� ����� ����� ������ ����� ������";
	public static String MISTAKE_ALERT_BODY = MESSAGE_TITLE +
												"������ ����� ����� ������ �� ��� ����� ������ ����� ������.\n" +
												"���� ����� �� ������:\n\n" +
												"%s\n\n" +
												"����� ���� ����� �����, ���� ���� �� ����� ������ ������ ������� ���� ����� ����� �� ( ��� + ��� ).\n" +
												"������� - ������ ����� �� ������ ���� ��� �� ��� 5 � -8  ���� ����.\n\n" +
												"����� ��� ����� ����� - ���! ������ �� ������.";
	
}
