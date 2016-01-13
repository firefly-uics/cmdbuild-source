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
			_CMCache.initAddingTranslations();
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
			}
		},

		saveSuccessCB: function(r) {
			this.view.disableModify(enableCMTBar = true);
			var result = Ext.JSON.decode(r.responseText);
			this.selection = _CMCache.onClassSaved(result.table);
			_CMCache.flushTranslationsToSave(result.table.name);
		},

		buildSaveParams: function() {
			var withDisabled = true;
			var params = this.view.getData(withDisabled);

			/*
			 * If has no selection, we are adding
			 * a new table. Set the parameter forceCreation
			 * to say to the server that must check that
			 * does not exists another table with that name
			 */
			params.forceCreation = this.selection == null;
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

			var params = {};
			params[_CMProxy.parameter.CLASS_NAME] = this.selection.get("name");

			CMDBuild.ServiceProxy.classes.remove({
				params: params,
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

		/**
		 * @params {String} format
		 */
		onPrintClass: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selection.get(CMDBuild.core.proxy.CMProxyConstants.ID));
				params[CMDBuild.core.proxy.CMProxyConstants.FORMAT] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'classSchema',
					parameters: params
				});
			}
		}
	});

})();