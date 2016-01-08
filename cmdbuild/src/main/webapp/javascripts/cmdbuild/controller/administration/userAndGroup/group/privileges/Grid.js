(function() {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.privileges.Grid', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.userAndGroup.group.privileges.Classes',
			'CMDBuild.core.proxy.userAndGroup.group.privileges.DataView',
			'CMDBuild.core.proxy.userAndGroup.group.privileges.Filter'
		],

		mixins: ['CMDBuild.controller.common.field.filter.advanced.Advanced'], // Import fieldConfiguration, filter, selectedClass property methods

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		controllerFilterWindow: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.privileges.UiConfiguration}
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
			'fieldFilterAdvancedSelectedClassIsEmpty',
			'onUserAndGroupGroupFieldFilterAdvancedWindowgetEndpoint = onFieldFilterAdvancedWindowgetEndpoint',
			'onUserAndGroupGroupPrivilegesGridSetPrivilege',
			'onUserAndGroupGroupPrivilegesGridTabShow',
			'onUserAndGroupGroupPrivilegesGridRemoveFilterClick',
			'onUserAndGroupGroupPrivilegesGridSetFilterClick',
			'onUserAndGroupGroupPrivilegesGridUIConfigurationButtonClick'
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
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.privileges.GridPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.privileges.GridPanel', {
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
			this.controllerUiConfiguration = Ext.create('CMDBuild.controller.administration.userAndGroup.group.privileges.UiConfiguration', { parentDelegate: this });
		},

		/**
		 * @param {Object} resultObject
		 * @param {Object} resultObject.columnPrivileges
		 * @param {Object} resultObject.filter
		 *
		 * @override
		 */
		onUserAndGroupGroupFieldFilterAdvancedWindowgetEndpoint: function(resultObject) {
			if (Ext.encode(resultObject.filter).indexOf('"parameterType":"calculated"') < 0) {
				var params = {};
				params['privilegedObjectId'] = this.fieldFilterAdvancedSelectedClassGet(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(resultObject.columnPrivileges);
				params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(resultObject.filter);
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				this.proxy.setRowAndColumn({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.onUserAndGroupGroupPrivilegesGridTabShow();
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

		onUserAndGroupGroupPrivilegesGridTabShow: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

			this.view.getStore().load({ params: params });
		},

		/**
		 * @param {Object} parameters
		 * @param {Number} parameters.rowIndex
		 * @param {String} parameters.privilege
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onUserAndGroupGroupPrivilegesGridSetPrivilege: function(parameters) {
			if (Ext.isEmpty(this.proxy)) {
				_error('proxy object not defined', this);
			} else {
				var params = {};
				params['privilege_mode'] = parameters.privilege;
				params['privilegedObjectId'] = this.view.store.getAt(parameters.rowIndex).get(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				this.proxy.update({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.onUserAndGroupGroupPrivilegesGridTabShow();
					}
				});
			}
		},

		/**
		 * @param {CMDBuild.model.userAndGroup.group.privileges.GridRecord} record
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onUserAndGroupGroupPrivilegesGridRemoveFilterClick: function(record) {
			Ext.Msg.show({
				title: CMDBuild.Translation.attention,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function(buttonId, text, opt) {
					if (buttonId == 'yes') {
						var params = {};
						params['privilegedObjectId'] = record.get(CMDBuild.core.constants.Proxy.ID);
						params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

						// Set empty filter to clear value
						this.proxy.setRowAndColumn({
							params: params,
							scope: this,
							success: function(response, options, decodedResponse) {
								this.onUserAndGroupGroupPrivilegesGridTabShow();
							}
						});
					}
				}
			});
		},

		/**
		 * @param {CMDBuild.model.userAndGroup.group.privileges.GridRecord} record
		 */
		onUserAndGroupGroupPrivilegesGridUIConfigurationButtonClick: function(record) {
			this.controllerUiConfiguration.setRecord(record);
			this.controllerUiConfiguration.getView().show();
		},

		/**
		 * @param {CMDBuild.model.userAndGroup.group.privileges.GridRecord} record
		 */
		onUserAndGroupGroupPrivilegesGridSetFilterClick: function(record) {
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