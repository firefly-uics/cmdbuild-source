package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.common.utils.guava.Functions.build;
import static org.cmdbuild.scheduler.command.Commands.composeOnExeption;
import static org.cmdbuild.services.email.Predicates.named;

import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.common.java.sql.DataSourceHelper;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.email.StorableEmailAccount;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.SendTemplateEmail;
import org.cmdbuild.logic.taskmanager.ConnectorTask.ClassMapping;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SourceConfigurationVisitor;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.PredicateEmailAccountSupplier;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.Entry;
import org.cmdbuild.services.sync.store.ForwardingStore;
import org.cmdbuild.services.sync.store.SimpleAttribute;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.StoreSynchronizer;
import org.cmdbuild.services.sync.store.Type;
import org.cmdbuild.services.sync.store.internal.AttributeValueAdapter;
import org.cmdbuild.services.sync.store.internal.BuildableCatalog;
import org.cmdbuild.services.sync.store.internal.Catalog;
import org.cmdbuild.services.sync.store.internal.InternalStore;
import org.cmdbuild.services.sync.store.sql.BuildableAttributeMapping;
import org.cmdbuild.services.sync.store.sql.BuildableTableOrViewMapping;
import org.cmdbuild.services.sync.store.sql.BuildableTypeMapper;
import org.cmdbuild.services.sync.store.sql.SqlStore;
import org.cmdbuild.services.sync.store.sql.TableOrViewMapping;
import org.cmdbuild.services.sync.store.sql.TypeMapping;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

public class ConnectorTaskJobFactory extends AbstractJobFactory<ConnectorTask> {

	private static class PermissionBasedStore extends ForwardingStore {

		public static interface Permission {

			boolean allowsCreate(Entry<? extends Type> entry);

			boolean allowsUpdate(Entry<? extends Type> entry);

			boolean allowsDelete(Entry<? extends Type> entry);

		}

		private final Permission permission;

		public PermissionBasedStore(final Store delegate, final Permission permission) {
			super(delegate);
			this.permission = permission;
		}

		@Override
		public void create(final Entry<? extends Type> entry) {
			if (permission.allowsCreate(entry)) {
				super.create(entry);
			}
		}

		@Override
		public void update(final Entry<? extends Type> entry) {
			if (permission.allowsUpdate(entry)) {
				super.update(entry);
			}
		}

		@Override
		public void delete(final Entry<? extends Type> entry) {
			if (permission.allowsDelete(entry)) {
				super.delete(entry);
			}
		}

	}

	private static class ConnectorTaskPermission implements PermissionBasedStore.Permission {

		private static final Function<ClassMapping, String> TARGET_NAME = new Function<ClassMapping, String>() {

			@Override
			public String apply(final ClassMapping input) {
				return input.getTargetType();
			};

		};

		private static final ClassMapping ALWAYS_TRUE = ClassMapping.newInstance() //
				.withCreateStatus(true) //
				.withUpdateStatus(true) //
				.withDeleteStatus(true) //
				.build();

		private final Map<String, ClassMapping> classMappingByTypeName;

		public ConnectorTaskPermission(final ConnectorTask task) {
			classMappingByTypeName = uniqueIndex(task.getClassMappings(), TARGET_NAME);
		}

		@Override
		public boolean allowsCreate(final Entry<? extends Type> entry) {
			return mappingOf(entry).isCreate();
		}

		@Override
		public boolean allowsUpdate(final Entry<? extends Type> entry) {
			return mappingOf(entry).isUpdate();
		}

		@Override
		public boolean allowsDelete(final Entry<? extends Type> entry) {
			return mappingOf(entry).isDelete();
		}

		private ClassMapping mappingOf(final Entry<? extends Type> entry) {
			return defaultIfNull(classMappingByTypeName.get(entry.getType().getName()), ALWAYS_TRUE);
		}

	}

	private static class ConnectorTaskCommandWrapper implements Command {

