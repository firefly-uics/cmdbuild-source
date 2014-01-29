(function() {

	// Local step controller
	Ext.define("CMDBuild.view.administration.tasks.mail.CMFilterWindowDelegate", {

		parentDelegate: undefined,

		constructor: function(view) {
			this.view = view;
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddFilter': {
					this.view.contentComponent.add({
						frame: false,
						border: false,
						defaultType: 'textfield',
						layout: 'hbox',
						items: [
							{
								itemId: 'filter',
								flex: 1
							},
							{
								iconCls: 'delete',
								xtype: 'button',
								width: '22px',
								handler: function() {
									this.cmOn('onFilterDelete');
								}
							}
						]
					});
					this.view.contentComponent.doLayout();
				} break;

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	Ext.define("CMDBuild.view.administration.tasks.mail.CMFilterWindow", {
		extend: "Ext.window.Window",

		modal: true,
		delegate: undefined,
		title: undefined,

		initComponent: function() {
			this.autoScroll = true;
			this.width = 400;
			this.height = 300;
			var me = this;

			this.tbar = [{
				iconCls: 'add',
				type: 'button',
				text: '@@ Add filter',
				handler: function() {
					me.delegate.cmOn('onAddFilter');
				}
			}];

			this.contentComponent = Ext.create('Ext.form.Panel', {
				layout: {
					anchor: '100%'
				}
			});

			this.fbar = [
				{
					xtype: 'tbspacer',
					flex: 1
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.btns.confirm,
					handler: function() {
						me.delegate.cmOn('onFilterWindowConfirm', me.contentComponent.getValues());
					}
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.btns.abort,
					handler: function() {
						me.delegate.cmOn('onFilterWindowAbort');
					}
				},
				{
					xtype: 'tbspacer',
					flex: 1
				}
			];

			this.items = [this.contentComponent];

			this.delegate = new CMDBuild.view.administration.tasks.mail.CMFilterWindowDelegate(this);

			this.callParent(arguments);
		}
	});

})();
