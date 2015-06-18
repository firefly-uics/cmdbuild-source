(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.SectionBase', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {String}
		 */
		sectionId: undefined,

		/**
		 * @param {CMDBuild.model.localizations.advancedTable.TreeStore} startNode
		 * @param {Number} levelToReach
		 *
		 * @returns {CMDBuild.model.localizations.advancedTable.TreeStore} requestedNode or null
		 */
		getLevelNode: function(startNode, levelToReach) {
			var requestedNode = startNode;

			if (!Ext.isEmpty(requestedNode) && Ext.isNumber(levelToReach)) {
				while (requestedNode.getDepth() > levelToReach) {
					requestedNode = requestedNode.get(CMDBuild.core.proxy.CMProxyConstants.PARENT);
				}

				return requestedNode;
			}

			return null;
		},

		/**
		 * @returns {String}
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