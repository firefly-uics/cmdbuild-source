(function() {

	Ext.define('CMDBuild.controller.administration.accordion.UserAndGroup', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.UserAndGroup}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.UserAndGroup', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.userAndGroup.group.Group.readAll({
				scope: this,
				success: function(result, options, decodedResult) {
					decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

					if (!Ext.isEmpty(decodedResult)) {
						var nodes = [];

						Ext.Array.forEach(decodedResult, function(groupObject, i, allGroupObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmfg('accordionIdentifierGet');
							nodeObject['iconCls'] = 'cmdbuild-tree-group-icon';
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = groupObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = groupObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = groupObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', { components: groupObject[CMDBuild.core.constants.Proxy.ID] });
							nodeObject[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['group'];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodes.push(nodeObject);
						}, this);

						this.view.getStore().getRootNode().removeAll();
						this.view.getStore().getRootNode().appendChild([
							{
								cmName: this.cmfg('accordionIdentifierGet'),
								iconCls: 'cmdbuild-tree-user-group-icon',
								text: CMDBuild.Translation.groups,
								description: CMDBuild.Translation.groups,
								id: undefined, // Disable node selection
								leaf: false,

								children: nodes
							},
							{
								cmName: this.cmfg('accordionIdentifierGet'),
								iconCls: 'cmdbuild-tree-user-icon',
								text: CMDBuild.Translation.users,
								description: CMDBuild.Translation.users,
								id: this.cmfg('accordionBuildId', { components: 'user' }),
								sectionHierarchy: ['user'],
								leaf: true
							}
						]);
						this.view.getStore().sort();

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						this.updateStoreCommonEndpoint(nodeIdToSelect);
					}
				}
			});

			this.callParent(arguments);
		}
	});

})();