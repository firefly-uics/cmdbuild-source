(function() {

	Ext.define('CMDBuild.view.administration.accordion.Groups', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.accordion.CMGroupAccordionController}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.administration.modsecurity.title,

		initComponent: function() {
			this.callParent(arguments);

			this.delegate = Ext.create('CMDBuild.controller.accordion.Groups', this);
		},

		/**
		 * @return {Array}
		 *
		 * @override
		 */
		buildTreeStructure: function() {
			var nodes = [];

			Ext.Object.each(_CMCache.getGroups(), function(id, group, myself) {
				nodes.push({
					text: group.get(CMDBuild.core.proxy.Constants.TEXT),
					id: group.get(CMDBuild.core.proxy.Constants.ID),
					iconCls: 'cmdbuild-tree-group-icon',
					cmName: 'group',
					leaf: true
				});
			}, this);

			return [
				{
					text: CMDBuild.Translation.administration.modsecurity.groups,
					iconCls: 'cmdbuild-tree-user-group-icon',
					cmName: 'group',
					children: nodes,
					leaf: false
				},
				{
					text: CMDBuild.Translation.administration.modsecurity.users,
					iconCls: 'cmdbuild-tree-user-icon',
					cmName: 'users',
					leaf: true
				}
			];

		}
	});

})();