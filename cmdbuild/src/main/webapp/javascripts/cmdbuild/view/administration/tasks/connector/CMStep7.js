(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep7Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,
//		className: undefined,
		filterValues: undefined,

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
		 * @return (Object) filter's tab datas
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

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep7', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep7Delegate', this);

			this.deletionTypeCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.DELETION_TYPE,
				fieldLabel: tr.taskConnector.deletionType,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyTasks.getDeletionTypes(),
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false
			});

			this.operationsCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.OPERATIONS,
				fieldLabel: tr.taskConnector.operations,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyTasks.getOperations(),
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false
			});

			this.filterTabPanel = Ext.create('Ext.tab.Panel', {
				border: false
			});

			Ext.apply(this, {
				items: [
					this.deletionTypeCombo,
					this.operationsCombo,
					this.filterTabPanel
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			// Draw tabs on show
			show: function(panel, eOpts) {
				this.delegate.drawFilterTabs();
			}
		}
	});

})();
