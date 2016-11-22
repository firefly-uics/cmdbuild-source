(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.common.field.workflow.Workflow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.common.field.Workflow'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Ext.data.Store}
		 */
		attributesStore: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerCommonFieldWorkflowComboSelect',
			'onTaskManagerCommonFieldWorkflowGridAddButtonClick',
			'onTaskManagerCommonFieldWorkflowGridAttributeSelect',
			'onTaskManagerCommonFieldWorkflowGridBeforeEdit',
			'onTaskManagerCommonFieldWorkflowGridRemoveButtonClick',
			'taskManagerCommonFieldWorkflowGridDisabledSet',
			'taskManagerCommonFieldWorkflowGridEnable',
			'taskManagerCommonFieldWorkflowGridReset',
			'taskManagerCommonFieldWorkflowGridValueGet',
			'taskManagerCommonFieldWorkflowGridValueSet',
			'taskManagerCommonFieldWorkflowIsValid',
			'taskManagerCommonFieldWorkflowReset',
			'taskManagerCommonFieldWorkflowValueSet'
		],

		/**
		 * @cfg {CMDBuild.view.administration.taskManager.task.common.field.workflow.WorkflowView}
		 */
		view: undefined,

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {String} parameters.className
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildStoreAttributes: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.scope = Ext.isObject(parameters.scope) ? parameters.scope : this;

			if (Ext.isString(parameters.className) && !Ext.isEmpty(parameters.className)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = parameters.className;

				CMDBuild.proxy.administration.taskManager.task.common.field.Workflow.readAllAttributes({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var storeData = [];

							Ext.Array.forEach(decodedResponse, function (attributeObject, i, allAttributeObjects) {
								if (
										Ext.isObject(attributeObject) && !Ext.Object.isEmpty(attributeObject)
										&& !Ext.isEmpty(attributeObject[CMDBuild.core.constants.Proxy.NAME])
								) {
									storeData.push([attributeObject[CMDBuild.core.constants.Proxy.NAME]]);
								}
							}, this);

							this.attributesStore = Ext.create('Ext.data.ArrayStore', {
								fields: [CMDBuild.core.constants.Proxy.VALUE],
								data: storeData,
								sorters: [
									{ property: CMDBuild.core.constants.Proxy.VALUE, direction: 'ASC' }
								]
							});

							if (Ext.isFunction(parameters.callback))
								Ext.callback(parameters.callback, parameters.scope);
						} else {
							this.attributesStore = null;

							this.view.fieldGrid.getStore().removeAll();
							this.view.fieldGrid.disable();
						}
					}
				});
			}
		},

		/**
		 * Builds attributes store and clean grid store
		 *
		 * @param {String} className
		 *
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldWorkflowComboSelect: function () {
			this.buildStoreAttributes({
				className: this.view.fieldCombo.getValue(),
				scope: this,
				callback: function () {
					this.view.fieldGrid.getStore().removeAll();
					this.view.fieldGrid.getStore().insert(0, Ext.create('CMDBuild.model.administration.taskManager.task.common.field.workflow.Grid'));

					this.view.fieldGrid.pluginGridEditor.startEditByPosition({ row: 0, column: 0 });

					this.view.fieldGrid.enable();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldWorkflowGridAddButtonClick: function() {
			this.view.fieldGrid.getStore().insert(0, Ext.create('CMDBuild.model.administration.taskManager.task.common.field.workflow.Grid'));
		},

		/**
		 * @param {Number} rowIndex
		 *
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldWorkflowGridAttributeSelect: function (rowIndex) {
			this.view.fieldGrid.pluginGridEditor.startEditByPosition({ row: rowIndex, column: 1 });
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.name
		 * @param {Number} parameters.rowIndex
		 *
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldWorkflowGridBeforeEdit: function (parameters) {
			// Error handling
				if (!Ext.isNumber(parameters.rowIndex) || Ext.isEmpty(parameters.rowIndex))
					return _error('onTaskManagerCommonFieldWorkflowGridBeforeEdit(): unmanaged rowIndex parameter', this, parameters.rowIndex);
			// END: Error handling

			switch (parameters.name) {
				case CMDBuild.core.constants.Proxy.NAME: {
					if (Ext.isEmpty(this.view.fieldCombo.getValue()))
						return this.cmfg('taskManagerCommonFieldWorkflowGridDisabledSet', true);

					if (Ext.isObject(this.attributesStore) && !Ext.Object.isEmpty(this.attributesStore))
						this.view.fieldGrid.columns[0].setEditor({
							xtype: 'combo',
							valueField: CMDBuild.core.constants.Proxy.VALUE,
							displayField: CMDBuild.core.constants.Proxy.VALUE,
							forceSelection: true,
							allowBlank: false,

							store: this.attributesStore,
							queryMode: 'local',

							listeners: {
								scope: this,
								select: function (field, records, eOpts) {
									this.cmfg('onTaskManagerCommonFieldWorkflowGridAttributeSelect', parameters.rowIndex);
								}
							}
						});
				} break;
			}
		},

		/**
		 * @param {CMDBuild.model.administration.taskManager.task.common.field.workflow.Grid} record
		 *
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldWorkflowGridRemoveButtonClick: function (record) {
			// Error handling
				if (!Ext.isObject(record) || Ext.Object.isEmpty(record))
					return _error('onTaskManagerCommonFieldWorkflowGridRemoveButtonClick(): unmanaged record parameter', this, record);
			// END: Error handling

			this.view.fieldGrid.getStore().remove(record);
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		taskManagerCommonFieldWorkflowGridDisabledSet: function (state) {
			state = Ext.isBoolean(state) ? state : true;

			if (this.view.fieldGrid.rendered) // Avoid error while disable not already rendered item
				this.view.fieldGrid.setDisabled(state);
		},

		/**
		 * Just enable state change action
		 *
		 * @returns {Void}
		 */
		taskManagerCommonFieldWorkflowGridEnable: function () {
			return !Ext.isEmpty(this.view.fieldCombo.getValue());
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldWorkflowGridReset: function () {
			this.view.fieldGrid.getStore().removeAll();
		},

		/**
		 * @returns {Object} data
		 *
		 * 	Example:
		 * 		{
		 * 			name1: value1,
		 * 			name2: value2
		 * 		}
		 */
		taskManagerCommonFieldWorkflowGridValueGet: function () {
			var data = {};

			// To validate and filter grid rows
			this.view.fieldGrid.getStore().each(function (record) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.NAME))
					&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE))
				) {
					data[record.get(CMDBuild.core.constants.Proxy.NAME)] = record.get(CMDBuild.core.constants.Proxy.VALUE);
				}
			}, this);

			return data;
		},

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		taskManagerCommonFieldWorkflowGridValueSet: function (values) {
			var storeData = [];

			this.view.fieldGrid.getStore().removeAll();

			if (Ext.isObject(values) && !Ext.Object.isEmpty(values)) {
				Ext.Object.each(values, function (key, value, myself) {
					var record = {};

					record[CMDBuild.core.constants.Proxy.NAME] = key;
					record[CMDBuild.core.constants.Proxy.VALUE] = value;

					storeData.push(record);
				}, this);

				if (!Ext.isEmpty(storeData))
					this.view.fieldGrid.getStore().add(storeData);
			}
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldWorkflowReset: function () {
			this.view.fieldCombo.reset();

			this.cmfg('taskManagerCommonFieldWorkflowGridDisabledSet', true);
			this.cmfg('taskManagerCommonFieldWorkflowGridReset');
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Boolean}
		 */
		taskManagerCommonFieldWorkflowIsValid: function () {
			return this.view.fieldCombo.isValid();
		},

		/**
		 * Set field value without cleaning grid
		 *
		 * @param {String} value
		 *
		 * @returns {Void}
		 */
		taskManagerCommonFieldWorkflowValueSet: function (value) {
			this.buildStoreAttributes({
				className: value,
				scope: this,
				callback: function () {
					this.view.fieldCombo.setValue(value);
				}
			});
		}
	});

})();
