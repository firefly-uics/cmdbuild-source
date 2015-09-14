(function() {

	Ext.define('CMDBuild.controller.administration.group.Users', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.group.Users'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupAddButtonClick',
			'onGroupUsersGroupSelected = onGroupGroupSelected',
			'onGroupUsersSaveButtonClick',
			'onGroupUsersTabShow',
		],

		/**
		 * @cfg {CMDBuild.view.administration.group.users.UsersView}
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

			this.view = Ext.create('CMDBuild.view.administration.group.users.UsersView', { delegate: this });

			// Shorthands
			this.availableGrid = this.view.availableGrid;
			this.selectedGrid = this.view.selectedGrid;
		},

		/**
		 * Disable tab on add button click
		 */
		onGroupAddButtonClick: function() {
			this.view.disable();
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
				usersIdArray.push(record.get(CMDBuild.core.proxy.Constants.ID));
			}, this);

			var params = {};
			params[CMDBuild.core.proxy.Constants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.Constants.ID);
			params[CMDBuild.core.proxy.Constants.USERS] = usersIdArray.join();

			CMDBuild.core.proxy.group.Users.update({
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
				params[CMDBuild.core.proxy.Constants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.Constants.ID);
				params[CMDBuild.core.proxy.Constants.ALREADY_ASSOCIATED] = false;

				this.availableGrid.getStore().load({ params: params });

				params[CMDBuild.core.proxy.Constants.ALREADY_ASSOCIATED] = true;

				this.selectedGrid.getStore().load({ params: params });
			}
		}
	});

})();