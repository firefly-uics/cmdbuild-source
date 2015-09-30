(function() {

	Ext.define('CMDBuild.view.administration.accordion.UserAndGroup', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.usersAndGroups,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.userAndGroup.group.Group.readAll({
				scope: this,
				success: function(result, options, decodedResult) {
					decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

					var nodes = [];

					Ext.Array.forEach(decodedResult, function(groupObject, i, allGroupObjects) {
						nodes.push({
							text: groupObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
							description: groupObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
							id: groupObject[CMDBuild.core.constants.Proxy.ID],
							name: groupObject[CMDBuild.core.constants.Proxy.NAME],
							iconCls: 'cmdbuild-tree-group-icon',
							cmName: this.cmName,
							sectionHierarchy: ['group'],
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
							sectionHierarchy: ['group'],
							leaf: false
						},
						{
							text: CMDBuild.Translation.users,
							iconCls: 'cmdbuild-tree-user-icon',
							cmName: this.cmName,
							sectionHierarchy: ['user'],
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