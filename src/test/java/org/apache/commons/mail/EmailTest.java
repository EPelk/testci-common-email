package org.apache.commons.mail;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EmailTest {
	Email email;
	/**
	 * Initialize new Email object.
	 */
	@Before
	public void setup() {
		email = new EmailDummy();
	}
	
	
	// Test addBcc() //
	/**
	 * Passes a null email address into addBcc(),
	 * resulting in an exception.
	 * @throws EmailException
	 */
	@Test(expected = EmailException.class)
	public void addBccNullTest() throws EmailException {
		String addr[] = null;
		email.addBcc(addr);
	}
	
	/**
	 * Passes a string not following the format addr@domain
	 * to addBcc(), resulting in an exception.
	 * @throws EmailException
	 */
	@Test(expected = EmailException.class)
	public void addBccInvalidEmailTest() throws EmailException {
		String addr[] = {"invalid"};
		email.addBcc(addr);
	}
	
	/**
	 * Test addBcc() with 2 valid email addresses.
	 * Should return successfully.
	 * @throws EmailException - should not be thrown
	 */
	@Test
	public void addBccSuccessTest() throws EmailException {
		String addr[] = {"abc@def", "ghi@jkl"};
		// Add BCC addresses, then extract them to an ArrayList
		List<String> result = new ArrayList<String>();
		for(InternetAddress bcc : email.addBcc(addr).getBccAddresses()) {
			result.add(bcc.getAddress());
		}
		assertArrayEquals(addr, result.toArray());
	}
	
	
	// Test addCc() //
	
	/**
	 * Pass an invalid email address to addCc(), raising
	 * an exception.
	 * @throws EmailException
	 */
	@Test(expected = EmailException.class)
	public void addCcInvalidEmailTest() throws EmailException {
		email.addCc("invalid");
	}
	
	/**
	 * Pass a valid email address to addCc().
	 * @throws EmailException - should not be thrown
	 */
	@Test
	public void addCcSuccessTest() throws EmailException {
		// Use an array here for easy comparison with the output
		// of getCcAddresses()
		String addr[] = {"abc@def"};
		List<String> result = new ArrayList<String>();
		for(InternetAddress cc : email.addCc(addr[0]).getCcAddresses()) {
			result.add(cc.getAddress());
		}
		assertArrayEquals(addr, result.toArray());
	}
	
	
	// Test addHeader() //
	
	/**
	 * Attempt to add a header with no key.
	 * Should fail.
	 * @throws IllegalArgumentException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addHeaderIllegalNameTest() throws IllegalArgumentException {
		email.addHeader("", "test_val");
	}
	
	/**
	 * Attempt to add a header with no value.
	 * Should fail.
	 * @throws IllegalArgumentException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addHeaderIllegalValTest() throws IllegalArgumentException {
		email.addHeader("test_name", "");
	}
	
	/**
	 * Successfully add a header.
	 * @throws IllegalArgumentException - should not be thrown
	 */
	@Test
	public void addHeaderSuccessTest() throws IllegalArgumentException {
		String key = "test_name";
		// Email headers are stored in a HashMap. Create one here
		// for easy comparison during assertion.
		Map<String, String> expected = new HashMap<String, String>(1);
		expected.put(key, "test_val");
		email.addHeader(key, expected.get(key));
		assertEquals(true, email.headers.equals(expected));
	}
	
	
	// Test addReplyTo() //
	
	/**
	 * Attempt to add an invalid reply-to email address.
	 * Should fail.
	 * @throws EmailException
	 */
	@Test(expected = EmailException.class)
	public void addReplyToInvalidEmailTest() throws EmailException {
		email.addReplyTo("invalid", "name");
	}
	
	/**
	 * Successfully add a reply-to email address and name.
	 * @throws EmailException - should not be thrown
	 */
	@Test
	public void addReplyToSuccessTest() throws EmailException {
		String addr = "abc@def";
		String name = "name";
		// Use array to match output format of getReplyToAddresses().
		// Format the string the same as InternetAddress.toString().
		String expected[] = {name + " <" + addr + ">"};
		List<String> result = new ArrayList<String>();
		for(InternetAddress replyTo : email.addReplyTo(addr, name).getReplyToAddresses()) {
			result.add(replyTo.toString());
		}
		assertArrayEquals(expected, result.toArray());
	}
	
	
	// Test buildMimeMessage()
	
	/**
	 * Test setting from, to, BCC, and CC addresses in Mime messages.
	 * Should not throw any exceptions.
	 * @throws EmailException
	 * @throws MessagingException
	 */
	@Test
	public void buildMimeMessageParticipantsTest() throws EmailException, MessagingException {
		email.setHostName("localhost");
		// All addresses that will participate in the email.
		// to, CC, and BCC appear in the same order that
		// getAllRecipients() below returns them.
		String expected[] = {
			"aaa@aaa",  // from
			"bbb@bbb",  // to
			"ddd@ddd",  // CC
			"ccc@ccc"   // BCC
		};
		// Set addresses and build message
		email.setFrom(expected[0]);
		email.addTo(expected[1]);
		email.addCc(expected[2]);
		email.addBcc(expected[3]);
		email.buildMimeMessage();
		
		// Extract addresses from message
		MimeMessage msg = email.getMimeMessage();
		List<String> msgParticipants = new ArrayList<String>();
		// Get from
		msgParticipants.add(msg.getFrom()[0].toString());
		// Get to, CC, and BCC in that order
		for(Address i : msg.getAllRecipients()) {
			msgParticipants.add(i.toString());
		}
		assertArrayEquals(expected, msgParticipants.toArray());
	}
	
	/**
	 * Attempt to build a message with no from address.
	 * Should raise an exception.
	 * @throws EmailException
	 */
	@Test(expected = EmailException.class)
	public void buildMimeMessageNoFromTest() throws EmailException {
		// Setup
		email.setHostName("localhost");
		email.addTo("to@aaa");
		
		// Build message
		email.buildMimeMessage();
	}
	
	/**
	 * Attempt to build a message with no recipient addresses.
	 * Should raise an exception.
	 * @throws EmailException
	 */
	@Test(expected = EmailException.class)
	public void buildMimeMessageNoRecipientTest() throws EmailException {
		// Setup
		email.setHostName("localhost");
		email.setFrom("from@aaa");
		
		// Build message
		email.buildMimeMessage();
	}
	
	/**
	 * Test setting reply-to addresses in messages.
	 * Should not throw exceptions. 
	 * @throws EmailException
	 * @throws MessagingException
	 */
	@Test
	public void buildMimeMessageReplyTest() throws EmailException, MessagingException {
		// Setup
		email.setHostName("localhost");
		email.setFrom("from@aaa");
		email.addTo("to@aaa");
		
		// Set reply-to addresses
		Address expected[] = {new InternetAddress("aaa@aaa")};
		email.addReplyTo(expected[0].toString());
		
		// Build message
		email.buildMimeMessage();
		
		// Test
		assertArrayEquals(expected, email.getMimeMessage().getReplyTo());
	}
	
	/**
	 * Test adding headers to messages. Should not throw exceptions.
	 * @throws EmailException
	 * @throws MessagingException
	 */
	@Test
	public void buildMimeMessageHeaderTest() throws EmailException, MessagingException {
		// Setup
		email.setHostName("localhost");
		email.setFrom("from@aaa");
		email.addTo("to@aaa");
		
		// Set headers
		email.addHeader("test_name", "test_val");
		
		// Build message
		email.buildMimeMessage();
		
		// Test
		assertEquals("test_val", email.getMimeMessage().getHeader("test_name")[0]);
	}
	
	/**
	 * Test setting a subject but no charset in a message.
	 * Should not throw exceptions.
	 * @throws EmailException
	 * @throws MessagingException 
	 */
	@Test
	public void buildMimeMessageSubjectNoCharsetTest() throws EmailException, MessagingException {
		// Setup
		email.setHostName("localhost");
		email.setFrom("from@aaa");
		email.addTo("to@aaa");
		
		// Set subject and build
		email.setSubject("subject");
		email.buildMimeMessage();
		
		// Test
		assertEquals("subject", email.getMimeMessage().getSubject());
	}
	
	/**
	 * Test setting subject and charset on a message.
	 * @throws EmailException
	 * @throws MessagingException 
	 */
	@Test
	public void buildMimeMessageSubjectAndCharsetTest() throws EmailException, MessagingException {
		// Setup
		email.setHostName("localhost");
		email.setFrom("from@aaa");
		email.addTo("to@aaa");
		
		// Set subject and charset
		String expected = "\u2615";
		email.setSubject(expected);
		email.setCharset("utf-8");
		
		// Build
		email.buildMimeMessage();
		
		// Test
		assertEquals(expected, email.getMimeMessage().getSubject());
	}
	
	/**
	 * Attempt to build the message twice. Should throw
	 * an IllegalStateException.
	 * @throws EmailException
	 */
	@Test(expected = IllegalStateException.class)
	public void buildMimeMessageTwiceTest() throws EmailException {
		// Setup
		email.setHostName("localhost");
		email.setFrom("from@aaa");
		email.addTo("to@aaa");
		
		// Build
		email.buildMimeMessage();
		email.buildMimeMessage();
	}
	
	/**
	 * Test setting plaintext message content.
	 * @throws EmailException
	 * @throws IOException
	 * @throws MessagingException
	 */
	@Test
	public void buildMimeMessagePlaintextContentTest() throws EmailException, IOException, MessagingException {
		// Setup
		email.setHostName("localhost");
		email.setFrom("from@aaa");
		email.addTo("to@aaa");
		email.setContent("content", EmailConstants.TEXT_PLAIN);
		
		// Build and test
		email.buildMimeMessage();
		assertEquals("content", email.getMimeMessage().getContent());
	}
	
	
	// Test getHostName() //
	
	/**
	 * Attempt to retrieve the host name when no
	 * such value has been set. Should return null.
	 */
	@Test
	public void getHostNameNullTest() {
		assertEquals(null, email.getHostName());
	}
	
	/**
	 * Set a string host name, then retrieve it.
	 */
	@Test
	public void getHostNameNoSessionTest() {
		String name = "localhost";
		email.setHostName(name);
		assertEquals(name, email.getHostName());
	}
	
	/**
	 * Initialize the email session and set the host name
	 * as one of its properties; then retrieve it.
	 */
	@Test
	public void getHostNameSessionTest() {
		String name = "localhost";
		// Setup session
		Properties prop = new Properties();
		prop.setProperty(EmailConstants.MAIL_HOST, name);
		email.setMailSession(Session.getInstance(prop));
		// Get host name
		assertEquals(name, email.getHostName());
	}
	
	
	// Test getMailSession() //
	
	/**
	 * Attempt to get the mail session before a session or
	 * host name is set. Should fail.
	 * @throws EmailException
	 */
	@Test(expected = EmailException.class)
	public void getMailSessionNoSessionNoHostTest() throws EmailException {
		email.getMailSession();
	}
	
	/**
	 * Cause the email to create its own session, then
	 * compare that session with a predefined session designed to
	 * match the properties created by default in email.
	 * @throws EmailException - should not be thrown
	 */
	@Test
	public void getMailSessionNoSessionTest() throws EmailException {
		// Set email properties
		email.setHostName("localhost");
		email.setSSLOnConnect(true);
		email.setBounceAddress("abc@def");
		// Setup session similar to that expected from getMailSession()
		Session session = email.getMailSession();
		Properties prop = new Properties();
		prop.setProperty(EmailConstants.MAIL_HOST, "localhost");
		prop.setProperty(EmailConstants.MAIL_SMTP_SOCKET_FACTORY_PORT, "465");
		prop.setProperty(EmailConstants.MAIL_SMTP_SOCKET_FACTORY_CLASS, "javax.net.ssl.SSLSocketFactory");
		prop.setProperty(EmailConstants.MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false");
		prop.setProperty(EmailConstants.MAIL_TRANSPORT_PROTOCOL, EmailConstants.SMTP);
		prop.setProperty(EmailConstants.MAIL_PORT, "465"); // Not 25 due to setting SSL to true
		prop.setProperty(EmailConstants.MAIL_SMTP_FROM, "abc@def");
		
		// List of strings representing expected key-value pairs in the session
		List<String> expected = new ArrayList<String>();
		// List of expected keys in the session
		List<String> keys = new ArrayList<String>();
		
		// Populate above lists based on expected session created above
		for(Entry<Object, Object> pair : prop.entrySet()) {
			expected.add(pair.getKey() + ":" + pair.getValue());
			keys.add((String) pair.getKey());
		}
		
		// From email's session, extract key-value pairs defined in the
		// expected session. Extraneous pairs are omitted.
		List<String> actual = new ArrayList<String>();
		for(String key : keys) {
			actual.add(key + ":" + session.getProperty(key));
		}
		
		// Compare extracted pairs.
		assertArrayEquals(expected.toArray(), actual.toArray());
	}
	
	
	// Test getSentDate() //
	
	/**
	 * Test getSentDate() when no date is previously defined.
	 * This function only checks that a Date is returned since the
	 * exact value is not known in advance.
	 */
	@Test
	public void getSentDateNoDateTest() {
		Date d = email.getSentDate();
		// Since Email allocates the date based on the current time,
		// we lack an exact value to compare it to.
		assertEquals(true, d instanceof Date);
	}
	
	/**
	 * Set a send date on the email; then, extract it.
	 */
	@Test
	public void getSentDateSetDateTest() {
		Date expected = new Date();
		email.setSentDate(expected);
		Date d = email.getSentDate();
		assertEquals(expected, d);
	}
	
	
	// Test getSocketConnectionTimeout() //
	
	/**
	 * Test retrieval of default timeout.
	 */
	@Test
	public void getSocketConnectionTimeoutDefaultTest() {
		assertEquals(EmailConstants.SOCKET_TIMEOUT_MS, email.getSocketConnectionTimeout());
	}
	
	/**
	 * Set custom timeout, then retrieve it.
	 */
	@Test
	public void getSocketConnectionTimeoutSetValTest() {
		int timeout = 100;
		email.setSocketConnectionTimeout(timeout);
		assertEquals(timeout, email.getSocketConnectionTimeout());
	}
	
	
	// Test setFrom() //
	
	/**
	 * Attempt to set an invalid email address as the sender.
	 * Should fail.
	 * @throws EmailException
	 */
	@Test(expected = EmailException.class)
	public void setFromInvalidEmailTest() throws EmailException {
		email.setFrom("invalid");
	}
	
	/**
	 * Set a valid email address as the sender, then retrieve it
	 * for testing.
	 * @throws EmailException - should not be thrown
	 */
	@Test
	public void setFromSuccessTest() throws EmailException {
		String addr = "abc@def";
		assertEquals(addr, email.setFrom(addr).getFromAddress().getAddress());
	}
	
	
	/**
	 * Not used; all data is garbage collected
	 */
	@After
	public void teardown() {
		
	}

}
