(function () {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.Relations', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.Configurator}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedConfiguratorConfigurationRelationsEntryTypeSelect',
			'fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainGet',
			'fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainIsEmpty',
			'fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainReset = fieldFilterAdvancedConfiguratorReset',
			'fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainSet',
			'fieldFilterAdvancedConfiguratorConfigurationRelationsValueGet',
			'fieldFilterAdvancedConfiguratorConfigurationRelationsValueSet',
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsCheckchange',
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsDomainSelect'

		],

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.GridDomain}
		 */
		controllerGridDomain: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.GridCard}
		 */
		controllerGridCard: undefined,

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.DomainGrid}
		 *
		 * @private
		 */
		selectedDomain: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.RelationsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.configurator.Configurator} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.RelationsView', { delegate: this });

			// Build sub-controllers
			this.controllerGridCard = Ext.create('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.GridCard', { parentDelegate: this });
			this.controllerGridDomain = Ext.create('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.GridDomain', { parentDelegate: this });

			this.view.add([
				this.controllerGridCard.getView(),
				this.controllerGridDomain.getView()
			]);
		},

		/**
		 * Decodes filter object and launch creation of form items
		 *
		 * @param {Array} filterConfiguration
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		decodeFilterConfigurationObject: function (filterConfiguration) {
			if (Ext.isArray(filterConfiguration) && !Ext.isEmpty(filterConfiguration)) {
				Ext.Array.forEach(filterConfiguration, function (configurationObject, i, allConfigurationObjects) {
					var domainRecord = null;

					var recordIndex = this.controllerGridDomain.getView().getStore().findBy(function (record) {
						return (
							record.get([CMDBuild.core.constants.Proxy.DOMAIN, CMDBuild.core.constants.Proxy.NAME]) == configurationObject[CMDBuild.core.constants.Proxy.DOMAIN]
							&& record.get(CMDBuild.core.constants.Proxy.DIRECTION) == configurationObject[CMDBuild.core.constants.Proxy.DIRECTION]
						);
					});

					if (recordIndex >= 0)
						domainRecord = this.controllerGridDomain.getView().getStore().getAt(recordIndex);

					if (!Ext.isEmpty(domainRecord)) {
						domainRecord.setType(configurationObject[CMDBuild.core.constants.Proxy.TYPE]);

						if (Ext.isArray(configurationObject[CMDBuild.core.constants.Proxy.CARDS]) && !Ext.isEmpty(configurationObject[CMDBuild.core.constants.Proxy.CARDS]))
							domainRecord.set(CMDBuild.core.constants.Proxy.CHECKED_CARDS, configurationObject[CMDBuild.core.constants.Proxy.CARDS]);
					}
				}, this);
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 * @param {Boolean} parameters.visible
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorConfigurationRelationsEntryTypeSelect: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.scope = Ext.isObject(parameters.scope) ? parameters.scope : this;
			parameters.visible = Ext.isBoolean(parameters.visible) ? parameters.visible : false;

			var className = this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_NAME);

			// Error handling
				if (this.cmfg('fieldFilterAdvancedConfiguratorConfigurationIsEmpty'))
					return _error('fieldFilterAdvancedConfiguratorConfigurationRelationsEntryTypeSelect(): unmanaged configuration property', this, this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet'));

				if (!Ext.isString(className) || Ext.isEmpty(className))
					return _error('fieldFilterAdvancedConfiguratorConfigurationRelationsEntryTypeSelect(): unmanaged className property', this, className);
			// END: Error handling

			this.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainReset');

			var requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
				id: 'fieldFilterAdvancedConfiguratorRelationsEntryTypeSelectBarrier',
				scope: this,
				callback: function () {
					this.view.tab.setVisible(parameters.visible); // Show/Hide tab

					if (Ext.isFunction(parameters.callback))
						Ext.callback(parameters.callback, parameters.scope);
				}
			});

			this.controllerGridCard.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardEntryTypeSelect', {
				callback: requestBarrier.getCallback('fieldFilterAdvancedConfiguratorRelationsEntryTypeSelectBarrier')
			});

			this.controllerGridDomain.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainEntryTypeSelect', {
				callback: requestBarrier.getCallback('fieldFilterAdvancedConfiguratorRelationsEntryTypeSelectBarrier')
			});

			requestBarrier.finalize('fieldFilterAdvancedConfiguratorRelationsEntryTypeSelectBarrier', true);
		},

		// SelectedDomain property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @returns {Void}
			 */
			fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainReset: function () {
				this.propertyManageReset('selectedDomain');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainSet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.DomainGrid';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @returns {Array} out
		 */
		fieldFilterAdvancedConfiguratorConfigurationRelationsValueGet: function () {
			var out = [];

			if (this.controllerGridDomain.getView().getSelectionModel().hasSelection())
				this.controllerGridDomain.getView().getStore().each(function (domain) {
					var type = domain.getType();

					if (!Ext.isEmpty(type)) {
						var domainFilterConfiguration = {};
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DESTINATION] = domain.get([CMDBuild.core.constants.Proxy.DESTINATION, CMDBuild.core.constants.Proxy.NAME]);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DIRECTION] = domain.get(CMDBuild.core.constants.Proxy.DIRECTION);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DOMAIN] = domain.get([CMDBuild.core.constants.Proxy.DOMAIN, CMDBuild.core.constants.Proxy.NAME]);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.SOURCE] = domain.get([CMDBuild.core.constants.Proxy.SOURCE, CMDBuild.core.constants.Proxy.NAME]);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.TYPE] = type;

						if (
							type == 'oneof'
							&& this.controllerGridCard.getView().getSelectionModel().hasSelection()
						) {
							var checkedCards = [];

							Ext.Array.each(this.controllerGridCard.getView().getSelectionModel().getSelection(), function (record, i, allRecords) {
								if (!Ext.isEmpty(record)) {
									var checkedCardObject = {};
									checkedCardObject[CMDBuild.core.constants.Proxy.CLASS_NAME] = record.get('IdClass_value');
									checkedCardObject[CMDBuild.core.constants.Proxy.ID] = record.get('Id');

									checkedCards.push(checkedCardObject);
								}
							}, this);

							domainFilterConfiguration[CMDBuild.core.constants.Proxy.CARDS] = checkedCards;
						}

						out.push(domainFilterConfiguration);
					}
				}, this);

			return out;
		},

		/**
		 * @param {Object} filter
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorConfigurationRelationsValueSet: function (filter) {
			if (
				Ext.isObject(filter) && !Ext.Object.isEmpty(filter)
				&& Ext.isArray(filter[CMDBuild.core.constants.Proxy.RELATION]) && !Ext.isEmpty(filter[CMDBuild.core.constants.Proxy.RELATION])
			) {
				this.decodeFilterConfigurationObject(filter[CMDBuild.core.constants.Proxy.RELATION]);
			}
		},

		/**
		 * Forwarder method
		 *
		 * @param {Object} parameters
		 * @param {Boolean} parameters.checked
		 * @param {String} parameters.propertyName
		 * @param {CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.DomainGrid} parameters.record
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsCheckchange: function (parameters) {
			this.controllerGridDomain.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainCheckchange', parameters);
			this.controllerGridCard.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardCheckchange');
		},

		/**
		 * @param {CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.DomainGrid} record
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsDomainSelect: function (record) {
			if (Ext.isObject(record) && !Ext.Object.isEmpty(record)) {
				this.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsSelectedDomainSet', { value: record });

				this.controllerGridCard.cmfg('onFieldFilterAdvancedConfiguratorConfigurationRelationsGridCardDomainSelect');
			}
		}
	});

})();
