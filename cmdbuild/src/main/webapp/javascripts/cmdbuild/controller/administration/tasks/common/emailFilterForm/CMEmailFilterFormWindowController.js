(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormWindowController', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		type: undefined,
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
				case 'onAddFilter':
					return this.onAddFilter();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Builds window input and button item
		 *
		 * @param (Array) values
		 * @return (Array) items
		 */
		buildWindowItem: function(values) {
			var me = this;
			var items = [];

			if (typeof values === 'undefined') {
				values = [''];
			}

			for (key in values) {
				items.push({
					frame: false,
					border: false,
					layout: 'hbox',

					defaults: {
						xtype: 'textfield'
					},

					items: [
						{
							itemId: 'filter',
							flex: 1,
							value: values[key],
							listeners: {
								change: function() {
									me.parentDelegate.cmOn(
										'on' + me.type + 'FilterChange',
										me.view.contentComponent.getForm().getValues(),
										Ext.emptyFn()
									);
								}
							}
						},
						{
							xtype: 'button',
							iconCls: 'delete',
							width: 22,
							handler: function() {
								// HACK: to reset deleted textarea's value, probably for a bug the item is just hided
								this.up('panel').down('#filter').setValue('');

								// Remove input's panel container from form
								this.up('form').remove(this.up('panel').id);

								me.parentDelegate.cmOn(
									'onFilterChange',
									me.view.contentComponent.getForm().getValues(),
									Ext.emptyFn()
								);
							}
						}
					]
				});
			}

			return items;
		},

		onAddFilter: function() {
			this.view.contentComponent.add(this.buildWindowItem());
			this.view.contentComponent.doLayout();
		}
	});

})();