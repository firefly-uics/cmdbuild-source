(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMGeoAttributeController", {
		constructor: function(view) {
			if (Ext.isEmpty(view)) {
				this.view = new CMDBuild.view.administration.classes.CMGeoAttributesPanel({
					title: CMDBuild.Translation.administration.modClass.tabs.geo_attributes,
					disabled: true
				});
				this.form = this.view.form;
				this.grid = this.view.grid;
			} else {
				this.view = view;
				this.form = view.form;
				this.grid = view.grid;
			}

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

		getView: function() {
			return this.view;
		},

		onClassSelected: function(classId) {
			if (CMDBuild.configuration.gis.get('enabled') && !_CMUtils.isSimpleTable(classId)) { // TODO: use procy constants
				this.view.enable();
			} else {
				this.view.disable();
			}

			this.currentClassId = classId;
			this.currentAttribute = null;
			if (this.view.isActive()) {
				this.view.onClassSelected(this.currentClassId);
			} else {
				this.view.mon(this.view, "activate", function() {
					this.view.onClassSelected(this.currentClassId);
				}, this, {single: true});
			}
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
		var nonValid = this.form.getNonValidFields();
		if (nonValid.length > 0) {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			return;
		}

		this.form.enableModify(all = true);
		CMDBuild.core.LoadMask.show();

		var attributeConfig = this.form.getData();
		attributeConfig.style = Ext.encode(this.form.getStyle());

		var me = this;
		var params = {
			name: this.form.name.getValue(),
			params: Ext.apply(attributeConfig, {
				className: _CMCache.getEntryTypeNameById(this.currentClassId)
			}),
			callback: callback,
			success: function(a, b, decoded) {
				_CMCache.onGeoAttributeSaved();
				me.view.grid.refreshStore(me.currentClassId, attributeConfig.name);
			}
		};

		if (this.currentAttribute != null) {
			CMDBuild.ServiceProxy.geoAttribute.modify(params);
		} else {
			CMDBuild.ServiceProxy.geoAttribute.save(params);
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
			title: CMDBuild.Translation.management.findfilter.msg.attention,
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
		var me = this;
		var params = {
			"masterTableName": me.currentAttribute.getMasterTableName(),
			"name": me.currentAttribute.get("name")
		};

		CMDBuild.core.LoadMask.show();
		CMDBuild.ServiceProxy.geoAttribute.remove({
			params: params,
			success: function onDeleteGeoAttributeSuccess(response, request, decoded) {
				_CMCache.onGeoAttributeDeleted(params.masterTableName, params.name);
				me.view.onClassSelected(me.currentClassId);
			},
			callback: callback
		});
	}

	function onModifyButtonFormClick() {
		this.form.enableModify();
	}

	function onAddAttributeClick() {
		this.currentAttribute = null;

		this.form.reset();
		this.form.enableModify(enableAllFields = true);
		this.form.setDefaults();
		this.form.hideStyleFields();
		this.gridSM.deselectAll();
	}

	function callback() {
		CMDBuild.core.LoadMask.hide();
	}

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