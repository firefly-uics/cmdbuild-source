(function() {

	Ext.define('CMDBuild.controller.administration.dataView.DataView', {
		extend: 'CMDBuild.controller.common.abstract.BasePanel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onDataViewModuleInit = onModuleInit'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.dataView.DataViewView}
		 */
		view: undefined,

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @override
		 */
		onDataViewModuleInit: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.view.removeAll(true);

				switch (node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'sql': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.dataView.Sql', { parentDelegate: this });
					} break;

					case 'filter':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.dataView.Filter', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.onModuleInit(node); // Custom callParent() implementation
			}
		}
	});

})();