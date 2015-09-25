(function() {

	Ext.define('CMDBuild.view.administration.accordion.UserAndGroup', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.usersAndGroups,

		constructor: function() {
			this.callParent(arguments);

			this.updateStore();
		},

		listeners: {
			// Set groups root node as unselectable
			beforeselect: function(accordion, record, index, eOpts) {
				return !record.hasChildNodes();
			}
		},

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} node
		 *
		 * @returns {Boolean}
		 *
		 * @override
		 */
		nodeIsSelectable: function(node) {
			return this.callParent(arguments) && !node.hasChildNodes();;
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.group.Group.readAll({
				scope: this,
				success: function(result, options, decodedResult) {
					decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

					var nodes = [];

					Ext.Array.forEach(decodedResult, function(groupObject, i, allGroupObjects) {
						nodes.push({
							text: groupObject[CMDBuild.core.constants.Proxy.TEXT],
							id: groupObject[CMDBuild.core.constants.Proxy.ID],
							iconCls: 'cmdbuild-tree-group-icon',
							cmName: this.cmName,
							leaf: true
						});
					}, this);

					this.getStore().getRootNode().removeAll();
					this.getStore().getRootNode().appendChild([
						{
							text: CMDBuild.Translation.groups,
							iconCls: 'cmdbuild-tree-user-group-icon',
							cmName: this.cmName,
							children: nodes,
							leaf: false
						},
						{
							text: CMDBuild.Translation.users,
							iconCls: 'cmdbuild-tree-user-icon',
							cmName: 'users',
							leaf: true
						}
					]);

					this.getStore().sort();

					if (!Ext.isEmpty(nodeIdToSelect))
						this.selectNodeById(nodeIdToSelect);
				}
			});
		}
	});

})();