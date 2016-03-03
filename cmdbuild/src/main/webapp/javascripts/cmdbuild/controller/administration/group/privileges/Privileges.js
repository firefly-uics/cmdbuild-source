(function() {

	Ext.define('CMDBuild.controller.administration.group.privileges.Privileges', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.group.privileges.Classes',
			'CMDBuild.core.proxy.group.privileges.DataView',
			'CMDBuild.core.proxy.group.privileges.Filter',
			'CMDBuild.core.proxy.group.privileges.CustomPages',
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.tabs.Classes}
		 */
		controllerClassPrivileges: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.tabs.CustomPage}
		 */
		controllerCustomPageProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.tabs.DataView}
		 */
		controllerDataViewProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.tabs.Filter}
		 */
		controllerFilterProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.Grid}
		 */
		controllerWorkflowPrivileges: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupAddButtonClick',
			'onGroupPrivilegesTabShow',
			'onGroupPrivilegesGroupSelected = onGroupGroupSelected'
		],

		/**
		 * @cfg {CMDBuild.view.administration.group.privileges.PrivilegesView}
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

			this.view = Ext.create('CMDBuild.view.administration.group.privileges.PrivilegesView', { delegate: this });

			// Controller build
			this.controllerClassPrivileges = Ext.create('CMDBuild.controller.administration.group.privileges.tabs.Classes', { parentDelegate: this });
			this.controllerCustomPageProperties = Ext.create('CMDBuild.controller.administration.group.privileges.tabs.CustomPage', { parentDelegate: this });
			this.controllerDataViewProperties = Ext.create('CMDBuild.controller.administration.group.privileges.tabs.DataView', { parentDelegate: this });
			this.controllerFilterProperties = Ext.create('CMDBuild.controller.administration.group.privileges.tabs.Filter', { parentDelegate: this });
			this.controllerWorkflowPrivileges = Ext.create('CMDBuild.controller.administration.group.privileges.tabs.Workflow', { parentDelegate: this });

			// Inject tabs (sorted)
			this.view.add(this.controllerClassPrivileges.getView());
			this.view.add(this.controllerWorkflowPrivileges.getView());
			this.view.add(this.controllerDataViewProperties.getView());
			this.view.add(this.controllerFilterProperties.getView());
			this.view.add(this.controllerCustomPageProperties.getView());
		},

		/**
		 * Disable tab on add button click
		 */
		onGroupAddButtonClick: function() {
			this.view.disable();
		},

		/**
		 * Enable/Disable tab evaluating group privileges, administrator groups have full privileges so panel is disabled
		 */
		onGroupPrivilegesGroupSelected: function() {
			this.view.setDisabled(this.cmfg('selectedGroupIsEmpty') || this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.IS_ADMINISTRATOR));
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