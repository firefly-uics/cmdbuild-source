package org.cmdbuild.logger;

import org.apache.log4j.Logger;

public final class Log {
     private Log() {}
     public static final Logger ROOT = Logger.getRootLogger();
     public static final Logger PERSISTENCE = Logger.getLogger("persist");
     public static final Logger SQL = Logger.getLogger("sql");
     public static final Logger WORKFLOW = Logger.getLogger("workflow");
     public static final Logger JSONRPC = Logger.getLogger("jsonrpc");
     public static final Logger SOAP = Logger.getLogger("soap");
     public static final Logger DMS = Logger.getLogger("dms");
     public static final Logger REST = Logger.getLogger("rest");
     public static final Logger REPORT = Logger.getLogger("report");
     public static final Logger EMAIL = Logger.getLogger("email");
     public static final Logger AUTH = Logger.getLogger("auth");
     public static final Logger OTHER = Logger.getLogger("cmdbuild");

     static {
    	 // Note: this is not reloading automatically!
    	 Logger.getLogger("org.springframework.jdbc.core").setLevel(SQL.getEffectiveLevel());
     }
}
