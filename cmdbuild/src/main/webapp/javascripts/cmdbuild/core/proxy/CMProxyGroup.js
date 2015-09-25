(function() {

	var tr = CMDBuild.Translation.administration.modsecurity.group;

	Ext.require('CMDBuild.model.CMGroupModels');

	CMDBuild.ServiceProxy.group = {
		read: function(p) {
			p.method = 'GET';
			p.url = "services/json/schema/modsecurity/getgrouplist";
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		save: function(p) {
			p.method = 'POST';
			p.url = "services/json/schema/modsecurity/savegroup";
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		clearRowAndColumnPrivileges: function(p) {
			p.method = 'POST';
			p.url = _CMProxy.url.privileges.classes.clearRowAndColumnPrivileges;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		setRowAndColumnPrivileges: function(p) {
			p.method = 'POST';
			p.url = _CMProxy.url.privileges.classes.setRowAndColumnPrivileges;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		getClassPrivilegesGridStore: function(pageSize) {
			return getGridPrivilegeStore(_CMProxy.url.privileges.classes.read);
		},

		getDataViewPrivilegesGridStore: function() {
			return getGridPrivilegeStore(_CMProxy.url.privileges.dataView.read);
		},

		getFilterPrivilegesGridStore: function() {
			return getGridPrivilegeStore(_CMProxy.url.privileges.filter.read);
		},

		getUserPerGroupStoreForGrid: function() {
			return new Ext.data.Store({
				model : "CMDBuild.cache.CMUserForGridModel",
				autoLoad : false,
				proxy : {
					type : 'ajax',
					url : 'services/json/schema/modsecurity/getgroupuserlist',
					reader : {
						type : 'json',
						root : 'users'
					}
				},
				sorters : [ {
					property : 'username',
					direction : "ASC"
				}]
			});
		},

		getUIConfiguration: function(cbs) {
			cbs = cbs || {};

			CMDBuild.ServiceProxy.core.doRequest({
				url: "services/json/schema/modsecurity/getuiconfiguration",
				method: 'GET',
				success: cbs.success || Ext.emptyFn,
				failure: cbs.failure || Ext.emptyFn,
				callback: cbs.callback || Ext.emptyFn
			});
		},

		getGroupUIConfiguration: function(groupId, cbs) {
			cbs = cbs || {};

			CMDBuild.ServiceProxy.core.doRequest({
				url: "services/json/schema/modsecurity/getgroupuiconfiguration",
				params: {id: groupId},
				method: 'GET',
				success: cbs.success || Ext.emptyFn,
				failure: cbs.failure || Ext.emptyFn,
				callback: cbs.callback || Ext.emptyFn
			});
		},

		saveUIConfiguration: function(groupId, uiConfiguration, cbs) {
			cbs = cbs || {};

			CMDBuild.ServiceProxy.core.doRequest({
				url: "services/json/schema/modsecurity/savegroupuiconfiguration",
				params: {
					id: groupId,
					uiConfiguration: uiConfiguration
				},
				method: 'POST',
				success: cbs.success || Ext.emptyFn,
				failure: cbs.failure || Ext.emptyFn,
				callback: cbs.callback || Ext.emptyFn
			});
		},

		saveClassUiConfiguration: function(p) {
			p.method = 'POST';
			p.url = _CMProxy.url.privileges.classes.saveClassUiConfiguration;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		loadClassUiConfiguration: function(p) {
			p.method = 'GET';
			p.url = _CMProxy.url.privileges.classes.loadClassUiConfiguration;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 * @return {Ext.data.JsonStore}
		 */
		getGroupTypeStore: function() {
			return Ext.create('Ext.data.JsonStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					{
						value: CMDBuild.cache.CMGroupModel.type.NORMAL,
						description: tr.normal
					},
					{
						value: CMDBuild.cache.CMGroupModel.type.ADMIN,
						description: tr.administrator
					},
					{
						value: CMDBuild.cache.CMGroupModel.type.CLOUD_ADMIN,
						description: tr.limited_admin
					}
				]
			});
		},

		/**
		 * @return {Ext.data.Store} classes and processes store
		 */
		getStartingClassStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.CMGroupModels.startingClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: 'classes'
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [{
					property: CMDBuild.core.constants.Proxy.DESCRIPTION,
					direction: 'ASC'
				}]
			});
		}
	};

	function getGridPrivilegeStore(url) {
		return new Ext.data.Store({
			model : "CMDBuild.cache.CMPrivilegeModel",
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : url,
				reader : {
					type : 'json',
					root : 'privileges'
				}
			},
			sorters : [ {
				property : _CMProxy.parameter.PRIVILEGED_OBJ_DESCRIPTION,
				direction : "ASC"
			}]
		});
	}

})();