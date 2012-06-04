package org.cmdbuild.shark.util;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

public class ClientPasswordCallback implements CallbackHandler  {

	private String password;
	private String username;
	
	public ClientPasswordCallback() {}
	
	public ClientPasswordCallback(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
		if (username.equals(pc.getIdentifier())) {
            pc.setPassword(password);
        } 
	}

}
