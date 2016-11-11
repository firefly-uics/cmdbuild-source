(function () {

	Ext.require([
		'CMDBuild.core.constants.FieldWidths',
		'CMDBuild.core.constants.Proxy'
	]);

	Ext.define('CMDBuild.view.common.field.filter.advanced.configurator.ConfiguratorView', {
		extend: 'Ext.form.FieldContainer',

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.configurator.Configurator}
		 */
		delegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		isAdministration: false,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		border: false,
		frame: false,
		layout: 'fit',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.tabPanel = Ext.create('Ext.tab.Panel', {
						border: this.border,
						frame: this.frame,

						items: []
					})
				]
			});

			this.delegate = Ext.create('CMDBuild.controller.common.field.filter.advanced.configurator.Configurator', { view: this });

			this.callParent(arguments);
		},

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 * @param {String} configuration.className
		 * @param {Array} configuration.disabledPanels - ex. ['attributes', 'functions', 'relations']
		 * @param {Object} configuration.filter
		 * @param {Object} configuration.scope
		 *
		 * @returns {Void}
		 */
		configure: function (configuration) {
			this.delegate.cmfg('fieldFilterAdvancedConfiguratorConfigure', configuration);
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		disable: Ext.emptyFn,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		enable: Ext.emptyFn,

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		getValue: function (value) {
			return this.delegate.cmfg('fieldFilterAdvancedConfiguratorValueGet', value);
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
			this.delegate.cmfg('fieldFilterAdvancedConfiguratorReset');
		},

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			this.delegate.cmfg('fieldFilterAdvancedConfiguratorValueSet', value);
		}
	});

})();
