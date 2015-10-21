(function() {

	Ext.define('CMDBuild.controller.management.report.Report', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'reportSelectedAccordionGet',
			'reportSelectedAccordionIsEmpty'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @property {Object}
		 */
		sectionController: undefined,

		/**
		 * @property {CMDBuild.model.common.accordion.Report}
		 *
		 * @private
		 */
		selectedAccordion: undefined,

		/**
		 * @cfg {CMDBuild.view.management.report.ReportView}
		 */
		view: undefined,

		// SelectedAccordion property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			reportSelectedAccordionGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			reportSelectedAccordionIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 */
			reportSelectedAccordionSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.report.SelectedAccordion';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} node
		 */
		onViewOnFront: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.reportSelectedAccordionSet({ value: node.getData() });

				this.view.removeAll(true);

				switch (this.reportSelectedAccordionGet(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'custom':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.management.report.Custom', { parentDelegate: this });
					}
				}

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.view.add(this.sectionController.getView());

				this.sectionController.cmfg('onReportAccordionSelect');

				this.callParent(arguments);
			}
		}
	});

})();