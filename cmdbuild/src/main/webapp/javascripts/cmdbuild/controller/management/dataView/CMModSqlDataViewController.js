(function() {

	Ext.define("CMDBuild.controller.management.dataView.CMModCardController", {
		extend: "CMDBuild.controller.management.common.CMModController",

		constructor: function() {
			this.callParent(arguments);
		},

		onViewOnFront: function(node) {
			if (node) {
				var sourceFunction = node.get("sourceFunction");
				var outputConfiguration = _CMCache.getDataSourceOutput(sourceFunction);

				this.view.configureGrid( //
						getStore(outputConfiguration, sourceFunction), //
						getColumns(outputConfiguration)
					);

			}
		}
	
	});

	function getStore(outputConfiguration, functionName) {
		return new Ext.data.Store({
			fields: outputConfiguration,
			pageSize: _CMUtils.grid.getPageSize(),
			remoteSort: true,
			autoLoad: true,
			proxy: {
				type: "ajax",
				url: "services/json/management/modcard/getsqlcardlist",
				reader: {
					root: "cards",
					type: "json",
					totalProperty: "results",
				},
				extraParams: {
					"function": functionName
				}
			}
		});
	}

	function getColumns(outputConfiguration) {
		var columns = [];

		for (var i=0, l=outputConfiguration.length; i<l; ++i) {
			var attribute = outputConfiguration[i];
			columns.push({
				header: attribute.name,
				dataIndex: attribute.name,
				flex: 1
			});
		}

		return columns;
	}
})();