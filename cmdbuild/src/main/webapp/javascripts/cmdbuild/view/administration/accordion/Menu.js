(function() {

	Ext.define('CMDBuild.view.administration.accordion.Menu', {
		extend: 'CMDBuild.view.common.accordion.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.group.Group',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.menu,

		/**
		 * @param {Ext.panel.Panel} panel
		 * @param {Boolean} animate
		 * @param {Object} eOpts
		 *
		 * @override
		 */
		beforeExpand: function(panel, animate, eOpts) {
			CMDBuild.core.proxy.group.Group.readAll({
				scope: this,
				success: function(response, options, decodedResponse) {
					Ext.suspendLayouts();

					CMDBuild.core.Utils.objectArraySort(decodedResponse.groups, CMDBuild.core.constants.Proxy.TEXT);

					var out = [{
						cmName: this.cmName,
						iconCls: 'cmdbuild-tree-group-icon',
						id: 0,
						leaf: true,
						text: '* Default *'
					}];

					Ext.Object.each(decodedResponse.groups, function(key, group, myself) {
						out.push({
							cmName: this.cmName,
							iconCls: 'cmdbuild-tree-group-icon',
							id: group[CMDBuild.core.constants.Proxy.ID],
							leaf: true,
							name: group[CMDBuild.core.constants.Proxy.NAME],
							text: group[CMDBuild.core.constants.Proxy.TEXT]
						});
					}, this);

					this.getStore().getRootNode().removeAll();
					this.getStore().getRootNode().appendChild(out);

					Ext.resumeLayouts(true);

					this.deferExpand();
				}
			});

			return this.callParent(arguments);
		}
	});

})();