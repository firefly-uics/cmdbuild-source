(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMGeoAttributeController", {
		constructor: function(view) {
			this.view = view;
			this.form = view.form;
			this.grid = view.grid;

			this.gridSM = this.grid.getSelectionModel();
			this.gridSM.on('selectionchange', onSelectionChanged , this);

			this.currentClassId = null;
			this.currentAttribute = null;

			this.form.saveButton.on("click", onSaveButtonFormClick, this);
			this.form.abortButton.on("click", onAbortButtonFormClick, this);
			this.form.cancelButton.on("click", onCancelButtonFormClick, this);
			this.form.modifyButton.on("click", onModifyButtonFormClick, this);
			this.grid.addAttributeButton.on("click", onAddAttributeClick, this);
		},

		onClassSelected: function(classId) {
			this.currentClassId = classId;
			this.currentAttribute = null;
			this.view.onClassSelected(classId);
		},

		onAddClassButtonClick: function() {
			this.view.disable();
		}

	});
	
	function onSelectionChanged(selection) {
		if (selection.selected.length > 0) {
			this.currentAttribute = selection.selected.items[0];
			this.form.onAttributeSelected(this.currentAttribute);
		}
	}
	
	function onSaveButtonFormClick() {
		var style = this.form.getStyle();
		var p = {
			params: this.form.getData()
		};

		p.params.style = Ext.encode(style);
		p.params.idClass = this.currentClassId;
		p.name  = this.form.name.getValue();

		p.success = Ext.bind(function(a, b, decoded) {
			_CMCache.onGeoAttributeSaved(this.currentClassId, decoded.geoAttribute);
			this.grid.selectAttribute(decoded.geoAttribute);
		}, this);
		
		if (this.currentAttribute != null) {
			CMDBuild.ServiceProxy.geoAttribute.modify(p);
		} else {
			CMDBuild.ServiceProxy.geoAttribute.save(p);
		}
	}

	function onAbortButtonFormClick() {
		this.form.disableModify();
		if (this.currentAttribute != null) {
			this.form.onAttributeSelected(this.currentAttribute);
		} else {
			this.form.reset();
		}
	}
	
	function onCancelButtonFormClick() {
		Ext.Msg.show({
			title: "@@ Delete attribute",
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteAttribute.call(this);
				}
			}
		});
	}
	
	function deleteAttribute() {
		function onSuccess(response, request, decoded) {
			alert("@@ Deleted");
		};
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.geoAttribute.remove({
			params : {
				"idClass": this.currentClassId,
				"name": this.currentAttribute.get("name")
			},
			success: Ext.Function.bind(onSuccess, this),
			callback: Function.bind(callback, this)
		})
	}
	
	function onModifyButtonFormClick() {
		this.form.enableModify();
	}
	
	function onAddAttributeClick() {
		this.currentAttribute = null;

		this.form.reset();
		this.form.enableModify();
		this.form.hideStyleFields();
		this.gridSM.deselectAll();
	}
	
	function callback() {
		CMDBuild.LoadMask.get().hide();
	}
	
	// FORM
	

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