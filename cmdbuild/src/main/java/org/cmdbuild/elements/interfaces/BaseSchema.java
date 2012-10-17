package org.cmdbuild.elements.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.meta.MetadataMap;

public interface BaseSchema extends CMPrivilegedObject {
	public enum CMTableType {
	 	CLASS("class"),
		SIMPLECLASS("simpleclass"),
		DOMAIN("domain");
	 	
	 	private List<AttributeType> classAttributes;
	 	
	 	@SuppressWarnings("serial")
		CMTableType(final String type) {
	 		classAttributes = new ArrayList<AttributeType>() {{
	 			add(AttributeType.BOOLEAN);
	 			add(AttributeType.INTEGER);
	 			add(AttributeType.DECIMAL);
	 			add(AttributeType.DOUBLE);
	 			add(AttributeType.DATE);
	 			add(AttributeType.TIMESTAMP);
	 			add(AttributeType.CHAR);
	 			add(AttributeType.STRING);
	 			add(AttributeType.TEXT);
	 			add(AttributeType.LOOKUP);
	 			add(AttributeType.INET);
	 			add(AttributeType.TIME);
	 			add(AttributeType.REGCLASS);
	 			add(AttributeType.BINARY);
	 			add(AttributeType.INTARRAY);
	 			add(AttributeType.STRINGARRAY);
		 		if ("class".equals(type)) {
	 				add(AttributeType.REFERENCE);
		 		} else if ("simpleclass".equals(type)) {
		 			add(AttributeType.FOREIGNKEY);
		 			add(AttributeType.POINT);
		 			add(AttributeType.LINESTRING);
		 			add(AttributeType.POLYGON);
		 		}
	 		}};
	 	}
	 	
	 	public List<AttributeType> getAvaiableAttributeList() {
	 		return this.classAttributes;
	 	}
	 	
	 	public static CMTableType fromMetaValue(String metaValue) {
	 		return CMTableType.valueOf(metaValue.toUpperCase());
	 	}

	 	public String toMetaValue() {
	 		return name().toLowerCase();
	 	}
	}

	public enum Mode {
		WRITE("write", true, true, true, false), 
		READ("read", true, true, false, false),
		BASECLASS("baseclass", false, true, false, true),
		SYSREAD("sysread", false, true, true, true),
		RESERVED("reserved", false, false, true, false),
		NOACTIVE("noactive", false, false, false, false);

		private final String modeString;
		private final boolean custom;
		private final boolean displayable;
		private final boolean cardsAllowed;
		private final boolean readEveryone;

		Mode(String mode, boolean custom, boolean displayable, boolean cardsAllowed, boolean readEveryone) { 
			this.modeString = mode;
			this.custom = custom;
			this.displayable = displayable;
			this.cardsAllowed = cardsAllowed;
			this.readEveryone = readEveryone;
		}

	    public String getModeString() { 
	    	return modeString; 
	    }

		public boolean isCustom() {
			return custom;
		}

		public boolean alwaysReadPrivileges() {
			return this.readEveryone;
		}

		public boolean isMetaModifiable() {
			return custom;
		}

		public boolean isDisplayable() {
			return displayable;
		}

		public boolean cardsAllowed() {
			return cardsAllowed;
		}

	    public static Mode getValueOf(String modeString) {
	    	Mode[] modes = Mode.values();
	    	try {
	    		for (Mode m : modes) {
	    			if (m.getModeString().equals(modeString)) {
	    				return m;
	    			}
	    		}
	    		// Fallback to case insensitive match
	    		for (Mode m : modes) {
	    			if (m.getModeString().equalsIgnoreCase(modeString)) {
	    				Log.PERSISTENCE.warn(String.format("Wrong case for mode %s", modeString));
	    				return m;
	    			}
	    		}
	    		Log.PERSISTENCE.error(String.format("Mode %s cannot be found: using %s", modeString, Mode.WRITE.getModeString()));
	    	} catch (IllegalArgumentException e) {
	    		Log.PERSISTENCE.error(String.format("Mode %s cannot be parsed: using %s", modeString, Mode.WRITE.getModeString()), e);
	    	}
	    	return Mode.WRITE;
	    }
	}
	
	public enum SchemaStatus {
		ACTIVE("active"), NOTACTIVE("noactive");
	
		private final String value;
	
		SchemaStatus(String value) {
			this.value = value;
		}

		public String commentString() {
			return value;
		}

		public static SchemaStatus fromBoolean(boolean active) {
			if (active)
				return SchemaStatus.ACTIVE;
			else
				return SchemaStatus.NOTACTIVE;
		}
		public static SchemaStatus fromStatusString(String statusString) {
			for (SchemaStatus status : SchemaStatus.values()) {
				if (status.value.equals(statusString))
					return status;
			}
			return SchemaStatus.ACTIVE; // TODO for backward compatibility
		}
		public boolean isActive() {
			return ACTIVE.equals(this);
		}
	}

	public void setName(String name);
	public String getName();

	public void setTableType(CMTableType type);
	public CMTableType getTableType();
	
	public void setMode(String mode);
	public Mode getMode();

	public void setStatus(SchemaStatus status);
	public SchemaStatus getStatus();

	public int getId();

	public void addAttribute(IAttribute attribute);
	public Map<String, IAttribute> getAttributes();
	public IAttribute getAttribute(String name);

	/*
	 * For classes is the class name, but for domains it contains the domain prefix
	 */
	public String getDBName();

	public String toString();

	void readDataDefinitionMeta(Map<String, String> dataDefinitionMeta);
	Map<String, String> genDataDefinitionMeta();

	// Modified metadata are NOT saved
	public MetadataMap getMetadata();
}
