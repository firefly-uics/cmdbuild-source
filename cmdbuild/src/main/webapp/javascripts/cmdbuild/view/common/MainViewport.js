(function() {

	Ext.define('CMDBuild.view.common.MainViewport', {
		extend: 'Ext.container.Viewport',

		requires: ['CMDBuild.core.Splash'],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		accordionContainer: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		moduleContainer: undefined,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.panel.Panel', {
						region: 'north',
						border: false,
						cls: 'cmdb-border-bottom',
						contentEl: 'header',
						frame: false,
						height: 45
					}),
					this.accordionContainer = Ext.create('Ext.panel.Panel', {
						region: 'west',
						border: true,
						collapsed: this.delegate.cmfg('mainViewportAccordionIsCollapsed'),
						collapsible: true,
						frame: false,
						header: false, // Hide panel header as CMDBuild UI look
						layout: 'accordion',
						padding: '5 0 5 5',
						split: true,
						width: 200,

						items: []
					}),
					this.moduleContainer = Ext.create('Ext.panel.Panel', {
						region: 'center',
						border: false,
						frame: false,
						layout: 'card',
						padding: '5 5 5 0',

						bodyStyle: {
							border: '0px'
						},

						items: [],
					}),
					Ext.create('Ext.panel.Panel', {
						region: 'south',
						border: true,
						contentEl: 'footer',
						frame: false,
						height: 18
					})
				]
			});

			this.callParent(arguments);

			if (!Ext.isEmpty(Ext.get('cmdbuild-credits-link')))
				Ext.get('cmdbuild-credits-link').on('click', function(e, t, eOpts) {
					if (!Ext.isEmpty(this.delegate))
						this.delegate.cmfg('onMainViewportCreditsClick');
				}, this);

			if (!Ext.isEmpty(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.GROUP_DESCRIPTIONS)))
				Ext.create('Ext.tip.ToolTip', {
					target: 'msg-inner-hidden',
					html: Ext.String.format(
						'<div class="msg-inner-hidden-tooltip">'
							+ '<p><strong>' + CMDBuild.Translation.groups + ':</strong> {0}</p>'
							+ '<p><strong>' + CMDBuild.Translation.defaultGroup + ':</strong> {1}</p>'
						+ '</div>',
						CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.GROUP_DESCRIPTIONS),
						CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_DESCRIPTION)
					)
				});
		}
	});

})();