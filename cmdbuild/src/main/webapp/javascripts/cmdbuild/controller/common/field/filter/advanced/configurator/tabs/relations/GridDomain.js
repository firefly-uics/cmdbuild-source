(function () {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.GridDomain', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Relations'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.Relations}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Object}
		 *
		 * @private
		 */
		bufferEntryTypes: {},

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainBeforeEdit',
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainCheckchange',
			'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainEntryTypeSelect'
		],

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.DomainGridPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.Relations} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.DomainGridPanel', { delegate: this })
		},

		// BufferEntryTypes property methods
			/**
			 * @param {String} name
			 * @param {String} propertyName
			 *
			 * @returns {CMDBuild.model.core.buttons.iconized.add.relation.EntryType or mixed}
			 *
			 * @private
			 */
			bufferEntryTypesGet: function (name, propertyName) {
				// Error handling
					if (!Ext.isString(name) || Ext.isEmpty(name))
						return _error('bufferEntryTypesGet(): unmanaged name parameter', this, name);
				// END: Error handling

				if (Ext.isString(propertyName) && !Ext.isEmpty(propertyName))
					return this.bufferEntryTypes[name].get(propertyName);

				return this.bufferEntryTypes[name];
			},

			/**
			 * @param {Function} callback
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			bufferEntryTypesSet: function (callback) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Relations.readAllEntryTypes({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							Ext.Array.forEach(decodedResponse, function (entryTypeObject, i, allEntryTypeObjects) {
								if (Ext.isObject(entryTypeObject) && !Ext.Object.isEmpty(entryTypeObject))
									this.bufferEntryTypes[entryTypeObject[CMDBuild.core.constants.Proxy.NAME]] = Ext.create(
										'CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.EntryType',
										entryTypeObject
									);
							}, this);

							if (Ext.isFunction(callback))
								Ext.callback(callback, this);
						} else {
							_error('bufferEntryTypesSet(): unmanaged response', this, decodedResponse);
						}
					}
				});
			},

		/**
		 * Apply filter to classes store to display only related items (no simple classes)
		 *
		 * @param {Object} parameters
		 * @param {Number} parameters.colIdx
		 * @param {Object} parameters.column
		 * @param {CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.DomainGrid} parameters.record
		 *
		 * @returns {Boolean}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainBeforeEdit: function (parameters) {
			if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
				var colIdx = parameters.colIdx,
					column = parameters.column,
					recordDestinationId = !Ext.isEmpty(parameters.record.get) && Ext.isFunction(parameters.record.get)
						? parameters.record.get([CMDBuild.core.constants.Proxy.DESTINATION, CMDBuild.core.constants.Proxy.ID]) : null;

				if (
					Ext.isObject(column) && !Ext.Object.isEmpty(column)
					&& !Ext.isEmpty(recordDestinationId)
					&& colIdx == 2 // Avoid to go in edit of unwanted columns
				) {
					column.getEditor().getStore().clearFilter();
					column.getEditor().getStore().filterBy(function (storeRecord, id) {
						return (
							storeRecord.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) != CMDBuild.core.constants.Global.getTableTypeSimpleTable()
							&& (
								storeRecord.get(CMDBuild.core.constants.Proxy.PARENT) == recordDestinationId
								|| storeRecord.get(CMDBuild.core.constants.Proxy.ID) == recordDestinationId
							)
						);
					}, this);

					return true;
				}
			}

			return false;
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.checked
		 * @param {String} parameters.propertyName
		 * @param {CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.DomainGrid} parameters.record
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainCheckchange: function (parameters) {
			if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
				var checked = Ext.isBoolean(parameters.checked) ? parameters.checked : false,
					propertyName = parameters.propertyName,
					record = parameters.record;

				if (
					Ext.isString(propertyName) && !Ext.isEmpty(propertyName)
					&& Ext.isObject(record) && !Ext.Object.isEmpty(record)
				) {
					this.view.getSelectionModel().select(record); // Autoselect on checkchange

					// Makes properties mutual exclusive only on check action
					if (checked)
						record.setType(propertyName);
				}
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainEntryTypeSelect: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.scope = Ext.isObject(parameters.scope) ? parameters.scope : this;

			// Error handling
				if (this.cmfg('fieldFilterAdvancedConfiguratorConfigurationIsEmpty', CMDBuild.core.constants.Proxy.CLASS_NAME))
					return _error(
						'onFieldFilterAdvancedConfiguratorConfigurationRelationsGridDomainEntryTypeSelect(): unmanaged configuration className property',
						this,
						this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_NAME)
					);
			// END: Error handling

			this.view.getStore().removeAll();
			this.view.getSelectionModel().clearSelections();

			this.bufferEntryTypesSet(function (options, success, response) {
				var domainsDestination = [],
					domainsSource = [],
					records = [];

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE_ONLY] = true;
				params[CMDBuild.core.constants.Proxy.SOURCE] = this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_NAME);

				CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Relations.getDomains({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse))
							Ext.Array.forEach(decodedResponse, function (domainObject, i, allDomainObjects) {
								if (Ext.isObject(domainObject) && !Ext.Object.isEmpty(domainObject))
									domainsSource.push(Ext.create('CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.Domain', domainObject));
							}, this);

						var params = {};
						params[CMDBuild.core.constants.Proxy.ACTIVE_ONLY] = true;
						params[CMDBuild.core.constants.Proxy.DESTINATION] = this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_NAME);

						CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Relations.getDomains({
							params: params,
							scope: this,
							success: function (response, options, decodedResponse) {
								decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

								if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse))
									Ext.Array.each(decodedResponse, function (domainObject, i, allDomainObjects) {
										if (Ext.isObject(domainObject) && !Ext.Object.isEmpty(domainObject))
											domainsDestination.push(Ext.create('CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.Domain', domainObject));
									}, this);

								if (!Ext.isEmpty(domainsSource) || !Ext.isEmpty(domainsDestination)) {
									// Domains where entryType is source manage
									if (Ext.isArray(domainsSource) && !Ext.isEmpty(domainsSource))
										Ext.Array.each(domainsSource, function (domainModel, i, allDomainModels) {
											if (Ext.isObject(domainModel) && !Ext.Object.isEmpty(domainModel))
												records.push(
													Ext.create('CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.DomainGrid', {
														destination: this.bufferEntryTypesGet(domainModel.get(CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME)),
														direction: '_1',
														domain: domainModel,
														domainDescription: domainModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
														orientedDescription: domainModel.get(CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION),
														source: this.bufferEntryTypesGet(domainModel.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME))
													})
												);
										}, this);

									// Domains where entryType is destination manage
									if (Ext.isArray(domainsDestination) && !Ext.isEmpty(domainsDestination))
										Ext.Array.each(domainsDestination, function (domainModel, i, allDomainModels) {
											if (Ext.isObject(domainModel) && !Ext.Object.isEmpty(domainModel))
												records.push(
													Ext.create('CMDBuild.model.common.field.filter.advanced.configurator.tabs.relations.DomainGrid', {
														destination: this.bufferEntryTypesGet(domainModel.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME)),
														direction: '_2',
														domain: domainModel,
														domainDescription: domainModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
														orientedDescription: domainModel.get(CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION),
														source: this.bufferEntryTypesGet(domainModel.get(CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME))
													})
												);
										}, this);

									if (!Ext.isEmpty(records))
										this.view.getStore().add(CMDBuild.core.Utils.objectArraySort(records, CMDBuild.core.constants.Proxy.DOMAIN_DESCRIPTION)); // Ascending items sort
								}

								if (Ext.isFunction(parameters.callback))
									Ext.callback(parameters.callback, parameters.scope);
							}
						});
					}
				});
			});
		}
	});

})();
