(function() {

	var tr = CMDBuild.Translation.administration.tasks;

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

		setClassName: function(className) {
			this.className = className;
		},

		drawFilterTabs: function() {
			var me = this;
			var entryType = _CMCache.getEntryTypeByName(this.className);
			var filter = this.filter || Ext.create('CMDBuild.model.CMFilterModel', {
				entryType: me.className,
				local: true,
				name: CMDBuild.Translation.management.findfilter.newfilter + " " + _CMUtils.nextId()
			});

			_CMCache.getAttributeList(entryType.getId(), function(attributes) {

				// Filter tabs
//				me.view.filterTabAttributes = Ext.create('CMDBuild.view.management.common.filter.CMFilterAttributes', {
//					attributes: attributes,
//					className: me.className
//				});
//				me.view.filterTabRelations = Ext.create('CMDBuild.view.management.common.filter.CMRelations', {
//					attributes: attributes,
//					className: me.className
//				});
//				me.view.filterTabFunctions = Ext.create('CMDBuild.view.management.common.filter.CMFunctions', {
//					attributes: attributes,
//					className: me.className
//				});
//
//				me.view.filterTabs = Ext.create('Ext.tab.Panel', {
//					border: false,
//					items: [
//						me.filterTabAttributes,
//						me.filterTabRelations,
//						me.filterTabFunctions
//					]
//				});
//
//				me.view.items = [
//					me.view.filterTabs
//				];
				me.view.filterTabAttributes.attributes = attributes;
				me.view.filterTabAttributes.className = me.className;
				me.view.filterTabAttributes.doLayout();

				me.view.filterTabRelations.attributes = attributes;
				me.view.filterTabRelations.className = me.className;
				me.view.filterTabRelations.doLayout();

				me.view.filterTabFunctions.attributes = attributes;
				me.view.filterTabFunctions.className = me.className;
				me.view.filterTabFunctions.doLayout();

_debug('drawFilterTabs');
_debug(me.view);
				me.view.doLayout();
			});
		},
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep2', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'workflow',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
//			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep2Delegate', this);

			// Filter tabs
				this.filterTabAttributes = Ext.create('CMDBuild.view.management.common.filter.CMFilterAttributes');
				this.filterTabRelations = Ext.create('CMDBuild.view.management.common.filter.CMRelations');
				this.filterTabFunctions = Ext.create('CMDBuild.view.management.common.filter.CMFunctions');

			this.filterTabs = Ext.create('Ext.tab.Panel', {
				border: false,
				items: [
					this.filterTabAttributes,
					this.filterTabRelations,
					this.filterTabFunctions
				]
			});

			Ext.apply(this, {
				items: [
					this.filterTabs
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.drawFilterTabs();
			}
		}
	});

})();