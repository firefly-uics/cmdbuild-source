package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Security;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.exception.EmailException.EmailExceptionType;

public class SendMailServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String CC = "cc";
	private static final String SUBJECT = "subject";
	private static final String TEXT = "emailtext";
	private static final String CLASSNAME = "classname";
	private static final String PROCESSID = "processid";
	private static final String EMAIL_TYPE = "type";

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out = response.getWriter();

		final String from = request.getParameter(FROM);
		final String subject = request.getParameter(SUBJECT);
		final String emailtext = request.getParameter(TEXT);
		final String type = request.getParameter(EMAIL_TYPE);
		final String processid = StringUtils.defaultIfEmpty(request.getParameter(PROCESSID), "");
		final String classname = StringUtils.defaultIfEmpty(request.getParameter(CLASSNAME), "");

		final String to = StringUtils.defaultIfEmpty(request.getParameter(TO), "");
		final String toaddresses = StringUtils.deleteWhitespace(to);

		final String cc = StringUtils.defaultIfEmpty(request.getParameter(CC), "");
		final String ccaddresses = StringUtils.deleteWhitespace(cc);

		final String smtpserver = PortletConfiguration.getInstance().getSMTPAddress();
		final String port = PortletConfiguration.getInstance().getSMTPPort();
		final String user = PortletConfiguration.getInstance().getSMTPUser();
		final String password = PortletConfiguration.getInstance().getSMTPPassword();

		final boolean useSSL = PortletConfiguration.getInstance().useSSL();
		final Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		Log.PORTLET.debug("Setting host " + smtpserver);
		props.setProperty("mail.host", smtpserver);
		props.put("mail.smtp.host", smtpserver);
		Log.PORTLET.debug("Setting port " + port);
		props.put("mail.smtp.port", port);

		Session mailsession;

		if ((user != null && user.length() > 0) || (password != null && password.length() > 0)) {
			Log.PORTLET.debug("Trying to send email with authentication");
			props.put("mail.smtp.auth", "true");
			Log.PORTLET.debug("Setting user " + user);
			final Authenticator auth = new SMTPAuthenticator(user, password);
			if (useSSL) {
				Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
				Log.PORTLET.debug("Using SSL to autheticate");
				props.put("mail.smtp.socketFactory.port", port);
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.put("mail.smtp.socketFactory.fallback", "false");
				props.setProperty("mail.smtp.quitwait", "false");
				mailsession = Session.getDefaultInstance(props, auth);
			} else {
				mailsession = Session.getDefaultInstance(props, auth);
			}

			// Enable mail debug
			// props.put("mail.debug", "true");
			// mailsession.setDebug(true);
		} else {
			mailsession = Session.getInstance(props);
		}
		try {

			final MimeMessage msg = createMimeMessage(type, mailsession, from, toaddresses, ccaddresses, subject,
					classname, processid);
			// Create an "Alternative" Multipart message
			// Multipart message is explicitly marked as "alternative".
			// Without this string, you would see both the HTML and text emails
			// in your email browser
			final Multipart mp = new MimeMultipart("alternative");
			final BodyPart bp = new MimeBodyPart();
			bp.setContent(emailtext, "text/html; charset=UTF-8");
			mp.addBodyPart(bp);
			msg.setContent(mp);
			Transport.send(msg);
			out.print("<p>Il messaggio è stato inviato</p>");
			Log.PORTLET.debug("Email sent");
		} catch (final MessagingException ex) {
			out.print("<p>Errore nella spedizione del messaggio\n" + ex.getLocalizedMessage() + "</p>");
			Log.PORTLET.debug("Error sending email.", ex);
		} finally {
			out.flush();
			out.close();
		}
	}

	private MimeMessage createMimeMessage(final String type, final Session mailsession, final String from,
			final String to, final String cc, String subject, final String classname, final String processid) {
		try {
			// Creating email message
			final MimeMessage msg = new MimeMessage(mailsession);
			msg.setFrom(new InternetAddress(from));
			if ("support".equals(type)) {
				final String tosupport = PortletConfiguration.getInstance().getSupportEmail();
				final InternetAddress[] toaddress = { new InternetAddress(tosupport) };
				msg.setRecipients(Message.RecipientType.TO, toaddress);
			} else if ("workflow".equals(type)) {
				setRecipients(msg, to, TO);
				setRecipients(msg, cc, CC);
				final String bcc = PortletConfiguration.getInstance().getWorkflowEmail();
				final InternetAddress[] bccaddress = { new InternetAddress(bcc) };
				msg.setRecipients(Message.RecipientType.BCC, bccaddress);
				subject = "[" + classname + " " + processid + "] " + subject;
				Log.PORTLET.debug("Sending email with subject " + subject);
			} else {
				Log.PORTLET.warn("Wrong email type: " + type);
				throw EmailExceptionType.EMAIL_DESTINATION.createException();
			}

			msg.setSubject(subject);
			msg.setSentDate(new Date());
			return msg;
		} catch (final AddressException ex) {
			Log.PORTLET.warn("Error sending email: wrongly formatted address/es", ex);
			throw EmailExceptionType.ADDRESS_EXCEPTION.createException();
		} catch (final MessagingException ex) {
			Log.PORTLET.warn("Error sending email. See detailed exception", ex);
			throw EmailExceptionType.MESSAGE_EXCEPTION.createException();
		}
	}

	public void setRecipients(final MimeMessage msg, final String addresses, final String type) {
		final String[] toaddresses = addresses.split(",");
		final InternetAddress[] toaddress = new InternetAddress[toaddresses.length];
		try {
			if (!"".equals(addresses)) {
				for (int i = 0; i < toaddresses.length; i++) {
					toaddress[i] = new InternetAddress(toaddresses[i]);
				}
				if (type.equals(TO)) {
					Log.PORTLET.debug("Sending email to: " + toaddress.toString());
					msg.setRecipients(Message.RecipientType.TO, toaddress);
				} else if (type.equals(CC)) {
					Log.PORTLET.debug("Sending email to (CC): " + toaddress.toString());
					msg.setRecipients(Message.RecipientType.CC, toaddress);
				}
			}
		} catch (final AddressException ex) {
			Log.PORTLET.warn("Error sending email: wrongly formatted address/es", ex);
			throw EmailExceptionType.ADDRESS_EXCEPTION.createException();
		} catch (final MessagingException ex) {
			Log.PORTLET.warn("Error sending email. See detailed exception", ex);
			throw EmailExceptionType.MESSAGE_EXCEPTION.createException();
		}
	}

	public class SMTPAuthenticator extends javax.mail.Authenticator {

		private final String username;
		private final String password;

		public SMTPAuthenticator(final String username, final String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			Log.PORTLET.debug("Using password authenticator with following credentials");
			Log.PORTLET.debug("- username: " + username);
			Log.PORTLET.debug("- password: " + password);
			final PasswordAuthentication passwordAutentication = new PasswordAuthentication(username, password);
			return passwordAutentication;
		}
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}
}
