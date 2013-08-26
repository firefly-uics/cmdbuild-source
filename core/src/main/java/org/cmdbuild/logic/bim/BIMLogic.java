package org.cmdbuild.logic.bim;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.cmdbuild.services.bim.BimDataModelUpdater;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.joda.time.DateTime;

public class BIMLogic implements Logic {

	final private Store<BimProjectInfo> store;
	final private Store<BimMapperInfo> mapperInfoStore;
	final private BimService bimService;
	final private DataDefinitionLogic dataDefinitionLogic;
	final private CMDataView dataView;

	private final BimServiceFacade bimServiceFacade;
	private final BimDataPersistence bimDataPersistence;

	private static final String bimSchemaName = "bim";
	private static final String defaultBimTablePrefix = "_bim";
	private static final String bimProjectTableName = "_BimProject";

	public BIMLogic( //
			final Store<BimProjectInfo> store, //
			final Store<BimMapperInfo> mapperInfoStore, //
			final BimService bimService, //
			final DataDefinitionLogic dataDefinitionLogic, //
			final CMDataView dataView, //
			final BimServiceFacade bimServiceFacade, //
			final BimDataPersistence bimDataPersistence //
	) {
		this.store = store;
		this.mapperInfoStore = mapperInfoStore;
		this.bimService = bimService;
		this.dataDefinitionLogic = dataDefinitionLogic;
		this.dataView = dataView;
		this.bimServiceFacade = bimServiceFacade;
		this.bimDataPersistence = bimDataPersistence;
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
			CMClass bimClass = dataView.findClass(defaultBimTablePrefix + className);
			if (bimClass == null) {
				createBimTable(className);
			}
		} else if (attribute.equals("bimRoot")) {
			BimMapperInfo oldBimRoot = findBimRoot();
			if (Boolean.parseBoolean(value)) {
				if (oldBimRoot != null && !oldBimRoot.getClassName().equals(className)) {
					deleteBimDomain(oldBimRoot.getClassName());

					oldBimRoot.setBimRoot(false);
					mapperInfoStore.update(oldBimRoot);

					createBimDomain(className);
				} else if (oldBimRoot == null) {
					CMDomain domain = dataView.findDomain(className + "BimProject");
					if (domain == null) {
						createBimDomain(className);
					} else {
						disableBimDomain(className);
					}
				}
			} else {
				if (oldBimRoot != null && oldBimRoot.isBimRoot()) {
					disableBimDomain(className);
				}
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

	public BimMapperInfo findBimRoot() {
		List<BimMapperInfo> bimMapperInfoList = readBimMapperInfo();
		for (BimMapperInfo mapperInfo : bimMapperInfoList) {
			if (mapperInfo.isBimRoot()) {
				return mapperInfo;
			}
		}
		return null;
	}

	private void createBimDomain(String className) {
		CMClass clazz = dataView.findClass(className);
		CMClass projectClass = dataView.findClass(bimProjectTableName);
		DomainBuilder domainBuilder = Domain.newDomain() //
				.withName(className + "BimProject") //
				.withIdClass1(clazz.getId()) //
				.withIdClass2(projectClass.getId()) //
				.withCardinality("N:1");

		Domain domain = domainBuilder.build();

		dataDefinitionLogic.create(domain);
	}

	private void enableBimDomain(String className) {
		CMClass clazz = dataView.findClass(className);
		CMClass projectClass = dataView.findClass(bimProjectTableName);
		DomainBuilder domainBuilder = Domain.newDomain() //
				.withName(className + "BimProject") //
				.withIdClass1(clazz.getId()) //
				.withIdClass2(projectClass.getId()) //
				.thatIsActive(true);
		Domain domain = domainBuilder.build();

		dataDefinitionLogic.update(domain);
	}

	private void disableBimDomain(String className) {
		CMClass clazz = dataView.findClass(className);
		CMClass projectClass = dataView.findClass(bimProjectTableName);
		DomainBuilder domainBuilder = Domain.newDomain() //
				.withName(className + "BimProject") //
				.withIdClass1(clazz.getId()) //
				.withIdClass2(projectClass.getId()) //
				.thatIsActive(false);
		Domain domain = domainBuilder.build();

		dataDefinitionLogic.update(domain);
	}

	private void deleteBimDomain(String className) {
		dataDefinitionLogic.deleteDomainByName(className + "BimProject");
	}

	private void createBimTable(String className) {
		ClassBuilder classBuilder = EntryType.newClass() //
				.withName(defaultBimTablePrefix + className) //
				.withNamespace(bimSchemaName) //
				.thatIsSystem(true);
		dataDefinitionLogic.createOrUpdate(classBuilder.build());

		AttributeBuilder attributeBuilder = Attribute.newAttribute() //
				.withName("GlobalId") //
				.withType(Attribute.AttributeTypeBuilder.STRING) //
				.withLength(22) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(defaultBimTablePrefix + className) //
				.withOwnerNamespace(bimSchemaName);
		dataDefinitionLogic.createOrUpdate(attributeBuilder.build());

		attributeBuilder = Attribute.newAttribute() //
				.withName("Master") //
				.withType(Attribute.AttributeTypeBuilder.FOREIGNKEY) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(defaultBimTablePrefix + className) //
				.withOwnerNamespace(bimSchemaName) //
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
	
	//CRUD operations on BimProjectInfo

	public void create2(BimProjectInfo projectInfo, final File ifcFile) {
		String identifier = bimServiceFacade.create(projectInfo.getName());
		projectInfo.setProjectId(identifier);
		bimDataPersistence.store(projectInfo);
		
		DateTime timestamp = bimServiceFacade.upload(projectInfo,ifcFile);
		projectInfo.setLastCheckin(timestamp);
		bimDataPersistence.store(projectInfo);
	}
	
	public List<BimProjectInfo> readBimProjectInfo2(){
		return bimDataPersistence.readBimProjectInfo();
	}

	public void disableProject2(final String projectId) {
		bimServiceFacade.disableProject(projectId);
		bimDataPersistence.disableProject(projectId);
	}
	
	public void enableProject2(final String projectId) {
		bimServiceFacade.enableProject(projectId);
		bimDataPersistence.enableProject(projectId);
	}
	
	public void update2(BimProjectInfo projectInfo, final File ifcFile) {
		bimServiceFacade.update(projectInfo);
		if(ifcFile != null){
			DateTime timestamp = bimServiceFacade.upload(projectInfo,ifcFile);
			projectInfo.setLastCheckin(timestamp);
		}
		bimDataPersistence.store(projectInfo);
	}
	
	
	//CRUD operations on BimMapperInfo
	
	public List<BimMapperInfo> readBimMapperInfo2(){
		return bimDataPersistence.readBimMapperInfo();
	}
	
	
	//FIXME 
	
	public void saveBimMapperInfo2(String className, String attribute, String value){
		BimDataModelUpdater bimDataModelUpdater = new BimDataModelUpdater(dataView, dataDefinitionLogic);
		if(attribute.equals("active")){
			bimDataModelUpdater.handleBimTable(className,value);
		}else if(attribute.equals("bimRoot")){
			bimDataModelUpdater.handleBimDomain(className,value);
		}
		try {
			final BimMapperInfo mapperInfo = mapperInfoStore.read(storableFromIdentifier(className));
			MapperInfoUpdater.of(attribute).update(mapperInfo, value);
			bimDataPersistence.store(mapperInfo);
		} catch (NoSuchElementException e) {
			final BimMapperInfo _mapperInfo = new BimMapperInfo(className);
			MapperInfoUpdater.of(attribute).update(_mapperInfo, value);
			bimDataPersistence.create(_mapperInfo);
		}
	}
	
	
	
}
