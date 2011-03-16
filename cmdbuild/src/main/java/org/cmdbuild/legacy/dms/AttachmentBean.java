package org.cmdbuild.legacy.dms;

import java.util.Date;

public class AttachmentBean {
	
	private static final long serialVersionUID = 1L;

	private CMDBAlfrescoMeta metadata;
	private String name; //
	private String uuid; //
	private String description; //
	private String version; //
	private String author; //
	
	private Date created; //
	private Date modified; //
	private String category; //this must be in the defined category lookup
	
	private String path; //
	private String[] fspath; //--
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public CMDBAlfrescoMeta getMetadata() {
		return metadata;
	}
	public void setMetadata(CMDBAlfrescoMeta metadata) {
		this.metadata = metadata;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String[] getFspath() {
		return fspath;
	}
	public void setFspath(String[] fspath) {
		this.fspath = fspath;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	

/*
 * here are the things alfresco returns when searching for something
 * (at least the things i think are useful)
 * 
{http://www.alfresco.org/model/content/1.0}name : 
cmdb.20070307.backup.sql


{http://www.alfresco.org/model/system/1.0}node-dbid : 
2680

{http://www.alfresco.org/model/content/1.0}modified : 
2007-09-25T15:45:58.346+02:00

{http://www.alfresco.org/model/content/1.0}initialVersion : 
true

{http://www.alfresco.org/model/content/1.0}description : 
cmdb.20070307.backup.sql

{http://www.alfresco.org/model/system/1.0}node-uuid : 
a02cf3a3-6b6d-11dc-8bb4-c5afb2bb3313

{http://www.alfresco.org/model/content/1.0}autoVersion : 
true

{http://www.alfresco.org/model/system/1.0}store-protocol : 
workspace

{http://www.alfresco.org/model/content/1.0}modifier : 
admin

{http://www.alfresco.org/model/content/1.0}title : 
cmdb.20070307.backup.sql

{http://www.alfresco.org/model/content/1.0}content : 
contentUrl=store://2007/9/25/15/45/a03cd226-6b6d-11dc-8bb4-c5afb2bb3313.bin|mimetype=text/plain|size=15364335|encoding=UTF-8|locale=en_AU_

{http://www.alfresco.org/model/system/1.0}store-identifier : 
SpacesStore

{http://www.alfresco.org/model/content/1.0}created : 
2007-09-25T15:45:49.800+02:00

{http://www.alfresco.org/model/content/1.0}versionLabel : 
1.0

{http://www.alfresco.org/model/content/1.0}creator : 
admin

{http://www.alfresco.org/model/content/1.0}path : 
/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}user_homes/{http://www.alfresco.org/model/content/1.0}CMDBuild/{http://www.alfresco.org/model/content/1.0}test/{http://www.alfresco.org/model/content/1.0}asset/{http://www.alfresco.org/model/content/1.0}aClassName/{http://www.alfresco.org/model/content/1.0}cmdb.20070307.backup.sql


 */
}
