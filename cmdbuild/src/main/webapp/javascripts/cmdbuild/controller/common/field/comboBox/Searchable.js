(function() {

	Ext.define('CMDBuild.controller.common.field.comboBox.Searchable', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.searchWindow.SearchWindow}
		 */
		controllerSearchWindow: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldComboBoxSearchableGetStore = fiedlGetStore',
			'fieldComboBoxSearchableGetValue = fiedlGetValue',
			'fieldComboBoxSearchableSetValue = fiedlSetValue',
			'fieldComboBoxSearchableStoreExceedsLimit',
			'onFieldComboBoxSearchableKeyUp',
			'onFieldComboBoxSearchableSetValue',
			'onFieldComboBoxSearchableTrigger1Click',
			'onFieldComboBoxSearchableTrigger2Click',
			'onFieldComboBoxSearchableTrigger3Click'
		],

		/**
		 * @property {CMDBuild.model.common.field.comboBox.searchable.Configuration}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Searchable}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.common.field.comboBox.Searchable} configurationObject.view
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.configurationSet(this.view.configuration);

			// Controller build
			this.controllerSearchWindow = Ext.create('CMDBuild.controller.common.field.searchWindow.SearchWindow', { parentDelegate: this });
		},

		// Configuration property methods
			/**
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.common.field.comboBox.searchable.Configuration} or Mixed
			 */
			configurationGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.configuration.get(parameterName);

				return this.configuration;
			},

			/**
			 * @property {Object} configurationObject
			 */
			configurationSet: function(configurationObject) {
				this.configuration = Ext.create('CMDBuild.model.common.field.comboBox.searchable.Configuration', configurationObject);
			},

		/**
		 * @returns {Ext.data.Store}
		 */
		fieldComboBoxSearchableGetStore: function() {
			return this.view.getStore();
		},

		/**
		 * @returns {Number}
		 */
		fieldComboBoxSearchableGetValue: function() {
			return this.view.getValue();
		},

		/**
		 * @param {Ext.data.Model} selectedRecord
		 */
		fieldComboBoxSearchableSetValue: function(selectedRecord) {
			if (!Ext.isEmpty(selectedRecord)) {
				this.view.blur(); // Allow 'change' event that occurs on blur
				this.cmfg('onFieldComboBoxSearchableSetValue', selectedRecord.get(this.view.valueField));
			}
		},

		/**
		 * @returns {Boolean}
		 */
		fieldComboBoxSearchableStoreExceedsLimit: function() {
			if (!Ext.isEmpty(this.view.getStore()))
				return this.view.getStore().getTotalCount() > parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);

			return false;
		},

		onFieldComboBoxSearchableKeyUp: function() {
			this.onFieldComboBoxSearchableTrigger3Click(this.view.getRawValue());
		},

		/**
		 * Adds values in store if not already inside
		 *
		 * @param {String} value
		 */
		onFieldComboBoxSearchableSetValue: function(value) {
			if (
				!Ext.isEmpty(value)
				&& this.view.getStore().find(this.view.valueField, value) < 0
			) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.view.attributeModel.get(CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS);
				params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = value;

				CMDBuild.core.proxy.common.field.ForeignKey.readCard({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.proxy.CMProxyConstants.CARD];

						if (!Ext.isEmpty(decodedResponse)) {
							if (!Ext.isEmpty(this.view.getStore()))
								this.view.getStore().add(
									Ext.create('CMDBuild.model.common.attributes.ForeignKeyStore', {
										Id: decodedResponse['Id'],
										Description: decodedResponse['Description']
									})
								);

							this.view.setValue(decodedResponse[this.view.valueField]);
						}

						this.view.validate();
					}
				});
			}
		},

		/**
		 * If store has more than configuration limit records, no drop down but opens searchWindow
		 */
		onFieldComboBoxSearchableTrigger1Click: function() {
			if (this.view.getStore().isLoading()) {
				this.view.getStore().on('load', this.trigger1Manager, this, { single: true });
			} else {
				this.trigger1Manager();
			}
		},

		onFieldComboBoxSearchableTrigger2Click: function() {
			if (!this.view.isDisabled())
				this.view.setValue();
		},

		/**
		 * @param {String} value
		 */
		onFieldComboBoxSearchableTrigger3Click: function(value) {
			value = Ext.isString(value) ? value : '';

			if (!this.view.isDisabled()) {
				// Get class data from server
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;

				CMDBuild.core.proxy.Classes.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.proxy.CMProxyConstants.CLASSES];

						var targetClassObject = Ext.Array.findBy(decodedResponse, function(item, i) {
							return item[CMDBuild.core.proxy.CMProxyConstants.NAME] == this.view.attributeModel.get(CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS);
						}, this);

						if (!Ext.isEmpty(targetClassObject)) {
							var configurationObject = {};
							configurationObject[CMDBuild.core.proxy.CMProxyConstants.ENTRY_TYPE] = Ext.create('CMDBuild.cache.CMEntryTypeModel', targetClassObject);
							configurationObject[CMDBuild.core.proxy.CMProxyConstants.GRID_CONFIGURATION] = {
								presets: { quickSearch: value }
							};
							configurationObject[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY] = this.configurationGet(CMDBuild.core.proxy.CMProxyConstants.READ_ONLY_SEARCH_WINDOW);

							this.controllerSearchWindow.fieldSearchWindowConfigurationSet(configurationObject);
							this.controllerSearchWindow.getView().show();
						}
					}
				});
			}
		},

		trigger1Manager: function() {
			if (this.fieldComboBoxSearchableStoreExceedsLimit()) {
				this.onFieldComboBoxSearchableTrigger3Click();
			} else {
				this.view.onTriggerClick();
			}
		}
	});

})();