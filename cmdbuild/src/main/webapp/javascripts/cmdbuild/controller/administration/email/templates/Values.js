(function () {

	Ext.define('CMDBuild.controller.administration.email.templates.Values', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Templates}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onValuesWindowAbortButtonClick',
			'onValuesWindowSaveButtonClick'
		],

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.templates.ValuesWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.email.templates.Templates} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.templates.ValuesWindow', {
				delegate: this
			});

			// Shorthands
			this.grid = this.view.grid;

			// Show window
			if (!Ext.isEmpty(this.view)) {
				this.storeDataSet();

				this.view.show();
			}
		},

		onValuesWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onValuesWindowSaveButtonClick: function() {
			this.cmfg('valuesDataSet', this.storeDataGet());

			this.onValuesWindowAbortButtonClick();
		},

		/**
		 * @return {Object} data
		 *
		 * 	Example:
		 * 		{
		 * 			key1: value1,
		 * 			key2: value2,
		 * 			...
		 * 		}
		 */
		storeDataGet: function() {
			var data = {};

			// To validate and filter grid rows
			this.grid.getStore().each(function(record) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.KEY))
					&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE))
				) {
					data[record.get(CMDBuild.core.proxy.CMProxyConstants.KEY)] = record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE);
				}
			});

			return data;
		},

		/**
		 * Rewrite of loadData
		 */
		storeDataSet: function() {
			var data = this.cmfg('valuesDataGet');
			var store = this.grid.getStore();
			store.removeAll();

			if (!Ext.isEmpty(data))
				Ext.Object.each(data, function(key, value, myself) {
					var recordConf = {};
					recordConf[CMDBuild.core.proxy.CMProxyConstants.KEY] = key;
					recordConf[CMDBuild.core.proxy.CMProxyConstants.VALUE] = value || '';

					store.add(recordConf);
				}, this);
		}
	});

})();