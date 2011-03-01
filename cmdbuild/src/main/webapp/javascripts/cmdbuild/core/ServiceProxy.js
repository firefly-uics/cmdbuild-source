CMDBuild.ServiceProxy = (function() {
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

	return {
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
		getCard: function(classId, cardId, success, failure, callback) {
			CMDBuild.Ajax.request({
				url: 'services/json/management/modcard/getcard',
		        params: {
		            "IdClass": classId,
		            "Id": cardId
		        },
		        method: 'GET',
		        success: success || Ext.emptyFn,
		        failure: failure || Ext.emptyFn,
		        callback: callback || Ext.eptyFn
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
		saveGeoAttribute: function(params, success, failure, callback) {
			CMDBuild.Ajax.request({
				scope : this,
				important: true,
				url : 'services/json/gis/addgeoattribute',
				params : params,
				method: 'POST',
				success: success,
				failure: failure,
				callback: callback
			});
		},
		deleteGeoAttribute: function(classId, name, success, failure, callback) {
			CMDBuild.Ajax.request({
				scope : this,
				important: true,
				url : 'services/json/gis/deletegeoattribute',
				params : {
					"idClass": classId,
					"name": name
				},
				method: 'POST',
				success: success,
				failure: failure,
				callback: callback
			});
		},
		modifyGeoAttribute: function(params, success, failure, callback) {
			CMDBuild.Ajax.request({
				scope : this,
				important: true,
				url : 'services/json/gis/modifygeoattribute',
				params : params,
				method: 'POST',
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
		
		administration: {
			saveTable: 'services/json/schema/modclass/savetable',
			deleteTable: 'services/json/schema/modclass/deletetable',
			printSchema: 'services/json/schema/modreport/printclassschema'
		},
		
		geoServer: {
			addUrl: "services/json/gis/addgeoserverlayer",
			modifyUrl: "services/json/gis/modifygeoserverlayer",
			getGeoServerLayerStore: function() {
				var layerStore =  new Ext.data.JsonStore({
					url: "services/json/gis/getgeoserverlayers",
					root: "layers",
					fields: ["maxZoom", "minZoom", "style", "description", "index", "name", "type"],
					autoLoad: true,
					sortInfo: {
					    field: 'index',
					    direction: 'ASC'
					}
				});
				
				var reload = function(o) {
					if (o) {
						this.nameToSelect = o.nameToSelect;
					}
					this.reload();
				};
				
				layerStore.subscribe("cmdb-modified-geoserverlayers", reload, layerStore);
				
				return layerStore;
			},
			
			deleteLayer: function(params, success, failure, callback) {
				CMDBuild.Ajax.request({
					scope : this,
					important: true,
					url : "services/json/gis/deletegeoserverlayer",
					params : params,
					method: 'POST',
					success: success || Ext.emptyFn,
					failure: failure || Ext.emptyFn,
					callback: callback || Ext.emptyFn
				});
			}
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
			var layerStore =  new Ext.data.JsonStore({
				url: "services/json/gis/getalllayers",
				root: "layers",
				fields: ["maxZoom", "minZoom", "style", "description", "index",
				         "name", "masterTableId", "type", "masterTableName", "isvisible"],
				autoLoad: true,
				sortInfo: {
				    field: 'index',
				    direction: 'ASC'
				}
			});
			
			var reload = function() {
				this.reload();
			};
			
			layerStore.subscribe("cmdb-new-geoattr", reload, layerStore);
			layerStore.subscribe("cmdb-modify-geoattr", reload, layerStore);
			layerStore.subscribe("cmdb-delete-geoattr", reload, layerStore);
			layerStore.subscribe("cmdb-geoservices-config-changed", reload, layerStore);
			layerStore.subscribe("cmdb-modified-geoserverlayers", reload, layerStore);
			return layerStore;
		},
		
		getLookupFieldStore: function(type) {
			return new Ext.data.JsonStore({
				url: 'services/json/schema/modlookup/getlookuplist',
				baseParams: {
		        	type: type,
					active: true,
					short: true
		        },
		        root: "rows",
		        fields : [
		            lookupFields.Id,
		            lookupFields.Description,
		            lookupFields.ParentId
		        ],
		        autoLoad: true
			});
		},
		
		LOOKUP_FIELDS: lookupFields
	};
})();