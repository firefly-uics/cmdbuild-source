(function() {

Ext.ns("CMDBuild.ServiceProxy");

var LOOKUP_FIELDS = {
	Id: 'Id',
	Code: 'Code',
	Description: 'Description',
	ParentId: 'ParentId',
	Index: 'Number',
	Type: 'Type',
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
	
	LOOKUP_FIELDS: LOOKUP_FIELDS
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
		var successWithAdapter = Ext.Function.createInterceptor(p.success || Ext.emptyFn, function(response) {
			if (p.adapter) {
				var json =  Ext.JSON.decode(response.responseText);
				var adaptedJson = p.adapter(json);
				_debug("Adapted JSON result", json, adaptedJson);
				response.responseText = Ext.JSON.encode(adaptedJson);
			} 
		});

		CMDBuild.Ajax.request( {
			url: p.url,
			method: p.method,
			params: p.params || {},
			scope: p.scope || this,
			success: successWithAdapter,
			failure: p.failure || Ext.emptyFn,
			callback: p.callback || Ext.emptyFn,
			important: p.important
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
};

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
};

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
};

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
	},
	
	remove: function(p) {
		p.method = 'POST';
		p.url = 'services/json/management/modcard/deletecard',

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

/*
 * Workflow adapters
 */

function adaptVariables(inputVars) {
	var outputVars = {};
	for (i = 0, len = inputVars.length; i < len; ++i) {
		var v = inputVars[i];
		outputVars[v.name] = "";
		outputVars[v.name+"_index"] = i;
		outputVars[v.name+"_type"] = {
			READ_ONLY: "VIEW",
			READ_WRITE: "UPDATE",
			READ_WRITE_REQUIRED: "UPDATEREQUIRED"
		}[v.type];
	}
	return outputVars;
}

function adaptWidgets(inputWidgets) {
	var outputWidgets = [];
	Ext.Array.forEach(inputWidgets, function(w) {
		outputWidgets.push(adaptWidget(w));
	});
	return outputWidgets;
}

function adaptWidget(inputWidget) {
	return Ext.apply({
		identifier : inputWidget.id,
		ButtonLabel : inputWidget.label,
		btnLabel : inputWidget.label
	}, {
		".OpenNote" : function() {
			return {
				extattrtype : "openNote"
			};
		},
		".OpenAttachment" : function() {
			return {
				extattrtype : "openAttachment"
			};
		}
	}[inputWidget.type]());
}

CMDBuild.ServiceProxy.workflow = {
	getstartactivitytemplate: function(classId, p) {
		CMDBuild.ServiceProxy.core.doRequest(Ext.apply({
			url: 'services/json/workflow/getstartactivity',
			method: 'GET',
			params: {
				classId : classId
			}
		}, p));
	},

	getActivityInstance: function(params, conf) {
		conf.url = 'services/json/workflow/getactivityinstance';
		conf.method = "POST";
		conf.params = params;
		conf.important = true;

		if (typeof conf.callback == "undefined") {
			conf.callback = function() {
				CMDBuild.LoadMask.get().hide();
			};
		}

		CMDBuild.ServiceProxy.core.doRequest(conf);
	},

	terminateActivity: function(p) {
		p.url = 'services/json/workflow/abortprocess';
		p.method = "POST";

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	saveActivity: function(p) {
		p.url = 'services/json/workflow/saveactivity';
		p.method = 'POST';

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
},


CMDBuild.ServiceProxy.lookup = {
	readAllTypes: function(p) {
		p.method = "GET";
		p.url = "services/json/schema/modlookup/tree";
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	getLookupFieldStore: function(type) {
		var s = Ext.create("Ext.data.Store", {
			model: "CMLookupFieldStoreModel",
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
					"short" : true
				},
				actionMethods: 'POST' // Lookup types can have UTF-8 names not handled correctly
			},
			sorters : [ {
				property : LOOKUP_FIELDS.Index,
				direction : "ASC"
			}],
			autoLoad : true,
			pageSize: 0 // Disable paging
		});

		return s;
	},

	getLookupGridStore: function() {
		return new Ext.data.Store({
			model : "CMLookupForGrid",
			pageSize: parseInt(CMDBuild.Config.cmdbuild.referencecombolimit) || 20,
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modlookup/getlookuplist',
				reader : {
					type : 'json',
					root : 'rows'
				},
				actionMethods: 'POST' // Lookup types can have UTF-8 names not handled correctly
			},
			sorters : [ {
				property : 'Number',
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
};

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
	},

	getUIConfiguration: function(groupId, cbs) {
		cbs = cbs || {};

		CMDBuild.ServiceProxy.core.doRequest({
			url: "services/json/schema/modsecurity/getgroupuiconfiguration",
			params: {id: groupId},
			method: "GET",
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
			method: "POST",
			success: cbs.success || Ext.emptyFn,
			failure: cbs.failure || Ext.emptyFn,
			callback: cbs.callback || Ext.emptyFn
		});
	}
};

CMDBuild.ServiceProxy.report = {
	getMenuTree: function(p) {
		p.method = "GET";
		p.url = "services/json/schema/modreport/menutree",

		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	getTypesTree: function(p) {
		p.method = "GET";
		p.url = "services/json/management/modreport/getreporttypestree";

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

CMDBuild.ServiceProxy.menu = {
	read: function(p) {
		p.method = "GET";
		p.url = 'services/json/schema/modmenu/getgroupmenu';

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

CMDBuild.ServiceProxy.url = {}; // filled in the extensions
})();
