(function() {

	Ext.define('CMDBuild.controller.administration.group.privileges.Privileges', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.group.privileges.Classes',
			'CMDBuild.core.proxy.group.privileges.DataView',
			'CMDBuild.core.proxy.group.privileges.Filter',
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.Grid}
		 */
		controllerClassPrivileges: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.Grid}
		 */
		controllerViewProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.Grid}
		 */
		controllerFilterProperties: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupPrivilegesAddButtonClick = onGroupAddButtonClick',
			'onGroupPrivilegesGroupSelected = onGroupGroupSelected',
			'onGroupPrivilegesTabShow'
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
			this.controllerClassPrivileges = Ext.create('CMDBuild.controller.administration.group.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.group.privileges.Classes,
				title: CMDBuild.Translation.classes,
				enableCRUDRead: true,
				enableCRUDWrite: true,
				enablePrivilegesAndUi: true
			});
			this.controllerViewProperties = Ext.create('CMDBuild.controller.administration.group.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.group.privileges.DataView,
				title: CMDBuild.Translation.views,
				enableCRUDRead: true
			});
			this.controllerFilterProperties = Ext.create('CMDBuild.controller.administration.group.privileges.Grid', {
				parentDelegate: this,
				proxy: CMDBuild.core.proxy.group.privileges.Filter,
				title: CMDBuild.Translation.searchFilters,
				enableCRUDRead: true
			});

			// Inject tabs (sorted)
			this.view.add(this.controllerClassPrivileges.getView());
			this.view.add(this.controllerViewProperties.getView());
			this.view.add(this.controllerFilterProperties.getView());
		},

		/**
		 * Disable tab on add button click
		 */
		onGroupPrivilegesAddButtonClick: function() {
			this.view.disable();
		},

		/**
		 * Enable/Disable tab evaluating group privileges, administrator groups have full privileges so panel is disabled
		 */
		onGroupPrivilegesGroupSelected: function() {
			this.view.setDisabled(this.cmfg('groupSelectedGroupIsEmpty') || this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR));
		},

		/**
		 * Evaluate group privileges to set active first tab
		 */
		onGroupPrivilegesTabShow: function() {
			if (!this.cmfg('groupSelectedGroupIsEmpty') && this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR)) {
				this.cmfg('onGroupSetActiveTab');
			} else {
				this.view.setActiveTab(0);
				this.view.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
			}
		}
	});

})();