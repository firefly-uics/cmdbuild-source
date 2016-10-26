(function () {

	/**
	 * Adapter class to use CMCardGrid
	 *
	 * FIXME: refactor to build own grid card
	 */
	Ext.define('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.GridCard', {
		extend: 'CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.CMCardGridController',

		mixins: {
			base: 'CMDBuild.controller.common.abstract.Base',
			cardGridDelegate: 'CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.CardGridPanelDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.Relations}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardCheckchange',
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardDomainSelect',
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardEntryTypeSelect',
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardLoad',
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardSelectionChange',
		],

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.CardGridPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} parameters
		 * @param {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.Relations} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (parameters) {
			// Base controller callParent alias
			this.mixins.base.constructor.call(this, arguments);
			Ext.apply(this, parameters);

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.CardGridPanel', { delegate: this })

			this.callParent([this.view, this.parentDelegate]);

			this.view.addDelegate(this);
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardCheckchange: function () {
			if (!this.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainIsEmpty'))
				return this.view.setDisabled(!this.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainGet', 'oneof'));

			return this.view.setDisabled(true);
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardDomainSelect: function () {
			this.view.getSelectionModel().clearSelections();

			this.view.updateStoreForClassId( // FIXME: implementation of own card grid
				this.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainGet', [CMDBuild.core.constants.Proxy.DESTINATION, CMDBuild.core.constants.Proxy.ID])
			);

			this.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardCheckchange');
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardEntryTypeSelect: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.scope = Ext.isObject(parameters.scope) ? parameters.scope : this;

			this.view.getStore().removeAll();
			this.view.getSelectionModel().clearSelections();

			this.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardCheckchange');

			if (Ext.isFunction(parameters.callback))
				Ext.callback(parameters.callback, parameters.scope);
		},

		/**
		 * Select configuration object cards, executed only on domain selection
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardLoad: function () {
			if (!this.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainIsEmpty')) {
				var checkedCards = this.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainGet', CMDBuild.core.constants.Proxy.CHECKED_CARDS);

				if (Ext.isArray(checkedCards) && !Ext.isEmpty(checkedCards))
					Ext.Array.forEach(checkedCards, function (selectedCardObject, i, allSelectedCardObject) {
						this.view.getSelectionModel().select(
							this.view.getStore().findBy(function (storeRecord) {
								return (
									selectedCardObject[CMDBuild.core.constants.Proxy.CLASS_NAME] == storeRecord.get('IdClass_value')
									&& selectedCardObject[CMDBuild.core.constants.Proxy.ID] == storeRecord.get('Id')
								);
							}),
							true
						);
					}, this);
			}
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardSelectionChange: function () {
			var checkedCards = [];

			if (this.view.getSelectionModel().hasSelection())
				Ext.Array.each(this.view.getSelectionModel().getSelection(), function (record, i, allRecords) {
					if (Ext.isObject(record) && !Ext.isEmpty(record)) {
						var checkedCardObject = {};
						checkedCardObject[CMDBuild.core.constants.Proxy.CLASS_NAME] = record.get('IdClass_value');
						checkedCardObject[CMDBuild.core.constants.Proxy.ID] = record.get('Id');

						checkedCards.push(checkedCardObject);
					}
				}, this);

			this.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainSet', {
				propertyName: CMDBuild.core.constants.Proxy.CHECKED_CARDS,
				value: checkedCards
			});
		},

		// Overrides
			/**
			 * @param {CMDBuild.view.management.common.CMCardGrid} grid
			 * @param {Ext.data.Model} record
			 *
			 * @returns {Void}
			 *
			 * @override
			 */
			onCMCardGridDeselect: function (grid, record) {
				this.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardSelectionChange');
			},

			/**
			 * @param {CMDBuild.view.management.common.CMCardGrid} grid
			 * @param {Ext.data.Model} record
			 *
			 * @returns {Void}
			 *
			 * @override
			 */
			onCMCardGridSelect: function (grid, record) {
				this.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardSelectionChange');
			},

			/**
			 * @returns {Void}
			 *
			 * @override
			 */
			onCardSelected: Ext.emptyFn,

			/**
			 * @param {CMDBuild.view.management.common.CMCardGrid} grid
			 *
			 * @returns {Void}
			 *
			 * @override
			 */
			onCMCardGridLoad: function (grid) {
				this.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardLoad');
			}
	});

})();
