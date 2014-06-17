(function() {

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep2Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,

		className: undefined,
		filterValues: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Create and draw filter tabs
		 */
		drawFilterTabs: function() {
			var me = this;

			if (this.className) {
				_CMCache.getAttributeList(
					_CMCache.getEntryTypeByName(this.className).getId(),
					function(attributes) {
						me.view.filterTabPanel.removeAll();

						// Filter tabs
						me.view.filterAttributesTab = Ext.create('CMDBuild.view.management.common.filter.CMFilterAttributes', {
							attributes: attributes
						});
						me.view.filterRelationsTab = Ext.create('CMDBuild.view.management.common.filter.CMRelations', {
							className: me.className,
							height: '100%'
						});
						me.view.filterFunctionsTab = Ext.create('CMDBuild.view.management.common.filter.CMFunctions', {
							className: me.className
						});

						// To setup filters values
						if (!Ext.isEmpty(me.filterValues)) {
							if (!Ext.isEmpty(me.view.filterAttributesTab) && !Ext.isEmpty(me.filterValues.attributes))
								me.view.filterAttributesTab.setData(me.filterValues.attributes);

							if (!Ext.isEmpty(me.view.filterRelationsTab) && !Ext.isEmpty(me.filterValues.relations))
								me.view.filterRelationsTab.setData(me.filterValues.relations);

							if (!Ext.isEmpty(me.view.filterFunctionsTab) && !Ext.isEmpty(me.filterValues.functions))
								me.view.filterFunctionsTab.setData(me.filterValues.functions);
						}

						me.view.filterTabPanel.add([me.view.filterAttributesTab, me.view.filterRelationsTab, me.view.filterFunctionsTab]);
						me.view.filterTabPanel.doLayout();
					}
				);
			}
		},

		/**
		 * Function to get filter's datas
		 *
		 * @return (Object) filter's tab datas or null
		 */
		getDataFilters: function() {
			if (
				!Ext.isEmpty(this.view.filterAttributesTab)
				&& !Ext.isEmpty(this.view.filterRelationsTab)
				&& !Ext.isEmpty(this.view.filterFunctionsTab)
			) {
				return {
					attributes: this.view.filterAttributesTab.getData(),
					relations: this.view.filterRelationsTab.getData(),
					functions: this.view.filterFunctionsTab.getData()
				};
			}

			return null;
		},

		/**
		 * To setup all filters
		 *
		 * @param (Object) filterValuesObject
		 *
		 * example:
		 * 		{
		 * 			"attributes": {...},
		 * 			"relations": {...},
		 * 			"functions": {...}
		 * 		}
		 */
		setValueFilters: function(filterValuesObject) {
			this.filterValues = filterValuesObject;
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep2', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		layout: 'fit',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep2Delegate', this);

			this.filterTabPanel = Ext.create('Ext.tab.Panel', {
				border: false
			});

			Ext.apply(this, {
				items: [this.filterTabPanel]
			});

			this.callParent(arguments);
		},

		listeners: {
			// Draw tabs on activate
			activate: function(panel, eOpts) {
				this.delegate.drawFilterTabs();
			}
		}
	});

})();