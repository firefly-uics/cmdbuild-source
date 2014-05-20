package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Sets.newHashSet;
import static org.cmdbuild.common.utils.guava.Functions.build;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SourceConfigurationVisitor;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.SimpleAttribute;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.StoreSynchronizer;
import org.cmdbuild.services.sync.store.internal.BuildableAttributeMapping;
import org.cmdbuild.services.sync.store.internal.BuildableCatalog;
import org.cmdbuild.services.sync.store.internal.BuildableTableOrViewMapping;
import org.cmdbuild.services.sync.store.internal.BuildableTypeMapper;
import org.cmdbuild.services.sync.store.internal.Catalog;
import org.cmdbuild.services.sync.store.internal.InternalStore;
import org.cmdbuild.services.sync.store.internal.SqlStore;
import org.cmdbuild.services.sync.store.internal.TableOrViewMapping;
import org.cmdbuild.services.sync.store.internal.TypeMapping;
import org.postgresql.ds.PGSimpleDataSource;

import com.google.common.base.Function;

public class ConnectorTaskJobFactory extends AbstractJobFactory<ConnectorTask> {

	private static class ConnectorTaskCommandWrapper implements Command {

		private static final Function<Builder<? extends ClassType>, ClassType> BUILD_CLASS_TYPE = build();
		private static final Function<Builder<? extends TypeMapping>, TypeMapping> BUILD_TYPE_MAPPING = build();

		private final CMDataView dataView;
		private final ConnectorTask task;

		private ConnectorTaskCommandWrapper(final CMDataView dataView, final ConnectorTask task) {
			this.dataView = dataView;
			this.task = task;
		}

		@Override
		public void execute() {
			final Catalog catalog = catalog();
			final Store left = left(catalog);
			final Store rightAndTarget = InternalStore.newInstance() //
					.withDataView(dataView) //
					.withCatalog(catalog) //
					.build();
			StoreSynchronizer.newInstance() //
					.withLeft(left) //
					.withRight(rightAndTarget) //
					.withTarget(rightAndTarget) //
					.build() //
					.sync();
		}

		private Catalog catalog() {
			final Map<String, ClassType.Builder> typeBuildersByName = newHashMap();
			for (final ConnectorTask.AttributeMapping attributeMapping : task.getAttributeMappings()) {
				final String typeName = attributeMapping.getTargetType();
				ClassType.Builder typeBuilder = typeBuildersByName.get(typeName);
				if (typeBuilder == null) {
					typeBuilder = ClassType.newInstance();
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
					final PGSimpleDataSource dataSource = new PGSimpleDataSource();
					dataSource.setServerName(sourceConfiguration.getHost());
					dataSource.setPortNumber(sourceConfiguration.getPort());
					dataSource.setDatabaseName(sourceConfiguration.getDatabase());
					dataSource.setUser(sourceConfiguration.getUsername());
					dataSource.setPassword(sourceConfiguration.getPassword());

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
					for (final Entry<String, Map<String, BuildableTypeMapper.Builder>> entry : allTypeMapperBuildersByTableOrViewName
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
	}

	private final CMDataView dataView;

	public ConnectorTaskJobFactory(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	protected Class<ConnectorTask> getType() {
		return ConnectorTask.class;
	}

	@Override
	protected Command command(final ConnectorTask task) {
		return new ConnectorTaskCommandWrapper(dataView, task);
	}

}
