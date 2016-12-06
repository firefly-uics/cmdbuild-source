(function () {

	Ext.require([
		'CMDBuild.core.constants.FieldWidths',
		'CMDBuild.core.constants.Proxy'
	]);

	/**
	 * Events to manage externally:
	 * 	- onTaskManagerCommonFieldEditableBeforeEdit
	 *  - onTaskManagerCommonFieldEditableRowRemove
	 *
	 * NOTE: model isValid method is used to validate grid rows
	 */
	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.editableGrid.EditableGridView', {
		extend: 'Ext.form.FieldContainer',

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.common.field.workflow.Workflow}
		 */
		delegate: undefined,

		/**
		 * @property {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		allowBlank: true,

		/**
		 * @cfg {Array}
		 */
		columns: [],

		/**
		 * @cfg {Boolean}
		 */
		enableFireEventBeforeEdit: false,

		/**
		 * @cfg {Boolean}
		 */
		enableFireEventRowRemove: false,

		/**
		 * @property {Ext.grid.Panel}
		 */
		fieldGrid: undefined,

		/**
		 * @cfg {String}
		 */
		fieldLabel: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		pluginGridCellEditor: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: [],

		border: false,
		frame: false,
		hideLabel: true,
		layout: 'fit',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.controller.administration.taskManager.task.common.field.editableGrid.EditableGrid', {
				parentDelegate: this.parentDelegate,
				view: this
			});

			Ext.apply(this, {
				items: [
					this.fieldGrid = Ext.create('Ext.grid.Panel', {
						delegate: this.delegate,
						border: true,
						dnablePanelFunctions: true,
						frame: false,
						title: this.fieldLabel,

						columns: this.delegate.cmfg('taskManagerCommonFieldEditableGridColumnsBuild'),
						dockedItems: [
							Ext.create('Ext.toolbar.Toolbar', {
								dock: 'top',
								itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

								items: [
									Ext.create('CMDBuild.core.buttons.icon.add.Add', {
										scope: this,

										handler: function (button, e) {
											this.delegate.cmfg('onTaskManagerCommonFieldEditableGridAddButtonClick');
										}
									})
								]
							})
						],
						store: this.delegate.cmfg('taskManagerCommonFieldEditableGridStoreBuild'),
						plugins: [
							this.pluginGridCellEditor = Ext.create('Ext.grid.plugin.CellEditing', {
								clicksToEdit: 1,

								listeners: {
									scope: this,
									beforeedit: function (editor, e, eOpts) {
										return this.delegate.cmfg('onTaskManagerCommonFieldEditableGridBeforeEdit', {
											columnName: e.field,
											record: e.record
										});
									}
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Void}
		 */
		completeEdit: function () {
			this.delegate.cmfg('taskManagerCommonFieldEditableGridCompleteEdit');
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		getStore: function () {
			return this.delegate.cmfg('taskManagerCommonFieldEditableGridStoreGet');
		},

		/**
		 * @returns {Object}
		 */
		getValue: function (value) {
			return this.delegate.cmfg('taskManagerCommonFieldEditableGridValueGet');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('taskManagerCommonFieldEditableGridIsValid');
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('taskManagerCommonFieldEditableGridReset');
		},

		/**
		 * @param {Array or String} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			return this.delegate.cmfg('taskManagerCommonFieldEditableGridValueSet', value);
		}
	});

})();
