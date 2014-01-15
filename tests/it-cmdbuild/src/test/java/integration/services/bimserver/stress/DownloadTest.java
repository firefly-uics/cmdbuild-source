package integration.services.bimserver.stress;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.bimserver.client.BimServerClient;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.bimserver.BimserverClientHolder;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration;
import org.cmdbuild.bim.service.bimserver.BimserverService;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class DownloadTest {
	private BimService service;
	private BimServerClient client;
	private final String url = "http://localhost:8082";
	private final String username = "admin@tecnoteca.com";
	private final String password = "admin";

	@Before
	public void setUp() {
		BimserverConfiguration configuration = new BimserverConfiguration() {

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getUsername() {
				return username;
			}

			@Override
			public String getUrl() {
				return url;
			}

			@Override
			public String getPassword() {
				return password;
			}

			@Override
			public void addListener(ChangeListener listener) {
			}

			@Override
			public void disable() {
				// TODO Auto-generated method stub
			}
		};
		service = new BimserverService(new BimserverClientHolder(configuration));
		System.out.println("Connection established\n");
		client = ((BimserverService) service).getClient();
	}

	@Test
	public void ping() throws Exception {

	}

	@Test
	public void download() throws Exception {
		List<BimProject> projectList = service.getAllProjects();
		if (projectList.size() > 0) {
			for (int i = 0; i < 1; i++) {
				String revisionId = projectList.get(0).getLastRevisionId();
				Set<Long> roids = Sets.newHashSet();

				roids.add(new Long(revisionId));
				Set<String> classNames = Sets.newHashSet();
				classNames.add("IfcBuilding");
				classNames.add("IfcColumn");
				classNames.add("IfcDoor");
				classNames.add("IfcRoof");
				classNames.add("IfcSlab");
				classNames.add("IfcStair");
				classNames.add("IfcWallStandardCase");
				classNames.add("IfcWindow");
				classNames.add("IfcBuildingElementProxy");
				classNames.add("IfcWall");
				classNames.add("IfcBeam");
				classNames.add("IfcRailing");
				classNames.add("IfcProxy");
				
				Long serializerOid = client.getPluginInterface()
						.getSerializerByPluginClassName("org.bimserver.geometry.json.JsonGeometrySerializerPlugin")
						.getOid();
				
				Long actionId = client.getBimsie1ServiceInterface().downloadByTypes(roids, classNames, serializerOid,
						false, false, false, true);

				String token = client.getToken();
				System.out.println("token: " + token);

				String url = "http://localhost:8082/download?token=" + token + "&longActionId=" + actionId + "&zip=off"
						+ "&serializerOid=" + serializerOid;
				System.out.println(url);

				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");

				int responseCode = con.getResponseCode();
				System.out.println("\nSending 'GET' request to URL : " + url);
				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);

				}
				in.close();
				// print result
				System.out.println("length " + response.length());

				//client.getBimsie1ServiceInterface().getDownloadData(actionId);

				//
				
				System.out.println(i + " download completed");
			}
		}
	}

}
