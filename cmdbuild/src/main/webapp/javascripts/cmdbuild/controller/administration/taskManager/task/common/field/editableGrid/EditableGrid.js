(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.common.field.editableGrid.EditableGrid', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerCommonFieldEditableGridAddButtonClick',
			'onTaskManagerCommonFieldEditableGridBeforeEdit',
			'onTaskManagerCommonFieldEditableGridRemoveButtonClick',
			'taskManagerCommonFieldEditableGridColumnsBuild',
			'taskManagerCommonFieldEditableGridCompleteEdit',
			'taskManagerCommonFieldEditableGridIsValid',
			'taskManagerCommonFieldEditableGridReset',
			'taskManagerCommonFieldEditableGridStoreBuild',
			'taskManagerCommonFieldEditableGridStoreGet',
			'taskManagerCommonFieldEditableGridValueGet',
			'taskManagerCommonFieldEditableGridValueSet'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.cron.CronView}
		 */
		view: undefined,

		/**
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldEditableGridAddButtonClick: function () {
			this.cmfg('taskManagerCommonFieldEditableGridStoreGet').insert(0, Ext.create(this.view.storeModelName));
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.columnName
		 * @param {Ext.data.Model} parameters.record
		 *
		 * @returns {Boolean}
		 */
		onTaskManagerCommonFieldEditableGridBeforeEdit: function (parameters) {
			if (this.view.enableFireEventBeforeEdit)
				return this.cmfg('onTaskManagerCommonFieldEditableBeforeEdit', parameters);
		},

		/**
		 * @returns {Boolean}
		 */
		taskManagerCommonFieldEditableGridIsValid: function () {
			var isValid = true;

			if (!this.view.allowBlank) {
				isValid = !Ext.isEmpty(this.cmfg('taskManagerCommonFieldEditableGridValueGet'));

				if (isValid) {
					this.view.fieldGrid.removeBodyCls('x-grid-invalid');
				} else {
					this.view.fieldGrid.addBodyCls('x-grid-invalid');
				}
			}

			return isValid;
		},

		/**
		 * @param {Ext.data.Model} record
		 *
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldEditableGridRemoveButtonClick: function (record) {
			if (Ext.isObject(record) && !Ext.Object.isEmpty(record)) {
				this.cmfg('taskManagerCommonFieldEditableGridStoreGet').remove(record);

				if (this.view.enableFireEventRowRemove)
					this.cmfg('onTaskManagerCommonFieldEditableRowRemove');
			}
		},

		/**
		 * @returns {Array}
		 */
		taskManagerCommonFieldEditableGridColumnsBuild: function () {
			return Ext.Array.push(this.view.columns, [
				Ext.create('Ext.grid.column.Action', {
					align: 'center',
					width: 30,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,

					items: [
						Ext.create('CMDBuild.core.buttons.iconized.Remove', {
							withSpacer: true,
							tooltip: CMDBuild.Translation.remove,
							scope: this,

							handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.cmfg('onTaskManagerCommonFieldEditableGridRemoveButtonClick', record);
							}
						})
					]
				})
			]);
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldEditableGridCompleteEdit: function () {
			this.view.pluginGridCellEditor.completeEdit();
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldEditableGridReset: function () {
			this.cmfg('taskManagerCommonFieldEditableGridStoreGet').removeAll();
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		taskManagerCommonFieldEditableGridStoreBuild: function () {
			return Ext.create('Ext.data.Store', {
				model: this.view.storeModelName,
				data: []
			});
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		taskManagerCommonFieldEditableGridStoreGet: function () {
			return this.view.fieldGrid.getStore();
		},

		/**
		 * @returns {Array} data
		 */
		taskManagerCommonFieldEditableGridValueGet: function () {
			var data = [];

			// To validate and filter grid rows
			this.cmfg('taskManagerCommonFieldEditableGridStoreGet').each(function (record) {
				if (record.isValid())
					data.push(record.getData());
			}, this);

			return data;
		},

		/**
		 * @param {Array} value
		 *
		 * @returns {Void}
		 */
		taskManagerCommonFieldEditableGridValueSet: function (value) {
			if (Ext.isArray(value) && !Ext.isEmpty(value))
				this.cmfg('taskManagerCommonFieldEditableGridStoreGet').loadData(value);
		}
	});

})();
