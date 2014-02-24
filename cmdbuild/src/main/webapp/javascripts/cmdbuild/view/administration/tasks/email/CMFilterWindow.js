(function() {

	// Local step controller
	Ext.define('CMDBuild.view.administration.tasks.email.CMFilterWindowDelegate', {

		parentDelegate: undefined,
		type: undefined,
		view: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddFilter': {
					this.view.contentComponent.add(this.buildWindowItem());
					this.view.contentComponent.doLayout();
				} break;

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		buildWindowItem: function(values) {
			var me = this,
				items = [];

			if (typeof values === 'undefined') {
				values = [''];
			}

			for (key in values) {
				items.push({
					frame: false,
					border: false,
					defaultType: 'textfield',
					layout: 'hbox',
					items: [
						{
							itemId: 'filter',
							flex: 1,
							value: values[key],
							listeners: {
								'change': function() {
									me.parentDelegate.cmOn(
										'on' + me.type + 'FilterChange',
										me.view.contentComponent.getForm().getValues(),
										Ext.emptyFn()
									);
								}
							}
						},
						{
							iconCls: 'delete',
							xtype: 'button',
							width: '22px',
							handler: function() {
								// HACK: to reset deleted textarea's value, probably for a bug the item is just hided
								this.up('panel').down('#filter').setValue('');

								// Remove input's panel container from form
								this.up('form').remove(this.up('panel').id);

								me.parentDelegate.cmOn(
									'on' + me.type + 'FilterChange',
									me.view.contentComponent.getForm().getValues(),
									Ext.emptyFn()
								);
							}
						}
					]
				});
			}

			return items;
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMFilterWindow', {
		extend: 'Ext.window.Window',

		autoScroll: true,
		content: undefined,
		delegate: undefined,
		height: 300,
		modal: true,
		title: undefined,
		type: undefined,
		width: 400,

		initComponent: function() {
			var me = this,
				contentItems = null;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMFilterWindowDelegate');
			this.delegate.view = this;
			this.delegate.type = this.type;

			this.tbar = [{
				iconCls: 'add',
				type: 'button',
				text: '@@ Add filter',
				handler: function() {
					me.delegate.cmOn('onAddFilter');
				}
			}];

			if (typeof this.content !== 'undefined') {
				contentItems = this.content.split(' OR ');
			}

			this.contentComponent = Ext.create('Ext.form.Panel', {
				layout: {
					anchor: '100%'
				},
				items: this.delegate.buildWindowItem(contentItems)
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
						me.delegate.cmOn('onFilterWindowConfirm');
					}
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.btns.abort,
					handler: function() {
						me.delegate.cmOn('on' + me.type + 'FilterWindowAbort');
					}
				},
				{
					xtype: 'tbspacer',
					flex: 1
				}
			];

			this.items = [this.contentComponent];

			this.callParent(arguments);
		}
	});

})();