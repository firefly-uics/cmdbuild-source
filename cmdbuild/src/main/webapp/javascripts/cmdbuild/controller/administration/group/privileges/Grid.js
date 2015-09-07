(function() {

	Ext.define('CMDBuild.controller.administration.group.privileges.Grid', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.group.privileges.Classes',
			'CMDBuild.core.proxy.group.privileges.DataView',
			'CMDBuild.core.proxy.group.privileges.Filter'
		],

		// Here to avoid a complete refactor of FilterChooser structure
		mixins: {
			filterChooserWindowDelegate: 'CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate',
			filterWindow: 'CMDBuild.view.management.common.filter.CMFilterWindowDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.group.privileges.Privileges}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.UiConfiguration}
		 */
		controllerUiConfiguration: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
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

			this.controllerUiConfiguration = Ext.create('CMDBuild.controller.administration.group.privileges.UiConfiguration', { parentDelegate: this });

			this.view = Ext.create('CMDBuild.view.administration.group.privileges.GridPanel', {
				delegate: this,
				title: this.title,
				store: this.proxy.getStore(),
				enableCRUDRead: this.enableCRUDRead,
				enableCRUDWrite: this.enableCRUDWrite,
				enablePrivilegesAndUi: this.enablePrivilegesAndUi
			});
		},

		onGroupPrivilegesGridTabShow: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

			this.view.getStore().load({ params: params });
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
				params['privilegedObjectId'] = this.view.store.getAt(parameters.rowIndex).get(CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

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
						params['privilegedObjectId'] = record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
						params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

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
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onGroupPrivilegesSetFilterClick: function(record) {
			var entryType = _CMCache.getEntryTypeByName(record.get(CMDBuild.core.proxy.CMProxyConstants.NAME));

			var filter = new CMDBuild.model.CMFilterModel({
				configuration: Ext.decode(record.get(CMDBuild.core.proxy.CMProxyConstants.FILTER) || '{}'),
				entryType: record.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
				local: true,
				name: ''
			});

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = false;
			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = entryType.getName();

			CMDBuild.core.proxy.Attributes.read({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					var filterWindow = Ext.create('CMDBuild.view.administration.group.privileges.filterWindow.FilterWindow', {
						attributes: decodedResponse.attributes,
						className: record.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
						filter: filter,
						group: record
					});

					filterWindow.addDelegate(this);

					filterWindow.show();
				}
			});
		},

		// As cmFilterWindowDelegate
			/**
			 * @param {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
			 *
			 * BUSINNESS RULE: The user could not save the privileges if the filter has some runtime parameter
			 */
			onCMFilterWindowSaveButtonClick: function(filterWindow) {
				var filter = filterWindow.getFilter();
				var runtimeParameters = filter.getRuntimeParameters();
				var calculatedParameters = filter.getCalculatedParameters();

				if (runtimeParameters && runtimeParameters.length > 0) {
					CMDBuild.core.Message.error(
						CMDBuild.Translation.error,
						CMDBuild.Translation.itIsNotAllowedFilterWithRuntimeParams,
						false
					);

					return;
				} else if (calculatedParameters && calculatedParameters.length > 0) {
					CMDBuild.core.Message.error(
						CMDBuild.Translation.error,
						CMDBuild.Translation.itIsNotAllowedFilterWithCalculatedParams,
						false
					);

					return;
				}

				var params = {};
				params['privilegedObjectId'] = filterWindow.group.get(CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES] = Ext.encode(filterWindow.getAttributePrivileges());
				params[CMDBuild.core.proxy.CMProxyConstants.FILTER] = Ext.encode(filter.getConfiguration());
				params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

				this.proxy.setRowAndColumn({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						filterWindow.group.set(CMDBuild.core.proxy.CMProxyConstants.FILTER, params[CMDBuild.core.proxy.CMProxyConstants.FILTER]);
						filterWindow.group.set(CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES_PRIVILEGES, filterWindow.getAttributePrivileges());
						filterWindow.destroy();
					}
				});
			},

			/**
			 * @param {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
			 */
			onCMFilterWindowAbortButtonClick: function(filterWindow) {
				filterWindow.destroy();
			},

		// As filterChooserWindowDelegate
			/**
			 * @param {CMDBuild.view.common.field.CMFilterChooserWindow} filterWindow
			 * @params {Ext.data.Model} filter
			 */
			onCMFilterChooserWindowRecordSelect: function(filterWindow, filter) {
				filterWindow.setFilter(filter);
			}
	});

})();