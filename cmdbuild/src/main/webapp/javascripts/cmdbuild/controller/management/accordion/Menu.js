(function() {

	Ext.define('CMDBuild.controller.management.accordion.Menu', {
		extend: 'CMDBuild.controller.common.AbstractAccordionController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onAccordionBeforeSelect',
			'onAccordionDeselect',
			'onAccordionExpand',
			'onAccordionGetFirtsSelectableNode',
			'onAccordionGetNodeById',
			'onAccordionIsEmpty',
			'onAccordionIsNodeSelectable',
			'onAccordionSelectFirstSelectableNode',
			'onAccordionSelectionChange',
			'onAccordionSelectNodeById',
			'onAccordionUpdateStore'
		],

		/**
		 * @property {CMDBuild.view.management.accordion.DataView}
		 */
		view: undefined,

		/**
		 * @override
		 */
		onAccordionExpand: function() {
			_CMMainViewportController.bringTofrontPanelByCmName('class');

			this.callParent(arguments);
		},

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @returns {Boolean}
		 *
		 * @override
		 */
		onAccordionIsNodeSelectable: function(node) {
			return (
				!(node.getDepth() == 1 && node.hasChildNodes())
				&& !node.isRoot() // Root is hidden by default
			);
		}
	});

})();