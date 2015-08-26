(function() {

	Ext.define('CMDBuild.view.administration.groups.userInterface.UserInterfaceView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.groups.UserInterface}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.groups.userInterface.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.administration.modsecurity.uiconfiguration.title,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.administration.groups.userInterface.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onGroupUserInterfaceTabShow');
			}
		}
	});

})();