(function() {

	Ext.require(['CMDBuild.bim.proxy.Bim']);

	Ext.define("CMDBuild.delegate.administration.bim.CMBIMGridConfigurator", {

		getStore: function() {
			if (this.store == null) {
				this.store = CMDBuild.bim.proxy.Bim.getStore();
			}

			return this.store;
		},

		/**
		 * @return an array of Ext.grid.column.Column to use for the grid
		 */
		getColumns: function() {
			var columns = [{
				header: CMDBuild.Translation.administration.modClass.attributeProperties.name,
				dataIndex: CMDBuild.core.constants.Proxy.NAME,
				flex: 1
			}, {
				header: CMDBuild.Translation.administration.modClass.attributeProperties.description,
				dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
				flex: 1
			}];

			columns.push({
				dataIndex: "active",
				header: CMDBuild.Translation.active,
				flex: 1
			}, {
				dataIndex: "lastCheckin",
				header: CMDBuild.Translation.last_checkin,
				flex: 1
			}, {
				header: '&nbsp',
				sortable: false,
				tdCls: "grid-button",
				fixed: true,
				menuDisabled: true,
				hideable: false,
				header: "",
				align: "center",
				width: 40,
				renderer: renderIfcIcon
			});

			return columns;
		}
	});
	function renderIfcIcon(value, metadata, record) {
			return '<img style="cursor:pointer" title="'+CMDBuild.Translation.download_ifc_file+'" class="action-download-ifc" src="images/icons/downloadifc.png"/>';

	}
})();
