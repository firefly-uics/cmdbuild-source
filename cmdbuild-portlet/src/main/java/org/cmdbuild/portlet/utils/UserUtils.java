package org.cmdbuild.portlet.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.metadata.User;


public class UserUtils {

    public static final String CONNECTED_USER = "cmdbmeta.userid";
    public static final String USER_GROUP = "connectedCMDBuildUserGroup";
    public static final String EMAIL = "useremail";

    public User getUser(HttpServletRequest request){
        User user = new User();
        HttpSession session = request.getSession();
        String connectedGroup = (String) session.getAttribute(USER_GROUP);
        if (connectedGroup != null) {
            user.setGroup(connectedGroup);
        }
        String userField = (String) session.getAttribute(CONNECTED_USER);
        if (userField != null) {
            user.setName(userField);
        }
        user.setEmail(StringUtils.defaultIfEmpty((String) session.getAttribute(EMAIL), ""));
        return user;
    }

}
