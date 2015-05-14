(function() {

	Ext.define('CMDBuild.view.administration.domain.enabledClasses.EnabledClassesView', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

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
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.enabledClasses,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Modify', {
								text: CMDBuild.Translation.modifyDomain,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainModifyButtonClick');
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.wrapper = Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmgraypanel-nopadding',
						border: false,
						frame: false,

						layout: {
							type: 'hbox',
							align:'stretch'
						},

						items: []
					})
				]
			});

			this.callParent(arguments);

			this.buildTrees();
		},

		buildTrees: function() {
			var selectedDomain = this.delegate.cmfg('selectedDomainGet');

			this.wrapper.removeAll();
			this.wrapper.add([
				this.originTree = Ext.create('CMDBuild.view.administration.domain.enabledClasses.TreePanel', {
					delegate: this.delegate,

					disabledClasses: !Ext.isEmpty(selectedDomain) ? selectedDomain.get('disabled1') : [],
					title: CMDBuild.Translation.origin,
					type: 'origin'
				}),
				{ xtype: 'splitter' },
				this.destinationTree = Ext.create('CMDBuild.view.administration.domain.enabledClasses.TreePanel', {
					delegate: this.delegate,

					disabledClasses: !Ext.isEmpty(selectedDomain) ? selectedDomain.get('disabled2') : [],
					title: CMDBuild.Translation.destination,
					type: 'destination'
				})
			]);
		}
	});

})();