(function () {

	Ext.require([
		'CMDBuild.core.constants.Proxy',
		'CMDBuild.model.common.field.grid.KeyValue'
	]);

	Ext.define('CMDBuild.view.common.field.grid.KeyValue', {
		extend: 'Ext.grid.Panel',

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @cfg {CMDBuild.controller.common.field.grid.KeyValue}
		 */
		delegate: undefined,

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		actionColumns: [],

		/**
		 * @cfg {Array}
		 */
		additionalColumns: [],

		/**
		 * @cfg {Boolean}
		 */
		enableCellEditing: false,

		/**
		 * @cfg {Boolean}
		 */
		enableRowAdd: false,

		/**
		 * @cfg {Boolean}
		 */
		enableRowDelete: false,

		/**
		 * @cfg {String}
		 */
		keyAttributeName: CMDBuild.core.constants.Proxy.KEY,

		/**
		 * @cfg {Object}
		 */
		keyEditor: { xtype: 'textfield' },

		/**
		 * @cfg {String}
		 */
		keyLabel: CMDBuild.Translation.key,

		/**
		 * @cfg {String}
		 */
		modelName: 'CMDBuild.model.common.field.grid.KeyValue',

		/**
		 * @cfg {Object}
		 *
		 * @private
		 */
		pluginCellEditing: undefined,

		/**
		 * @cfg {String}
		 */
		valueAttributeName: CMDBuild.core.constants.Proxy.VALUE,

		/**
		 * @cfg {Object}
		 */
		valueEditor: { xtype: 'textfield' },

		/**
		 * @cfg {String}
		 */
		valueLabel: CMDBuild.Translation.value,

		enablePanelFunctions: true,
		flex: 1,
		frame: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.controller.common.field.grid.KeyValue', { view: this });

			if (this.enableCellEditing)
				Ext.apply(this, {
					plugins: [
						this.pluginCellEditing = Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })
					]
				});

			if (this.enableRowAdd)
				Ext.apply(this, {
					dockedItems: [
						Ext.create('Ext.toolbar.Toolbar', {
							dock: 'top',
							itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

							items: [
								Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
									scope: this,

									handler: function (button, e) {
										this.delegate.cmfg('onFieldGridKeyValueAddButtonClick');
									}
								})
							]
						})
					]
				});

			Ext.apply(this, {
				columns: this.getColumns(),
				store: Ext.create('Ext.data.ArrayStore', {
					model: this.modelName,
					data: [],
					sorters: [
						{ property: this.keyAttributeName, direction: 'ASC' }
					]
				})
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Array}
		 */
		getColumns: function () {
			return this.delegate.cmfg('fieldGridKeyValueColumnsGet');
		},

		/**
		 * @param {Boolean} enableValidation
		 *
		 * @returns {Object}
		 */
		getValue: function (enableValidation) {
			enableValidation = Ext.isBoolean(enableValidation) ? enableValidation : true;

			return this.delegate.cmfg('fieldGridKeyValueValueGet', enableValidation);
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return true;
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('fieldGridKeyValueReset');
		},

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			this.delegate.cmfg('fieldGridKeyValueValueSet', value);
		}
	});

})();
