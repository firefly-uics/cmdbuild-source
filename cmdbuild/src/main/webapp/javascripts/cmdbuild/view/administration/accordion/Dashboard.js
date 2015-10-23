(function() {

	Ext.define('CMDBuild.view.administration.accordion.Dashboard', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.accordion.Dashboard'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Dashboard}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.Dashboard',

		title: CMDBuild.Translation.dashboard,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.ServiceProxy.Dashboard.fullList({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.DASHBOARDS] || [];

					var nodes = [];

					if (!Ext.isEmpty(decodedResponse)) {
						Ext.Object.each(decodedResponse, function(id, dashboardObject, myself) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmName;
							nodeObject['iconCls'] = 'cmdbuild-tree-dashboard-icon';
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = dashboardObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = dashboardObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = id;
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.delegate.cmfg('accordionBuildId', id);
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = dashboardObject[CMDBuild.core.constants.Proxy.NAME];
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