(function() {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.UserInterface', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.UserInterface'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupUserInterfaceAbortButtonClick',
			'onUserAndGroupGroupUserInterfaceAddButtonClick = onUserAndGroupGroupAddButtonClick',
			'onUserAndGroupGroupUserInterfaceGroupSelected = onUserAndGroupGroupSelected',
			'onUserAndGroupGroupUserInterfaceSaveButtonClick',
			'onUserAndGroupGroupUserInterfaceTabShow'
		],

		/**
		 * @property {CMDBuild.model.userAndGroup.group.userInterface.UserInterface}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.group.userInterface.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.userInterface.UserInterfaceView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.group.Group} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.userInterface.UserInterfaceView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		// Configuration property methods
			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.userAndGroup.group.userInterface.UserInterface} or Mixed
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
					if (Ext.getClassName(configurationObject) == 'CMDBuild.model.userAndGroup.group.userInterface.UserInterface') {
						this.configuration = configurationObject;
					} else {
						this.configuration = Ext.create('CMDBuild.model.userAndGroup.group.userInterface.UserInterface', configurationObject);
					}
				}
			},

		onUserAndGroupGroupUserInterfaceAbortButtonClick: function() {
			this.onUserAndGroupGroupUserInterfaceTabShow();
		},

		/**
		 * Disable tab on add button click
		 */
		onUserAndGroupGroupUserInterfaceAddButtonClick: function() {
			this.view.disable();
		},

		/**
		 * Enable/Disable tab evaluating group privileges, CloudAdministrators couldn't change UIConfiguration of full administrator groups
		 */
		onUserAndGroupGroupUserInterfaceGroupSelected: function() {
			CMDBuild.core.proxy.userAndGroup.group.Group.read({ // TODO: waiting for refactor (crud)
				scope: this,
				success: function(result, options, decodedResult) {
					decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

					var loggedUserCurrentGroup = Ext.Array.findBy(decodedResult, function(groupObject, i) {
						return CMDBuild.Runtime.DefaultGroupId == groupObject[CMDBuild.core.constants.Proxy.ID];
					}, this);

					if (!Ext.isEmpty(loggedUserCurrentGroup)) {
						this.view.setDisabled(
							this.cmfg('userAndGroupGroupSelectedGroupIsEmpty')
							|| loggedUserCurrentGroup[CMDBuild.core.constants.Proxy.IS_CLOUD_ADMINISTRATOR]
							&& this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR)
							&& !this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_CLOUD_ADMINISTRATOR)
						);
					}
				}
			});
		},

		onUserAndGroupGroupUserInterfaceSaveButtonClick: function() {
			this.configurationSet(this.form.getForm().getValues());

			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.UI_CONFIGURATION] = Ext.encode(this.configurationGet().getData());

			CMDBuild.core.proxy.userAndGroup.group.UserInterface.update({
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
		onUserAndGroupGroupUserInterfaceTabShow: function() {
			if (!this.cmfg('userAndGroupGroupSelectedGroupIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.core.proxy.userAndGroup.group.UserInterface.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						this.configurationSet(decodedResponse);

						this.configurationLoad();
					}
				});
			}
		}
	});

})();