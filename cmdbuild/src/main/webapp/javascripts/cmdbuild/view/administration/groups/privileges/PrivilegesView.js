(function() {

	Ext.define('CMDBuild.view.administration.groups.privileges.PrivilegesView', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.groups.privileges.Privileges}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		title: CMDBuild.Translation.permissions,

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onGroupPrivilegesTabShow');
			}
		}
	});

})();