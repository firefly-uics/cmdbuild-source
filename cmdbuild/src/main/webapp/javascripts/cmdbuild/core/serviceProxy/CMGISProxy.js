CMDBuild.ServiceProxy.getFeature = function(classId, cardId, success, failure, callback) {
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
};
	
CMDBuild.ServiceProxy.getGeoCardList = function(classId, success, failure, callback) {
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
};

CMDBuild.ServiceProxy.saveLayerVisibility = function(p) {
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
};

CMDBuild.ServiceProxy.saveLayerOrder = function(p) {
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
};

// store builders

/**
 * @param classId (optional) adds visibility on the specified class
 */ 
CMDBuild.ServiceProxy.getAllLayerStore = function() {
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

	_CMEventBus.subscribe("cmdb-geoservices-config-changed", reload, layerStore);

	return layerStore;
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

CMDBuild.ServiceProxy.gis = {
	getGisTreeNavigation: function(config) {
		config.method = "POST";
		config.url = "services/json/gis/getgistreenavigation";

		CMDBuild.Ajax.request(config);
	},

	saveGisTreeNavigation: function(config) {
		config.method = "POST";
		config.url = "services/json/gis/savegistreenavigation";

		CMDBuild.Ajax.request(config);
	},

	removeGisTreeNavigation: function(config) {
		config.method = "POST";
		config.url = "services/json/gis/removegistreenavigation";

		CMDBuild.Ajax.request(config);
	}
};