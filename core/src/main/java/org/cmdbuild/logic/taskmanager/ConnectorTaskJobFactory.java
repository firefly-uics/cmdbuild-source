package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static org.cmdbuild.common.utils.guava.Functions.build;
import static org.cmdbuild.scheduler.command.SafeCommand.safe;

import java.util.Map;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.task.ConnectorTask.AttributeMapping;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.command.BuildableCommandBasedJob;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.SimpleAttribute;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.StoreSynchronizer;
import org.cmdbuild.services.sync.store.internal.BuildableCatalog;
import org.cmdbuild.services.sync.store.internal.Catalog;
import org.cmdbuild.services.sync.store.internal.InternalStore;

import com.google.common.base.Function;

public class ConnectorTaskJobFactory extends AbstractJobFactory<ConnectorTask> {

	private static class ConnectorTaskCommandWrapper implements Command {

		private static final Function<Builder<? extends ClassType>, ClassType> BUILD = build();

		private final CMDataView dataView;
		private final ConnectorTask task;

		private ConnectorTaskCommandWrapper(final CMDataView dataView, final ConnectorTask task) {
			this.dataView = dataView;
			this.task = task;
		}

		@Override
		public void execute() {
			final Catalog catalog = catalog();
			final Store left = null; // TODO
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
			for (final AttributeMapping attributeMapping : task.getAttributeMappings()) {
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
			final Iterable<ClassType> types = transformValues(typeBuildersByName, BUILD).values();
			final Catalog catalog = BuildableCatalog.newInstance() //
					.withTypes(types) //
					.build();
			return catalog;
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
	protected Job doCreate(final ConnectorTask task) {
		final String name = task.getId().toString();
		final Command command = new ConnectorTaskCommandWrapper(dataView, task);
		return BuildableCommandBasedJob.newInstance() //
				.withName(name) //
				.withAction(safe(command)) //
				.build();
	}

}
