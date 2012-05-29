package integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.shark.api.client.wfservice.PackageInvalid;
import org.junit.Test;

public class XpdlHandlingTest extends LocalWorkflowServiceTest {

	@Test
	public void definitionsCannotBeRubbish() throws XpdlException, CMWorkflowException {
		try {
			ws.uploadPackage(packageId, new byte[0]);
			fail();
		} catch (final CMWorkflowException we) {
			assertThat(we.getCause().getMessage(), containsString("The package byte[] representation can't be parsed"));
		}
	}

	@Test
	public void definitionsMustHaveDefaultScriptingLanguage() throws XpdlException, CMWorkflowException {
		final XpdlDocument xpdl = new XpdlDocument(packageId);
		try {
			ws.uploadPackage(packageId, serialize(xpdl));
			fail();
		} catch (final CMWorkflowException we) {
			assertThat(we.getCause(), instanceOf(PackageInvalid.class));
			final PackageInvalid sharkException = (PackageInvalid) we.getCause();
			assertThat(sharkException.getMessage(), containsString("Error in package"));
			assertThat(sharkException.getXPDLValidationErrors(), containsString("Unsupported script language"));
		}
		xpdl.setDefaultScriptingLanguage(ScriptLanguage.JAVA);
		ws.uploadPackage(packageId, serialize(xpdl));
	}

	@Test
	public void packageVersionIncreasesWithEveryUpload() throws CMWorkflowException {
		final XpdlDocument xpdl = newXpdl(packageId);
		final byte[] xpdlFile = serialize(xpdl);

		String[] versions = ws.getPackageVersions(packageId);
		assertEquals(0, versions.length);

		ws.uploadPackage(packageId, xpdlFile);

		versions = ws.getPackageVersions(packageId);
		assertThat(versions, is(new String[] { "1" }));

		ws.uploadPackage(packageId, xpdlFile);
		ws.uploadPackage(packageId, xpdlFile);
		versions = ws.getPackageVersions(packageId);
		assertThat(versions, is(new String[] { "1", "2", "3" }));
	}

	@Test
	public void anyPackageVersionCanBeDownloaded() throws CMWorkflowException {
		final XpdlDocument xpdl = newXpdl(packageId);
		Package pkg = xpdl.getPkg();

		pkg.setName("n1");
		ws.uploadPackage(packageId, serialize(xpdl));

		pkg.setName("n2");
		ws.uploadPackage(packageId, serialize(xpdl));

		pkg.setName("n3");
		ws.uploadPackage(packageId, serialize(xpdl));

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(packageId, "1"));
		assertThat(pkg.getName(), is("n1"));

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(packageId, "3"));
		assertThat(pkg.getName(), is("n3"));
	}

	@Test
	public void xpdl1PackagesAreNotConvertedToXpdl2() throws CMWorkflowException {
		final XpdlDocument xpdl = newXpdl(packageId);
		Package pkg = xpdl.getPkg();

		pkg.getPackageHeader().setXPDLVersion("1.0");
		ws.uploadPackage(packageId, serialize(xpdl));

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(packageId, "1"));
		assertThat(pkg.getPackageHeader().getXPDLVersion(), is("1.0"));
	}

	@Test
	public void canDownloadAllPackages() throws CMWorkflowException {
		final String ID1 = randomName();
		final String ID2 = randomName();
		final int initialSize = ws.downloadAllPackages().length;

		ws.uploadPackage(ID1, serialize(newXpdl(ID1)));

		assertThat(ws.downloadAllPackages().length, is(initialSize + 1));

		ws.uploadPackage(ID2, serialize(newXpdl(ID2)));
		ws.uploadPackage(ID2, serialize(newXpdl(ID2)));

		assertThat(ws.downloadAllPackages().length, is(initialSize + 2));
	}

}
