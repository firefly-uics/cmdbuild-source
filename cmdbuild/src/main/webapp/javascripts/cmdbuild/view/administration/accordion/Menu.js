(function() {

	Ext.define('CMDBuild.view.administration.accordion.Menu', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.menu,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.userAndGroup.group.Group.readAll({
				loadMask: false,
				scope: this,
				success: function(result, options, decodedResult) {
					decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

					if (!Ext.isEmpty(decodedResult)) {
						CMDBuild.core.Utils.objectArraySort(decodedResult, CMDBuild.core.constants.Proxy.TEXT);

						var nodes = [{
							cmName: this.cmName,
							iconCls: 'cmdbuild-tree-group-icon',
							id: 0,
							leaf: true,
							text: '* Default *'
						}];

						Ext.Array.forEach(decodedResult, function(groupObject, i, allGroupObjects) {
							nodes.push({
								cmName: this.cmName,
								iconCls: 'cmdbuild-tree-group-icon',
								id: groupObject[CMDBuild.core.constants.Proxy.ID],
								leaf: true,
								name: groupObject[CMDBuild.core.constants.Proxy.NAME],
								text: groupObject[CMDBuild.core.constants.Proxy.TEXT]
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