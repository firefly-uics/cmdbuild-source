(function() {

	Ext.define('CMDBuild.view.administration.accordion.UserAndGroup', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.common.abstract.Accordion}
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
							var nodeObject = {};
							nodeObject['cmName'] = this.cmName;
							nodeObject['iconCls'] = 'cmdbuild-tree-group-icon';
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = groupObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = groupObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = groupObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', { components: groupObject[CMDBuild.core.constants.Proxy.ID] });
							nodeObject[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['group'];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodes.push(nodeObject);
						}, this);

						this.getStore().getRootNode().removeAll();
						this.getStore().getRootNode().appendChild([
							{
								cmName: this.cmName,
								iconCls: 'cmdbuild-tree-user-group-icon',
								text: CMDBuild.Translation.groups,
								description: CMDBuild.Translation.groups,
								id: undefined, // Disable node selection
								leaf: false,

								children: nodes
							},
							{
								cmName: this.cmName,
								iconCls: 'cmdbuild-tree-user-icon',
								text: CMDBuild.Translation.users,
								description: CMDBuild.Translation.users,
								id: this.delegate.cmfg('accordionBuildId', { components: 'user' }),
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