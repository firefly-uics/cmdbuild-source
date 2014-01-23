(function() {

	var tr = CMDBuild.Translation.administration.setup.email, // Path to translation
		delegate = null; // Controller handler

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationEmailGrid", {
		extend: "Ext.grid.Panel",

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{ text: tr.isDefault, dataIndex: 'isDefault', align: 'center', width: '60px', renderer: defaultGridColumnRenderer },
					{ text: tr.name, dataIndex: 'name', flex: 1 },
					{ text: tr.emailAddress, dataIndex: 'address', flex: 1 },
					new Ext.ux.CheckColumn({
						header: tr.active,
						dataIndex: 'isActive',
						cmReadOnly: true,
						width: '60px'
					})
				],
				// TODO: use a server call to get columns from database
				// columns: CMDBuild.ServiceProxy.configuration.email.getStoreColumns(),
				store: CMDBuild.ServiceProxy.configuration.email.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected', {
					'row': row,
					'record': record,
					'index': index
				}, null);
			}
		}
	});

	/**
	 * @param {Object} value
	 * Used to render isDefault database value to add icon
	 */
	function defaultGridColumnRenderer(value) {
		if(typeof value == 'boolean') {
			if(value) {
				value = '<img src="images/icons/tick.png" alt="Is Default" />';
			} else {
				value = null;
			}
		}

		return value;
	}

})();