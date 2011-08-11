(function() {
	Ext.define("CMDBuild.controller.management.utilities.CMModBulkUpdateController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		
		constructor: function() {
			this.callParent(arguments);
			this.gridSM = this.view.cardGrid.getSelectionModel();
			this.treeSM = this.view.classTree.getSelectionModel();

			this.treeSM.on("selectionchange", onClassTreeSelected, this);
			this.gridSM.on("selectionchange", onGridSelectionChanged, this);
			
			this.view.saveButton.on("click", onSaveButtonClick, this);
			this.view.abortButton.on("click", onAbortButtonClick, this);

		},
		
		onViewOnFront: function() {
			var s = this.treeSM.getSelection();
			if (s.length == 0) {
				this.treeSM.select(0);
			}
		}
	});
	
	function onSaveButtonClick() {
		if (this.gridSM.cmInverseSelection) {
			Ext.Msg.show({
				title: CMDBuild.Translation.warnings.warning_message,
				msg: CMDBuild.Translation.warnings.only_filtered,
				buttons: Ext.Msg.OKCANCEL,
				fn: function(button) {
					if (button == "ok") {
						save.call(this);
					}
				},
				icon: Ext.MessageBox.WARNING,
				scope: this
			});
		} else {
			save.call(this)
		}
		
	}
	
	function save() {
		var p = builSaveParams.call(this);
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/updatebulkcards',
			params: p, 
			scope: this,
			success: function(response) {
				this.view.cardGrid.reload(reselect = false);
				onAbortButtonClick.call(this);
			}
		});
	}

	function onAbortButtonClick() {
		this.gridSM.cmDeselectAll();
		this.view.cardForm.reset();		
	}
	
	function onGridSelectionChanged(sm, selection) {
		var toDisable = selection.length == 0;
		this.view.saveButton.setDisabled(toDisable);
		this.view.abortButton.setDisabled(toDisable);
	}
	
	function onClassTreeSelected(sm, selection) {
		if (selection.length > 0) {
			this.currentClassId = selection[0].get("id")
			this.view.onClassTreeSelected(this.currentClassId);
		}
	}
	
	function builSaveParams() {
		var params = this.view.cardForm.getCheckedValues();

		params["FilterCategory"] = this.view.filterType;
		params['IdClass'] = this.currentClassId;

		params["isInverted"] = this.gridSM.cmInverseSelection;

		var fullTextQuery = this.view.cardGrid.gridSearchField.getValue();

		if (fullTextQuery != "") {
			params["fullTextQuery"] = fullTextQuery;
		}

		params['selections'] = (function formatSelections() {
			var ss = this.gridSM.getCmSelections();
			var out = [];
			var s;
			for (var i=0, l=ss.length; i<l; i++) {
				s = ss[i];
				out.push(s.get("IdClass") + "_" + s.get("Id"));
			}
			return out;
		}).call(this);
		
		return params;
	}
})();