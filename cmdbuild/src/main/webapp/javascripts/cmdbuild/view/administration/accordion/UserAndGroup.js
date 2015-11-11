(function() {

	Ext.define('CMDBuild.view.administration.accordion.UserAndGroup', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.accordion.CMGroupAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.usersAndGroups,

		listeners: {
			// Set groups root node as unselectable
			beforeselect: function(accordion, record, index, eOpts) {
				return !record.hasChildNodes();
			}
		},

		/**
		 * @return {Array}
		 *
		 * @override
		 */
		buildTreeStructure: function() {
			var nodes = [];

			Ext.Object.each(_CMCache.getGroups(), function(id, group, myself) { // TODO: refactor to avoid cache usage
				nodes.push({
					text: group.get(CMDBuild.core.proxy.CMProxyConstants.TEXT),
					id: group.get(CMDBuild.core.proxy.CMProxyConstants.ID),
					iconCls: 'cmdbuild-tree-group-icon',
					cmName: this.cmName,
					leaf: true
				});
			}, this);

			return [
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
			];

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
		}
	});

})();