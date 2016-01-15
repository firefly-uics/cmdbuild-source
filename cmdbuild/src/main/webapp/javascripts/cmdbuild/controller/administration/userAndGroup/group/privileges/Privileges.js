(function() {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.privileges.Classes',
			'CMDBuild.core.proxy.userAndGroup.group.privileges.DataView',
			'CMDBuild.core.proxy.userAndGroup.group.privileges.Filter',
			'CMDBuild.core.proxy.userAndGroup.group.privileges.CustomPages',
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.privileges.Grid}
		 */
		controllerClassPrivileges: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.privileges.Grid}
		 */
		controllerViewProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.privileges.Grid}
		 */
		controllerFilterProperties: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupTabPrivilegesAddButtonClick',
			'onUserAndGroupGroupTabPrivilegesGroupSelected = onUserAndGroupGroupSelected',
			'onUserAndGroupGroupTabPrivilegesShow'
		],

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.privileges.PrivilegesView}
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

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.privileges.PrivilegesView', { delegate: this });

			// Controller build
			this.controllerClassPrivileges = Ext.create('CMDBuild.controller.administration.userAndGroup.group.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.userAndGroup.group.privileges.Classes,
				title: CMDBuild.Translation.classes,
				enableCRUDRead: true,
				enableCRUDWrite: true,
				enablePrivilegesAndUi: true
			});
			this.controllerViewProperties = Ext.create('CMDBuild.controller.administration.userAndGroup.group.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.userAndGroup.group.privileges.DataView,
				title: CMDBuild.Translation.views,
				enableCRUDRead: true
			});
			this.controllerFilterProperties = Ext.create('CMDBuild.controller.administration.userAndGroup.group.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.userAndGroup.group.privileges.Filter,
				title: CMDBuild.Translation.searchFilters,
				enableCRUDRead: true
			});

			this.controllerCustomPagesProperties = Ext.create('CMDBuild.controller.administration.userAndGroup.group.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.userAndGroup.group.privileges.CustomPages,
				title: CMDBuild.Translation.customPages,
				enableCRUDRead: true
			});

			// Inject tabs (sorted)
			this.view.add(this.controllerClassPrivileges.getView());
			this.view.add(this.controllerViewProperties.getView());
			this.view.add(this.controllerFilterProperties.getView());
			this.view.add(this.controllerCustomPagesProperties.getView());
		},

		/**
		 * Disable tab on add button click
		 */
		onUserAndGroupGroupTabPrivilegesAddButtonClick: function() {
			this.view.disable();
		},

		/**
		 * Enable/Disable tab evaluating group privileges, administrator groups have full privileges so panel is disabled
		 */
		onUserAndGroupGroupTabPrivilegesGroupSelected: function() {
			this.view.setDisabled(this.cmfg('userAndGroupGroupSelectedGroupIsEmpty') || this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR));
		},

		/**
		 * Evaluate group privileges to set active first tab
		 */
		onUserAndGroupGroupTabPrivilegesShow: function() {
			if (!this.cmfg('userAndGroupGroupSelectedGroupIsEmpty') && this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR)) {
				this.cmfg('onUserAndGroupGroupSetActiveTab');
			} else {
				this.view.setActiveTab(0);
				this.view.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
			}
		}
	});

})();