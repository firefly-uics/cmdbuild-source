(function() {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.common.AbstractAccordion', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.accordion.Generic'
		],

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: false,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.Generic',

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
					model: this.storeModelName,
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
				if (!Ext.isEmpty(this.delegate))
					this.delegate.cmfg('onAccordionExpand');
			}, this);

			// Add listener to avoid selection of unselectable nodes
			this.on('beforeselect', function(accordion, record, index, eOpts) {
				if (!Ext.isEmpty(this.delegate))
					return this.delegate.cmfg('onAccordionBeforeSelect', record);
			}, this);

			// Add listener for selectionchange
			this.getSelectionModel().on('selectionchange', function(selectionModel, selected, eOpts) {
				if (!Ext.isEmpty(this.delegate))
					this.delegate.cmfg('onAccordionSelectionChange');
			}, this);

			this.updateStore();
		},

		deselect: function() {
			if (!Ext.isEmpty(this.delegate))
				this.delegate.cmfg('onAccordionDeselect');
		},

		/**
		 * @returns {CMDBuild.model.common.accordion.Generic} node or null
		 */
		getFirtsSelectableNode: function() {
			if (!Ext.isEmpty(this.delegate))
				return this.delegate.cmfg('onAccordionGetFirtsSelectableNode');;
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {CMDBuild.model.common.accordion.Generic}
		 */
		getNodeById: function(id) {
			if (!Ext.isEmpty(this.delegate))
				return this.delegate.cmfg('onAccordionGetNodeById', id);
		},

		/**
		 * @returns {Boolean}
		 */
		isEmpty: function() {
			if (!Ext.isEmpty(this.delegate))
				return this.delegate.cmfg('onAccordionIsEmpty', id);
		},

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @returns {Boolean}
		 */
		isNodeSelectable: function(node) {
			if (!Ext.isEmpty(this.delegate))
				return this.delegate.cmfg('onAccordionIsNodeSelectable', node);
		},

		selectFirstSelectableNode: function() {
			if (!Ext.isEmpty(this.delegate))
				this.delegate.cmfg('onAccordionSelectFirstSelectableNode');
		},

		/**
		 * @param {Number} id
		 */
		selectNodeById: function(id) {
			if (!Ext.isEmpty(this.delegate))
				this.delegate.cmfg('onAccordionSelectNodeById', id);
		},

		/**
		 * @param {Number or String} nodeIdToSelect
		 */
		updateStore: function(nodeIdToSelect) {
			if (!Ext.isEmpty(this.delegate))
				this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
		}
	});

})();