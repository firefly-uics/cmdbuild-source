(function() {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.properties.PropertiesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.group.properties.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.properties,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.administration.userAndGroup.group.properties.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupPropertiesTabShow');
			}
		}
	});

})();