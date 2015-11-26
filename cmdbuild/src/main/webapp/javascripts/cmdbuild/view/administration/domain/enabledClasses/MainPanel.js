(function() {

	Ext.define('CMDBuild.view.administration.domain.enabledClasses.MainPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.domain.EnabledClasses}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.enabledClasses.TreePanel}
		 */
		destinationTree: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.enabledClasses.TreePanel}
		 */
		originTree: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		wrapper: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		buttonAlign: 'center',
		frame: false,

		layout: 'fit',

		title: CMDBuild.Translation.enabledClasses,

		initComponent: function() {
			// Panel wrapper
			this.wrapper = Ext.create('Ext.panel.Panel', {
				bodyCls: 'cmgraypanel-nopadding',
				border: false,
				frame: false,

				layout: {
					type: 'hbox',
					align:'stretch'
				},

				items: []
			});

			Ext.apply(this, {
				items: [this.wrapper],
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('Ext.button.Button', {
								iconCls: 'modify',
								text: CMDBuild.Translation.administration.modClass.domainProperties.modify_domain,
								scope: this,

								handler: function() {
									this.delegate.cmOn('onModifyButtonClick');
								}
							})
						]
					},
					{
						xtype: 'toolbar',
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.buttons.SaveButton', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onEnabledClassesSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.buttons.AbortButton', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onAbortButtonClick');
								}
							})
						]
					}
				]
			});

			this.callParent(arguments);

			this.buildTrees();
		},

		buildTrees: function() {
			var selectedDomain = this.delegate.getSelectedDomain();

			this.originTree = Ext.create('CMDBuild.view.administration.domain.enabledClasses.TreePanel', {
				delegate: this.delegate,

				disabledClasses: !Ext.isEmpty(selectedDomain) ? selectedDomain.get('disabled1') : [],
				title: CMDBuild.Translation.origin,
				type: 'origin'
			});

			this.destinationTree = Ext.create('CMDBuild.view.administration.domain.enabledClasses.TreePanel', {
				delegate: this.delegate,

				disabledClasses: !Ext.isEmpty(selectedDomain) ? selectedDomain.get('disabled2') : [],
				title: CMDBuild.Translation.destination,
				type: 'destination'
			});

			this.wrapper.removeAll();
			this.wrapper.add([
				this.originTree,
				{ xtype: 'splitter' },
				this.destinationTree
			]);
		}
	});

})();