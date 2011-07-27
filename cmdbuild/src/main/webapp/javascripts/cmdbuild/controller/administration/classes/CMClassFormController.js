(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;
	
	Ext.define("CMDBuild.controller.administration.classes.CMClassFormController", {
		constructor: function(view) {
			this.view = view;
			this.selection = null;

			this.view.abortButton.on("click", this.onAbortClick, this);
			this.view.saveButton.on("click", this.onSaveClick, this);
			this.view.deleteButton.on("click", this.onDeleteClick, this);
			this.view.printClassButton.on("click", this.onPrintClass, this);
		},

		onClassSelected: function(classId) {
			this.selection = _CMCache.getClassById(classId);
			if (this.selection) {
				this.view.onClassSelected(this.selection);
			}
		},
		
		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.onAddClassButtonClick();
		},
		
		onSaveClick: function() {
			CMDBuild.LoadMask.get().show();

			CMDBuild.ServiceProxy.classes.save({
				params: this.buildSaveParams(),
				callback: callback,
				success: this.saveSuccessCB,
				scope: this
			});

			function callback() {
				CMDBuild.LoadMask.get().hide();
				this.view.disableModify(enableCMTBar = true);
			}
		},

		saveSuccessCB: function(r) {
			var result = Ext.JSON.decode(r.responseText);
			this.selection = _CMCache.onClassSaved(result.table);
		},

		buildSaveParams: function() {
			var params = this.view.getData();
			if (this.selection != null) {
				params.idClass = this.selection.get("id");
			}
			params.isprocess = false;
			params.description = params.text; // adapter: maybe one day everything will be better
			params.inherits = params.parent; // adapter
			
			return params
		},
		
		onDeleteClick: function() {
			Ext.Msg.show({
				title: tr.remove_class,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == "yes") {
						this.deleteCurrentClass();
					}
				}
			});
		},

		deleteCurrentClass: function() {
			CMDBuild.LoadMask.get().hide();
			
			CMDBuild.ServiceProxy.classes.remove({
				params: {idClass: this.selection.get("id")},
				callback: callback,
				success: this.deleteSuccessCB,
				scope: this
			});

			function callback() {
				CMDBuild.LoadMask.get().hide();
				this.view.disableModify();
				this.view.reset();
			}

		},
		
		deleteSuccessCB: function(r) {
			var removedClassId = this.selection.get("id");
			_CMCache.onClassDeleted(removedClassId);
			this.selection = null;
		},
		
		onAbortClick: function() {
			this.view.disableModify();
			this.view.reset();
			if (this.selection != null) {
				this.view.onClassSelected(this.selection);
			}
		},
		
		onPrintClass: function(format) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request( {
				url : CMDBuild.ServiceProxy.administration.printSchema,
				method : 'POST',
				scope : this,
				params : {
					idClass : this.selection.get("id"),
					format : format
				},
				success : function(response) {
					CMDBuild.LoadMask.get().hide();
					var popup = window.open(
						"services/json/management/modreport/printreportfactory",
						"Report",
						"height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable"
					);
					if (!popup) {
						CMDBuild.Msg.warn(
							CMDBuild.Translation.warnings.warning_message,
							CMDBuild.Translation.warnings.popup_block
						);
					}
				},
				failure : function(response) {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	});

})();