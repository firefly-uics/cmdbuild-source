(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.stringList.TextArea', {
		extend: 'Ext.form.field.TextArea',

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.common.field.stringList.StringList}
		 */
		delegate: undefined,

		/**
		 * @param {Array or String} value
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		setValue: function (value) {
			if (Ext.isString(value) || Ext.isEmpty(value))
				return this.callParent(arguments);

			return this.delegate.cmfg('taskManagerCommonFieldStringListValueSet', value);
		}
	});

})();
