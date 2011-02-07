package org.cmdbuild.portlet.configuration;

import java.io.File;

public class AttachmentConfiguration {

	private File file;
	private String category;
	private String description;
	private String classname;
	private String filename;
	private int cardid;

	public AttachmentConfiguration() {
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public File getFile() {
		return file;
	}

	public void setFile(final File file) {
		this.file = file;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	// public String getFilename() {
	// return file.getName();
	// }

	public String getClassname() {
		return classname;
	}

	public void setClassname(final String classname) {
		this.classname = classname;
	}

	public int getCardid() {
		return cardid;
	}

	public void setCardid(final int cardid) {
		this.cardid = cardid;
	}

}
