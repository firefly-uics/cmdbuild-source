(function() {

	Ext.define('CMDBuild.view.administration.reports.jasper.form.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.reports.Jasper}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.reports.jasper.form.Step1Panel}
		 */
		step1Panel: undefined,

		/**
		 * @property {CMDBuild.view.administration.reports.jasper.form.Step2Panel}
		 */
		step2Panel: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		overflowY: 'auto',
		split: true,

		layout: 'card',

		initComponent: function() {

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyReport,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportsJasperModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Delete', {
								text: CMDBuild.Translation.removeReport,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportsJasperRemoveButtonClick');
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportsJasperSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onReportsJasperAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.step1Panel = Ext.create('CMDBuild.view.administration.reports.jasper.form.Step1Panel'),
					this.step2Panel = Ext.create('CMDBuild.view.administration.reports.jasper.form.Step2Panel')
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();