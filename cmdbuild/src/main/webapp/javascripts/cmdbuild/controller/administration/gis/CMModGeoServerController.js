(function() {
	var tr = CMDBuild.Translation.administration.modcartography.geoserver;
	
	Ext.define("CMDBuild.controller.administration.gis.CMModGeoServerController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);

			this.view.on("show", function() {
				this.layersGrid.onModShow(this.firstShow);
				this.firstShow = false;
			}, this.view);

			this.view.layersGrid.getSelectionModel().on("selectionchange", onLayerSelect, this);

			this.view.addLayerButton.on("click", onAddButtonClick, this);

			this.view.form.saveButton.on("click", onSaveButtonClick, this);
			this.view.form.abortButton.on("click", onAbortButtonClick, this);
			this.view.form.deleteButton.on("click", onDeleteButtonClick, this);
		},

		onViewOnFront: function() {
			if (!geoserverIsEnabled()) {
				var msg = Ext.String.format(tr.service_not_available
						, CMDBuild.Translation.administration.modcartography.title +
							"/" + CMDBuild.Translation.administration.modcartography.external_services.title);
				
				_CMMainViewportController.bringTofrontPanelByCmName("notconfiguredpanel", msg);
				return false;
			}

			this.view.layersGrid.selectFirstIfUnselected();
		}
	});

	function onAddButtonClick() {
		this.lastSelection = null;
		this.view.form.onAddLayer();
		this.view.layersGrid.clearSelection();
	}

	function onLayerSelect(view, selection) {
		if (selection[0]) {
			this.lastSelection = selection[0];
			this.view.form.onLayerSelect(this.lastSelection);
		}
	}

	function onSaveButtonClick() {
		CMDBuild.LoadMask.get().show();
		var url = this.lastSelection ?
				CMDBuild.ServiceProxy.geoServer.modifyUrl:
				CMDBuild.ServiceProxy.geoServer.addUrl,
			nameToSelect = this.view.form.getName(),
			form = this.view.form.getForm();

		form.submit({
			method: 'POST',
			url: url,
			params: {
				name: nameToSelect
			},
			scope: this,
			success: function() {
				_CMCache.onGeoAttributeSaved();
				this.view.layersGrid.loadStoreAndSelectLayerWithName(nameToSelect);
			},
			failure: function() {
				_debug("Failed to add or modify a Geoserver Layer", arguments);	
			},
			callback: function() {
				this.view.form.disableModify();
				CMDBuild.LoadMask.get().hide();
			}
		});
	};

	function onAbortButtonClick() {
		if (this.lastSelection) {
			this.view.form.onLayerSelect(this.lastSelection);
		} else {
			this.view.form.disableModify();
			this.view.form.reset();
		}
	}

	function onDeleteButtonClick() {
		var me = this;
		Ext.Msg.show({
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					CMDBuild.LoadMask.get().show();
					var layerName = me.view.form.getName();
					CMDBuild.ServiceProxy.geoServer.deleteLayer({
						params: {
							name: layerName
						},
						callback: function() {
							_CMCache.onGeoAttributeDeleted("_Geoserver", layerName);
							me.view.layersGrid.loadStoreAndSelectLayerWithName();
							CMDBuild.LoadMask.get().hide();
						}
					});
				}
			}
		});
	};

	function geoserverIsEnabled() {
		return CMDBuild.Config.gis.geoserver && CMDBuild.Config.gis.geoserver == "on";
	}
})();