(function() {

	Ext.define('CMDBuild.view.management.accordion.Report', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.report.Report',
			'CMDBuild.model.common.accordion.Report'
		],

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
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

						Ext.Array.forEach(decodedResult, function(groupObject, i, allGroupObjects) {
							nodes.push({
								text: groupObject[CMDBuild.core.constants.Proxy.TEXT],
								description: groupObject[CMDBuild.core.constants.Proxy.TEXT],
								name: groupObject[CMDBuild.core.constants.Proxy.NAME],
								cmName: this.cmName,
								sectionHierarchy: ['custom'],
								type: 'custom',
								leaf: groupObject[CMDBuild.core.constants.Proxy.LEAF]
							});
						}, this);

						this.getStore().getRootNode().removeAll();
						this.getStore().getRootNode().appendChild(nodes);

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
					}
				}
			});
		}
	});

})();