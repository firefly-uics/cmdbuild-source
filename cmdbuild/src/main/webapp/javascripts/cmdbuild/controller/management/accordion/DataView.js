(function() {

	/**
	 * Adapter class to make this accordion nodes compatible with class module
	 *
	 * TODO: should be refactored implementing a specific view from filter section
	 */
	Ext.define('CMDBuild.controller.management.accordion.DataView', {
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
		onAccordionSelectionChange: function() {
			if (this.view.getSelectionModel().hasSelection()) {
				var adaptedSelectedNodeModel = this.view.getSelectionModel().getSelection()[0].copy();

				// Adapt node to class module
				if (adaptedSelectedNodeModel.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0] == 'filter')
					adaptedSelectedNodeModel.set(CMDBuild.core.constants.Proxy.ID, adaptedSelectedNodeModel.get(CMDBuild.core.constants.Proxy.CLASS_ID));

				if (_CMMainViewportController.bringTofrontPanelByCmName(adaptedSelectedNodeModel.get('cmName'), adaptedSelectedNodeModel) === false) {
					// If the panel was not brought to front (report from the navigation menu), select the previous node or deselect the tree
					if (!Ext.isEmpty(this.lastSelection)) {
						this.view.getSelectionModel().select(this.lastSelection);
					} else {
						this.view.getSelectionModel().deselectAll(true);
					}
				} else {
					this.lastSelection = this.view.getSelectionModel().getSelection()[0];
				};
			}
		}
	});

})();