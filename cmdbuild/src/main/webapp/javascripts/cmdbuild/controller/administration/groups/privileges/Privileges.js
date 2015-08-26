(function() {

	Ext.define('CMDBuild.controller.administration.groups.privileges.Privileges', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.groups.privileges.Classes',
			'CMDBuild.core.proxy.groups.privileges.DataView',
			'CMDBuild.core.proxy.groups.privileges.Filter',
		],

		/**
		 * @cfg {CMDBuild.controller.administration.groups.Groups}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.administration.groups.privileges.Grid}
		 */
		controllerClassPrivileges: undefined,

		/**
		 * @property {CMDBuild.controller.administration.groups.privileges.Grid}
		 */
		controllerViewProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.groups.privileges.Grid}
		 */
		controllerFilterProperties: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupPrivilegesTabShow',
			'onGroupPrivilegesGroupSelected = onGroupGroupSelected'
		],

		/**
		 * @cfg {CMDBuild.view.administration.groups.privileges.PrivilegesView}
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

			this.view = Ext.create('CMDBuild.view.administration.groups.privileges.PrivilegesView', { delegate: this });

			// Controller build
			this.controllerClassPrivileges = Ext.create('CMDBuild.controller.administration.groups.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.groups.privileges.Classes,
				title: CMDBuild.Translation.administration.modClass.tree_title,
				enableCRUDRead: true,
				enableCRUDWrite: true,
				enablePrivilegesAndUi: true
			});
			this.controllerViewProperties = Ext.create('CMDBuild.controller.administration.groups.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.groups.privileges.DataView,
				title: CMDBuild.Translation.views,
				enableCRUDRead: true
			});
			this.controllerFilterProperties = Ext.create('CMDBuild.controller.administration.groups.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.groups.privileges.Filter,
				title: CMDBuild.Translation.search_filters,
				enableCRUDRead: true
			});

			// Inject tabs (sorted)
			this.view.add(this.controllerClassPrivileges.getView());
			this.view.add(this.controllerViewProperties.getView());
			this.view.add(this.controllerFilterProperties.getView());
		},

		/**
		 * Enable/Disable tab evaluating group privileges, administrator groups have full privileges so panel is disabled
		 */
		onGroupPrivilegesGroupSelected: function() {
			this.view.setDisabled(!this.cmfg('selectedGroupIsEmpty') && this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.IS_ADMINISTRATOR));
		},

		/**
		 * Evaluate group privileges to set active first tab
		 */
		onGroupPrivilegesTabShow: function() {
			if (!this.cmfg('selectedGroupIsEmpty') && this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.IS_ADMINISTRATOR)) {
				this.cmfg('onGroupSetActiveTab');
			} else {
				this.view.setActiveTab(0);
				this.view.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
			}
		}
	});

})();