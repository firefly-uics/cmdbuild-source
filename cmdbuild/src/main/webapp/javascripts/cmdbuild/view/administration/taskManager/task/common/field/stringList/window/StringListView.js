(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.stringList.StringListView', {
		extend: 'Ext.form.FieldContainer',

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.common.field.stringList.StringList}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.TextArea}
		 */
		textarea: undefined,

		border: false,
		frame: false,
		layout: 'hbox',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.administration.taskManager.task.common.field.stringList.StringList', { view: this }),

				items: [
					this.textarea = Ext.create('Ext.form.field.TextArea', {
						name: this.name,
						disablePanelFunctions: true,
						flex: 1,
						readOnly: true,
						submitValue: false,
						resizable: true
					}),
					Ext.create('CMDBuild.core.buttons.iconized.modify.Table', {
						tooltip: CMDBuild.Translation.modify + ' ' + this.fieldLabel.toLowerCase(),
						margin: '0 0 0 5',
						scope: this,

						handler: function (button, e) {
							this.delegate.cmfg('onTaskManagerCommonFieldStringListModifyButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {String}
		 */
		getValue: function () {
			return this.delegate.cmfg('taskManagerCommonFieldStringListValueGet');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('taskManagerCommonFieldStringListIsValid');
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('taskManagerCommonFieldStringListReset');
		},

		/**
		 * @param {Array or String} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
_debug('setValue', value);
			return this.delegate.cmfg('taskManagerCommonFieldStringListValueSet', value);
		}
	});

})();
