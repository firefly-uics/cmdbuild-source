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
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguages;
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
	public void definitionsCannotBeRubbish() throws XpdlException, CMWorkflowException {
		try {
			ws.uploadPackage(pkgId, new byte[0]);
			fail();
		} catch (final CMWorkflowException we) {
			assertThat(we.getCause().getMessage(), containsString("The package byte[] representation can't be parsed"));
		}
	}

	@Test
	public void definitionsMustHaveDefaultScriptingLanguage() throws XpdlException, CMWorkflowException {
		final XpdlDocument xpdl = new XpdlDocument(pkgId);
		try {
			ws.uploadPackage(pkgId, XpdlPackageFactory.xpdlByteArray(xpdl.getPkg()));
			fail();
		} catch (final CMWorkflowException we) {
			assertThat(we.getCause(), instanceOf(PackageInvalid.class));
			PackageInvalid sharkException = (PackageInvalid) we.getCause();
			assertThat(sharkException.getMessage(), containsString("Error in package"));
			assertThat(sharkException.getXPDLValidationErrors(), containsString("Unsupported script language"));
		}
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		ws.uploadPackage(pkgId, XpdlPackageFactory.xpdlByteArray(xpdl.getPkg()));
	}

	@Test
	public void packageVersionIncreasesWithEveryUpload() throws CMWorkflowException {
		final XpdlDocument xpdl = new XpdlDocument(pkgId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		final byte[] xpdlFile = XpdlPackageFactory.xpdlByteArray(xpdl.getPkg());

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
		final XpdlDocument xpdl = new XpdlDocument(pkgId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		Package pkg = xpdl.getPkg();

		pkg.setName("n1");
		ws.uploadPackage(pkgId, XpdlPackageFactory.xpdlByteArray(pkg));

		pkg.setName("n2");
		ws.uploadPackage(pkgId, XpdlPackageFactory.xpdlByteArray(pkg));

		pkg.setName("n3");
		ws.uploadPackage(pkgId, XpdlPackageFactory.xpdlByteArray(pkg));

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(pkgId, "1"));
		assertThat(pkg.getName(), is("n1"));

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(pkgId, "3"));
		assertThat(pkg.getName(), is("n3"));
	}

	@Test
	public void xpdl1PackagesAreNotConvertedToXpdl2() throws CMWorkflowException {
		final XpdlDocument xpdl = new XpdlDocument(pkgId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		Package pkg = xpdl.getPkg();

		pkg.getPackageHeader().setXPDLVersion("1.0");
		ws.uploadPackage(pkgId, XpdlPackageFactory.xpdlByteArray(pkg));

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(pkgId, "1"));
		assertThat(pkg.getPackageHeader().getXPDLVersion(), is("1.0"));
	}

	@Test
	public void canDownloadAllPackages() throws CMWorkflowException {
		String ID1 = UUID.randomUUID().toString();
		String ID2 = UUID.randomUUID().toString();
		int initialSize = ws.downloadAllPackages().length;

		ws.uploadPackage(ID1, createXpdl(ID1));

		assertThat(ws.downloadAllPackages().length, is(initialSize+1));

		ws.uploadPackage(ID2, createXpdl(ID2));
		ws.uploadPackage(ID2, createXpdl(ID2));

		assertThat(ws.downloadAllPackages().length, is(initialSize+2));
	}

	private byte[] createXpdl(final String packageId) throws XpdlException {
		XpdlDocument xpdl = new XpdlDocument(packageId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		return XpdlPackageFactory.xpdlByteArray(xpdl.getPkg());
	}
}
