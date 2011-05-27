package org.cmdbuild.tags;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;

public class Translation extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private String key=null;

	public void setKey(String key){
		this.key = key;
	}

	public String getKey(){
		return key;
	}

	public int doStartTag() {
		try {
			JspWriter out = pageContext.getOut();
			String lang = new SessionVars().getLanguage();
			out.println(TranslationService.getInstance().getTranslation(lang, key));
		} catch (IOException e) {
			Log.OTHER.debug("Error printing translation: "+ key, e);
		}

		return TagSupport.SKIP_BODY;
	}

	public int doEndTag(){
		return TagSupport.EVAL_PAGE;
	}
}
