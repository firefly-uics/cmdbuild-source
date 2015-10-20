(function() {

	Ext.define('CMDBuild.controller.administration.report.Report', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.report.ReportView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @override
		 */
		onViewOnFront: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.view.removeAll(true);

				switch(node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'jasper':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.report.Jasper', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.callParent(arguments);
			}
		}
	});

})();