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
			if (CMDBuild.Config.gis.enabled && !_CMUtils.isSimpleTable(classId)) {
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
		CMDBuild.LoadMask.get().show();
		var p = {
			name: this.form.name.getValue(),
			callback: callback,
			params: Ext.apply(this.form.getData(), {
				style: Ext.encode(this.form.getStyle()),
				idClass: this.currentClassId
			})
		};

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

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.geoAttribute.remove({
			params : {
				"idClass": me.currentClassId,
				"name": me.currentAttribute.get("name")
			},
			success: function onDeleteGeoAttributeSuccess(response, request, decoded) {
				_CMCache.onGeoAttributeDeleted(me.currentClassId, me.currentAttribute.data);
			},
			callback: callback
		})
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
		CMDBuild.LoadMask.get().hide();
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