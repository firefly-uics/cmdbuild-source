(function () {

	/**
	 * Classes specific history tab controller
	 */
	Ext.define('CMDBuild.controller.management.classes.tabs.History', {
		extend: 'CMDBuild.controller.management.common.tabs.History',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
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
			CMDBuild.core.proxy.CMProxyConstants.BEGIN_DATE,
			CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME,
			CMDBuild.core.proxy.CMProxyConstants.ID,
			CMDBuild.core.proxy.CMProxyConstants.USER
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
			var predecessorRecord = this.grid.getStore().getAt(1); // Get expanded record predecessor record
			var selectedEntityAttributes = {};
			var selectedEntityMergedData = Ext.Object.merge(this.selectedEntity.raw, this.selectedEntity.getData());

			// Filter selectedEntity's attributes values to avoid the display of incorrect data
			Ext.Object.each(selectedEntityMergedData, function(key, value, myself) {
				if (!Ext.Array.contains(this.attributesKeysToFilter, key) && key.indexOf('_') != 0)
					selectedEntityAttributes[key] = value;
			}, this);

			selectedEntityMergedData[CMDBuild.core.proxy.CMProxyConstants.ID] = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ID);

			if (!Ext.isEmpty(predecessorRecord)) {
				var predecessorParams = {};
				predecessorParams[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = predecessorRecord.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic card ID
				predecessorParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = selectedEntityMergedData[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME];

				this.getProxy().getHistoric({
					params: predecessorParams,
					scope: this,
					failure: function(response, options, decodedResponse) {
						_error('get historic predecessor card failure', this);
					},
					success: function(response, options, decodedResponse) {
						this.valuesFormattingAndCompare(selectedEntityAttributes, decodedResponse.response[CMDBuild.core.proxy.CMProxyConstants.VALUES]);

						this.clearStoreAdd(this.buildCurrentEntityModel(selectedEntityMergedData, selectedEntityAttributes));
					}
				});
			} else {
				this.valuesFormattingAndCompare(selectedEntityAttributes);

				this.clearStoreAdd(this.buildCurrentEntityModel(selectedEntityMergedData, selectedEntityAttributes));
			}
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
		 * @return {CMDBuild.model.common.tabs.history.classes.CardRecord} currentEntityModel
		 */
		buildCurrentEntityModel: function(entityData, entityAttributeData) {
			var currentEntityModel = Ext.create('CMDBuild.model.common.tabs.history.classes.CardRecord', entityData);
			currentEntityModel.set(CMDBuild.core.proxy.CMProxyConstants.VALUES, entityAttributeData);
			currentEntityModel.commit();

			return currentEntityModel;
		},

		/**
		 * @return {CMDBuild.core.proxy.common.tabs.history.Classes}
		 *
		 * @override
		 */
		getProxy: function() {
			return CMDBuild.core.proxy.common.tabs.history.Classes;
		},

		/**
		 * @param {Object} card
		 */
		onCardSelected: function(card) {
			this.tabHistorySelectedEntitySet(card);

			if (!Ext.isEmpty(this.entryType) && this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE) != 'simpletable') // SimpleTables hasn't history
				this.view.setDisabled(Ext.isEmpty(this.tabHistorySelectedEntityGet()));

			this.cmfg('onTabHistoryPanelShow');
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onEntryTypeSelected: function(entryType) {
			this.entryType = entryType;

			this.view.disable();
		},
	});

})();