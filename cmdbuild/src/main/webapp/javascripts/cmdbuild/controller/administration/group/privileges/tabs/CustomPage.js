(function() {

	Ext.define('CMDBuild.controller.administration.group.privileges.tabs.CustomPage', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.group.privileges.CustomPages'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.privileges.Privileges}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupPrivilegesTabCustomPageSetPrivilege',
			'onGroupPrivilegesTabCustomPageShow'
		],

		/**
		 * @cfg {CMDBuild.view.administration.group.privileges.tabs.CustomPage}
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

			this.view = Ext.create('CMDBuild.view.administration.group.privileges.tabs.CustomPage', { delegate: this });
		},

		/**
		 * @param {Object} parameters
		 * @param {Number} parameters.rowIndex
		 * @param {String} parameters.privilege
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onGroupPrivilegesTabCustomPageSetPrivilege: function(parameters) {
			if (!Ext.isEmpty(parameters) && Ext.isObject(parameters)) {
				var params = {};
				params['privilege_mode'] = parameters.privilege;
				params['privilegedObjectId'] = this.view.store.getAt(parameters.rowIndex).get(CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

				CMDBuild.core.proxy.group.privileges.CustomPages.update({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.cmfg('onGroupPrivilegesTabCustomPageShow');
					}
				});
			} else {
				_error('wrong or empty parameters in onGroupPrivilegesTabCustomPageSetPrivilege()', this);
			}
		},

		onGroupPrivilegesTabCustomPageShow: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

			this.view.getStore().load({ params: params });
		}
	});

})();