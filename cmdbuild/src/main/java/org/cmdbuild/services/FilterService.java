package org.cmdbuild.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cmdbuild.elements.TableImpl.OrderEntry;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.UserContext;

public class FilterService {

	public static final String DEFAULT_FILTER_CATEGORY = "";
	public static final String DEFAULT_FILTER_SUBCATEGORY = "";

	public static CardQuery getFilter(int classId, String categoryOrNull, String subcategoryOrNull) {
		String category = getFilterCategory(categoryOrNull);
		String subcategory = getFilterSubcategory(subcategoryOrNull);
		CardQuery filter = getCurrentFilter(category, subcategory);
		if (filter == null) {
			filter = createAndPutFilter(classId, category, subcategory);
		} else {
			if (filter.getTable().getId() != classId) {
				clearFilters(category, subcategory);
				filter = createAndPutFilter(classId, category, subcategory);
			}
		}
		return filter;
	}

	public static void clearFilters(String categoryOrNull, String subcategoryOrNull) {
		String category = getFilterSubcategory(categoryOrNull);
		String subcategory = getFilterCategory(subcategoryOrNull);
		if (isDefaultCategory(category)) {
			clearAllFilters();
		} else if (isDefaultSubcategory(subcategory)) {
			clearFilterCategory(category);
		} else {
			clearFilterSubcategory(category, subcategory);
		}
	}

	private static void clearAllFilters() {
		createAndBindFilterMap();
	}

	private static void clearFilterCategory(String category) {
		Map<String, CardQuery> filterMap = getfilterMap();
		for (String filterKey : filterMap.keySet()) {
			if (filterKey.startsWith(category))
				filterMap.remove(filterKey);
		}
	}

	private static void clearFilterSubcategory(String category, String subcategory) {
		Map<String, CardQuery> filterMap = getfilterMap();
		filterMap.remove(category+subcategory);
	}

	private static CardQuery getCurrentFilter(String category, String subcategory) {
		Map<String, CardQuery> filterMap = getfilterMap();
		return filterMap.get(category+subcategory);
	}

	private static CardQuery createAndPutFilter(int classId, String category,
			String subcategory) throws NotFoundException {
		Map<String, CardQuery> filterMap = getfilterMap();
		CardQuery filter = createFilter(classId);
		filterMap.put(category+subcategory, filter);
		return filter;
	}

	private static CardQuery createFilter(int classId) throws NotFoundException {
		CardQuery filter;
		UserContext userCtx = new SessionVars().getCurrentUserContext();
		ITable table = userCtx.tables().get(classId);
		filter = table.cards().list();
		// TODO: Use the default Class filter if present when implemented		
		for (OrderEntry sortEntry : table.getOrdering()) {
			filter.order(sortEntry.getAttributeName(), sortEntry.getOrderDirection());
		}
		
		return filter;
	}

	/**
	 * Gets the filter map from the session or creates it
	 * 
	 * @return a filter map bounded to the session
	 */
	private static Map<String, CardQuery> getfilterMap() {
		Map<String, CardQuery> filterMap = new SessionVars().getFilterMap();
		if (filterMap == null)
			filterMap = createAndBindFilterMap();
		return filterMap;
	}

	private static Map<String, CardQuery> createAndBindFilterMap() {
		Map<String, CardQuery> filterMap = new ConcurrentHashMap<String, CardQuery>();
		new SessionVars().setFilterMap(filterMap);
		return filterMap;
	}

	private static boolean isDefaultCategory(String category) {
		return DEFAULT_FILTER_CATEGORY.equals(category);
	}

	private static boolean isDefaultSubcategory(String subcategory) {
		return DEFAULT_FILTER_SUBCATEGORY.equals(subcategory);
	}

	/**
	 * Gets the requested filter category or default if not present
	 * 
	 * @return The requested filter category or default
	 */
	private static String getFilterCategory(String category) {
	    if (category == null)
		category = FilterService.DEFAULT_FILTER_CATEGORY;
	    return category;
	}

	private static String getFilterSubcategory(String subcategory) {
		if (subcategory == null)
			subcategory = FilterService.DEFAULT_FILTER_SUBCATEGORY;
		return subcategory;
	}
}
