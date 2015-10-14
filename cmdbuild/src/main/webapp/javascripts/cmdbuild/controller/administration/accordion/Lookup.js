(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Lookup', {
		extend: 'CMDBuild.controller.common.AbstractAccordionController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.accordion.Lookup}
		 */
		accordion: undefined,

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
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @returns {Boolean}
		 */
		onAccordionIsNodeSelectable: function(node) {
			return !node.isRoot(); // Root is hidden by default
		}
	});

})();