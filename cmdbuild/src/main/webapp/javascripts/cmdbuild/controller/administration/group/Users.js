(function() {

	Ext.define('CMDBuild.controller.administration.group.Users', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
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
			'onGroupUsersAddButtonClick = onGroupAddButtonClick',
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
		onGroupUsersAddButtonClick: function() {
			this.view.disable();
		},

		/**
		 * Enable/Disable tab evaluating selected group
		 */
		onGroupUsersGroupSelected: function() {
			this.view.setDisabled(this.cmfg('groupSelectedGroupIsEmpty'));
		},

		/**
		 * TODO: waiting for refactor (use an array of id not a string)
		 */
		onGroupUsersSaveButtonClick: function() {
			var usersIdArray = [];

			Ext.Array.forEach(this.selectedGrid.getStore().getRange(), function(record, i, allRecords) {
				usersIdArray.push(record.get(CMDBuild.core.constants.Proxy.ID));
			}, this);

			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.USERS] = usersIdArray.join();

			CMDBuild.core.proxy.group.Users.update({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					CMDBuild.core.Message.success();
				}
			});
		},

		onGroupUsersTabShow: function() {
			if (!this.cmfg('groupSelectedGroupIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.ALREADY_ASSOCIATED] = false;

				this.availableGrid.getStore().load({
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

				params[CMDBuild.core.constants.Proxy.ALREADY_ASSOCIATED] = true;

				this.selectedGrid.getStore().load({
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
			}
		}
	});

})();