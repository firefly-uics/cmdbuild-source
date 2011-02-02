package org.cmdbuild.portlet.utils;

import java.util.List;
import org.cmdbuild.portlet.metadata.User;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.portlet.operation.WSOperation;
import org.cmdbuild.portlet.plugin.CMPortletPlugin;
import org.cmdbuild.portlet.plugin.CMPortletPluginLibrary;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Metadata;

public class FieldUtils {

    public String checkString(String s) {
        if (s == null) {
            return "";
        }
        s = replace(s, "&", "&#38");
        s = replace(s, "\'", "&#39;");
        s = replace(s, "\"", "&#34;");
        s = replace(s, "<", "&#60");
        s = replace(s, ">", "&#62");
        return s;
    }

    private String replace(String s, String one, String another) {
        if (s.equals("")) {
            return "";
        }
        String res = "";
        int i = s.indexOf(one, 0);
        int lastpos = 0;
        while (i != -1) {
            res += s.substring(lastpos, i) + another;
            lastpos = i + one.length();
            i = s.indexOf(one, lastpos);
        }
        res += s.substring(lastpos);
        return res;
    }

    public String setMandatoryField(AttributeSchema schema) {
        if (isRequired(schema)) {
            return schema.getDescription() + " * ";
        } else {
            return schema.getDescription();
        }
    }

    public boolean isRequired(AttributeSchema schema) {
        if (schema != null) {
            return (schema.isNotnull() || "required".equalsIgnoreCase(schema.getVisibility()));
        } else {
            return false;
        }
    }

     public boolean checkIsEditable(Card card, String type) {
        boolean editable = true;
        List<Metadata> meta = card.getMetadata();
        if ("process".equals(type) || "advance".equals(type)) {
            if (checkIsEditabileByCurrentUser(meta)) {
                editable = true;
            } else {
                editable = false;
            }
        } else if ("card".equals(type)){
            editable = true;
        }
        return editable;
    }

    public boolean checkIsEditabileByCurrentUser(List<Metadata> metadata) {
        boolean editable = false;
        if (metadata != null){
            for (Metadata meta : metadata){
                if (meta.getKey().equals("runtime.privileges")){
                    editable = isWritable(meta.getValue());
                }
            }
        }
        return editable;
    }

    public boolean checkVisibility(String visibility) {
        if ("update".equalsIgnoreCase(visibility) || "required".equalsIgnoreCase(visibility)){
            return true;
        } else {
            return false;
        }
    }

    public boolean isWritable(String privilege) {
        if ("write".equalsIgnoreCase(privilege)){
            return true;
        } else {
            return false;
        }
    }

    public User getUserInformation(SOAPClient client, String useremail) {
        WSOperation operation = new WSOperation(client);
        User user = operation.getUser(useremail);
        return user;
    }

    public String getCustomJS(String contextPath) {
        String javascript="";
        for (CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
            String[] customjs = plugin.getCustomjs();
            if (customjs != null) {
                for (String js : customjs) {
                    javascript = javascript +
                            String.format("<script src=\"%s/js/%s.js\" type=\"text/javascript\" ></script>\n", contextPath, js);
                }
            }
        }
        return javascript;
    }

    public String getCustomCSS(String contextPath) {
        String javascript="";
        for (CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
            String[] customCSS = plugin.getCustomcss();
            if (customCSS != null) {
                for (String css : customCSS) {
                    javascript = javascript +
                            String.format("<link href=\"%s/css/%s.css\" rel=\"stylesheet\" type=\"text/css\" />", contextPath, css);
                }
            }
        }
        return javascript;
    }

}
