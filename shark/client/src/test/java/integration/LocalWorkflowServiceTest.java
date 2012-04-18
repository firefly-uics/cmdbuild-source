package integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.LocalSharkService;
import org.cmdbuild.workflow.xpdl.XPDLDocument;
import org.cmdbuild.workflow.xpdl.XPDLException;
import org.cmdbuild.workflow.xpdl.XPDLPackageFactory;
import org.cmdbuild.workflow.xpdl.XPDLDocument.ScriptLanguages;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.shark.api.client.wfservice.PackageInvalid;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalWorkflowServiceTest {

	private static String USERNAME = "admin";

	private static CMWorkflowService ws;
	private String pkgId;

	@BeforeClass
	public static void initWorkflowService() {
		ws = new LocalSharkService(new LocalSharkService.Config() {
			public String getUsername() {
				return USERNAME;
			}
		});
	}

	@Before
	public void createRandomPackageName() {
		pkgId = UUID.randomUUID().toString();
	}

	@Test
	public void definitionsCannotBeRubbish() throws XPDLException, CMWorkflowException {
		try {
			ws.uploadPackage(pkgId, new byte[0]);
			fail();
		} catch (final CMWorkflowException we) {
			assertThat(we.getCause().getMessage(), containsString("The package byte[] representation can't be parsed"));
		}
	}

	@Test
	public void definitionsMustHaveDefaultScriptingLanguage() throws XPDLException, CMWorkflowException {
		final XPDLDocument xpdl = new XPDLDocument(pkgId);
		try {
			ws.uploadPackage(pkgId, XPDLPackageFactory.xpdlByteArray(xpdl.getPkg()));
			fail();
		} catch (final CMWorkflowException we) {
			assertThat(we.getCause(), instanceOf(PackageInvalid.class));
			PackageInvalid sharkException = (PackageInvalid) we.getCause();
			assertThat(sharkException.getMessage(), containsString("Error in package"));
			assertThat(sharkException.getXPDLValidationErrors(), containsString("Unsupported script language"));
		}
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		ws.uploadPackage(pkgId, XPDLPackageFactory.xpdlByteArray(xpdl.getPkg()));
	}

	@Test
	public void packageVersionIncreasesWithEveryUpload() throws CMWorkflowException {
		final XPDLDocument xpdl = new XPDLDocument(pkgId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		final byte[] xpdlFile = XPDLPackageFactory.xpdlByteArray(xpdl.getPkg());

		String[] versions = ws.getPackageVersions(pkgId);
		assertEquals(0, versions.length);

		ws.uploadPackage(pkgId, xpdlFile);

		versions = ws.getPackageVersions(pkgId);
		assertThat(versions, is(new String[] {"1"}));

		ws.uploadPackage(pkgId, xpdlFile);
		ws.uploadPackage(pkgId, xpdlFile);
		versions = ws.getPackageVersions(pkgId);
		assertThat(versions, is(new String[] {"1","2","3"}));
	}

	@Test
	public void anyPackageVersionCanBeDownloaded() throws CMWorkflowException {
		final XPDLDocument xpdl = new XPDLDocument(pkgId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		Package pkg = xpdl.getPkg();

		pkg.setName("n1");
		ws.uploadPackage(pkgId, XPDLPackageFactory.xpdlByteArray(pkg));

		pkg.setName("n2");
		ws.uploadPackage(pkgId, XPDLPackageFactory.xpdlByteArray(pkg));

		pkg.setName("n3");
		ws.uploadPackage(pkgId, XPDLPackageFactory.xpdlByteArray(pkg));

		pkg = XPDLPackageFactory.readXpdl(ws.downloadPackage(pkgId, "1"));
		assertThat(pkg.getName(), is("n1"));

		pkg = XPDLPackageFactory.readXpdl(ws.downloadPackage(pkgId, "3"));
		assertThat(pkg.getName(), is("n3"));
	}

	@Test
	public void xpdl1PackagesAreNotConvertedToXpdl2() throws CMWorkflowException {
		final XPDLDocument xpdl = new XPDLDocument(pkgId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		Package pkg = xpdl.getPkg();

		pkg.getPackageHeader().setXPDLVersion("1.0");
		ws.uploadPackage(pkgId, XPDLPackageFactory.xpdlByteArray(pkg));

		pkg = XPDLPackageFactory.readXpdl(ws.downloadPackage(pkgId, "1"));
		assertThat(pkg.getPackageHeader().getXPDLVersion(), is("1.0"));
	}

}
