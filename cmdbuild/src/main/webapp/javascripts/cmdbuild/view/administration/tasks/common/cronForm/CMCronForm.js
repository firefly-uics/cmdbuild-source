(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.cronForm.CMCronForm', {
		extend: 'Ext.panel.Panel',

		border: false,

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController', this);

			this.advancedPanel = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormAdvanced', {
				delegate: me.delegate
			});
			this.basePanel = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormBase', {
				delegate: me.delegate
			});

			this.delegate.advancedPanel = this.advancedPanel;
			this.delegate.basePanel = this.basePanel;

			Ext.apply(this, {
				items: [this.basePanel, this.advancedPanel]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * To correctly enable radio fields on tab show
			 */
			show: function(view, eOpts) {
				if (this.delegate.isBaseEmpty() && !this.delegate.isAdvancedEmpty()) {
					this.advancedPanel.advanceRadio.setValue(true);
				} else {
					this.basePanel.baseRadio.setValue(true);
				}
			}
		}
	});

})();