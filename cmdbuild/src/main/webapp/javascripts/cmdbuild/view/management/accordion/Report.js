(function() {

	Ext.define('CMDBuild.view.management.accordion.Report', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.report.Report',
			'CMDBuild.model.common.accordion.Report'
		],

		/**
		 * @cfg {CMDBuild.controller.common.abstract.Accordion}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: true,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.Report',

		title: CMDBuild.Translation.report,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.report.Report.getTypesTree({
				loadMask: false,
				scope: this,
				success: function(result, options, decodedResult) {
					if (!Ext.isEmpty(decodedResult) && Ext.isArray(decodedResult)) {
						var nodes = [];

						Ext.Array.forEach(decodedResult, function(reportObject, i, allReportObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmName;
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = reportObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = reportObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = reportObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', { components: reportObject[CMDBuild.core.constants.Proxy.ID] });
							nodeObject[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['custom'];
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = reportObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodes.push(nodeObject);
						}, this);

						this.getStore().getRootNode().removeAll();
						this.getStore().getRootNode().appendChild(nodes);
						this.getStore().sort();

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						if (!Ext.isEmpty(this.delegate))
							this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
					}
				}
			});
		}
	});

})();