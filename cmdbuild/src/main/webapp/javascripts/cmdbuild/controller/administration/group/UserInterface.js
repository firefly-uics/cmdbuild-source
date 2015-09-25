(function() {

	Ext.define('CMDBuild.controller.administration.group.UserInterface', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.group.UserInterface'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupUserInterfaceAbortButtonClick',
			'onGroupUserInterfaceAddButtonClick = onGroupAddButtonClick',
			'onGroupUserInterfaceGroupSelected = onGroupGroupSelected',
			'onGroupUserInterfaceSaveButtonClick',
			'onGroupUserInterfaceTabShow'
		],

		/**
		 * @property {CMDBuild.model.group.userInterface.UserInterface}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.administration.group.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.groups.properties.UserInterfaceView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.group.Group} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.group.userInterface.UserInterfaceView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		// Configuration property methods
			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.group.userInterface.UserInterface} or Mixed
			 */
			configurationGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.configuration.get(parameterName);

				return this.configuration;
			},

			/**
			 * Loads data model and sub model data to form
			 */
			configurationLoad: function() {
				this.form.reset();

				this.form.getForm().loadRecord(this.configurationGet());
				this.form.getForm().loadRecord(this.configurationGet(CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS));
				this.form.getForm().loadRecord(this.configurationGet(CMDBuild.core.constants.Proxy.DISABLED_MODULES));
				this.form.getForm().loadRecord(this.configurationGet(CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS));
			},

			/**
			 * @property {Object} configurationObject
			 */
			configurationSet: function(configurationObject) {
				this.configuration = null;

				if (!Ext.isEmpty(configurationObject) && Ext.isObject(configurationObject)) {
					if (Ext.getClassName(configurationObject) == 'CMDBuild.model.group.userInterface.UserInterface') {
						this.configuration = configurationObject;
					} else {
						this.configuration = Ext.create('CMDBuild.model.group.userInterface.UserInterface', configurationObject);
					}
				}
			},

		onGroupUserInterfaceAbortButtonClick: function() {
			if (this.cmfg('selectedLookupTypeIsEmpty')) {
				this.form.reset();
			} else {
				this.onGroupUserInterfaceTabShow();
			}
		},

		/**
		 * Disable tab on add button click
		 */
		onGroupUserInterfaceAddButtonClick: function() {
			this.view.disable();
		},

		/**
		 * Enable/Disable tab evaluating group privileges, CloudAdministrators couldn't change UIConfiguration of full administrator groups
		 */
		onGroupUserInterfaceGroupSelected: function() {
			var loggedUserCurrentGroup = _CMCache.getGroupById(CMDBuild.Runtime.DefaultGroupId);

			this.view.setDisabled(
				this.cmfg('groupSelectedGroupIsEmpty')
				|| loggedUserCurrentGroup.get(CMDBuild.core.constants.Proxy.IS_CLOUD_ADMINISTRATOR)
				&& this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR)
				&& !this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_CLOUD_ADMINISTRATOR)
			);
		},

		onGroupUserInterfaceSaveButtonClick: function() {
			this.configurationSet(this.form.getForm().getValues());

			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.UI_CONFIGURATION] = Ext.encode(this.configurationGet().getData());

			CMDBuild.core.proxy.group.UserInterface.update({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					CMDBuild.core.Message.success();
				}
			});
		},

		/**
		 * Loads tab data
		 */
		onGroupUserInterfaceTabShow: function() {
			if (!this.cmfg('groupSelectedGroupIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.core.proxy.group.UserInterface.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.configurationSet(decodedResponse.response);

						this.configurationLoad();
					}
				});
			}
		}
	});

})();