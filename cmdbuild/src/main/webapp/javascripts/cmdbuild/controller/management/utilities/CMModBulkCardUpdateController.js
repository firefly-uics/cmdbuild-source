(function() {
	Ext.define("CMDBuild.controller.management.utilities.CMModBulkUpdateController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		constructor: function() {
			this.callParent(arguments);
			this.gridSM = this.view.cardGrid.getSelectionModel();
			this.treeSM = this.view.classTree.getSelectionModel();

			this.treeSM.on("selectionchange", onClassTreeSelected, this);

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
		if (this.gridSM.cmReverse) {
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
			save.call(this);
		}
	}

	function save() {
		if (!this.gridSM.cmReverse && !this.gridSM.hasSelection()) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.no_selections);
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, msg, false);

			return;
		}

		var p = builSaveParams.call(this);
		p.confirmed = true;

		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			url: 'services/json/management/modcard/updatebulkcards',
			params: p,
			scope: this,
			success: function(response) {
				this.view.cardGrid.reload(reselect = false);
				onAbortButtonClick.call(this);
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function onAbortButtonClick() {
		this.gridSM.reset();
		this.view.cardForm.reset();

		this.view.cardGrid.reload(reselect = false);
	}

	function onClassTreeSelected(sm, selection) {
		if (selection.length > 0) {
			var node = selection[0],
				selectable = false;

			try {
				selectable = node.raw.cmData.selectable;
			} catch (e) {
				// the folder has not cmData
			}

			if (selectable) {
				this.currentClassId = node.get("id");
				this.view.onClassTreeSelected(this.currentClassId);
			}
		}
	}

	function builSaveParams() {
		var params = this.view.cardForm.getCheckedValues();

		params["FilterCategory"] = this.view.filterType;
		params['IdClass'] = this.currentClassId;

		params["isInverted"] = this.gridSM.cmReverse;

		var fullTextQuery = this.view.cardGrid.gridSearchField.getValue();

		if (fullTextQuery != "") {
			params["fullTextQuery"] = fullTextQuery;
		}

		params['selections'] = (function formatSelections() {
			var ss = this.gridSM.getSelection();
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