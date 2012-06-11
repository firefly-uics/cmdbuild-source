package integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.shark.api.common.SharkConstants;
import org.junit.Before;
import org.junit.Test;

import utils.AbstractRemoteWorkflowServiceTest;

/**
 * Smoke tests to be reasonably sure that the web connection works just like the
 * local one. This is not tested throughly because we assume that it is going to
 * work just like the embedded Shark instance.
 */
public class RemoteWorkflowServiceTest extends AbstractRemoteWorkflowServiceTest {

	private String pkgId;

	@Before
	public void createRandomPackageName() {
		pkgId = randomName();
	}

	@Test
	public void packagesCanBeUploadedAndDownloaded() throws CMWorkflowException {
		assertEquals(0, ws.getPackageVersions(pkgId).length);

		Package pkg = new Package();
		pkg.setId(pkgId);
		pkg.getScript().setType(SharkConstants.GRAMMAR_JAVA);
		pkg.getPackageHeader().setXPDLVersion("2.1");
		pkg.setName("n1");
		ws.uploadPackage(pkgId, XpdlPackageFactory.xpdlByteArray(pkg));

		pkg.getPackageHeader().setXPDLVersion("1.0");
		pkg.setName("n2");
		ws.uploadPackage(pkgId, XpdlPackageFactory.xpdlByteArray(pkg));

		assertEquals(2, ws.getPackageVersions(pkgId).length);

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(pkgId, "1"));
		assertThat(pkg.getName(), is("n1"));
	}

}
