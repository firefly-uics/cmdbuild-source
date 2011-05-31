(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMGeoAttributeControllerController", {
		constructor: function(view) {
			this.view = view;
			this.form = view.form;
			this.grid = view.grid;
			
			this.selection = null;
			
			this.form.saveButton.on("click", onSaveButtonFormClick, this);
			this.form.abortButton.on("click", onAbortButtonFormClick, this);
			this.form.cancelButton.on("click", onCancelButtonFormClick, this);
			this.form.modifyButton.on("click", onModifyButtonFormClick, this);
		},

		onSelectClass: function(classId) {
			var classObject = _CMCache.getClassById(classId);
			
			if (classObject.data.meta) {
				var geoAttributes = classObject.data.meta.geoAttributes || [];
				
				this.grid.store.removeAll();
				for (var i=0,l=geoAttributes.length; i<l; ++i) {
					var attr = geoAttributes[i];
					var r = Ext.create("GISLayerModel", attr);
					if (isItMineOrOfMyParents(attr, classObject)) {
						this.grid.store.insert(0, r);
					}
				}
			}

//			this.form.setClass(classId);
		},

		onAddClassButtonClick: function() {

		}

	});
	
	function onSaveButtonFormClick() {
		alert("save");
	}
	
	function onAbortButtonFormClick() {
		alert("abort");
	}
	
	function onCancelButtonFormClick() {
		alert("cancel");
	}
	
	function onModifyButtonFormClick() {
		alert("modify");
	}
	
	// FORM
	
//	var callback = function() {
//		CMDBuild.LoadMask.get().hide();
//	};
//	
//    var onSave = function() {
//    	var params = this.getForm().getValues();
//    	var out = {};
//    	var style = this.getStyle();
//    	
//    	for (var key in params) {
//    		if (!this.styleFieldsMap[key]) {
//    			out[key] = params[key];
//    		}
//    	}
//    	
//    	out.style = Ext.encode(style);
//    	out.idClass = this.classId;    	
//    	out.name = this.name.getValue();
//    	
//    	CMDBuild.LoadMask.get().show();
//    	if (this.modifyMode) {
//    		var onSuccess = function(response, request, decoded) {
//    			this.publish("cmdb-modify-geoattr", decoded);
//    		};
//    		CMDBuild.ServiceProxy.modifyGeoAttribute(out, 
//    				onSuccess.createDelegate(this),
//    				Ext.emptyFn,
//    				callback.createDelegate(this));
//    	} else {
//    		var onSuccess = function(response, request, decoded) {
//    			this.publish("cmdb-new-geoattr", decoded);
//    		};
//    		CMDBuild.ServiceProxy.saveGeoAttribute(out,
//    				onSuccess.createDelegate(this),
//    				Ext.emptyFn,
//    				callback.createDelegate(this));
//    	}    	
//    };
//    
//    var onAbort = function() {
//    	this.resetForm();
//    	this.disableAllField();
//    	this.saveButton.disable();
//    	this.abortButton.disable();
//    	this.stopMonitoring();
//    };
//    
//    var onModify = function() {
//    	this.modifyMode = true;
//    	this.enableAllField();
//    	this.types.disable();
//    	this.name.disable();
//    	this.saveButton.enable();
//    	this.abortButton.enable();
//    	this.startMonitoring();
//    };
//    
//    var onDelete = function() {
//    	var onSuccess = function(response, request, decoded) {
//    		var table = {id: this.classId};
//    		var geoAttribute = {name: this.currentName};
//			this.publish("cmdb-delete-geoattr", {table: table, geoAttribute: geoAttribute});
//		};
//		CMDBuild.LoadMask.get().show();
//    	CMDBuild.ServiceProxy.deleteGeoAttribute(this.classId,
//    			this.currentName, 
//    			onSuccess.createDelegate(this),
//    			Ext.emptyFn,
//    			callback.createDelegate(this));
//    };
	
	function isItMineOrOfMyParents(attr, classId) {
		var table = CMDBuild.Cache.getTableById(classId);
		while (table) {
			if (attr.masterTableId == table.id) {
				return true;
			} else {
				table = CMDBuild.Cache.getTableById(table.parent);
			}
		}
		return false;
	};
})();