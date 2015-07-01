(function() {

	var _idCount = 0;

	Ext.define('CMDBuild.view.management.dataView.CMDataViewAccordion', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.proxy.Constants'],

		cmName: 'dataView',

		title: CMDBuild.Translation.views,
		excludeSimpleTables: false,

		/**
		 * @override
		 */
		afterUpdateStore: function() {},

		/**
		 * @param {Object} viewConfiguration
		 *
		 * @return {Object} node or null
		 */
		buildNodeConf: function(viewConfiguration) {
			var node = {
				text: viewConfiguration[CMDBuild.core.proxy.Constants.DESCRIPTION],
				tableType: 'standard',
				leaf: true
			};

			if (viewConfiguration[CMDBuild.core.proxy.Constants.TYPE] == 'FILTER') {
				node[CMDBuild.core.proxy.Constants.VIEW_TYPE] = 'FILTER';

				var entryType = _CMCache.getEntryTypeByName(viewConfiguration[CMDBuild.core.proxy.Constants.SOURCE_CLASS_NAME]);

				if (Ext.Object.isEmpty(entryType)) {
					return null;
				}

				node[CMDBuild.core.proxy.Constants.ID] = entryType.getId();
				node[CMDBuild.core.proxy.Constants.FILTER] = viewConfiguration.filter;
				node.cmName = 'class'; // To act as a regular class node
			} else {
				node[CMDBuild.core.proxy.Constants.VIEW_TYPE] = 'SQL';
				node[CMDBuild.core.proxy.Constants.SOURCE_FUNCTION] = viewConfiguration[CMDBuild.core.proxy.Constants.SOURCE_FUNCTION];
				node.cmName = 'dataView';
			}

			node[CMDBuild.core.proxy.Constants.ID] = addProgressiveNumberToId(node[CMDBuild.core.proxy.Constants.ID]);

			return node;
		},

		/**
		 * @param {Array} items
		 *
		 * @return {Array} children
		 *
		 * @override
		 */
		buildTreeStructure: function(items) {
			var children = [];

			for (var i = 0; i < items.length; ++i) {
				var configurationNode = this.buildNodeConf(items[i]);

				if (!Ext.Object.isEmpty(configurationNode))
					children.push(configurationNode);
			}

			return children;
		}
	});

	/**
	 * @param {Int} cmdbuildId
	 */
	function addProgressiveNumberToId(cmdbuildId) {
		return _idCount++ + '#' + cmdbuildId;
	}

})();