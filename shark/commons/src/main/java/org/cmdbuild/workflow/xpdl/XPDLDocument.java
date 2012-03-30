package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.jxpdl.elements.DataField;
import org.enhydra.jxpdl.elements.DataTypes;
import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.jxpdl.elements.Participant;
import org.enhydra.jxpdl.elements.TypeDeclaration;
import org.enhydra.jxpdl.elements.TypeDeclarations;
import org.enhydra.jxpdl.elements.WorkflowProcess;
import org.enhydra.shark.api.common.SharkConstants;

/**
 * It makes easier the handling of the XPDL DOM
 */
public class XPDLDocument {

	public enum ScriptLanguages {
		JAVA(SharkConstants.GRAMMAR_JAVA),
		JAVASCRIPT(SharkConstants.GRAMMAR_JAVA_SCRIPT),
		PYTHON(SharkConstants.GRAMMAR_PYTHON_SCRIPT),
		GROOVY("text/groovy");

		private final String mimeType;

		private ScriptLanguages(final String mimeType) {
			this.mimeType = mimeType;
		}
	}

	public enum StandardAndCustomTypes {
		BOOLEAN {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeBOOLEAN();
			}
		},
		DATETIME {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeDATETIME();
			}
		},
		FLOAT {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeFLOAT();
			}
		},
		INTEGER {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeINTEGER();
			}
		},
		STRING {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeSTRING();
			}
		},
		/*
		 * For backward compatibility
		 */
		REFERENCE("Reference", ReferenceType.class.getName()) {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.setDeclaredType();
				dataTypes.getDeclaredType().setId(getDeclaredTypeId());
			}
		},
		LOOKUP("Lookup", LookupType.class.getName()) {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.setDeclaredType();
				dataTypes.getDeclaredType().setId(getDeclaredTypeId());
			}
		};

		private final String declaredTypeId;
		private final String declaredTypeLocation;

		private StandardAndCustomTypes() {
			this.declaredTypeId = null;
			this.declaredTypeLocation = null;
		}

		private StandardAndCustomTypes(final String declaredTypeId, final String declaredTypeLocation) {
			this.declaredTypeId = declaredTypeId;
			this.declaredTypeLocation = declaredTypeLocation;
		}

		public boolean isCustom() {
			return (declaredTypeId != null);
		}

		public String getDeclaredTypeId() {
			return declaredTypeId;
		}

		public String getDeclaredTypeLocation() {
			return declaredTypeLocation;
		}

		public void setTypeToField(final DataField df) {
			selectDataType(df.getDataType().getDataTypes());
		}

		abstract protected void selectDataType(DataTypes dataTypes);
	}

	/*
	 * Wish it was not a magic constant in txm!
	 * 
	 * http://en.wikipedia.org/wiki/Magic_number_(programming)#
	 * Unnamed_numerical_constants
	 */
	private static final String DEFAULT_XPDL_VERSION = "2.1";

	public static final String ARRAY_DECLARED_TYPE_NAME_SUFFIX = "s";
	public static final String ARRAY_DECLARED_TYPE_LOCATION_SUFFIX = "<>";

	private final Package pkg;

	public XPDLDocument(final String pkgId) {
		this(new Package());
		pkg.setId(pkgId);
		pkg.getPackageHeader().setXPDLVersion(DEFAULT_XPDL_VERSION);
	}

	public XPDLDocument(final Package pkg) {
		this.pkg = pkg;
	}

	public Package getPkg() {
		return pkg;
	}

	public void addProcess(final String wpId) {
		WorkflowProcess wp = (WorkflowProcess) pkg.getWorkflowProcesses().generateNewElement();
		wp.setId(wpId);
		pkg.getWorkflowProcesses().add(wp);
	}

	public void addPackageField(final String dfId, final StandardAndCustomTypes type) {
		DataField df = createDataField(dfId, type);
		pkg.getDataFields().add(df);
	}

	public void addProcessField(final String wpId, final String dfId, final StandardAndCustomTypes type) {
		DataField df = createDataField(dfId, type);
		pkg.getWorkflowProcess(wpId).getDataFields().add(df);
	}

	private DataField createDataField(final String dfId, final StandardAndCustomTypes type) {
		DataField df = (DataField) pkg.getDataFields().generateNewElement();
		df.setId(dfId);
		type.setTypeToField(df);
		return df;
	}

	public void createCustomTypeDeclarations() {
		TypeDeclarations types = pkg.getTypeDeclarations();
		for (StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			if (t.isCustom()) {
				addExternalReferenceType(types, t);
				addExternalReferenceArrayType(types, t);
			}
		}
	}

	public void setDefaultScriptingLanguage(final ScriptLanguages lang) {
		pkg.getScript().setType(lang.mimeType);
	}

	public void addProcessExtendedAttribute(final String wpId, final String key, final String value) {
		ExtendedAttributes xattrs = pkg.getWorkflowProcess(wpId).getExtendedAttributes();
		ExtendedAttribute xa = (ExtendedAttribute) xattrs.generateNewElement();
		xa.setName(key);
		xa.setVValue(value);
		xattrs.add(xa);
	}

	public void addRoleParticipant(final String participantId) {
		Participant p = (Participant) pkg.getParticipants().generateNewElement();
		p.setId(participantId);
		p.getParticipantType().setTypeROLE(); // Default but better safe than sorry
		pkg.getParticipants().add(p);
	}

	public void addSystemParticipant(final String participantId) {
		Participant p = (Participant) pkg.getParticipants().generateNewElement();
		p.setId(participantId);
		p.getParticipantType().setTypeSYSTEM();
		pkg.getParticipants().add(p);
	}

	private void addExternalReferenceType(TypeDeclarations types, StandardAndCustomTypes t) {
		addExternalReferenceType(types, t.getDeclaredTypeId(), t.getDeclaredTypeLocation());
	}

	/*
	 * For backward compatibility
	 */
	private void addExternalReferenceArrayType(TypeDeclarations types, StandardAndCustomTypes t) {
		addExternalReferenceType(types, t.getDeclaredTypeId()+ARRAY_DECLARED_TYPE_NAME_SUFFIX, t.getDeclaredTypeLocation()+ARRAY_DECLARED_TYPE_LOCATION_SUFFIX);
	}

	private void addExternalReferenceType(final TypeDeclarations types, final String id, final String location) {
		TypeDeclaration type = (TypeDeclaration) types.generateNewElement();
		type.setId(id);
		type.getDataTypes().getExternalReference().setLocation(location);
		type.getDataTypes().setExternalReference();
		types.add(type);
	}

}
