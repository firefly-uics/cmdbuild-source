package org.cmdbuild.model.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.logic.DataAccessLogic;
import org.cmdbuild.workflow.CMActivityInstance;

public class LinkCards extends Widget {

	public static final String CREATED_CARD_ID_SUBMISSION_PARAM = "output";

	/**
	 * A CQL query to fill the linkCard grid Use it or the className
	 */
	private String filter;

	/**
	 * Fill the linkCard grid with the cards of this class. Use it or the filter
	 */
	private String className;

	/**
	 * A CQL query to define the starting selection
	 */
	private String defaultSelection;

	/**
	 * If true, the grid is in read-only mode so you can not select its rows
	 */
	private boolean readOnly;

	/**
	 * To allow the selection of only a row
	 */
	private boolean singleSelect;

	/**
	 * Add an icon at the right of each row to edit the referred card
	 */
	private boolean allowCardEditing;

	/**
	 * If true, the user must select a card on this widget before to can advance
	 * with the process
	 */
	private boolean required;

	/**
	 * If true, enable the map module for this widget
	 */
	private boolean enableMap;

	/**
	 * The latitude to use as default for the map module
	 */
	private Integer mapLatitude;

	/**
	 * The longitude to use as default for the map module
	 */
	private Integer mapLongitude;

	/**
	 * The zoom level to use as default for the map module
	 */
	private Integer mapZoom;

	/**
	 * The name of the variable where to put the selections of the widget during
	 * the save operation
	 */
	private String outputName;

	/**
	 * Templates to use for the CQL filters
	 */
	private Map<String, String> templates;

	private final DataAccessLogic dataAccessLogic;

	public LinkCards(final DataAccessLogic dataAccessLogic) {
		this.dataAccessLogic = dataAccessLogic;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public String getDefaultSelection() {
		return defaultSelection;
	}

	public void setDefaultSelection(final String defaultSelection) {
		this.defaultSelection = defaultSelection;
	}

	public boolean isSingleSelect() {
		return singleSelect;
	}

	public void setSingleSelect(final boolean singleSelect) {
		this.singleSelect = singleSelect;
	}

	public boolean isAllowCardEditing() {
		return allowCardEditing;
	}

	public void setAllowCardEditing(final boolean allowCardEditing) {
		this.allowCardEditing = allowCardEditing;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public boolean isEnableMap() {
		return enableMap;
	}

	public void setEnableMap(final boolean enableMap) {
		this.enableMap = enableMap;
	}

	public Integer getMapLatitude() {
		return mapLatitude;
	}

	public void setMapLatitude(final Integer mapLatitude) {
		this.mapLatitude = mapLatitude;
	}

	public Integer getMapLongitude() {
		return mapLongitude;
	}

	public void setMapLongitude(final Integer mapLongitude) {
		this.mapLongitude = mapLongitude;
	}

	public Integer getMapZoom() {
		return mapZoom;
	}

	public void setMapZoom(final Integer mapZoom) {
		this.mapZoom = mapZoom;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(final Map<String, String> templates) {
		this.templates = templates;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output) throws Exception {
		if (outputName != null) {
			output.put(outputName, outputValue(input));
		}
	}

	private CardReference[] outputValue(final Object input) {
		@SuppressWarnings("unchecked") final Map<String, List<Object>> inputMap = (Map<String, List<Object>>) input;
		final List<Object> selectedCardIds = inputMap.get(CREATED_CARD_ID_SUBMISSION_PARAM);
		final List<CardReference> selectedCards = new ArrayList<CardReference>(selectedCardIds.size());
		for (Object cardId : selectedCardIds) {
			final CMCard card = dataAccessLogic.getCard(className, cardId);
			final CardReference cardReference = CardReference.newInstance(card);
			if (cardReference != null) {
				selectedCards.add(cardReference);
			}
		}
		return selectedCards.toArray(new CardReference[selectedCards.size()]);
	}
}
