(function() {

	Ext.define('CMDBuild.controller.common.field.comboBox.Searchable', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
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

		/**
		 * @param {Ext.data.Model} record
		 */
		addToStoreIfNotAlreadyInside: function(record) {
			if (
				!Ext.isEmpty(record)
				&& !Ext.isEmpty(this.view.getStore())
				&& this.view.getStore().find('Id', record.get('Id')) == -1
			) {
				this.view.getStore().add(Ext.create('CMDBuild.model.common.attributes.ForeignKeyStore', record.getData())); // Model conversion

				this.view.validate();
			}
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
				this.addToStoreIfNotAlreadyInside(selectedRecord);
				this.view.blur(); // Allow 'change' event that occurs on blur
				this.view.setValue(selectedRecord.get('Id'));
			}
		},

		/**
		 * @returns {Boolean}
		 */
		fieldComboBoxSearchableStoreExceedsLimit: function() {
			if (!Ext.isEmpty(this.view.getStore()))
				return this.view.getStore().getTotalCount() > CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT);

			return false;
		},

		onFieldComboBoxSearchableKeyUp: function() {
			this.onFieldComboBoxSearchableTrigger3Click(this.view.getRawValue());
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
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.core.proxy.Classes.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						var targetClassObject = Ext.Array.findBy(decodedResponse, function(item, i) {
							return item[CMDBuild.core.constants.Proxy.NAME] == this.view.attributeModel.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);
						}, this);

						if (!Ext.isEmpty(targetClassObject)) {
							var configurationObject = {};
							configurationObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = Ext.create('CMDBuild.cache.CMEntryTypeModel', targetClassObject);
							configurationObject[CMDBuild.core.constants.Proxy.GRID_CONFIGURATION] = {
								presets: { quickSearch: value }
							};
							configurationObject[CMDBuild.core.constants.Proxy.READ_ONLY] = this.configurationGet(CMDBuild.core.constants.Proxy.READ_ONLY_SEARCH_WINDOW);

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