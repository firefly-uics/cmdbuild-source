(function() {

	/**
	 * Abstract class to be extended from all accordion menu witch implements a delay to load store before expand action.
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

		/**
		 * @cfg {Boolean}
		 */
		storeLoaded: false,

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
			afteritemexpand: function(node, index, item, eOpts) {
				this.storeLoaded = false; // Restore flag state
			},
			beforeexpand: function(panel, animate, eOpts) {
				return this.beforeExpand(panel, animate, eOpts);
			}
		},

		/**
		 * @param {Ext.panel.Panel} panel
		 * @param {Boolean} animate
		 * @param {Object} eOpts
		 */
		beforeExpand: function(panel, animate, eOpts) {
			return this.storeLoaded; // Stop expand action
		},

		/**
		 * Resumes accordion's expand
		 */
		deferExpand: function() {
			this.storeLoaded = true;

			Ext.Function.defer(function() {
				this.expand();

				// Auto-select first child
				if (!Ext.isEmpty(this.getRootNode()) && !Ext.isEmpty(this.getRootNode().firstChild))
					this.getSelectionModel().select(this.getRootNode().firstChild);
			}, 100, this);
		}
	});

})();