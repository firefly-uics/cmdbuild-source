package org.cmdbuild.dao.backend.postgresql;



public class ReportQueryBuilder {
	
	public enum ReportQueries {
		FIND_TYPES("select distinct \"Type\" from \"Report\" where \"Status\"='A';"),
		FIND_INITIAL_CLASSES("SELECT DISTINCT classid,classname,classcomment,classidcategory,classcategory,classdescription,classmode,issuperclass,classmanager,classstatus FROM cmdbclasscatalog WHERE classmode<>'reserved' order by classname;");

		private final String query;
		ReportQueries(String query) { this.query = query; }
	    public String toString() { return query; }
	}
	
}
