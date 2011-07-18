(function() {
	
	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;
	
	Ext.define("CMDBuild.controller.administration.classes.CMClassAttributeController", {
		constructor: function(view) {
			this.view = view;
			this.currentClassId = null;

			this.gridSM = this.view.gridPanel.getSelectionModel();
			this.gridSM.on('selectionchange', onSelectionChanged , this);

            this.view.on("activate", onViewActivate, this);

			this.view.formPanel.abortButton.on("click", onAbortClick, this);
			this.view.formPanel.saveButton.on("click", onSaveClick, this);
			this.view.formPanel.deleteButton.on("click", onDeleteClick, this);
			this.view.gridPanel.addAttributeButton.on("click", onAddAttributeClick, this);
			this.view.gridPanel.orderButton.on("click", buildOrderingWindow, this);
			this.view.gridPanel.on("cm_attribute_moved", onAttributeMoved, this);
            this.view.gridPanel.store.on("load", onAttributesAreLoaded, this);
		},

		onClassSelected: function(classId) {
			this.currentClassId = classId;
			this.view.enable();
            if (tabIsActive(this.view)) {
                this.toLoad = false;
                this.view.onClassSelected(this.currentClassId);
            } else {
                this.toLoad = true;
            }
		},

		onAddClassButtonClick: function() {
			this.view.disable();
		}

	});
    
    function onAttributesAreLoaded(store, records) {
        this.view.formPanel.fillAttributeGroupsStore(records);
    }
    
    function onViewActivate() {
        if (this.toLoad) {
            this.view.onClassSelected(this.currentClassId);
        }
    }

	function onSaveClick() {
		var nonValid = this.view.formPanel.getNonValidFields();
		if (nonValid.length > 0) {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, "@@Ci sono campi non validi", false);
			return;
		}
		var data = this.view.formPanel.getData(withDisabled = true);
		data.tableId = this.currentClassId;
		
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request( {
			method : "POST",
			url : "services/json/schema/modclass/saveattribute",
			params : data,
			scope: this,
			success : function(form, action, decoded) {
				this.view.gridPanel.refreshStore(this.currentClassId, decoded.attribute.index);
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function onAbortClick() {
		if (this.currentAttribute == null) {
			this.view.formPanel.reset();
			this.view.formPanel.disableModify();
		} else {
			onSelectionChanged.call(this, null, [this.currentAttribute]);
		}
	}

	function onDeleteClick() {
		Ext.Msg.show({
			title: tr.delete_attribute,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteCurrentAttribute.call(this);
				}
			}
		});
	}

	function deleteCurrentAttribute() {
		if (this.currentAttribute == null) {
			return; //nothing to delete
		}

		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			url : "services/json/schema/modclass/deleteattribute",
			method: "POST",
			params: {
				tableId: this.currentClassId,
				name: this.currentAttribute.get("name")
			},
			scope: this,
			callback : function() {
				CMDBuild.LoadMask.get().hide();
				this.view.formPanel.reset();
				this.view.formPanel.disableModify();
				this.view.gridPanel.refreshStore(this.currentClassId);
			}
		});
	}

	function onSelectionChanged(sm, selection) {
		if (selection.length > 0) {
			this.currentAttribute = selection[0];
			this.view.formPanel.onAttributeSelected(this.currentAttribute);
		}
	}

	function onAddAttributeClick() {
		this.currentAttribute = null;
		this.view.formPanel.onAddAttributeClick();
		this.view.gridPanel.onAddAttributeClick();
	}

	function onAttributeMoved() {
		var g = this.view.gridPanel;
		var rowList = [];
		var gStore = g.getStore();

		for (var i=0; i<gStore.getCount(); i++) {
			var rec = gStore.getAt(i);
			rowList.push({ name: rec.get("name"), idx: i+1 });
		}

		CMDBuild.Ajax.request({
			url: 'services/json/schema/modclass',
			method: 'POST',
			params: {
				method: 'reorderAttribute',
				idClass: this.currentClassId,
				attributes: Ext.JSON.encode(rowList)
			}
		});
	}
	
	function buildOrderingWindow() {
		if (this.currentClassId) {
			var win = new CMDBuild.Administration.SetOrderWindow( {
				idClass : this.currentClassId
			}).show(); 
		}
	}
    
    function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}
})();