(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.common.field.report.Report', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.common.field.Report',
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * Selected report attributes
		 *
		 * @param {Object}
		 *
		 * @private
		 */
		attributes: {},

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerCommonFieldReportComboSelect',
			'onTaskManagerCommonFieldReportGridBeforeEdit',
			'onTaskManagerCommonFieldReportGridEditingModeCheckChange',
			'taskManagerCommonFieldReportDisable',
			'taskManagerCommonFieldReportEnable',
			'taskManagerCommonFieldReportGridIsValid',
			'taskManagerCommonFieldReportGridReset',
			'taskManagerCommonFieldReportGridValueGet',
			'taskManagerCommonFieldReportGridValueSet',
			'taskManagerCommonFieldReportIsValid',
			'taskManagerCommonFieldReportReset'
		],

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.report.ReportView}
		 */
		view: undefined,

		// Attributes property methods
			/**
			 * @param {String} name
			 *
			 * @returns {Object}
			 *
			 * @private
			 */
			attributesGet: function (name) {
				if (this.attributesExists(name))
					return this.attributes[name];

				return this.attributes;
			},

			/**
			 * @param {String} name
			 *
			 * @returns {Boolean}
			 *
			 * @private
			 */
			attributesExists: function (name) {
				if (Ext.isString(name) && !Ext.isEmpty(name))
					return !Ext.isEmpty(this.attributes[name]);

				return false;
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			attributesReset: function () {
				this.attributes = {};
			},

			/**
			 * @param {Array} attributes
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			attributesSet: function (attributes) {
				if (Ext.isArray(attributes) && !Ext.isEmpty(attributes))
					Ext.Array.each(attributes, function (attributeObject, i, allAttributeObject) {
						if (Ext.isObject(attributeObject) && !Ext.Object.isEmpty(attributeObject))
							this.attributes[attributeObject[CMDBuild.core.constants.Proxy.NAME]] = attributeObject;
					}, this);
			},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		columnEditorSet: function (column, record) {
			// Error handling
				if (!Ext.isObject(column) || Ext.Object.isEmpty(column))
					return _warning('columnEditorSet(): unmanaged column parameter', this, column);

				if (!Ext.isObject(record) || Ext.Object.isEmpty(record))
					return _warning('columnEditorSet(): unmanaged record parameter', this, record);
			// END: Error handling

			if (record.get(CMDBuild.core.constants.Proxy.EDITING_MODE)) { // Default editor (text)
				column.setEditor({ xtype: 'textfield' });
			} else if (this.attributesExists(record.get(CMDBuild.core.constants.Proxy.NAME))) { // Custom attribute editor
				var attribute = this.attributesGet(record.get(CMDBuild.core.constants.Proxy.NAME)),
					fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				if (fieldManager.isAttributeManaged(attribute[CMDBuild.core.constants.Proxy.TYPE])) {
					var attributeCustom = Ext.create('CMDBuild.model.administration.taskManager.task.generic.Attribute', attribute);
					attributeCustom.setAdaptedData(attribute);

					fieldManager.attributeModelSet(attributeCustom);

					column.setEditor(fieldManager.buildEditor());
				} else { // @deprecated - Old field manager
					var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(attribute);

					if (!Ext.isEmpty(editor)) {
						if (attribute.defaultvalue)
							editor.setValue(attribute.defaultvalue);

						column.setEditor(editor);
					}
				}
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.merge
		 * @param {CMDBuild.model.administration.taskManager.task.common.field.report.Report} parameters.record
		 *
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldReportComboSelect: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.merge = Ext.isBoolean(parameters.merge) ? parameters.merge : false;

			this.attributesReset();

			if (Ext.isObject(parameters.record) && !Ext.Object.isEmpty(parameters.record)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.EXTENSION] = this.view.extension.getValue();
				params[CMDBuild.core.constants.Proxy.ID] = parameters.record.get(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.TYPE] = 'CUSTOM';

				CMDBuild.proxy.administration.taskManager.task.common.field.Report.readParameters({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTE];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							this.attributesSet(decodedResponse);

							this.taskManagerCommonFieldReportGridFill(parameters.merge);

							this.view.grid.enable();
						} else { // Clear and disable grid if no attributes
							this.cmfg('taskManagerCommonFieldReportGridReset');

							this.view.grid.disable();
						}
					}
				});
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Object} parameters.column
		 * @param {String} parameters.columnName
		 * @param {CMDBuild.model.administration.taskManager.task.common.field.report.Grid} parameters.record
		 *
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldReportGridBeforeEdit: function (parameters) {
			switch (parameters.columnName) {
				case CMDBuild.core.constants.Proxy.VALUE:
					return this.columnEditorSet(parameters.column, parameters.record);
			}
		},

		/**
		 * @param {Number} rowIndex
		 *
		 * @returns {Void}
		 */
		onTaskManagerCommonFieldReportGridEditingModeCheckChange: function (rowIndex) {
			this.view.grid.cellEditingPlugin.completeEdit();

			if (!Ext.isEmpty(rowIndex)) {
				var record = this.view.grid.getStore().getAt(rowIndex);

				// Error handling
					if (!Ext.isObject(record) || Ext.Object.isEmpty(record))
						return _warning('onTaskManagerCommonFieldReportGridEditingModeCheckChange(): row not found', this, rowIndex);
				// END: Error handling

				record.set(CMDBuild.core.constants.Proxy.VALUE, '');
				record.commit();
			}
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldReportDisable: function () {
			this.view.combo.disable();
			this.view.extension.disable();
			this.view.grid.disable();
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldReportEnable: function () {
			if (this.view.combo.rendered) {
				this.view.combo.enable();
				this.view.extension.enable();
				this.view.grid.setDisabled(Ext.isEmpty(this.view.combo.getValue()));
			}
		},

		/**
		 * @param {Boolean} merge
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		taskManagerCommonFieldReportGridFill: function (merge) {
			merge = Ext.isBoolean(merge) ? merge : false;

			var records = [];

			Ext.Object.each(this.attributesGet(), function (name, attribute, myself) {
				if (Ext.isObject(attribute) && !Ext.Object.isEmpty(attribute)) {
					var attributeModelObject = {};
					attributeModelObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = attribute[CMDBuild.core.constants.Proxy.DESCRIPTION];
					attributeModelObject[CMDBuild.core.constants.Proxy.NAME] = attribute[CMDBuild.core.constants.Proxy.NAME];

					if (merge) {
						var gridRecord =  this.view.grid.getStore().findRecord(CMDBuild.core.constants.Proxy.NAME, attribute[CMDBuild.core.constants.Proxy.NAME]);

						if (!Ext.isEmpty(gridRecord))
							attributeModelObject[CMDBuild.core.constants.Proxy.VALUE] = gridRecord.get(CMDBuild.core.constants.Proxy.VALUE);
					}

					records.push(Ext.create('CMDBuild.model.administration.taskManager.task.common.field.report.Grid', attributeModelObject));
				}
			}, this);

			this.view.grid.getStore().removeAll();

			if (!Ext.isEmpty(records))
				this.view.grid.getStore().add(records);
		},

		/**
		 * Check required field value of grid store records
		 *
		 * @returns {Boolean} isValid
		 */
		taskManagerCommonFieldReportGridIsValid: function () {
			var isValid = true,
				requiredAttributes = [];

			// Build required attributes names array
			Ext.Object.each(this.attributesGet(), function (name, attribute, myself) {
				if (attribute['isnotnull'])
					requiredAttributes.push(attribute[CMDBuild.core.constants.Proxy.NAME]);
			}, this);

			// Check grid store records empty required fields
			this.view.grid.getStore().each(function (record) {
				if (
					Ext.Array.contains(requiredAttributes, record.get(CMDBuild.core.constants.Proxy.NAME))
					&& Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE))
				) {
					isValid = false;

					return false;
				}
			}, this);

			if (!isValid)
				this.view.grid.addBodyCls('x-grid-invalid');

			return isValid;
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldReportGridReset: function () {
			this.view.grid.getStore().removeAll();
		},

		/**
		 * @returns {Object} data
		 */
		taskManagerCommonFieldReportGridValueGet: function () {
			var data = {};

			this.view.grid.getStore().each(function (record) {
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
		 * Preset value in grid store, on report selection grid data will be merged to attribute values
		 *
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		taskManagerCommonFieldReportGridValueSet: function (value) {
			var records = [];

			this.cmfg('taskManagerCommonFieldReportGridReset');

			if (Ext.isObject(value) && !Ext.Object.isEmpty(value))
				Ext.Object.each(value, function (key, value, myself) {
					if (!Ext.isEmpty(value)) {
						var recordConf = {};
						recordConf[CMDBuild.core.constants.Proxy.NAME] = key;
						recordConf[CMDBuild.core.constants.Proxy.VALUE] = value;

						records.push(recordConf);
					}
				}, this);

			if (!Ext.isEmpty(records))
				this.view.grid.getStore().add(records);
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldReportIsValid: function () {
			return (
				this.view.combo.isValid()
				&& this.view.extension.isValid()
				&& this.cmfg('taskManagerCommonFieldReportGridIsValid')
			);
		},

		/**
		 * @returns {Void}
		 */
		taskManagerCommonFieldReportReset: function () {
			this.view.combo.reset();
			this.view.extension.reset();

			this.cmfg('taskManagerCommonFieldReportGridReset');
			this.cmfg('taskManagerCommonFieldReportEnable');
		}
	});

})();
