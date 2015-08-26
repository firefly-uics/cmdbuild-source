(function() {

	Ext.define('CMDBuild.controller.administration.groups.Users', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.groups.Users'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.groups.Groups}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupUsersGroupSelected = onGroupGroupSelected',
			'onGroupUsersSaveButtonClick',
			'onGroupUsersTabShow',
		],

		/**
		 * @cfg {CMDBuild.view.administration.groups.users.UsersView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.groups.Groups} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.groups.users.UsersView', { delegate: this });

			// Shorthands
			this.availableGrid = this.view.availableGrid;
			this.selectedGrid = this.view.selectedGrid;
		},


		/**
		 * Enable/Disable tab evaluating selected group
		 */
		onGroupUsersGroupSelected: function() {
			this.view.setDisabled(this.cmfg('selectedGroupIsEmpty'));
		},

		/**
		 * TODO: waiting for refactor (use an array of id not a string)
		 */
		onGroupUsersSaveButtonClick: function() {
			var usersIdArray = [];

			Ext.Array.forEach(this.selectedGrid.getStore().getRange(), function(record, i, allRecords) {
				usersIdArray.push(record.get(CMDBuild.core.proxy.CMProxyConstants.ID));
			}, this);

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.USERS] = usersIdArray.join();

			CMDBuild.core.proxy.groups.Users.update({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					CMDBuild.core.Message.success();
				}
			});
		},

		onGroupUsersTabShow: function() {
			if (!this.cmfg('selectedGroupIsEmpty')) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.ALREADY_ASSOCIATED] = false;

				this.availableGrid.getStore().load({ params: params });

				params[CMDBuild.core.proxy.CMProxyConstants.ALREADY_ASSOCIATED] = true;

				this.selectedGrid.getStore().load({ params: params });
			}
		}
	});

})();