(function() {

	Ext.define('CMDBuild.view.common.field.translatable.window.FormPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.field.translatable.Window}
		 */
		delegate: undefined,

		/**
		 * @property {Object}
		 */
		oldValues: {},

		frame: true,
		border: false,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
			anchor: '100%'
		},

		initComponent: function() {
			var me = this;

			this.callParent(arguments);

			_CMCache.readTranslations(
				this.delegate.translationsKeyType,
				this.delegate.translationsKeyName,
				this.delegate.translationsKeySubName,
				this.delegate.translationsKeyField,
				function(result, options, decodedResult) {
					me.oldValues = decodedResult.response;

					me.delegate.buildWindowItem(decodedResult.response);
				}
			);
		},

		/**
		 * @return {Object}
		 */
		getOldValues: function() {
			return this.oldValues;
		}
	});

})();