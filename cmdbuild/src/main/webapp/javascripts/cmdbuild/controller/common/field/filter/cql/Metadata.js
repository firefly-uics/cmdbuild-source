(function() {

	Ext.define('CMDBuild.controller.common.field.filter.cql.Metadata', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.cql.Cql}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onMetadataWindowAbortButtonClick',
			'onMetadataWindowSaveButtonClick',
			'onMetadataWindowShow'
		],

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.cql.MetadataWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.cql.Cql} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.cql.MetadataWindow', {
				delegate: this
			});

			// Shorthands
			this.grid = this.view.grid;
		},

		onMetadataWindowAbortButtonClick: function() {
			this.view.hide();
		},

		onMetadataWindowSaveButtonClick: function() {
			var gridData = {};

			this.grid.getStore().each(function(record) {
				gridData[record.get(CMDBuild.core.proxy.Constants.KEY)] = record.get(CMDBuild.core.proxy.Constants.VALUE);
			}, this);

			this.cmfg('fieldFilterCqlFilterSet', {
				filterObject: gridData,
				propertyName: CMDBuild.core.proxy.Constants.CONTEXT
			});

			this.onMetadataWindowAbortButtonClick();
		},

		/**
		 * Loads data object in store
		 */
		onMetadataWindowShow: function() {
			if (!this.cmfg('fieldFilterCqlFilterIsAttributeEmpty', CMDBuild.core.proxy.Constants.CONTEXT)) {
				this.grid.getStore().removeAll();

				Ext.Object.each(this.cmfg('fieldFilterCqlFilterGet', CMDBuild.core.proxy.Constants.CONTEXT), function(key, value, myself) {
					var recordConf = {};
					recordConf[CMDBuild.core.proxy.Constants.KEY] = key;
					recordConf[CMDBuild.core.proxy.Constants.VALUE] = value;

					this.grid.getStore().add(recordConf);
				}, this);
			}
		},

		/**
		 * Forward method
		 */
		show: function() {
			if (!Ext.isEmpty(this.view))
				this.view.show();
		}
	});

})();