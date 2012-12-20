package org.cmdbuild.dao.backend.postgresql;

public class SchemaQueries {

	public enum TableQueries {
		CREATE("{ ?= call cm_create_class(?,?,?) }"),
		MODIFY("{ call cm_modify_class(?,?) }"),
		DELETE("{ call cm_delete_class(?) }"),

		FIND_ALL("SELECT * FROM system_classcatalog"),
		LOAD_TREE("SELECT parentid, childid FROM system_treecatalog");

		private final String query;
		TableQueries(String query) { this.query = query; }
	    public String toString() { return query; }
	}
	
	public enum DomainQueries {
		CREATE("{ ?= call cm_create_domain(?,?) }"),
		MODIFY("{ call cm_modify_domain(?,?) }"),
		DELETE("{ call cm_delete_domain(?) }"),

		FIND_ALL("SELECT * FROM system_domaincatalog"),
		FIND_ALL_BY_TABLE("SELECT * FROM system_domaincatalog WHERE domainclass1=? OR domainclass2=?"),
		FIND_ALL_INHERITED_BY_TABLE("SELECT * FROM system_domaincatalog WHERE (domainclass1 = ANY (string_to_array(?, ','))) OR (domainclass2 = ANY (string_to_array(?, ',')))");

		private final String query;
		DomainQueries(String query) { this.query = query; }
	    public String toString() { return query; }
	}
	
	public enum AttributeQueries {
		CREATE("{ call cm_create_attribute(?,?,?,?,?,?,?) }"),
		MODIFY("{ call cm_modify_attribute(?,?,?,?,?,?,?) }"),
		DELETE("{ call cm_delete_attribute(?,?) }"),

		FIND_ALL_BY_TABLE("SELECT * FROM system_attributecatalog WHERE classid=? ORDER BY attributeindex, dbindex");

		private final String query;
		AttributeQueries(String query) { this.query = query; }
	    public String toString() { return query; }
	}

	public enum LookupQueries {
		LOAD_TREE_TYPES("SELECT \"Type\", MAX(\"ParentType\") AS \"ParentType\" FROM \"LookUp\" GROUP BY \"Type\";"),
		CREATE_LOOKUPTYPE("INSERT INTO \"LookUp\" (\"IdClass\",\"Type\",\"Number\",\"Status\",\"ParentType\",\"IsDefault\") VALUES ('\"LookUp\"'::regclass,?,1,'A',?,FALSE)"),
		MODIFY_LOOKUPTYPE("UPDATE \"LookUp\" SET \"Type\"=? WHERE \"Type\"=?; UPDATE \"LookUp\" SET \"ParentType\"=? WHERE \"ParentType\"=?"),
		DELETE_LOOKUPTYPE("UPDATE \"LookUp\" SET \"Status\"='N' WHERE \"Type\"=?;");

		private final String query;
		LookupQueries(String query) { this.query = query; }
	    public String toString() { return query; }
	}

}
