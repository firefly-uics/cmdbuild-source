(function () {

	/**
	 * Classes specific history tab controller
	 */
	Ext.define('CMDBuild.controller.management.classes.tabs.History', {
		extend: 'CMDBuild.controller.management.common.tabs.History',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.common.tabs.history.Classes'
		],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @cfg {CMDBuild.controller.management.classes.CMModCardController}
		 */
		parentDelegate: undefined,

		/**
		 * Attributes to hide from selectedEntity object
		 *
		 * @cfg {Array}
		 */
		attributesKeysToFilter: [
			'Code',
			'Id',
			'IdClass',
			'IdClass_value',
			CMDBuild.core.constants.Proxy.BEGIN_DATE,
			CMDBuild.core.constants.Proxy.CLASS_NAME,
			CMDBuild.core.constants.Proxy.ID,
			CMDBuild.core.constants.Proxy.USER
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getTabHistoryGridColumns',
			'getTabHistoryGridStore',
			'onTabHistoryRowExpand',
			'onTabHistoryPanelShow = onTabHistoryIncludeRelationCheck', // Reloads store to be consistent with includeRelationsCheckbox state
			'tabHistorySelectedEntityGet',
			'tabHistorySelectedEntitySet'
		],

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @property {Object}
		 */
		selectedEntity: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.classes.CMModCardController} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.grid = Ext.create('CMDBuild.view.management.classes.tabs.history.GridPanel', {
				delegate: this
			});

			this.view.add(this.grid);

			this.buildCardModuleStateDelegate();
		},

		/**
		 * It's implemented with ugly workarounds because server side ugly code.
		 *
		 * TODO: should be better to refactor this method when a getCard service will returns a better model of card data
		 *
		 * @override
		 */
		addCurrentCardToStore: function() {
			var selectedEntityAttributes = {};
			var selectedEntityMergedData = Ext.Object.merge(this.selectedEntity.raw, this.selectedEntity.getData());

			// Filter selectedEntity's attributes values to avoid the display of incorrect data
			Ext.Object.each(selectedEntityMergedData, function(key, value, myself) {
				if (!Ext.Array.contains(this.attributesKeysToFilter, key) && key.indexOf('_') != 0)
					selectedEntityAttributes[key] = value;
			}, this);

			selectedEntityMergedData[CMDBuild.core.constants.Proxy.ID] = this.selectedEntity.get(CMDBuild.core.constants.Proxy.ID);

			this.valuesFormattingAndCompare(selectedEntityAttributes); // Formats values only

			this.clearStoreAdd(this.buildCurrentEntityModel(selectedEntityMergedData, selectedEntityAttributes));
		},

		buildCardModuleStateDelegate: function() {
			var me = this;

			this.cardStateDelegate = new CMDBuild.state.CMCardModuleStateDelegate();

			this.cardStateDelegate.onEntryTypeDidChange = function(state, entryType) {
				me.onEntryTypeSelected(entryType);
			};

			this.cardStateDelegate.onCardDidChange = function(state, card) {
				Ext.suspendLayouts();
				me.onCardSelected(card);
				Ext.resumeLayouts();
			};

			_CMCardModuleState.addDelegate(this.cardStateDelegate);

			if (!Ext.isEmpty(this.view))
				this.mon(this.view, 'destroy', function(view) {
					_CMCardModuleState.removeDelegate(this.cardStateDelegate);

					delete this.cardStateDelegate;
				}, this);
		},

		/**
		 * @param {Object} entityData
		 * @param {Object} entityAttributeData
		 *
		 * @returns {CMDBuild.model.common.tabs.history.classes.CardRecord} currentEntityModel
		 */
		buildCurrentEntityModel: function(entityData, entityAttributeData) {
			var currentEntityModel = Ext.create('CMDBuild.model.common.tabs.history.classes.CardRecord', entityData);
			currentEntityModel.set(CMDBuild.core.constants.Proxy.VALUES, entityAttributeData);
			currentEntityModel.commit();

			return currentEntityModel;
		},

		/**
		 * @param {CMDBuild.model.common.tabs.history.classes.RelationRecord} record
		 *
		 * @override
		 */
		currentCardRowExpand: function(record) {
			var predecessorRecord = this.getRecordPredecessor(record);
			var selectedEntityAttributes = {};
			var selectedEntityMergedData = Ext.Object.merge(this.selectedEntity.raw, this.selectedEntity.getData());

			// Filter selectedEntity's attributes values to avoid the display of incorrect data
			Ext.Object.each(selectedEntityMergedData, function(key, value, myself) {
				if (!Ext.Array.contains(this.attributesKeysToFilter, key) && key.indexOf('_') != 0)
					selectedEntityAttributes[key] = value;
			}, this);

			selectedEntityMergedData[CMDBuild.core.constants.Proxy.ID] = this.selectedEntity.get(CMDBuild.core.constants.Proxy.ID);

			if (!Ext.isEmpty(predecessorRecord)) {
				var predecessorParams = {};
				predecessorParams[CMDBuild.core.constants.Proxy.CARD_ID] = predecessorRecord.get(CMDBuild.core.constants.Proxy.ID); // Historic card ID
				predecessorParams[CMDBuild.core.constants.Proxy.CLASS_NAME] = selectedEntityMergedData[CMDBuild.core.constants.Proxy.CLASS_NAME];

				this.getProxy().getHistoric({
					params: predecessorParams,
					scope: this,
					failure: function(response, options, decodedResponse) {
						_error('get historic predecessor card failure', this);
					},
					success: function(response, options, decodedResponse) {
						this.valuesFormattingAndCompare(selectedEntityAttributes, decodedResponse.response[CMDBuild.core.constants.Proxy.VALUES]);

						// Setup record property with historic card details to use XTemplate functionalities to render
						record.set(CMDBuild.core.constants.Proxy.VALUES, selectedEntityAttributes);
					}
				});
			}
		},

		/**
		 * @returns {CMDBuild.core.proxy.common.tabs.history.Classes}
		 *
		 * @override
		 */
		getProxy: function() {
			return CMDBuild.core.proxy.common.tabs.history.Classes;
		},

		/**
		 * @returns {Array} columns
		 *
		 * @override
		 */
		getTabHistoryGridColumns: function() {
			var columns = this.callParent(arguments);

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_CARD)) {
				Ext.Array.push(columns, [
					{
						dataIndex: CMDBuild.core.constants.Proxy.IS_CARD,
						text: CMDBuild.Translation.attributes,
						width: 65,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						renderer: function(value, metaData, record) {
							return value ? '<img src="images/icons/tick.png" alt="' + CMDBuild.Translation.attributes + '" />' : null;
						}
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.IS_RELATION,
						text: CMDBuild.Translation.relation,
						width: 65,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						renderer: function(value, metaData, record) {
							return value ? '<img src="images/icons/tick.png" alt="' + CMDBuild.Translation.relation + '" />' : null;
						}
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.DOMAIN,
						text: CMDBuild.Translation.domain,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESTINATION_DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					}
				]);
			}

			return columns;
		},

		/**
		 * @param {Object} card
		 */
		onCardSelected: function(card) {
			this.tabHistorySelectedEntitySet(card);

			if (!Ext.isEmpty(this.entryType) && this.entryType.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) != CMDBuild.core.constants.Global.getTableTypeSimpleTable()) // SimpleTables hasn't history
				this.view.setDisabled(Ext.isEmpty(this.tabHistorySelectedEntityGet()));

			this.cmfg('onTabHistoryPanelShow');
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onEntryTypeSelected: function(entryType) {
			this.entryType = entryType;

			this.view.disable();
		}
	});

})();