(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Menu', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group',
			'CMDBuild.core.Utils'
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
		 * @property {CMDBuild.view.administration.accordion.Menu}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Menu', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.core.proxy.userAndGroup.group.Group.readAll({
				scope: this,
				success: function (result, options, decodedResult) {
					decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

					this.view.getStore().getRootNode().removeAll();

					if (!Ext.isEmpty(decodedResult)) {
						CMDBuild.core.Utils.objectArraySort(decodedResult, CMDBuild.core.constants.Proxy.TEXT);

						var nodes = [{
							cmName: this.cmfg('accordionIdentifierGet'),
							text: '* Default *',
							description: '* Default *',
							iconCls: 'cmdbuild-tree-group-icon',
							id: this.cmfg('accordionBuildId', { components: 'default-group' }),
							leaf: true
						}];

						Ext.Array.forEach(decodedResult, function (groupObject, i, allGroupObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmfg('accordionIdentifierGet');
							nodeObject['iconCls'] = 'cmdbuild-tree-group-icon';
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = groupObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = groupObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = groupObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', { components: groupObject[CMDBuild.core.constants.Proxy.ID] });
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = groupObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodes.push(nodeObject);
						}, this);

						if (!Ext.isEmpty(nodes))
							this.view.getStore().getRootNode().appendChild(nodes);

						// Alias of this.callParent(arguments), inside proxy function doesn't work
						this.updateStoreCommonEndpoint(nodeIdToSelect);
					}
				}
			});

			this.callParent(arguments);
		}
	});

})();
