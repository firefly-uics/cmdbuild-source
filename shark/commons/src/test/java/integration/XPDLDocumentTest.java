package integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.cmdbuild.workflow.xpdl.XPDLDocument;
import org.cmdbuild.workflow.xpdl.XPDLPackageFactory;
import org.cmdbuild.workflow.xpdl.XPDLDocument.ScriptLanguages;
import org.cmdbuild.workflow.xpdl.XPDLDocument.StandardAndCustomTypes;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.BasicType;
import org.enhydra.jxpdl.elements.DataType;
import org.enhydra.jxpdl.elements.DataTypes;
import org.enhydra.jxpdl.elements.DeclaredType;
import org.enhydra.jxpdl.elements.ExternalReference;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.jxpdl.elements.Participant;
import org.enhydra.jxpdl.elements.TypeDeclaration;
import org.enhydra.jxpdl.elements.WorkflowProcess;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class XPDLDocumentTest {

	private static final String TEST_WP_ID = "myWP";
	private static final String TEST_PKG_ID = "myPkg";
	private static final String TEST_ROLE_PARTICIPANT_ID = "myRole";
	private static final String TEST_SYSTEM_PARTICIPANT_ID = "mySystemParticipant";
	private static final String TEST_XA_KEY = "myXAk";
	private static final String TEST_XA_VALUE = "myXAv";

	@Rule
	public TestRule testWatcher = new WriteXpdlOnFailure();

	private XPDLDocument doc; 

	@Test
	public void createdPackageHasNameAndXpdlVersion() {
		doc = new XPDLDocument(TEST_PKG_ID);
		Package pkg = doc.getPkg();

		assertThat(pkg.getId(), is(TEST_PKG_ID));
		assertThat(doc.getPackageId(), is(TEST_PKG_ID));
		assertThat(pkg.getPackageHeader().getXPDLVersion(), is("2.1"));

		assertThat(pkg.getName(), is(""));
		assertThat(pkg.getApplications().size(), is(0));
		assertThat(pkg.getParticipants().size(), is(0));
		assertThat(pkg.getPools().size(), is(0));
		assertThat(pkg.getWorkflowProcesses().size(), is(0));
	}

	@Test
	public void itDoesNotAlterThePassedPackage() {
		Package emptyPkg = new Package();
		Package passedPkg = new Package();

		assertThat(emptyPkg, is(equalTo(passedPkg)));
		assertThat(emptyPkg, is(not(sameInstance(passedPkg))));

		doc = new XPDLDocument(passedPkg);

		assertThat(emptyPkg, is(equalTo(passedPkg)));
		assertThat(doc.getPkg(), is(sameInstance(passedPkg)));
	}

	@Test
	public void processIsCreatedWithTheIdProvided() {
		doc = new XPDLDocument(TEST_PKG_ID);
		Package pkg = doc.getPkg();

		assertThat(pkg.getWorkflowProcesses().size(), is(0));
		assertThat(pkg.getWorkflowProcess(TEST_WP_ID), is(nullValue()));

		doc.addProcess(TEST_WP_ID);

		assertThat(pkg.getWorkflowProcesses().size(), is(1));
		assertThat(pkg.getWorkflowProcess(TEST_WP_ID).getId(), is(TEST_WP_ID));
	}

	@Test
	public void customTypesCanBeAdded() {
		doc = new XPDLDocument(TEST_PKG_ID);
		doc.createCustomTypeDeclarations();
		Package pkg = doc.getPkg();

		for (StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			if (t.isCustom()) {
				TypeDeclaration tdec = pkg.getTypeDeclarations().getTypeDeclaration(t.getDeclaredTypeId());
				assertThat(tdec.getDataTypes().getChoosen(), is(instanceOf(ExternalReference.class)));
				assertThat(tdec.getDataTypes().getExternalReference().getLocation(), is(t.getDeclaredTypeLocation()));

				tdec = pkg.getTypeDeclarations().getTypeDeclaration(t.getDeclaredTypeId()+XPDLDocument.ARRAY_DECLARED_TYPE_NAME_SUFFIX);
				assertThat(tdec.getDataTypes().getChoosen(), is(instanceOf(ExternalReference.class)));
				assertThat(tdec.getDataTypes().getExternalReference().getLocation(), is(t.getDeclaredTypeLocation()+XPDLDocument.ARRAY_DECLARED_TYPE_LOCATION_SUFFIX));
			}
		}
	}

	@Test
	public void defaultScriptLanguageCanBeAdded() {
		doc = new XPDLDocument(TEST_PKG_ID);
		doc.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		Package pkg = doc.getPkg();

		assertThat(pkg.getScript().getType(), is("text/java"));
	}

	@Test
	public void fieldsCanBeAddedToThePackage() {
		doc = new XPDLDocument(TEST_PKG_ID);
		Package pkg = doc.getPkg();

		assertThat(pkg.getDataFields().size(), is(0));

		for (StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			doc.addPackageField(t.name(), t);			
		}

		assertThat(pkg.getDataFields().size(), is(StandardAndCustomTypes.values().length));
		for (StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			final DataType dt = pkg.getDataField(t.name()).getDataType();
			assertMatchesType(dt, t);
		}
	}

	@Test
	public void fieldsCanBeAddedToAProcess() {
		doc = new XPDLDocument(TEST_PKG_ID);
		doc.addProcess(TEST_WP_ID);
		WorkflowProcess wp = doc.getPkg().getWorkflowProcess(TEST_WP_ID);

		assertThat(wp.getDataFields().size(), is(0));

		for (StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			doc.addProcessField(TEST_WP_ID, t.name(), t);			
		}

		assertThat(wp.getDataFields().size(), is(StandardAndCustomTypes.values().length));
		for (StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			final DataType dt = wp.getDataField(t.name()).getDataType();
			assertMatchesType(dt, t);
		}
		assertThat(doc.getPkg().getDataFields().size(), is(0));
	}

	@Test
	public void participantsCanBeAddedToThePackage() {
		doc = new XPDLDocument(TEST_PKG_ID);
		Package pkg = doc.getPkg();

		assertThat(pkg.getParticipants().size(), is(0));

		doc.addRoleParticipant(TEST_ROLE_PARTICIPANT_ID);
		doc.addSystemParticipant(TEST_SYSTEM_PARTICIPANT_ID);

		assertThat(pkg.getParticipants().size(), is(2));
		Participant p = pkg.getParticipant(TEST_ROLE_PARTICIPANT_ID);
		assertThat(p.getId(), is(TEST_ROLE_PARTICIPANT_ID));
		assertThat(p.getParticipantType().getType(), is(XPDLConstants.PARTICIPANT_TYPE_ROLE));

		p = pkg.getParticipant(TEST_SYSTEM_PARTICIPANT_ID);
		assertThat(p.getId(), is(TEST_SYSTEM_PARTICIPANT_ID));
		assertThat(p.getParticipantType().getType(), is(XPDLConstants.PARTICIPANT_TYPE_SYSTEM));
	}

	@Test
	public void extendedAttributesCanBeAddedToAProcess() {
		doc = new XPDLDocument(TEST_PKG_ID);
		doc.addProcess(TEST_WP_ID);
		WorkflowProcess wp = doc.getPkg().getWorkflowProcess(TEST_WP_ID);

		assertThat(wp.getExtendedAttributes().size(), is(0));
		assertThat(doc.getProcessExtendedAttribute(TEST_WP_ID, TEST_XA_KEY), is(nullValue()));

		doc.addProcessExtendedAttribute(TEST_WP_ID, TEST_XA_KEY, TEST_XA_VALUE);

		assertThat(wp.getExtendedAttributes().size(), is(1));
		assertThat(wp.getExtendedAttributes().getFirstExtendedAttributeForName(TEST_XA_KEY).getVValue(), is(TEST_XA_VALUE));
		assertThat(doc.getProcessExtendedAttribute(TEST_WP_ID, TEST_XA_KEY), is(TEST_XA_VALUE));
	}

	/*
	 * Utils 
	 */

	private void assertMatchesType(final DataType dt, StandardAndCustomTypes t) {
		final DataTypes dataTypes = dt.getDataTypes();
		if (t.isCustom()) {
			assertThat(dataTypes.getChoosen(), is(instanceOf(DeclaredType.class)));
			assertThat(dataTypes.getDeclaredType().getId(), is(t.getDeclaredTypeId()));
		} else {
			assertThat(dataTypes.getChoosen(), is(instanceOf(BasicType.class)));
			assertThat(dataTypes.getBasicType().getType(), is(t.name()));
		}
	}

	/*
	 * Test diagnostics
	 */

	private class WriteXpdlOnFailure extends TestWatcher {
		final String tmpDir = System.getProperty("java.io.tmpdir");

		protected void succeeded(final Description description) {
			if (description.getAnnotation(WriteXpdl.class) != null) {
				writeXpdl(description);
			}
		}

		protected void failed(final Throwable e, final Description description) {
			writeXpdl(description);
		}

		private void writeXpdl(final Description description) {
			if (doc != null) {
				try {
					final String fileName = getFileName(description);
					System.err.println("Saving XPDL from test to " + fileName);
					final FileOutputStream fos = new FileOutputStream(fileName);
					XPDLPackageFactory.writeXpdl(doc.getPkg(), fos);
				} catch (Throwable t) {
					System.err.println("Cannot save XPDL: " + t.getMessage());
				}
			}
		}

		private String getFileName(final Description description) {
			final String testName = String.format("%s.%s", description.getClassName(), description.getMethodName());
			return String.format("%s%s%s.xpdl", tmpDir, File.separator, testName.replace(".", "_"));
		}
	}

	/**
	 * Annotate tests with this annotation to print the XPDL on success as well.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	private @interface WriteXpdl {
	}

}
