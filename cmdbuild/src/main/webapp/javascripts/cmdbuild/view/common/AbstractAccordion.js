(function() {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.common.AbstractAccordion', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.AccordionStore'
		],

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

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
					autoLoad: true,
					model: 'CMDBuild.model.common.AccordionStore',
					root: {
						expanded: true,
						children: []
					},
					sorters: [
						{ property: 'cmIndex', direction: 'ASC' },
						{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
					]
				})
			});

			this.callParent(arguments);

			// Add listener for accordion expand
			this.on('expand', function(accordion, eOpts) {
				this.delegate.cmfg('onAccordionExpand');
			}, this);

			// Add listener to avoid selection of unselectable nodes
			this.on('beforeselect', function(accordion, record, index, eOpts) {
				return this.delegate.cmfg('onAccordionBeforeSelect', record);
			}, this);

			// Add listener for selectionchange
			this.getSelectionModel().on('selectionchange', function(selectionModel, selected, eOpts) {
				this.delegate.cmfg('onAccordionSelectionChange');
			}, this);

			this.updateStore();
		},

		deselect: function() {
			this.delegate.cmfg('onAccordionDeselect');
		},

		/**
		 * @returns {CMDBuild.model.common.AccordionStore} node or null
		 */
		getFirtsSelectableNode: function() {
			return this.delegate.cmfg('onAccordionGetFirtsSelectableNode');;
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {CMDBuild.model.common.AccordionStore}
		 */
		getNodeById: function(id) {
			return this.delegate.cmfg('onAccordionGetNodeById', id);
		},

		/**
		 * @returns {Boolean}
		 */
		isEmpty: function() {
			return this.delegate.cmfg('onAccordionIsEmpty', id);
		},

		/**
		 * @param {CMDBuild.model.common.AccordionStore} node
		 *
		 * @returns {Boolean}
		 */
		isNodeSelectable: function(node) {
			return this.delegate.cmfg('onAccordionIsNodeSelectable', node);
		},

		selectFirstSelectableNode: function() {
			this.delegate.cmfg('onAccordionSelectFirstSelectableNode');
		},

		/**
		 * @param {Number} id
		 */
		selectNodeById: function(id) {
			this.delegate.cmfg('onAccordionSelectNodeById', id);
		},

		/**
		 * This is a controller function but must stay here because of not compatibility with CMDBuild view/controller automatic instantiation
		 *
		 * @param {Number} nodeIdToSelect
		 */
		updateStore: function(nodeIdToSelect) {
			if (!Ext.isEmpty(nodeIdToSelect))
				this.selectNodeById(nodeIdToSelect);
		}
	});

})();