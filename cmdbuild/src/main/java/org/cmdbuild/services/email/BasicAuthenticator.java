package org.cmdbuild.services.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.cmdbuild.config.EmailProperties;

public class BasicAuthenticator extends Authenticator {

    protected PasswordAuthentication getPasswordAuthentication() {
        EmailProperties props = EmailProperties.getInstance();
        return new PasswordAuthentication(props.getEmailUsername(), props.getEmailPassword());
    }
}
