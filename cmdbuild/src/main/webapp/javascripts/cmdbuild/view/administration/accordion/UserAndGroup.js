(function() {

	Ext.define('CMDBuild.view.administration.accordion.UserAndGroup', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

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
				loadMask: false,
				scope: this,
				success: function(result, options, decodedResult) {
					decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

					if (!Ext.isEmpty(decodedResult)) {
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

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						if (!Ext.isEmpty(this.delegate))
							this.delegate.cmfg('onAccordionUpdateStore', nodeIdToSelect);
					}
				}
			});
		}
	});

})();