		private static final Function<Builder<? extends ClassType>, ClassType> BUILD_CLASS_TYPE = build();
		private static final Function<Builder<? extends TypeMapping>, TypeMapping> BUILD_TYPE_MAPPING = build();

		private final CMDataView dataView;
		private final DataSourceHelper dataSourceHelper;
		private final AttributeValueAdapter attributeValueAdapter;
		private final ConnectorTask task;

		private ConnectorTaskCommandWrapper(final CMDataView dataView, final DataSourceHelper dataSourceHelper,
				final AttributeValueAdapter attributeValueAdapter, final ConnectorTask task) {
			this.dataView = dataView;
			this.dataSourceHelper = dataSourceHelper;
			this.attributeValueAdapter = attributeValueAdapter;
			this.task = task;
		}

		@Override
		public void execute() {
			final Catalog catalog = catalog();
			final Store left = left(catalog);
			final Store rightAndTarget = InternalStore.newInstance() //
					.withDataView(dataView) //
					.withCatalog(catalog) //
					.withAttributeValueAdapter(attributeValueAdapter) //
					.build();
			StoreSynchronizer.newInstance() //
					.withLeft(left) //
					.withRight(rightAndTarget) //
					.withTarget(wrap(rightAndTarget)) //
					.build() //
					.sync();
		}

		private Catalog catalog() {
			final Map<String, ClassType.Builder> typeBuildersByName = newHashMap();
			for (final ConnectorTask.AttributeMapping attributeMapping : task.getAttributeMappings()) {
				final String typeName = attributeMapping.getTargetType();
				ClassType.Builder typeBuilder = typeBuildersByName.get(typeName);
				if (typeBuilder == null) {
					typeBuilder = ClassType.newInstance().withName(typeName);
					typeBuildersByName.put(typeName, typeBuilder);
				}
				typeBuilder.withAttribute(SimpleAttribute.newInstance() //
						.withName(attributeMapping.getTargetAttribute()) //
						.withKeyStatus(attributeMapping.isKey()) //
						.build());
			}
			final Iterable<ClassType> types = transformValues(typeBuildersByName, BUILD_CLASS_TYPE).values();
			final Catalog catalog = BuildableCatalog.newInstance() //
					.withTypes(types) //
					.build();
			return catalog;
		}

		private Store left(final Catalog catalog) {
			return new SourceConfigurationVisitor() {

				private Store store;

				public Store store() {
					task.getSourceConfiguration().accept(this);
					Validate.notNull(store, "conversion error");
					return store;
				}

				@Override
				public void visit(final SqlSourceConfiguration sourceConfiguration) {
					final DataSource dataSource = dataSourceHelper.create(sourceConfiguration);
					final Map<String, Map<String, BuildableTypeMapper.Builder>> allTypeMapperBuildersByTableOrViewName = newHashMap();
					for (final ConnectorTask.AttributeMapping attributeMapping : task.getAttributeMappings()) {
						final String tableOrViewName = attributeMapping.getSourceType();
						final String typeName = attributeMapping.getTargetType();
						Map<String, BuildableTypeMapper.Builder> typeMapperBuildersByTypeName = allTypeMapperBuildersByTableOrViewName
								.get(tableOrViewName);
						if (typeMapperBuildersByTypeName == null) {
							typeMapperBuildersByTypeName = newHashMap();
							allTypeMapperBuildersByTableOrViewName.put(tableOrViewName, typeMapperBuildersByTypeName);
						}
						BuildableTypeMapper.Builder typeMapperBuilder = typeMapperBuildersByTypeName.get(typeName);
						if (typeMapperBuilder == null) {
							final ClassType type = catalog.getType(typeName, ClassType.class);
							typeMapperBuilder = BuildableTypeMapper.newInstance().withType(type);
							typeMapperBuildersByTypeName.put(typeName, typeMapperBuilder);
						}
						typeMapperBuilder.withAttributeMapper(BuildableAttributeMapping.newInstance() //
								.withFrom(attributeMapping.getSourceAttribute()) //
								.withTo(attributeMapping.getTargetAttribute()) //
								.build());
					}
					final Collection<TableOrViewMapping> tableOrViewMappings = newHashSet();
					for (final Map.Entry<String, Map<String, BuildableTypeMapper.Builder>> entry : allTypeMapperBuildersByTableOrViewName
							.entrySet()) {
						final String tableOrViewName = entry.getKey();
						final Map<String, TypeMapping> typeMappingBuildersByTypeName = transformValues(
								entry.getValue(), BUILD_TYPE_MAPPING);
						final TableOrViewMapping tableOrViewMapping = BuildableTableOrViewMapping.newInstance() //
								.withName(tableOrViewName) //
								.withTypeMappings(typeMappingBuildersByTypeName.values()) //
								.build();
						tableOrViewMappings.add(tableOrViewMapping);
					}

					store = SqlStore.newInstance() //
							.withDataSource(dataSource) //
							.withTableOrViewMappings(tableOrViewMappings) //
							.build();
				}

			}.store();
		}

