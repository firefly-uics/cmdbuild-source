(function() {

	Ext.define('CMDBuild.view.administration.group.privileges.PrivilegesView', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.group.privileges.Privileges}
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