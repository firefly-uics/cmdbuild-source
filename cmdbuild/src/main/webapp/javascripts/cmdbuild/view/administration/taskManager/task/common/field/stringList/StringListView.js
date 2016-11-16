(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.stringList.StringListView', {
		extend: 'Ext.form.FieldContainer',

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.common.field.stringList.StringList}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.stringList.TextArea}
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
			this.delegate = Ext.create('CMDBuild.controller.administration.taskManager.task.common.field.stringList.StringList', { view: this });

			Ext.apply(this, {
				items: [
					this.textarea = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.stringList.TextArea', {
						delegate: this.delegate,
						name: this.name,
						flex: 1,
						readOnly: true,
						disablePanelFunctions: true,
						submitValue: false
					}),
					Ext.create('CMDBuild.core.buttons.icon.modify.Table', {
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
			return this.delegate.cmfg('taskManagerCommonFieldStringListValueSet', value);
		}
	});

})();
