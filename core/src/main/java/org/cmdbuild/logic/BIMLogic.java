package org.cmdbuild.logic;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.joda.time.DateTime;

public class BIMLogic implements Logic {

	final private DataViewStore<BimProjectInfo> store;
	final private DataViewStore<BimMapperInfo> mapperInfoStore;
	final private BimService bimService;
	final private DataDefinitionLogic dataDefinitionLogic;
	final private CMDataView dataView;

	private static final String bimTablePrefix = "_bim_";

	public BIMLogic( //
			final DataViewStore<BimProjectInfo> store, //
			final DataViewStore<BimMapperInfo> mapperInfoStore, //
			final BimService bimService, //
			final DataDefinitionLogic dataDefinitionLogic, //
			final CMDataView dataView //
	) {
		this.store = store;
		this.mapperInfoStore = mapperInfoStore;
		this.bimService = bimService;
		this.dataDefinitionLogic = dataDefinitionLogic;
		this.dataView = dataView;
	}

	public List<BimProjectInfo> readBimProjectInfo() {
		return store.list();
	}

	public List<BimMapperInfo> readBimMapperInfo() {
		return mapperInfoStore.list();
	}

	public BimProjectInfo create(final BimProjectInfo projectInfo, final File fileIFC) {
		login();

		final BimProject createdProject = bimService.createProject(projectInfo.getName());
		projectInfo.setProjectId(createdProject.getIdentifier());
		store.create(projectInfo);

		doUpdate(projectInfo, fileIFC);

		logout();

		return projectInfo;
	}

	public void update(final BimProjectInfo projectInfo, final File fileIFC) {
		login();
		doUpdate(projectInfo, fileIFC);
		logout();
	}

	public void enableProject(final String projectId) {
		// update bim-server project
		login();
		bimService.enableProject(projectId);
		logout();

		// update in CMDBuild
		final BimProjectInfo fetchedProject = fetchProject(projectId);
		fetchedProject.setActive(true);
		store.update(fetchedProject);
	}

	public void disableProject(final String projectId) {
		// update bim-server project
		login();
		bimService.disableProject(projectId);
		logout();

		// update in CMDBuild
		final BimProjectInfo fetchedProject = fetchProject(projectId);
		fetchedProject.setActive(false);
		store.update(fetchedProject);
	}

	private void doUpdate(final BimProjectInfo projectInfo, final File fileIFC) {
		final BimProjectInfo fetchedProject = fetchProject(projectInfo.getIdentifier());

		// enable or disable if needed
		if (fetchedProject.isActive() != projectInfo.isActive()) {
			if (projectInfo.isActive()) {
				bimService.enableProject(projectInfo.getIdentifier());
			} else {
				bimService.disableProject(projectInfo.getIdentifier());
			}

			fetchedProject.setActive(projectInfo.isActive());
		}

		uploadIFCAndUpdateProjectInfo( //
				projectInfo.getIdentifier(), //
				fileIFC, //
				fetchedProject //
		);

		// update on CMDBuild
		store.update(fetchedProject);
	}

	private BimProjectInfo fetchProject(final String projectId) {
		return store.read(storableFromIdentifier(projectId));
	}

	private void login() {
		bimService.connect();
		bimService.login();
	}

	private void logout() {
		bimService.logout();
	}

	/*
	 * The service must be already connected and logged in
	 */
	private void uploadIFCAndUpdateProjectInfo( //
			final String projectId, //
			final File fileIFC, //
			final BimProjectInfo projectInfo) {

		// do the first check-in if there
		// is a IFC file
		if (fileIFC != null) {
			bimService.checkin(projectId, fileIFC);
			// retrieve the last check-in date to add it
			// to the info to store in CMDBuild
			final BimProject updatedProject = bimService.getProjectByPoid(projectId);
			final BimRevision lastRevision = bimService.getRevision(updatedProject.getLastRevisionId());
			projectInfo.setLastCheckin(new DateTime(lastRevision.getDate().getTime()));
		}
	}

	public void saveBimMapperInfo(String className, String attribute, String value) throws Exception {
		if (attribute.equals("active") && Boolean.parseBoolean(value)) {
			CMClass bimClass = dataView.findClass(bimTablePrefix + className);
			if (bimClass == null) {
				createBimTable(className);
			}
		}
		try {
			final BimMapperInfo mapperInfo = mapperInfoStore.read(storableFromIdentifier(className));
			MapperInfoUpdater.of(attribute).update(mapperInfo, value);
			mapperInfoStore.update(mapperInfo);
		} catch (NoSuchElementException e) {
			final BimMapperInfo _mapperInfo = new BimMapperInfo(className);
			MapperInfoUpdater.of(attribute).update(_mapperInfo, value);
			mapperInfoStore.create(_mapperInfo);
		}
	}

	private void createBimTable(String className) {
		ClassBuilder classBuilder = EntryType.newClass() //
				.withName(bimTablePrefix + className) //
				.withNamespace("bim") //
				.thatIsSystem(true);
		dataDefinitionLogic.createOrUpdate(classBuilder.build());

		AttributeBuilder attributeBuilder = Attribute.newAttribute() //
				.withName("GlobalId") //
				.withType(Attribute.AttributeTypeBuilder.STRING) //
				.withLength(22) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(bimTablePrefix + className) //
				.withOwnerNamespace("bim");
		dataDefinitionLogic.createOrUpdate(attributeBuilder.build());

		attributeBuilder = Attribute.newAttribute() //
				.withName("Master") //
				.withType(Attribute.AttributeTypeBuilder.FOREIGNKEY) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(bimTablePrefix + className) //
				.withOwnerNamespace("bim") //
				.withForeignKeyDestinationClassName(className);
		dataDefinitionLogic.createOrUpdate(attributeBuilder.build());
	}

	private static enum MapperInfoUpdater {
		active {
			@Override
			public void update(BimMapperInfo mapperInfo, String value) {
				mapperInfo.setActive(Boolean.parseBoolean(value));
			}
		}, //
		bimRoot {
			@Override
			public void update(BimMapperInfo mapperInfo, String value) {
				mapperInfo.setBimRoot(Boolean.parseBoolean(value));
			}
		}, //
		unknown {
			@Override
			public void update(BimMapperInfo mapperInfo, String value) {
				// nothing to do
			}
		}, //
		;

		public static MapperInfoUpdater of(final String attributeName) {
			for (final MapperInfoUpdater attribute : values()) {
				if (attribute.name().equals(attributeName)) {
					return attribute;
				}
			}
			logger.warn("undefined attribute '{}'", attributeName);
			return unknown;
		}

		public abstract void update(BimMapperInfo mapperInfo, String value);

	}

	private Storable storableFromIdentifier(final String identifier) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				return identifier;
			}

		};
	}
}
