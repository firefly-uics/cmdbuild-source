(function() {

	Ext.define('CMDBuild.controller.common.field.searchWindow.SearchWindow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.comboBox.Searchable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldSearchWindowConfigurationGet',
			'onFieldSearchWindowSaveButtonClick = onFieldSearchWindowItemDoubleClick',
			'onFieldSearchWindowSelectionChange',
			'onFieldSearchWindowShow',
			'onFieldSearchWindowStoreLoad'
		],

		/**
		 * @property {CMDBuild.model.common.field.searchWindow.Configuration}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.common.field.searchWindow.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {Mixed}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.comboBox.Searchable} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.searchWindow.SearchWindow', { delegate: this });
		},

		// Configuration property methods
			/**
			 * Attribute could be a single string (attribute name) or an array of strings that declares path to required attribute through model object's properties
			 *
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			fieldSearchWindowConfigurationGet: function(attributePath) {
				attributePath = Ext.isArray(attributePath) ? attributePath : [attributePath];

				var requiredAttribute = this.configuration;

				if (!Ext.isEmpty(attributePath))
					Ext.Array.forEach(attributePath, function(attributeName, i, allAttributeNames) {
						if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName))
							if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
								&& Ext.isFunction(requiredAttribute.get)
							) { // Model management
								requiredAttribute = requiredAttribute.get(attributeName);
							} else if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
							) { // Simple object management
								requiredAttribute = requiredAttribute[attributeName];
							}
					}, this);

				return requiredAttribute;
			},

			/**
			 * @param {String} attribute
			 *
			 * @returns {Boolean}
			 */
			fieldSearchWindowConfigurationIsEmpty: function(attribute) {
				if (!Ext.isEmpty(attribute))
					return Ext.isEmpty(this.fieldSearchWindowConfigurationGet(attribute));

				return Ext.isEmpty(this.configuration);
			},

			/**
			 * @param {CMDBuild.model.common.field.searchWindow.Configuration} configurationObject
			 */
			fieldSearchWindowConfigurationSet: function(configurationObject) {
				if (
					!Ext.isEmpty(configurationObject)
					&& Ext.isObject(configurationObject)
				) {
					this.configuration = Ext.create('CMDBuild.model.common.field.searchWindow.Configuration', configurationObject);
				} else {
					_error('invalid window configurationObject', this);
				}
			},

		onFieldSearchWindowSaveButtonClick: function() {
			if (this.grid.getSelectionModel().hasSelection())
				this.cmfg('fiedlSetValue', this.grid.getSelectionModel().getSelection()[0]);

			this.view.hide();
		},

		onFieldSearchWindowSelectionChange: function() {
			this.view.saveButton.setDisabled(!this.grid.getSelectionModel().hasSelection());
		},

		/**
		 * After window show setup presets (title, quick search filter, selection)
		 */
		onFieldSearchWindowShow: function() {
			if (this.fieldSearchWindowConfigurationIsEmpty()) {
				_error('search window configuration empty', this);
			} else {
				this.setupViewGrid();

				// Set window title
				this.setViewTitle(this.fieldSearchWindowConfigurationGet([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.TEXT]));

				this.setupViewAddCardButton();

				// Setup save button
				this.onFieldSearchWindowSelectionChange();
			}
		},

		/**
		 * Selected value setup
		 */
		onFieldSearchWindowStoreLoad: function() {
			if (!Ext.isEmpty(this.cmfg('fiedlGetValue')))
				this.grid.getSelectionModel().select(
					this.grid.getStore().find('Id', this.cmfg('fiedlGetValue'))
				);
		},

		/**
		 * Adapter function
		 *
		 * TODO: waiting for refactor (CMDBuild.view.management.common.CMCardWindow)
		 */
		setupViewAddCardButton: function() {
			this.view.addCardButton.setDisabled(this.cmfg('fieldSearchWindowConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY));

			if (!this.cmfg('fieldSearchWindowConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)) {
				this.view.addCardButton.updateForEntry(this.cmfg('fieldSearchWindowConfigurationGet', CMDBuild.core.constants.Proxy.ENTRY_TYPE));

				this.view.mon(this.view.addCardButton, 'cmClick', function(p) {
					var w = new CMDBuild.view.management.common.CMCardWindow({
						withButtons: true,
						title: p.className
					});

					new CMDBuild.controller.management.common.CMCardWindowController(w, {
						cmEditMode: true,
						card: null,
						entryType: p.classId
					});
					w.show();

					this.view.mon(w, 'destroy', function() {
						this.grid.reload();
					}, this);

				}, this);
			}
		},

		setupViewGrid: function() {
			this.view.removeAll(false);
			this.view.add(
				this.grid = Ext.create('CMDBuild.view.common.field.searchWindow.GridPanel', {
					delegate: this,
					CQL: Ext.isEmpty(this.cmfg('fiedlGetStore')) ? null : this.cmfg('fiedlGetStore').getProxy().extraParams,
					selModel: Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
						mode: 'SINGLE',
						idProperty: 'Id' // Required to identify the records for the data and not the id of Ext
					}),
				})
			);

			this.grid.updateStoreForClassId(
				this.fieldSearchWindowConfigurationGet([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
				{
					scope: this,
					cb: function(grid) {
						this.grid.getStore().loadPage(1);

						// Setup quick search value
						this.grid.gridSearchField.focus();
						this.grid.gridSearchField.setValue(
							this.fieldSearchWindowConfigurationGet([
								CMDBuild.core.constants.Proxy.GRID_CONFIGURATION,
								CMDBuild.core.constants.Proxy.PRESETS,
								'quickSearch'
							])
						);

						this.grid.getStore().on('load', function(store, records, successful, eOpts) {
							this.cmfg('onFieldSearchWindowStoreLoad');
						}, this);
					}
				}
			);
		}
	});

})();