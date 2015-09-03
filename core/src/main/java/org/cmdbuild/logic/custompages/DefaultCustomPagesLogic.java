package org.cmdbuild.logic.custompages;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.custompage.DBCustomPage;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class DefaultCustomPagesLogic implements CustomPagesLogic {

	public static interface Converter {

		DBCustomPage toStore(CustomPage value);

		CustomPage toLogic(DBCustomPage value);

	}

	public static interface AccessControlHelper {

		boolean isAccessible(final CustomPage value);

	}

	private final Store<DBCustomPage> store;
	private final Converter converter;
	private final AccessControlHelper accessControlHelper;

	public DefaultCustomPagesLogic(final Store<DBCustomPage> store, final Converter converter,
			final AccessControlHelper accessControlHelper) {
		this.store = store;
		this.converter = converter;
		this.accessControlHelper = accessControlHelper;
	}

	@Override
	public Iterable<CustomPage> read() {
		return from(store.readAll()) //
				.transform(toLogic());
	}

	@Override
	public Iterable<CustomPage> readForCurrentUser() {
		return from(read()) //
				.filter(hasAccess());
	}

	private Function<? super DBCustomPage, CustomPage> toLogic() {
		return new Function<DBCustomPage, CustomPage>() {

			@Override
			public CustomPage apply(final DBCustomPage input) {
				return converter.toLogic(input);
			}

		};
	}

	private Predicate<? super CustomPage> hasAccess() {
		return new Predicate<CustomPage>() {

			@Override
			public boolean apply(final CustomPage input) {
				return accessControlHelper.isAccessible(input);
			}

		};
	}

}
