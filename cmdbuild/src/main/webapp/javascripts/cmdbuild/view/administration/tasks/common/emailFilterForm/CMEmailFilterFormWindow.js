(function() {

	var tr = CMDBuild.Translation.filterWindow;

	Ext.define('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormWindow', {
		extend: 'Ext.window.Window',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormWindowController}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.buttons.AbortButton}
		 */
		abortButton: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		addRowButton: undefined,

		/**
		 * @property {CMDBuild.buttons.ConfirmButton}
		 */
		confirmButton: undefined,

		/**
		 * @property {Object}
		 */
		content: undefined,

		/**
		 * @cfg {String}
		 */
		title: undefined,

		/**
		 * @cfg {Object}
		 */
		type: undefined,

		/**
		 * @property {Object}
		 */
		textareaConcatParameter: undefined,

		buttonAlign: 'center',
		constrain: true,
		height: 300,
		modal: true,
		overflowY: 'auto',
		resizable: false,
		width: 400,

		initComponent: function() {
			var contentItems = null;

			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormWindowController', this);
			this.delegate.type = this.type;

			// Buttons configuration
				this.addRowButton = Ext.create('Ext.button.Button', {
					iconCls: 'add',
					text: tr.add,
					scope: this,

					handler: function() {
						this.delegate.cmOn('onFilterWindowAdd');
					}
				});

				this.confirmButton = Ext.create('CMDBuild.buttons.ConfirmButton', {
					text: CMDBuild.Translation.common.buttons.confirm,
					scope: this,

					handler: function() {
						this.delegate.cmOn('onFilterWindowConfirm');
					}
				});

				this.abortButton = Ext.create('CMDBuild.buttons.AbortButton', {
					text: CMDBuild.Translation.common.buttons.abort,
					scope: this,

					handler: function() {
						this.delegate.cmOn('onFilterWindowAbort');
					}
				});
			// END: Buttons configuration

			if (!Ext.isEmpty(this.content))
				contentItems = this.content.split(this.textareaConcatParameter);

			this.contentComponent = Ext.create('Ext.form.Panel', {
				layout: {
					anchor: '100%'
				},

				items: this.delegate.buildWindowItem(contentItems)
			});


			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: this.addRowButton
					}
				],
				items: [this.contentComponent],
				fbar: [this.confirmButton, this.abortButton]
			});

			this.callParent(arguments);
		}
	});

})();
