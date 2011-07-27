package integration.geoserver;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.cmdbuild.config.GisProperties;
import org.cmdbuild.services.gis.geoserver.GeoServerLayer;
import org.cmdbuild.services.gis.geoserver.GeoServerService;
import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreDataType;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GeoServerTest {

	private GeoServerService service = new GeoServerService();

	private static final String LAYER_1 = "Layer1";
	private static final String LAYER_1_FEATURE = "empty";
	private static final int LAYER_1_MINZOOM = 3;
	private static final int LAYER_1_MAXZOOM = 10;
	private static final int LAYER_1_POSITION = 0;
	private static final String LAYER_1_DESCRIPTION = "d";
	private static final String LAYER_2 = "Layer2";
//	private static final String LAYER_3 = "Layer3";

	private static final String RESOURCE_PATH = "integration/geoserver/";

	@BeforeClass
	public static void setupGeoServerConfiuration() {
		GisProperties.getInstance().setProperty(GisProperties.GEOSERVER_URL, "http://localhost:8585/geoserver");
		GisProperties.getInstance().setProperty(GisProperties.GEOSERVER_WORKSPACE, "cmdbuild");
		GisProperties.getInstance().setProperty(GisProperties.GEOSERVER_ADMIN_USER, "admin");
		GisProperties.getInstance().setProperty(GisProperties.GEOSERVER_ADMIN_PASSWORD, "geoserver");
	}

	@After
	public void cleanWorkspace() {
		for (GeoServerStore s : service.getStores()) {
			service.deleteStore(s.getName());
		}
	}

	@Test
	public void aLayerIsAddedWithEveryDataStore() throws FileNotFoundException {
		assertTrue(service.getStores().isEmpty());
		assertTrue(service.getLayers().isEmpty());

		service.createStore(LAYER_1, StoreDataType.SHAPE.toString(), shapeInputStream(),
				LAYER_1_MINZOOM, LAYER_1_MAXZOOM, LAYER_1_POSITION, LAYER_1_DESCRIPTION);

		List<GeoServerStore> dataStores = service.getStores();
		assertThat(dataStores.size(), is(1));
		assertThat(dataStores.get(0).getName(), is(LAYER_1));
		List<GeoServerLayer> layers = service.getLayers();
		assertThat(layers.size(), is(1));
		assertThat(layers.get(0).getName(), is(LAYER_1_FEATURE));
		assertThat(layers.get(0).getDescription(), is(LAYER_1_DESCRIPTION));
		assertThat(layers.get(0).getStoreName(), is(LAYER_1));
		assertThat(layers.get(0).getMinZoom(), is(LAYER_1_MINZOOM));
		assertThat(layers.get(0).getMaxZoom(), is(LAYER_1_MAXZOOM));
		assertThat(layers.get(0).getIndex(), is(LAYER_1_POSITION));

		service.createStore(LAYER_2, StoreDataType.WORLDIMAGE.toString(), worldImageInputStream(), 0, 0, 0, "");

		assertThat(service.getStores().size(), is(2));
		assertThat(service.getLayers().size(), is(2));
// Disabled because it's not a complete GeoTiff
//		service.createStore(new GeoStore(LAYER_3, DataStoreType.GEOTIFF), geoTiffInputStream());
//		assertThat(service.getStores().size(), is(3));
//		assertThat(service.getLayers().size(), is(3));
	}

//	private InputStream geoTiffInputStream() {
//		return Thread.currentThread().getContextClassLoader()
//			.getResourceAsStream(RESOURCE_PATH+"geotiff.tif");
//	}

	private InputStream shapeInputStream() {
		return Thread.currentThread().getContextClassLoader()
			.getResourceAsStream(RESOURCE_PATH+"shape.zip");
	}

	private InputStream worldImageInputStream() {
		return Thread.currentThread().getContextClassLoader()
			.getResourceAsStream(RESOURCE_PATH+"worldimage.zip");
	}
}
