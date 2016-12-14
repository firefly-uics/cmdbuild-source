(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.common.field.stringList.StringList', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerCommonFieldStringListModifyButtonClick',
			'taskManagerCommonFieldStringListFieldLabelGet',
			'taskManagerCommonFieldStringListIsValid',
			'taskManagerCommonFieldStringListReset',
			'taskManagerCommonFieldStringListSeparatorGet',
			'taskManagerCommonFieldStringListValueGet',
			'taskManagerCommonFieldStringListValueSet'
		],

		/**
		 * @property {String}
		 *
		 * @private
		 */
		separator: ' OR ',

		/**
		 * @cfg {CMDBuild.view.administration.taskManager.task.common.field.stringList.StringListView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.administration.taskManager.task.common.field.stringList.StringListView} configurationObject.view
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Build sub controllers
			this.controllerWindow = Ext.create('CMDBuild.controller.administration.taskManager.task.common.field.stringList.window.Edit', { parentDelegate: this });
		},

		/**
		 * Concatenate array values to string using separator
		 *
		 * @param {Array} values
		 *
		 * @return {String}
		 *
		 * @private
		 */
		encodeArray: function (values) {
			if (Ext.isArray(values) && !Ext.isEmpty(values)) {
				values = Ext.Array.clean(values);
				values = Ext.Array.unique(values);

				if (Ext.isArray(values) && !Ext.isEmpty(values))
					return values.join(this.cmfg('taskManagerCommonFieldStringListSeparatorGet'));
			}

			return '';
		},

		/**
		 * Concatenate object values to string using separator
		 *
		 * @param {Object} values
		 *
		 * @return {String}
		 *
		 * @private
		 */
		encodeObject: function (values) {
			if (Ext.isObject(values) && !Ext.Object.isEmpty(values)) {
				values = Ext.Object.getValues(values);
				values = Ext.Array.clean(values);
				values = Ext.Array.unique(values);

				if (Ext.isArray(values) && !Ext.isEmpty(values))
					return values.join(this.cmfg('taskManagerCommonFieldStringListSeparatorGet'));
			}

			return '';
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldStringListModifyButtonClick: function () {
			parameters = Ext.isObject(parameters) ? parameters : {};

			this.controllerWindow.cmfg('taskManagerCommonFieldStringListWindowShow');
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldStringListFieldLabelGet: function () {
			return this.view.fieldLabel;
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Boolean}
		 */
		taskManagerCommonFieldStringListIsValid: function () {
			return this.view.textarea.isValid();
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldStringListReset: function () {
			return this.view.textarea.reset();
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldStringListSeparatorGet: function () {
			return this.separator;
		},

		/**
		 * @returns {Array}
		 */
		taskManagerCommonFieldStringListValueGet: function () {
			var value = this.view.textarea.getValue();

			if (Ext.isString(value) && !Ext.isEmpty(value))
				return value.split(this.cmfg('taskManagerCommonFieldStringListSeparatorGet'));

			return [];
		},

		/**
		 * @param {Array or Object or String} value
		 *
		 * @returns {Void}
		 */
		taskManagerCommonFieldStringListValueSet: function (value) {
			switch (Ext.typeOf(value)) {
				case 'array':
					return this.cmfg('taskManagerCommonFieldStringListValueSet', this.encodeArray(value));

				case 'object':
					return this.cmfg('taskManagerCommonFieldStringListValueSet', this.encodeObject(value));

				case 'string':
				default:
					return this.view.textarea.setValue(value);
			}
		}
	});

})();
