(function() {

	/**
	 * Abstract class to be extended from all accordion menu
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.view.common.accordion.Abstract', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.model.common.AccordionStore'
		],

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		animCollapse: false,
		autoRender: true,
		border: true,
		floatable: false,
		layout: 'border',
		rootVisible: false,

		bodyStyle: {
			background: '#ffffff'
		},

		initComponent: function() {
			Ext.apply(this, {
				store: Ext.create('Ext.data.TreeStore', {
					model: 'CMDBuild.model.common.AccordionStore',

					root: {
						expanded: true,
						children: []
					},

					sorters: [
						{ property: 'cmIndex', direction: 'ASC' },
						{ property: CMDBuild.core.proxy.Constants.TEXT, direction: 'ASC' }
					]
				})
			});

			this.callParent(arguments);
		},

		listeners: {
			beforeexpand: function(panel, animate, eOpts) {
				_error('beforeexpand event not implemented', this);
			}
		}
	});

})();