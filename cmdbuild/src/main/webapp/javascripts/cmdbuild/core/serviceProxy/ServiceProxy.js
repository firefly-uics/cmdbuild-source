(function() {

Ext.ns("CMDBuild.ServiceProxy");

var lookupFields = {
	Id: 'Id',
	Code: 'Code',
	Description: 'Description',
	ParentId: 'ParentId',
	Index: 'Number',
	Type: 'Type',
	ParentId: 'ParentId',
	ParentDescription: 'ParentDescription',
	Active: 'Active',
	Notes: 'Notes'
};

CMDBuild.ServiceProxy = {
	doLogin : function(p) {
		CMDBuild.Ajax.request( {
			important: true,
			url: "services/json/login/login",
			method: "POST",
			params: p.params,
			success: p.success || Ext.emptyFn,
			failure: p.failure || Ext.emptyFn,
			callback: p.callback || Ext.emptyFn,
			scope: p.scope || this
		});
	},
	
	// TODO duplicate in card section, remove this
	getCardList: function(p) {
		CMDBuild.Ajax.request( {
		    url: "services/json/management/modcard/getcardlist",
		    method: "GET",
		    params: p.params,
		    success: p.success,
	        failure: p.failure,
	        callback: p.callback
		});
	},

	getFeature: function(classId, cardId, success, failure, callback) {
		CMDBuild.Ajax.request({
			url: 'services/json/gis/getfeature',
	        params: {
	            "IdClass": classId,
	            "Id": cardId
	        },
	        method: 'GET',
	        success: success,
	        failure: failure,
	        callback: callback
		});
	},
	
	getGeoCardList: function(classId, success, failure, callback) {
		CMDBuild.Ajax.request({
			scope : this,
			important: true,
			url : 'services/json/gis/getgeocardlist',
			params : {
				"idClass": classId		
			},
			method: 'GET',
			success: success,
			failure: failure,
			callback: callback
		});
	},
	
	getFKTargetingClass: function(option) {
		var conf = Ext.apply({
			url: 'services/json/schema/modclass/getfktargetingclass',
			method : 'GET'
		}, option);
		CMDBuild.Ajax.request(conf);
	},
	
	getDomainList: function(option) {
		var conf = Ext.apply({
			url: 'services/json/schema/modclass/getdomainlist',
			method : 'GET'
		}, option);
		CMDBuild.Ajax.request(conf);
	},
	
	saveLayerVisibility: function(p) {
		CMDBuild.Ajax.request({
			scope : this,
			important: true,
			url : 'services/json/gis/setlayervisibility',
			params : {
				"idClass": p.classId,
				"master": p.master,
				"featureTypeName": p.featureTypeName,
				"visible": p.checked
			},
			method: 'POST',
			success: p.success || Ext.emptyFn,
			failure: p.failure || Ext.emptyFn,
			callback: p.callback || Ext.emptyFn
		});
	},
	
	saveLayerOrder: function(p) {
		CMDBuild.Ajax.request({
			scope : this,
			important: true,
			url: 'services/json/gis/setlayersorder',
			params: {
				"oldIndex": p.oldIndex,
				"newIndex": p.newIndex
			},
			method: 'POST',
			success: p.success || Ext.emptyFn,
			failure: p.failure || Ext.emptyFn,
			callback: p.callback || Ext.emptyFn
		});
	},
	
	// Workflow
	workflow: {
		getstartactivitytemplate: function(p) {
			Ext.Ajax.request({
				url: 'services/json/management/modworkflow/getstartactivitytemplate',
				method: 'GET',
				params : {
					idClass : p.classId,
					id : -1
				},
				success: p.success || Ext.emptyFn,
				failure: p.failure || Ext.emptyFn,
				callback: p.callback || Ext.emptyFn
			});
		},
		terminateActivity: function(p) {
			Ext.Ajax.request({
    			url: 'services/json/management/modworkflow/abortprocess',
                params: {
					ProcessInstanceId: p.ProcessInstanceId,
					WorkItemId: p.WorkItemId
				},
                method: 'POST',                  
                success: p.success || Ext.emptyFn,
				failure: p.failure || Ext.emptyFn,
				callback: p.callback || Ext.emptyFn
      	 	});
		},
		startProcess: function(p) {
			Ext.Ajax.request({
				url: 'services/json/management/modworkflow/startprocess',
	            params: {
					idClass : p.idClass,
	                id : -1
				},
	            method: 'POST',                  
	            success: p.success || Ext.emptyFn,
				failure: p.failure || Ext.emptyFn,
				callback: p.callback || Ext.emptyFn
	  	 	});
		}
	},
	
	// store builders

	/**
	 * @param classId (optional) adds visibility on the specified class
	 */ 
	getAllLayerStore: function() {
		var layerStore =  new Ext.data.Store({
			model: "GISLayerModel",
			proxy: {
				type: "ajax",
				url: "services/json/gis/getalllayers",
				reader: {
					type: "json",
					root: "layers"
				}
			},
			//TODO 3 to 4 check the layers grid
//			fields: ["maxZoom", "minZoom", "style", "description", "index",
//			         "name", "masterTableId", "type", "masterTableName", "isvisible"],
			autoLoad: false,
			sorters: {
				property: 'index',
				direction: 'ASC'
			}
		});
		
		var reload = function() {
			this.load();
		};
		
		_CMEventBus.subscribe("cmdb-new-geoattr", reload, layerStore);
		_CMEventBus.subscribe("cmdb-modify-geoattr", reload, layerStore);
		_CMEventBus.subscribe("cmdb-delete-geoattr", reload, layerStore);
		_CMEventBus.subscribe("cmdb-geoservices-config-changed", reload, layerStore);
		_CMEventBus.subscribe("cmdb-modified-geoserverlayers", reload, layerStore);
		return layerStore;
	},
	
	LOOKUP_FIELDS: lookupFields
};

CMDBuild.ServiceProxy.core = {
	submitForm: function(p) {
		if (p.form) {
			p.form.submit({
				url: p.url,
				method: p.mothod,
				scope: p.scope || this,
				success: p.success || Ext.emptyFn,
				failure: p.failure || Ext.emptyFn,
				callback: p.callback || Ext.emptyFn
			});	
		} else {
			throw CMDBuild.core.error.serviceProxy.NO_FORM;
		}
	},
	doRequest: function(p) {
		CMDBuild.Ajax.request( {
			url: p.url,
			method: p.method,
			params: p.params || {},
			scope: p.scope || this,
			success: p.success || Ext.emptyFn,
			failure: p.failure || Ext.emptyFn,
			callback: p.callback || Ext.emptyFn
		});
	}
};

CMDBuild.ServiceProxy.geoAttribute = {
	remove: function(p) {
		p.method = "POST";
		p.url = 'services/json/gis/deletegeoattribute';
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	save: function(p) {
		p.method = "POST";
		p.url = 'services/json/gis/addgeoattribute';
		p.important = true;
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	modify: function(p) {
		p.method = "POST";
		p.url = 'services/json/gis/modifygeoattribute';
		p.important = true;
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	}
}

CMDBuild.ServiceProxy.geoServer = {
	addUrl: "services/json/gis/addgeoserverlayer",
	modifyUrl: "services/json/gis/modifygeoserverlayer",
	
	getGeoServerLayerStore: function() {
		var layerStore =  new Ext.data.Store({
			model: "GISLayerModel",
			proxy: {
				type: 'ajax',
				url: 'services/json/gis/getgeoserverlayers',
				reader: {
					type: 'json',
					root: 'layers'
				}
			},
			sorters: [{
				property: 'index',
				direction: 'ASC'
			}]
		});
		
		var reload = function(o) {
			if (o) {
				this.nameToSelect = o.nameToSelect;
			}
			this.reload();
		};
		
		_CMEventBus.subscribe("cmdb-modified-geoserverlayers", reload, layerStore);
		
		return layerStore;
	},
	
	deleteLayer: function(p) {
		p.method = "POST";
		p.url = "services/json/gis/deletegeoserverlayer";
		p.important = true;
		
		CMDBuild.Ajax.request(p);
	}
}

CMDBuild.ServiceProxy.classes = {
	read: function(p) {
		p.method = "GET";
		p.url = "services/json/schema/modclass/getallclasses";
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	save: function(p) {
		p.method = 'POST';
		p.url = 'services/json/schema/modclass/savetable';
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	remove: function(p) {
		p.method = 'POST';
		p.url = "services/json/schema/modclass/deletetable";
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	}
}

CMDBuild.ServiceProxy.card = {
	getPosition: function(p) {
		p.method = 'GET';
		p.url = 'services/json/management/modcard/getcardposition';
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	get: function(p) {
		p.method = 'GET';
		p.url = 'services/json/management/modcard/getcard',

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
}


CMDBuild.ServiceProxy.lookup = {
	readAllTypes: function(p) {
		p.method = "GET";
		p.url = "services/json/schema/modlookup/tree";
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	getLookupFieldStore: function(type) {
		return new Ext.data.Store({
			model: "CMLookupForCombo",
			proxy: {
				type: 'ajax',
				url : 'services/json/schema/modlookup/getlookuplist',
				reader: {
					type: 'json',
					root: 'rows'
				},
				params : {
					type : type,
					active : true,
					short : true
				}
			},
			autoLoad : true
		});
	},
	
	getLookupAttributeStore: function(type) {
		var s = Ext.create("Ext.data.Store", {
			fields: ["Id", "Description", "ParentId"],
			proxy: {
				type: 'ajax',
				url : 'services/json/schema/modlookup/getlookuplist',
				reader: {
					type: 'json',
					root: 'rows'
				},
				extraParams : {
					type : type,
					active : true,
					short : true
				}
			},
			sorters : [ {
				property : 'Description',
				direction : "ASC"
			}],
			autoLoad : true
		});

		return s;
	},

	getLookupGridStore: function(pageSize) {
		return new Ext.data.Store({
			model : "CMLookupForGrid",
			pageSize: pageSize || 20,
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modlookup/getlookuplist',
				reader : {
					type : 'json',
					root : 'rows'
				}
			},
			sorters : [ {
				property : 'Description',
				direction : "ASC"
			}]
		});
	},
	
	setLookupDisabled: function(p, disable) {
		var url = 'services/json/schema/modlookup/enablelookup';
		if (disable) {
			url = 'services/json/schema/modlookup/disablelookup';
		}
		
		p.method = "POST";
		p.url = url;
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	saveLookup: function(p) {
		p.method = "POST";
		p.url = "services/json/schema/modlookup/savelookup";
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	saveLookupType: function(p) {
		p.method = "POST";
		p.url = "services/json/schema/modlookup/savelookuptype";
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	}
}

CMDBuild.ServiceProxy.group = {
	read: function(p) {
		p.method = "GET";
		p.url = "services/json/schema/modsecurity/getgrouplist";
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	save: function(p) {
		p.method = "POST";
		p.url = "services/json/schema/modsecurity/savegroup";
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	getPrivilegesGridStore: function(pageSize) {
		return new Ext.data.Store({
			model : "CMDBuild.cache.CMPrivilegeModel",
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modsecurity/getprivilegelist',
				reader : {
					type : 'json',
					root : 'rows'
				}
			},
			sorters : [ {
				property : 'classname',
				direction : "ASC"
			}]
		});
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
	
	getUserStoreForGrid: function() {
		return new Ext.data.Store({
			model : "CMDBuild.cache.CMUserForGridModel",
			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : "services/json/schema/modsecurity/getuserlist",
				reader : {
					type : 'json',
					root : 'rows'
				}
			},
			sorters : [ {
				property : 'username',
				direction : "ASC"
			}]
		});
	}
}

CMDBuild.ServiceProxy.report = {
	read: function(p) {
		p.method = "GET";
		p.url = "services/json/schema/modreport/menutree",

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
}

CMDBuild.ServiceProxy.menu = {
		read: function(p) {
			p.method = "GET";
			p.url = 'services/json/schema/modmenu/getgroupmenu';

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	}


})();