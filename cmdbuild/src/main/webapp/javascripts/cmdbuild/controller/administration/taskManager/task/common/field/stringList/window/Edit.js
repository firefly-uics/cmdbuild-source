(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.common.field.stringList.window.Edit', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.common.field.stringList.StringList}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerCommonFieldStringListWindowAbortButtonClick',
			'onTaskManagerCommonFieldStringListWindowAddButtonClick',
			'onTaskManagerCommonFieldStringListWindowConfirmButtonClick',
			'taskManagerCommonFieldStringListWindowShow'
		],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		valueBuffer: [],

		/**
		 * @cfg {CMDBuild.view.administration.taskManager.task.common.field.stringList.window.EditWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.common.field.stringList.StringList} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.stringList.window.EditWindow', { delegate: this });
		},

		/**
		 * Adds at least one empty row if values is empty
		 *
		 * @param {Array} values
		 *
		 * @return {Array} items
		 *
		 * @private
		 */
		buildEditRow: function (values) {
			values = Ext.isArray(values) && !Ext.isEmpty(values) ? values : [''];

			var items = [];

			Ext.Array.forEach(values, function (value, i, allValues) {
				items.push(
					Ext.create('Ext.container.Container', {
						border: false,
						frame: false,

						layout: {
							type: 'hbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.field.Text', {
								value: value,
								flex: 1,

								listeners: {
									scope: this,
									change: function (field, newValue, oldValue, eOpts) {
										this.cmfg('taskManagerCommonFieldStringListValueSet', this.getValues());
									}
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								tooltip: CMDBuild.Translation.remove,
								scope: this,

								handler: function (button, e) {
									this.view.formPanel.remove(button.up('container'), false); // Remove input's panel container from form

									this.cmfg('taskManagerCommonFieldStringListValueSet', this.getValues());
								}
							})
						]
					})
				);
			}, this);

			return items;
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Object}
		 *
		 * @private
		 */
		getValues: function () {
			return this.view.formPanel.getForm().getValues();
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldStringListWindowAbortButtonClick: function () {
			this.cmfg('taskManagerCommonFieldStringListValueSet', this.valueBuffer);

			this.valueBuffer = [];

			this.view.close();
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldStringListWindowAddButtonClick: function () {
			this.view.formPanel.add(this.buildEditRow());
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldStringListWindowConfirmButtonClick: function () {
			this.view.close();
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldStringListWindowShow: function () {
			// Error handling
				if (!Ext.isObject(this.view) || Ext.Object.isEmpty(this.view))
					return _error('onTaskManagerCommonFieldStringListWindowConfigureAndShow(): unmanaged view property', this, this.view);
			// END: Error handling

			this.valueBuffer = this.cmfg('taskManagerCommonFieldStringListValueGet');

			this.view.setTitle(this.cmfg('taskManagerCommonFieldStringListFieldLabelGet'));

			// Build content items
			this.view.formPanel.removeAll(false);
			this.view.formPanel.add(
				this.buildEditRow(
					Ext.isEmpty(this.valueBuffer) ? null : this.valueBuffer
				)
			);

			this.view.show();
		}
	});

})();
