package org.cmdbuild.logic.taskmanager.task.connector;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Sets.newHashSet;
import static org.cmdbuild.common.utils.guava.Functions.build;

import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.common.java.sql.DataSourceHelper;
import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceType;
import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceTypeVisitor;
import org.cmdbuild.common.java.sql.DataSourceTypes.MySql;
import org.cmdbuild.common.java.sql.DataSourceTypes.Oracle;
import org.cmdbuild.common.java.sql.DataSourceTypes.PostgreSql;
import org.cmdbuild.common.java.sql.DataSourceTypes.SqlServer;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SourceConfigurationVisitor;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.SimpleAttribute;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.StoreSynchronizer;
import org.cmdbuild.services.sync.store.internal.AttributeValueAdapter;
import org.cmdbuild.services.sync.store.internal.BuildableCatalog;
import org.cmdbuild.services.sync.store.internal.Catalog;
import org.cmdbuild.services.sync.store.internal.InternalStore;
import org.cmdbuild.services.sync.store.sql.BuildableAttributeMapping;
import org.cmdbuild.services.sync.store.sql.BuildableTableOrViewMapping;
import org.cmdbuild.services.sync.store.sql.BuildableTypeMapper;
import org.cmdbuild.services.sync.store.sql.SqlStore;
import org.cmdbuild.services.sync.store.sql.TableOrViewMapping;
import org.cmdbuild.services.sync.store.sql.SqlType;
import org.cmdbuild.services.sync.store.sql.TypeMapping;

import com.google.common.base.Function;

class ConnectorTaskCommandWrapper implements Command {

	private static final Function<Builder<? extends ClassType>, ClassType> BUILD_CLASS_TYPE = build();
	private static final Function<Builder<? extends TypeMapping>, TypeMapping> BUILD_TYPE_MAPPING = build();

	private final CMDataView dataView;
	private final DataSourceHelper dataSourceHelper;
	private final AttributeValueAdapter attributeValueAdapter;
	private final ConnectorTask task;

	public ConnectorTaskCommandWrapper(final CMDataView dataView, final DataSourceHelper dataSourceHelper,
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
					final Map<String, TypeMapping> typeMappingBuildersByTypeName = transformValues(entry.getValue(),
							BUILD_TYPE_MAPPING);
					final TableOrViewMapping tableOrViewMapping = BuildableTableOrViewMapping.newInstance() //
							.withName(tableOrViewName) //
							.withTypeMappings(typeMappingBuildersByTypeName.values()) //
							.build();
					tableOrViewMappings.add(tableOrViewMapping);
				}

				store = SqlStore.newInstance() //
						.withDataSource(dataSource) //
						.withTableOrViewMappings(tableOrViewMappings) //
						.withType(typeOf(sourceConfiguration.getType())) //
						.build();
			}

			private SqlType typeOf(final DataSourceType dataSourceType) {
				return new DataSourceTypeVisitor() {

					private SqlType _type;

					public SqlType type() {
						dataSourceType.accept(this);
						return _type;
					}

					@Override
					public void visit(final MySql type) {
						_type = SqlType.MYSQL;
					}

					@Override
					public void visit(final Oracle type) {
						_type = SqlType.ORACLE;
					}

					@Override
					public void visit(final PostgreSql type) {
						_type = SqlType.POSTGRESQL;
					}

					@Override
					public void visit(final SqlServer type) {
						_type = SqlType.SQLSERVER;
					}

				}.type();
			}

		}.store();
	}

	private Store wrap(final Store store) {
		return new PermissionBasedStore(store, new ConnectorTaskPermission(task));
	}

}