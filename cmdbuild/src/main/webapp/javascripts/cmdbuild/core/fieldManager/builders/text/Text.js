(function () {

	/**
	 * Specific field attributes:
	 * 		- {String} editorType: PLAIN or HTML
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.text.Text', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 *
		 * @override
		 */
		buildColumn: function (parameters) {
			return this.buildSubFieldClass().buildColumn(parameters);
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildEditor: function () {
			return this.buildSubFieldClass().buildEditor();
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildField: function () {
			return this.buildSubFieldClass().buildField();
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildFieldReadOnly: function () {
			return this.buildSubFieldClass().buildFieldReadOnly();
		},

		/**
		 * @returns {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView}
		 *
		 * @override
		 */
		buildFilterCondition: function () {
			return this.buildSubFieldClass().buildFilterCondition();
		},

		/**
		 * @returns {Object}
		 *
		 * @private
		 */
		buildSubFieldClass: function () {
			switch (this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.EDITOR_TYPE)) {
				case 'HTML':
					return Ext.create('CMDBuild.core.fieldManager.builders.text.HtmlEditor', { parentDelegate: this });

				case 'PLAIN':
				default:
					return Ext.create('CMDBuild.core.fieldManager.builders.text.Plain', { parentDelegate: this });
			}
		}
	});

})();
