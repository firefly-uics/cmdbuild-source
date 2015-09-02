(function() {

	Ext.define('CMDBuild.view.administration.group.userInterface.UserInterfaceView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.group.UserInterface}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.group.userInterface.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.uiConfiguration,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.administration.group.userInterface.FormPanel', { delegate: this.delegate })
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