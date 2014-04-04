(function() {

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep2Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,
		className: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		drawFilterTabs: function() {
			var me = this;

			if (this.className) {
				_CMCache.getAttributeList(_CMCache.getEntryTypeByName(this.className).getId(), function(attributes) {
					me.view.filterTabPanel.removeAll();

					// Filter tabs
					me.view.filterAttributesTab = Ext.create('CMDBuild.view.management.common.filter.CMFilterAttributes', {
						attributes: attributes
					});
					me.view.relationsTab = Ext.create('CMDBuild.view.management.common.filter.CMRelations', {
						className: me.className,
						height: '100%'
					});
					me.view.functionsTab = Ext.create('CMDBuild.view.management.common.filter.CMFunctions', {
						className: me.className
					});

					me.view.filterTabPanel.add([me.view.filterAttributesTab, me.view.relationsTab, me.view.functionsTab]);
					me.view.filterTabPanel.doLayout();
				});
			}
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep2', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'workflow',

		border: false,
		overflowY: 'auto',
		layout: 'fit',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep2Delegate', this);

			this.filterTabPanel = Ext.create('Ext.tab.Panel', {
				border: false
			});
// TODO: ??????
//			this.wrapper = Ext.create('Ext.form.FieldSet', {
//				title: 'tr.filter',
//
//				padding: '0px',
//
//				layout: 'fit',
//				items: [this.filterTabPanel]
//			});

			Ext.apply(this, {
				items: [this.filterTabPanel]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Draw tabs on show
			 */
			show: function(panel, eOpts) {
				if (this.filterTabPanel.items.length < 1)
					this.delegate.drawFilterTabs();
			}
		}
	});

})();