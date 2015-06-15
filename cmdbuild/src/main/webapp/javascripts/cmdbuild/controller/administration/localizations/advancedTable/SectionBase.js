(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionBase', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {String}
		 */
		sectionId: undefined,

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 *
		 * @return {CMDBuild.model.localizations.advancedTable.TreeStore} node or null
		 */
		getFirstLevelNode: function(node) {
			if (!Ext.isEmpty(node)) {
				while (node.getDepth() > 1) {
					node = node.get(CMDBuild.core.proxy.CMProxyConstants.PARENT);
				}

				return node;
			}

			return null;
		},

		/**
		 * @return {String}
		 */
		getSectionId: function() {
			return this.sectionId;
		},

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} node
		 */
		onAdvancedTableNodeExpand: function(node) {
			if (!Ext.isEmpty(node) && node.getDepth() == 1) { // Entity node (class, domain, ...)
				this.nodeExpandLevel1(node);
			} else if (!Ext.isEmpty(node) && node.getDepth() == 2) { // Attributes main node
				this.nodeExpandLevel2(node);
			}
		}
	});

})();