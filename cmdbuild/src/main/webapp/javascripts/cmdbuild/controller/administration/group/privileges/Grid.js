(function() {

	Ext.define('CMDBuild.controller.administration.group.privileges.Grid', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.group.privileges.Classes',
			'CMDBuild.core.proxy.group.privileges.DataView',
			'CMDBuild.core.proxy.group.privileges.Filter'
		],

		mixins: ['CMDBuild.controller.common.field.filter.advanced.Advanced'], // Import fieldConfiguration, filter, selectedClass property methods

		/**
		 * @cfg {CMDBuild.controller.administration.group.privileges.Privileges}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		controllerFilterWindow: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.UiConfiguration}
		 */
		controllerUiConfiguration: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedConfigurationIsPanelEnabled',
			'fieldFilterAdvancedFilterGet',
			'fieldFilterAdvancedFilterIsEmpty',
			'fieldFilterAdvancedSelectedClassGet',
			'onFieldFilterAdvancedWindowgetEndpoint',
			'onGroupPrivilegesGridSetPrivilege',
			'onGroupPrivilegesGridTabShow',
			'onGroupPrivilegesRemoveFilterClick',
			'onGroupPrivilegesSetFilterClick',
			'onGroupPrivilegesUIConfigurationButtonClick'
		],

		/**
		 * @cfg {Boolean}
		 */
		enableCRUDRead: false,

		/**
		 * @cfg {Boolean}
		 */
		enableCRUDWrite: false,

		/**
		 * @cfg {Boolean}
		 */
		enablePrivilegesAndUi: false,

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.FieldConfiguration}
		 *
		 * @private
		 */
		fieldConfiguration: undefined,

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.Filter}
		 *
		 * @private
		 */
		filter: undefined,

		/**
		 * @cfg {Mixed}
		 */
		proxy: undefined,

		/**
		 * @cfg {String}
		 */
		title: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.group.privileges.GridPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.group.privileges.Privileges} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.group.privileges.GridPanel', {
				delegate: this,
				title: this.title,
				store: this.proxy.getStore(),
				enableCRUDRead: this.enableCRUDRead,
				enableCRUDWrite: this.enableCRUDWrite,
				enablePrivilegesAndUi: this.enablePrivilegesAndUi
			});

			// Filter advanced window configuration
			this.fieldFilterAdvancedConfigurationSet({ enabledPanels: ['attribute', 'relation', 'function', 'columnPrivileges'] });

			// Build sub controller
			this.controllerFilterWindow = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.Window', {
				parentDelegate: this,
				configuration: {
					mode: 'grid',
					tabs: {
						attributes: {
							selectAtRuntimeCheckDisabled: true // BUSINNESS RULE: user couldn't create privilege's filter with runtime parameters
						}
					}
				},
			});
			this.controllerUiConfiguration = Ext.create('CMDBuild.controller.administration.group.privileges.UiConfiguration', { parentDelegate: this });
		},

		/**
		 * @param {Object} resultObject
		 * @param {Object} resultObject.columnPrivileges
		 * @param {Object} resultObject.filter
		 *
		 * @override
		 */
		onFieldFilterAdvancedWindowgetEndpoint: function(resultObject) {
			if (Ext.encode(resultObject.filter).indexOf('"parameterType":"calculated"') < 0) {
				var params = {};
				params['privilegedObjectId'] = this.fieldFilterAdvancedSelectedClassGet(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(resultObject.columnPrivileges);
				params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(resultObject.filter);
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				this.proxy.setRowAndColumn({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.onGroupPrivilegesGridTabShow();
					}
				});
			} else {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.error,
					CMDBuild.Translation.warnings.itIsNotAllowedFilterWithCalculatedParams,
					false
				);
			}
		},

		onGroupPrivilegesGridTabShow: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

			this.view.getStore().load({
				params: params,
				scope: this,
				callback: function(records, operation, success) {
					// Store load errors manage
					if (!success) {
						CMDBuild.core.Message.error(null, {
							text: CMDBuild.Translation.errors.unknown_error,
							detail: operation.error
						});
					}
				}
			});
		},

		/**
		 * @param {Object} parameters
		 * @param {Number} parameters.rowIndex
		 * @param {String} parameters.privilege
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onGroupPrivilegesGridSetPrivilege: function(parameters) {
			if (Ext.isEmpty(this.proxy)) {
				_error('proxy object not defined', this);
			} else {
				var params = {};
				params['privilege_mode'] = parameters.privilege;
				params['privilegedObjectId'] = this.view.store.getAt(parameters.rowIndex).get(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				this.proxy.update({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.onGroupPrivilegesGridTabShow();
					}
				});
			}
		},

		/**
		 * @param {CMDBuild.model.group.privileges.GridRecord} record
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onGroupPrivilegesRemoveFilterClick: function(record) {
			Ext.Msg.show({
				title: CMDBuild.Translation.attention,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes') {
						var params = {};
						params['privilegedObjectId'] = record.get(CMDBuild.core.constants.Proxy.ID);
						params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

						// Set empty filter to clear value
						this.proxy.setRowAndColumn({
							params: params,
							scope: this,
							success: function(response, options, decodedResponse) {
								this.onGroupPrivilegesGridTabShow();
							}
						});
					}
				}
			});
		},

		/**
		 * @param {CMDBuild.model.group.privileges.GridRecord} record
		 */
		onGroupPrivilegesUIConfigurationButtonClick: function(record) {
			this.controllerUiConfiguration.setRecord(record);
			this.controllerUiConfiguration.getView().show();
		},

		/**
		 * @param {CMDBuild.model.group.privileges.GridRecord} record
		 */
		onGroupPrivilegesSetFilterClick: function(record) {
			// Filter advanced window configuration
			this.filter = Ext.create('CMDBuild.model.common.field.filter.advanced.Filter', { // Manual set to avoid label setup
				configuration: Ext.decode(record.get(CMDBuild.core.constants.Proxy.FILTER) || '{}'),
				entryType: record.get(CMDBuild.core.constants.Proxy.NAME)
			});
			this.selectedClass = _CMCache.getEntryTypeByName(record.get(CMDBuild.core.constants.Proxy.NAME)); // Manual setup to avoid filter setup

			this.controllerFilterWindow.fieldFilterAdvancedWindowSelectedRecordSet({ value: record });
			this.controllerFilterWindow.show();
		}
	});

})();