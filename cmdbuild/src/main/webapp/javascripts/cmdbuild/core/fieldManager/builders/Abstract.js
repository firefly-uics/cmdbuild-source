(function () {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.Abstract', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Number}
		 */
		headerWidth: undefined,

		/**
		 * @property {String}
		 */
		mandatoryLabelFlag: '* ',

		/**
		 * @param {String} string
		 *
		 * @returns {String or Mixed}
		 */
		applyMandatoryLabelFlag: function(string) {
			if (Ext.isString(string) && this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY))
				return this.mandatoryLabelFlag + string;

			return string;
		},

		/**
		 * @abstract
		 */
		buildColumn: Ext.emptyFn,

		/**
		 * @abstract
		 */
		buildEditor: Ext.emptyFn,

		/**
		 * @abstract
		 */
		buildField: Ext.emptyFn,

		/**
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return { name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'string' };
		},

		/**
		 * Column default renderer to apply cell class for required columns
		 *
		 * @param {Object} value
		 * @param {Object} metadata
		 * @param {Ext.data.Model} record
		 * @param {Number} rowIndex
		 * @param {Number} colIndex
		 * @param {Ext.data.Store} store
		 * @param {Ext.view.View} view
		 */
		rendererColumn: function(value, metadata, record, rowIndex, colIndex, store, view) {
			if (Ext.isEmpty(Ext.String.trim(String(value))) && this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY))
				metadata.tdCls += ' x-grid-invalid-cell-error';

			return value;
		}
	});

})();