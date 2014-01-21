(function() {

	CMDBuild.ServiceProxy.configuration.email = {
		createEmailAccount: function(params) {
			params.url = CMDBuild.ServiceProxy.url.configuration.email.create;

			CMDBuild.ServiceProxy.core.submitForm(params);
		},

//		getStore: function() {
//			return new Ext.data.Store({
//				model: 'CMDBuild.model.CMConfigurationEmailModel',
//				autoLoad: true,
//				proxy: {
//					type: 'ajax',
//					url: CMDBuild.ServiceProxy.url.configuration.email.read,
//					reader: {
//						type: 'json',
//						root: 'rows'
//					}
//				},
//				sorters: [{
//					property: 'name',
//					direction: "ASC"
//				}]
//			});
//		},

		getStore: function() {
			return Ext.data.Store({
				autoLoad: true,
				model: 'CMDBuild.model.CMConfigurationEmailModel',
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
			});
		},

		// TODO: to implement for dynamic columns object build with ExtJs grid column configuration
		getStoreColumns: function() {},

		removeEmailAccount: function(params) {
			params.url = CMDBuild.ServiceProxy.url.configuration.email.remove;

			CMDBuild.ServiceProxy.core.submitForm(params);
		},

		updateEmailAccount: function(params) {
			params.url = CMDBuild.ServiceProxy.url.configuration.email.update;

			CMDBuild.ServiceProxy.core.submitForm(params);
		}
	};

})();