		private Store wrap(final Store store) {
			return new PermissionBasedStore(store, new ConnectorTaskPermission(task));
		}

	}

	private static class SendEmailTemplateCommandWrapper implements Command {

		private final SendTemplateEmail sendEmailTemplate;

		public SendEmailTemplateCommandWrapper(
				final org.cmdbuild.data.store.Store<StorableEmailAccount> emailAccountStore,
				final EmailServiceFactory emailServiceFactory, final EmailTemplateLogic emailTemplateLogic,
				final ConnectorTask task) {
			final Supplier<EmailAccount> emailAccountSupplier = PredicateEmailAccountSupplier.of(emailAccountStore,
					named(task.getNotificationAccount()));
			final Supplier<Template> emailTemplateSupplier = new Supplier<Template>() {

				@Override
				public Template get() {
					final String name = defaultIfBlank(task.getNotificationErrorTemplate(), EMPTY);
					return emailTemplateLogic.read(name);
				}

			};
			sendEmailTemplate = SendTemplateEmail.newInstance() //
					.withEmailAccountSupplier(emailAccountSupplier) //
					.withEmailServiceFactory(emailServiceFactory) //
					.withEmailTemplateSupplier(emailTemplateSupplier) //
					.build();
		}

		@Override
		public void execute() {
			sendEmailTemplate.execute();
		}

	}

	private final CMDataView dataView;
	private final DataSourceHelper jdbcService;
	private final AttributeValueAdapter attributeValueAdapter;
	private final org.cmdbuild.data.store.Store<StorableEmailAccount> emailAccountStore;
	private final EmailServiceFactory emailServiceFactory;
	private final EmailTemplateLogic emailTemplateLogic;

	public ConnectorTaskJobFactory(final CMDataView dataView, final DataSourceHelper jdbcService,
			final AttributeValueAdapter attributeValueAdapter,
			final org.cmdbuild.data.store.Store<StorableEmailAccount> emailAccountStore,
			final EmailServiceFactory emailServiceFactory, final EmailTemplateLogic emailTemplateLogic) {
		this.dataView = dataView;
		this.jdbcService = jdbcService;
		this.attributeValueAdapter = attributeValueAdapter;
		this.emailServiceFactory = emailServiceFactory;
		this.emailAccountStore = emailAccountStore;
		this.emailTemplateLogic = emailTemplateLogic;
	}

	@Override
	protected Class<ConnectorTask> getType() {
		return ConnectorTask.class;
	}

	@Override
	protected Command command(final ConnectorTask task) {
		return composeOnExeption(connector(task), sendEmail(task));

	}

	private ConnectorTaskCommandWrapper connector(final ConnectorTask task) {
		return new ConnectorTaskCommandWrapper(dataView, jdbcService, attributeValueAdapter, task);
	}

	private Command sendEmail(final ConnectorTask task) {
		return new SendEmailTemplateCommandWrapper(emailAccountStore, emailServiceFactory, emailTemplateLogic, task);
	}

}
