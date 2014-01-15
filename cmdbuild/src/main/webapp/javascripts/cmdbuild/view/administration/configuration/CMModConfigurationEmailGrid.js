(function() {

	var tr = CMDBuild.Translation.administration.setup.email; // Path to translation

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationEmailGrid", {
		extend: "Ext.grid.Panel",

		border: false,
		frame: false,
		cls: "cmborderbottom",

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{ text: tr.isDefault,  dataIndex: 'isDefault', align: 'center', width: '60px', renderer: defaultGridRenderer },
					{ text: tr.name, dataIndex: 'name', flex: 1 },
					{ text: tr.emailAddress, dataIndex: 'address', flex: 1 },
					{ text: tr.active, dataIndex: 'isActive', width: '60px', xtype: 'checkcolumn' }
				],
//				store : CMDBuild.ServiceProxy.group.getUserStoreForGrid()
				store: Ext.data.Store({
					autoLoad: true,
					fields: ['isDefault', 'name', 'address', 'isActive'],
					data: {
						'items': [
							{ 'isDefault': false, 'name': 'Email account name A', 'address': 'email.account.a@tecnoteca.com', 'isActive': true },
							{ 'isDefault': true, 'name': 'Email account name B', 'address': 'email.account.b@tecnoteca.com', 'isActive': true },
							{ 'isDefault': false, 'name': 'Email account name C', 'address': 'email.account.c@tecnoteca.com', 'isActive': false },
							{ 'isDefault': false, 'name': 'Email account name D', 'address': 'email.account.d@tecnoteca.com', 'isActive': false }
						]
					},
					proxy: {
						type: 'memory',
						reader: {
							type: 'json',
							root: 'items'
						}
					}
				})
			});

			this.callParent(arguments);
		}
	});

	/**
	 * @param {Database value} boolean
	 * Used to render isDefault database value to add icon
	 */
	function defaultGridRenderer(value) {
		if(typeof value == "boolean") {
			if(value) {
				value = "<img src='images/icons/tick.png' alt='Is Default'/>";
			} else {
				value = null;
			}
		}

		return value;
	}

})();