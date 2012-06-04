package org.cmdbuild.shark.toolagent;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.MailMessageHandler;
import org.enhydra.shark.toolagent.SmtpAuthenticator;
import org.enhydra.shark.utilities.MiscUtilities;

@SuppressWarnings({"unchecked"})
public class SafeMailToolAgent implements MailMessageHandler, Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final String FROM_ADDRESS_NAME = "FromAddress";
	public static final String FROM_NAME_NAME = "FromName";
	public static final String LEGACY_FROM_ADDRESS_NAME = "FromAddresses";
	public static final String LEGACY_FROM_NAME_NAME = "FromNames";
	public static final String TO_ADDRESSES_NAME = "ToAddresses";
	public static final String CC_ADDRESSES_NAME = "CcAddresses";
	public static final String BCC_ADDRESSES_NAME = "BccAddresses";
	public static final String SUBJECT_NAME = "Subject";
	public static final String CONTENT_NAME = "Content";
	public static final String MIME_TYPE_NAME = "MimeType";
	public static final String FILE_ATTACHMENTS_NAME = "FileAttachments";
	public static final String URL_ATTACHMENTS_NAME = "UrlAttachments";
	public static final String VAR_ATTACHMENTS_NAME = "VarAttachments";
	public static final String VAR_ATTACHMENTS_MIME_TYPES_NAME = "VarAttachmentsMimeTypes";

	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	protected CallbackUtilities cus;

	protected AppParameter[] sharkParams;

	protected Map varAttachmentsVariablesMap = new HashMap();

	protected Map varAttachmentsMimeTypesMap = new HashMap();

	protected String procId;

	protected String assId;

	protected static boolean useAuthentication = false;

	protected static String SMTPMailServer;

	protected static String incomingMailServer;

	protected static int SMTPport;

	protected static int IMAPport;

	protected static int POP3port;

	protected static boolean SMTP_UseSSL;

	protected static String sourceAddress;

	protected static String login;

	protected static String password;

	protected static String incomingMailProtocol;

	protected static String storeFolderName;

	long cmdbuildCardId;

	public void configure(final CallbackUtilities cus,
			final WMSessionHandle shandle,
			final String procId,
			final String assId,
			final AppParameter[] aps) throws Exception {

		this.cus = cus;
		this.sharkParams = aps;
		this.procId = procId;
		this.assId = assId;

		final WAPI wapi = Shark.getInstance().getWAPIConnection();
		this.cmdbuildCardId = (Long)wapi.getProcessInstanceAttributeValue(shandle, procId, "ProcessId").getValue();

		if (SafeMailToolAgent.SMTPMailServer == null) {
			SafeMailToolAgent.SMTPMailServer = cus.getProperty("DefaultMailMessageHandler.SMTPMailServer");
			SafeMailToolAgent.incomingMailServer = cus.getProperty("DefaultMailMessageHandler.IncomingMailServer");
			try {
				SafeMailToolAgent.SMTPport = Integer.parseInt(cus.getProperty("DefaultMailMessageHandler.SMTPPortNo"));
				SafeMailToolAgent.IMAPport = Integer.parseInt(cus.getProperty("DefaultMailMessageHandler.IMAPPortNo"));
				SafeMailToolAgent.POP3port = Integer.parseInt(cus.getProperty("DefaultMailMessageHandler.POP3PortNo"));
				SafeMailToolAgent.SMTP_UseSSL = Boolean.valueOf(cus.getProperty("DefaultMailMessageHandler.SMTP_UseSSL"));
			} catch (final Exception ex) {
			}
			SafeMailToolAgent.sourceAddress = cus.getProperty("DefaultMailMessageHandler.SourceAddress");
			SafeMailToolAgent.login = cus.getProperty("DefaultMailMessageHandler.Login");
			SafeMailToolAgent.password = cus.getProperty("DefaultMailMessageHandler.Password");
			SafeMailToolAgent.incomingMailProtocol = cus.getProperty("DefaultMailMessageHandler.IncomingMailProtocol");
			SafeMailToolAgent.storeFolderName = cus.getProperty("DefaultMailMessageHandler.StoreFolderName");

			SafeMailToolAgent.useAuthentication = Boolean.valueOf(cus.getProperty("DefaultMailMessageHandler.useAuthentication"));
		}

		final String[] varAttachments = getVarAttachments();
		final String[] varAttachmentsMimeTypes=getVarAttachmentsMimeTypes();
		if (varAttachments != null && varAttachments.length > 0) {
			final SharkConnection sc = Shark.getInstance().getSharkConnection();
			sc.attachToHandle(shandle);
			final Map cntxt = sc.getActivity(procId,
					Shark.getInstance()
					.getAdminMisc()
					.getAssignmentActivityId(shandle, procId, assId))
					.process_context();
			for (int i = 0; i < varAttachments.length; i++) {
				final Object var = cntxt.get(varAttachments[i]);
				varAttachmentsVariablesMap.put(varAttachments[i], var);
				if (varAttachmentsMimeTypes!=null && varAttachmentsMimeTypes.length>i) {
					varAttachmentsMimeTypesMap.put(varAttachments[i], varAttachmentsMimeTypes[i]);
				} else {
					varAttachmentsMimeTypesMap.put(varAttachments[i], "text/plain");
				}
			}
		}
	}

	private InternetAddress validate(final String addr){
		return validate(addr,null);
	}
	private InternetAddress validate(final String addr,final String name){
		if(addr == null) return null;
		InternetAddress out = null;
		if(name != null){
			try{
				out = new InternetAddress(addr,name);
				out.validate();
			}catch(final Exception e){
				System.err.println("Mail address validation failed for: " + addr + " (" + name + ")");
			}
		}else{
			try{
				out = new InternetAddress(addr);
				out.validate();
			}catch(final Exception e){
				System.err.println("Mail address validation failed for: " + addr);
			}
		}
		return out;
	}

	public void sendMail() {
		if( SafeMailToolAgent.SMTPMailServer==null||SafeMailToolAgent.SMTPMailServer.equals("") ){
			System.out.println("SMTPMailServer null, mail not sent.");
			return;
		}
		new Thread(new Runnable(){
			public void run() {
				System.out.println("Asynchronous mail sending...");
				try{
					// Get system properties
					final Properties props = new Properties();

					// Setup mail server
					props.put("mail.transport.protocol", "smtp");
					props.put("mail.host", SafeMailToolAgent.SMTPMailServer);
					props.put("mail.smtp.host", SafeMailToolAgent.SMTPMailServer);


					if (SafeMailToolAgent.SMTPport>0) {
						props.put("mail.smtp.port", "" + SafeMailToolAgent.SMTPport);
						props.put("mail.smtp.socketFactory.port", SafeMailToolAgent.SMTPport);
					}
					props.put("mail.smtp.user", SafeMailToolAgent.login);
					props.put("mail.smtp.user", SafeMailToolAgent.password);
					props.put("mail.smtp.auth", SafeMailToolAgent.useAuthentication+"");

					if (SMTP_UseSSL) {
						props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
						props.put("mail.smtp.socketFactory.fallback", "false");
						props.setProperty("mail.smtp.quitwait", "false");
					}

					props.put("mail.smtp.starttls.enable",cus.getProperty("DefaultMailMessageHandler.StartTLS","false"));


					// Get session
					final javax.mail.Session session = Session.getInstance(props,
							new SmtpAuthenticator(SafeMailToolAgent.login,SafeMailToolAgent.password));

					// Define message
					final Message message = new MimeMessage(session);

					//add cmdbuild card id header
					message.addHeader("X-CMDBUILD-PROCESSID", cmdbuildCardId+"");

					final String[] fromAddresses = getFromAddresses();
					final String[] fromNames = getFromNames();
					if (fromAddresses.length > 1) {
						final List<Address> addresses = new ArrayList();
						Address tmp = null;
						for (int i = 0; i < fromAddresses.length; i++) {
							String fromName = null;
							if (fromNames != null && fromNames.length > i) {
								fromName = fromNames[i];
							}
							if (fromName != null) {
								tmp = validate(fromAddresses[i], fromName);
								if(tmp != null) addresses.add(tmp);
							} else {
								tmp = validate(fromAddresses[i]);
								if(tmp != null) addresses.add(tmp);
							}
						}
						final Address[] ar = new Address[addresses.size()];
						int idx = 0;
						for(final Address a : addresses){
							ar[idx] = a;
							idx++;
						}
						message.addFrom(ar);
					} else {
						String fromName = null;
						if (fromNames != null && fromNames.length > 0) {
							fromName = fromNames[0];
						}
						final Address from = validate(fromAddresses[0],fromName);
						if(from != null)
							message.setFrom(from);
						else{
							System.err.println("Cannot find a from address!");
							return;
						}
					}

					final String[] tos = getToAddresses();
					final String[] ccs = getCCAddresses();
					final String[] bccs = getBCCAddresses();
					if ((tos == null || tos.length == 0)
							&& (ccs == null || ccs.length == 0) && (bccs == null || bccs.length == 0)) {
						System.out.println("DefaultMailMessageHandler: at least one To, CC or BCC address must be specified! Mail not sent.");
						return;
					}
					Address tmp = null;
					if (tos != null) {
						for (int i = 0; i < tos.length; i++) {
							tmp = validate(tos[i]);
							if(tmp != null){
								message.addRecipient(Message.RecipientType.TO, tmp);
							}
						}
					}
					if (ccs != null) {
						for (int i = 0; i < ccs.length; i++) {
							tmp = validate(ccs[i]);
							if(tmp != null){
								message.addRecipient(Message.RecipientType.CC, tmp);
							}
						}
					}
					if (bccs != null) {
						for (int i = 0; i < bccs.length; i++) {
							tmp = validate(bccs[i]);
							if(tmp != null){
								message.addRecipient(Message.RecipientType.BCC, tmp);
							}
						}
					}

					final String subject = getSubject();
					if (subject != null) {
						message.setSubject(subject);
					}

					final String content = getContent();
					final String mimeType = getMimeType();

					final String[] fileAttachments = getFileAttachments();
					final String[] urlAttachments = getURLAttachments();
					final String[] varAttachments = getVarAttachments();

					if ((fileAttachments == null || fileAttachments.length == 0)
							&& (urlAttachments == null || urlAttachments.length == 0)
							&& (varAttachments == null || varAttachments.length == 0)) {
						if (mimeType == null
								|| mimeType.trim().equals("") || mimeType.equals("text/plain")) {
							message.setText(content);
						} else {
							message.setContent(content, mimeType);
						}
					} else {
						final BodyPart messageBodypart = new MimeBodyPart();
						if (mimeType == null
								|| mimeType.trim().equals("") || mimeType.equals("text/plain")) {
							messageBodypart.setText(content);
						} else {
							messageBodypart.setContent(content, mimeType);
						}

						final Multipart multipart = new MimeMultipart();
						multipart.addBodyPart(messageBodypart);

						final BodyPart[] fileBPs = getFileAttachments(fileAttachments);
						final BodyPart[] urlBPs = getURLAttachments(urlAttachments);
						final BodyPart[] varBPs = getVarAttachments(varAttachments);

						if (fileBPs != null && fileBPs.length > 0) {
							for (int i = 0; i < fileBPs.length; i++) {
								multipart.addBodyPart(fileBPs[i]);
							}
						}
						if (urlBPs != null && urlBPs.length > 0) {
							for (int i = 0; i < urlBPs.length; i++) {
								multipart.addBodyPart(urlBPs[i]);
							}
						}
						if (varBPs != null && varBPs.length > 0) {
							for (int i = 0; i < varBPs.length; i++) {
								multipart.addBodyPart(varBPs[i]);
							}
						}

						message.setContent(multipart);
					}
					message.setSentDate(new Date());

					// Send message
					Transport.send(message);
					System.out.println("...mail sent");
				}catch(final Exception e){
					// Email not sent: continue
					e.printStackTrace();
					System.out.println("Exception while sending mail, mail not sent.");
				}
			}
		}).start();
	}

	/**
	 * TODO implement receiving the mail with attachments
	 */
	public String receiveMail() throws Exception {
		// Get system properties
		final Properties props = new Properties();

		// Get session
		final javax.mail.Session session = Session.getInstance(props, null);

		// Get the store
		int imPort = SafeMailToolAgent.POP3port;
		if (!SafeMailToolAgent.incomingMailProtocol.equals("pop3")) {
			imPort = SafeMailToolAgent.IMAPport;
		}
		final Store store = session.getStore(SafeMailToolAgent.incomingMailProtocol);
		store.connect(SafeMailToolAgent.incomingMailServer,
				imPort,
				SafeMailToolAgent.login,
				SafeMailToolAgent.password);

		// Get folder
		Folder folder = null;
		Message messages[] = null;
		Message msg = null;
		folder = store.getFolder(SafeMailToolAgent.storeFolderName);
		String subject = null;
		if (folder.hasNewMessages()) {
			folder.open(Folder.READ_WRITE);
			messages = folder.getMessages();

			if (messages != null && messages.length > 0) {
				for (int i = 0; i < messages.length; i++) {
					final Flags flags = messages[i].getFlags();
					final Flags.Flag[] flagarr = flags.getSystemFlags();
					boolean valid = true;
					System.out.println("Checking flags for mail message "
							+ messages[i].getSubject());
					for (int j = 0; j < flagarr.length; j++) {
						if (flagarr[j].equals(Flags.Flag.SEEN)
								|| flagarr[j].equals(Flags.Flag.ANSWERED)
								|| flagarr[j].equals(Flags.Flag.DELETED)) {
							valid = false;
							break;
						}
					}
					if (!valid)
						continue;
					msg = messages[i];
					subject = msg.getSubject();
					// Once we have the subject we mark message as seen
					msg.setFlag(Flags.Flag.SEEN, true);
					break;
				}
			}
			// here we use handler to set parameters based on mail content
			if (msg != null) {
				this.setParamsBasedOnMailMessage(msg);
			} else {
				this.setParamsBasedOnMailMessage(null);
			}
			// Close connection
			folder.close(false);
			store.close();
		} else {
			this.setParamsBasedOnMailMessage(null);
		}

		return subject;
	}

	protected BodyPart[] getFileAttachments(final String[] locations) throws Exception {
		if (locations == null || locations.length == 0) {
			return null;
		}

		BodyPart attachmentMessageBodyPart = null;
		final BodyPart[] parts = new BodyPart[locations.length];
		DataSource source = null;
		for (int i = 0; i < locations.length; i++) {
			attachmentMessageBodyPart = new MimeBodyPart();
			final String oneAttachment = locations[i];
			source = new FileDataSource(oneAttachment);
			try {
				attachmentMessageBodyPart.setDataHandler(new DataHandler(source));
				String fname = oneAttachment;
				int indOfFS = oneAttachment.lastIndexOf("\\");
				if (indOfFS < 0) {
					indOfFS = oneAttachment.lastIndexOf("/");
				}
				if (indOfFS >= 0) {
					fname = oneAttachment.substring(indOfFS + 1);
				}
				attachmentMessageBodyPart.setFileName(fname);
			} catch (final MessagingException me) {
				this.cus.warn(null, "Unable to send file attachment [" + oneAttachment + "].");
				// if messaging exception occures
				// skip this attachment
			}
			parts[i] = attachmentMessageBodyPart;
		}
		return parts;
	}

	protected BodyPart[] getURLAttachments(final String[] locations) throws Exception {
		if (locations == null || locations.length == 0) {
			return null;
		}

		BodyPart attachmentMessageBodyPart = null;
		final BodyPart[] parts = new BodyPart[locations.length];
		DataSource source = null;
		for (int i = 0; i < locations.length; i++) {
			attachmentMessageBodyPart = new MimeBodyPart();
			final String oneAttachment = locations[i];
			System.out.println("Try attachment at location: " + locations[i]);
			source = new URLDataSource(new URL(oneAttachment));
			try {
				attachmentMessageBodyPart.setDataHandler(new DataHandler(source));
				String fname = oneAttachment;
				int indOfFS = oneAttachment.lastIndexOf("\\");
				if (indOfFS < 0) {
					indOfFS = oneAttachment.lastIndexOf("/");
				}
				if (indOfFS >= 0) {
					fname = oneAttachment.substring(indOfFS + 1);
				}
				attachmentMessageBodyPart.setFileName(fname);
			} catch (final MessagingException me) {
				this.cus.warn(null, "Unable to send URL attachment [" + oneAttachment + "].");
				// if messaging exception occures
				// skip this attachment
			}
			parts[i] = attachmentMessageBodyPart;
		}
		return parts;
	}

	protected BodyPart[] getVarAttachments(final String[] locations) throws Exception {
		if (locations == null || locations.length == 0) {
			return null;
		}
		final List parts=new ArrayList();
		final BodyPart[] bparts = new BodyPart[parts.size()];
		parts.toArray(bparts);
		return bparts;
	}

	public String[] getFromAddresses() throws Exception {
		AppParameter param = getParameterByName(FROM_ADDRESS_NAME);
		if (param == null) {
			param = getParameterByName(LEGACY_FROM_ADDRESS_NAME);
		}
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ",");
		} else {
			ret = new String[] {
					SafeMailToolAgent.sourceAddress
			};
		}
		return ret;
	}

	public String[] getFromNames() throws Exception {
		AppParameter param = getParameterByName(FROM_NAME_NAME);
		if (param == null) {
			param = getParameterByName(LEGACY_FROM_NAME_NAME);
		}
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ",");
		}
		return ret;
	}

	public String[] getToAddresses() throws Exception {
		final AppParameter param = getParameterByName(TO_ADDRESSES_NAME);
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ",");
		}
		return ret;
	}

	public String[] getCCAddresses() throws Exception {
		final AppParameter param = getParameterByName(CC_ADDRESSES_NAME);
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ",");
		}
		return ret;
	}

	public String[] getBCCAddresses() throws Exception {
		final AppParameter param = getParameterByName(BCC_ADDRESSES_NAME);
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ",");
		}
		return ret;
	}

	public String getSubject() throws Exception {
		final AppParameter param = getParameterByName(SUBJECT_NAME);
		if (param != null && param.the_value != null) {
			return param.the_value.toString();
		}
		return "";
	}

	public String getContent() throws Exception {
		final AppParameter param = getParameterByName(CONTENT_NAME);
		if (param != null && param.the_value != null) {
			return param.the_value.toString();
		}
		return "";
	}

	public String getMimeType() throws Exception {
		final AppParameter param = getParameterByName(MIME_TYPE_NAME);
		if (param != null && param.the_value != null) {
			return param.the_value.toString();
		}
		return null;
	}

	public String[] getFileAttachments() throws Exception {
		final AppParameter param = getParameterByName(FILE_ATTACHMENTS_NAME);
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ";");
		}
		return ret;
	}

	public String[] getURLAttachments() throws Exception {
		final AppParameter param = getParameterByName(URL_ATTACHMENTS_NAME);
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ";");
		}
		return ret;
	}

	public String[] getVarAttachments() throws Exception {
		final AppParameter param = getParameterByName(VAR_ATTACHMENTS_NAME);
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ";");
		}
		return ret;
	}

	public String[] getVarAttachmentsMimeTypes() throws Exception {
		final AppParameter param = getParameterByName(VAR_ATTACHMENTS_MIME_TYPES_NAME);
		String[] ret = null;
		if (param != null && param.the_value != null) {
			final String pval = param.the_value.toString();
			ret = MiscUtilities.tokenize(pval, ";");
		}
		return ret;
	}

	public void setParamsBasedOnMailMessage(final Message mmessage) throws Exception {
		if (mmessage != null) {
			AppParameter param = getParameterByName(FROM_ADDRESS_NAME);
			if (param == null) {
				param = getParameterByName(LEGACY_FROM_ADDRESS_NAME);
			}
			if (param != null) {
				final Address[] addresses = mmessage.getFrom();
				if (addresses != null) {
					String v = "";
					for (int i = 0; i < addresses.length; i++) {
						if (!v.equals("")) {
							v += ",";
						}
						v += addresses[i].toString();
					}
					param.the_value = v;
				}
			}
			final Address[] addresses = mmessage.getAllRecipients();
			if (addresses != null) {
				String tos = "";
				String ccs = "";
				String bccs = "";
				for (int i = 0; i < addresses.length; i++) {
					if (addresses[i].getType().equals(Message.RecipientType.CC)) {
						if (!ccs.equals("")) {
							ccs += ",";
						}
						ccs += addresses[i].toString();
					} else if (addresses[i].getType().equals(Message.RecipientType.BCC)) {
						if (!bccs.equals("")) {
							bccs += ",";
						}
						bccs += addresses[i].toString();
					} else {
						if (!tos.equals("")) {
							tos += ",";
						}
						tos += addresses[i].toString();
					}
				}
				if (!tos.equals("")) {
					param = getParameterByName(TO_ADDRESSES_NAME);
					param.the_value = tos;
				}
				if (!ccs.equals("")) {
					param = getParameterByName(CC_ADDRESSES_NAME);
					param.the_value = ccs;
				}
				if (!bccs.equals("")) {
					param = getParameterByName(BCC_ADDRESSES_NAME);
					param.the_value = bccs;
				}
			}
			param = getParameterByName(SUBJECT_NAME);
			if (param != null) {
				param.the_value = mmessage.getSubject();
			}
			param = getParameterByName(CONTENT_NAME);
			if (param != null) {
				final Object content = mmessage.getContent();
				if (content instanceof String) {
					param.the_value = content;
				} else if (content instanceof Multipart) {
					final Multipart mp = (Multipart) content;
					for (int i = 0; i < mp.getCount(); i++) {
						final BodyPart bp = mp.getBodyPart(i);
						if (bp.getContent() instanceof String) {
							param.the_value = bp.getContent();
							break;
						}
					}
				}
			}
			param = getParameterByName(MIME_TYPE_NAME);
			if (param != null) {
				param.the_value = mmessage.getContentType();
			}
		}
	}

	protected AppParameter getParameterByName(final String name) throws Exception {
		if (sharkParams != null) {
			for (int i = 0; i < sharkParams.length; i++) {
				if (name.equals(sharkParams[i].the_formal_name)) {
					return sharkParams[i];
				}
			}
		}
		return null;
	}
}
