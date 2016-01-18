(function() {

	Ext.define('CMDBuild.controller.administration.group.privileges.tabs.Filter', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.group.privileges.Filter'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.privileges.Privileges}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupPrivilegesTabFilterSetPrivilege',
			'onGroupPrivilegesTabFilterShow'
		],

		/**
		 * @cfg {CMDBuild.view.administration.group.privileges.tabs.Filter}
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

			this.view = Ext.create('CMDBuild.view.administration.group.privileges.tabs.Filter', { delegate: this });
		},

		onGroupPrivilegesTabFilterShow: function() {
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
		onGroupPrivilegesTabFilterSetPrivilege: function(parameters) {
			if (!Ext.isEmpty(parameters) && Ext.isObject(parameters)) {
				var params = {};
				params['privilege_mode'] = parameters.privilege;
				params['privilegedObjectId'] = this.view.store.getAt(parameters.rowIndex).get(CMDBuild.core.proxy.CMProxyConstants.ID);
				params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

				CMDBuild.core.proxy.group.privileges.Filter.update({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.cmfg('onGroupPrivilegesTabFilterShow');
					}
				});
			} else {
				_error('wrong or empty parameters in onGroupPrivilegesTabFilterSetPrivilege()', this);
			}
		}
	});

})();