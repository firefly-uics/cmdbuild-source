(function() {

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.core.proxy.utility.ImportCsv'
	]);

	var tr = CMDBuild.Translation.management.modutilities.csv;
	Ext.define("CMDBuild.controller.management.utilities.CMModImportCSVController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		constructor: function() {
			this.callParent(arguments);

			this.view.form.uploadButton.on("click", onUploadButtonClick, this);
			this.view.form.classList.on("select", onClassListSelect, this);
			this.view.updateButton.on("click", onUpdateButtonClick, this);
			this.view.confirmButton.on("click", onConfirmButtonClick, this);
			this.view.abortButton.on("click", onAbortButtonClick, this);
		},

		onViewOnFront: function() {}
	});

	function onClassListSelect(combo, selections) {
		if (selections.length > 0) {
			this.currentClass = selections[0].get("id");
			this.view.grid.updateStoreForClassId(this.currentClass);
		}
	}

	function onUploadButtonClick() {
		CMDBuild.core.LoadMask.show();
		CMDBuild.core.proxy.utility.ImportCsv.upload({
			form: this.view.form.getForm(),
			loadMask: false,
			scope: this,
			success: updateGridRecords,
			failure: function (response, options, decodedResponse) {
				CMDBuild.core.LoadMask.hide();
			}
		});
	}

	function onUpdateButtonClick() {
		var records = this.view.grid.getRecordToUpload();
		if (records.length == 0) {
			CMDBuild.core.Message.warning(tr.warning, tr.noupdate);
		} else {
			CMDBuild.core.LoadMask.show();
			CMDBuild.core.proxy.utility.ImportCsv.updateRecords({
				params: {
					data: Ext.encode(records)
				},
				loadMask: false,
				scope: this,
				success: updateGridRecords,
				failure: function (response, options, decodedResponse) {
					CMDBuild.core.LoadMask.hide();
				}
			});
		}
	}

	function onConfirmButtonClick() {
		CMDBuild.core.proxy.utility.ImportCsv.storeRecords({
			scope: this,
			failure: function (response, options, decodedResponse) {
				CMDBuild.core.Message.error(tr.error, tr.importfailure, true);
			},
			success: function (response, options, decodedResponse) {
				CMDBuild.core.Message.info(tr.info, tr.importsuccess);
				updateGridRecords.call(this);
			}
		});
	}

	function onAbortButtonClick() {
		this.view.form.reset();
		this.view.grid.removeAll();
	}

	// callback called after the upload of the csv file
	// and after the update of the grid records
	function updateGridRecords(response, options, decodedResponse) {
		CMDBuild.core.proxy.utility.ImportCsv.getRecords({
			scope: this,
			success: function (response, options, decodedResponse) {
				this.view.grid.configureHeadersAndStore(decodedResponse.headers);
				this.view.grid.loadData(decodedResponse.rows);
			}
		});
	}

})();