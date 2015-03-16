(function() {

	Ext.define('CMDBuild.view.administration.domain.hierarchy.MainPanel', {
		extend: 'Ext.panel.Panel',

		requires: [
//			'CMDBuild.core.proxy.CMProxyConstants',
//			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Hierarchy}
		 */
		delegate: undefined,

		destinationTree: undefined,
		originTree: undefined,
		wrapper: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		buttonAlign: 'center',
		frame: false,

		layout: 'fit',

		title: '@@ Classes hierarchy',

		initComponent: function() {
			// Panel wrapper
			this.wrapper = Ext.create('Ext.panel.Panel', {
				border: false,
				frame: false,

				layout: {
					type: 'hbox',
					align:'stretch'
				},

				items: [
					this.originTree,
					{ xtype: 'splitter' },
					this.destinationTree
				]
			});

			Ext.apply(this, {
				items: [this.wrapper],
				buttons: [
					Ext.create('CMDBuild.buttons.SaveButton', {
						scope: this,

						handler: function(button, e) {
							this.delegate.cmOn('onHierarchySaveButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function(button, e) {
							this.delegate.cmOn('onHierarchyAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);

			this.fillWrapper();
		},

		fillWrapper: function() {
			this.originTree = Ext.create('CMDBuild.view.administration.domain.hierarchy.TreePanel', {
				delegate: this.delegate,

				title: '@@ Origin'
			});

			this.destinationTree = Ext.create('CMDBuild.view.administration.domain.hierarchy.TreePanel', {
				delegate: this.delegate,

				title: '@@ Destination'
			});

			this.wrapper.add([
				this.originTree,
				{ xtype: 'splitter' },
				this.destinationTree
			]);
		}
	});

})();