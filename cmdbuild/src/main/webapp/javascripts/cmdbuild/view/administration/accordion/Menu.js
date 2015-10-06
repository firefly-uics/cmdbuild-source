(function() {

	Ext.define('CMDBuild.view.administration.accordion.Menu', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.menu,

		constructor: function() {
			this.callParent(arguments);

			this.updateStore();
		},

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

					if (!Ext.isEmpty(nodeIdToSelect))
						this.selectNodeById(nodeIdToSelect);
				}
			});
		}
	});

})();