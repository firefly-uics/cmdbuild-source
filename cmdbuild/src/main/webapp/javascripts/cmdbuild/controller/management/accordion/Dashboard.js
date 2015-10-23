(function() {

	Ext.define('CMDBuild.controller.management.accordion.Dashboard', {
		extend: 'CMDBuild.controller.common.AbstractAccordionController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.management.accordion.Dashboard}
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
			'accordionBuildId',
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
		 * @param {CMDBuild.model.common.accordion.Dashboard} node
		 *
		 * @returns {Boolean}
		 */
		onAccordionIsNodeSelectable: function(node) {
			return  !node.isRoot(); // Root is hidden by default
		}
	});

})();