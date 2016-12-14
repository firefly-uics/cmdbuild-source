(function () {

	Ext.define('CMDBuild.controller.administration.taskManager.task.connector.Step5', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.Connector'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Connector}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTaskManagerFormTaskConnectorStep5BeforeEdit = onTaskManagerCommonFieldEditableBeforeEdit',
			'onTaskManagerFormTaskConnectorStep5Show',
			'onTaskManagerFormTaskConnectorStep5ValidateSetup = onTaskManagerFormTaskConnectorValidateSetup'
		],

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.connector.Step5View}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.taskManager.task.connector.Connector} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.taskManager.task.connector.Step5View', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
		},

		/**
		 * @param {String} className
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildEditorClassAttributes: function (className) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = className;

			if (Ext.isEmpty(className))
				return this.view.columnClassAttribute.setEditor({
					xtype: 'combo',
					disabled: true
				});

			var store = CMDBuild.proxy.administration.taskManager.task.Connector.getStoreAttributes();
			store.load({ params: params });

			return this.view.columnClassAttribute.setEditor({
				xtype: 'combo',
				displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				forceSelection: true,
				allowBlank: false,

				store: store,
				queryMode: 'local'
			});
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.columnName
		 * @param {CMDBuild.model.administration.taskManager.task.connector.MappingAttribute} parameters.record
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep5BeforeEdit: function (parameters) {
			var columnName = parameters.columnName,
				record = parameters.record;

			switch (columnName) {
				case CMDBuild.core.constants.Proxy.CLASS_ATTRIBUTE:
					return this.buildEditorClassAttributes(record.get(CMDBuild.core.constants.Proxy.CLASS_NAME));
			}
		},

		/**
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep5Show: function () {
			this.view.columnSourceName.setEditor({
				xtype: 'combo',
				displayField: CMDBuild.core.constants.Proxy.NAME,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				forceSelection: true,
				allowBlank: false,

				store: this.cmfg('taskManagerFormTaskConnectorExternalEntityStoreGet'),
				queryMode: 'local'
			});

			this.view.columnClassName.setEditor({
				xtype: 'combo',
				displayField: CMDBuild.core.constants.Proxy.NAME,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				forceSelection: true,
				allowBlank: false,

				store: this.cmfg('taskManagerFormTaskConnectorClassesStoreGet'),
				queryMode: 'local'
			});
		},

		/**
		 * @param {Boolean} fullValidation
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskConnectorStep5ValidateSetup: function (fullValidation) {
			fullValidation = Ext.isBoolean(fullValidation) ? fullValidation : false;

			this.grid.allowBlank = !fullValidation;
		}
	});

})();
