(function() {

Ext.define("CMDBuild.Management.SearchableCombo", {
	extend: "CMDBuild.field.CMBaseCombo",

	trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
	trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
	trigger3Cls: Ext.baseCSSPrefix + 'form-search-trigger',

	gridExtraConfig: {},	// use it to pass some
							// configuration to the grid
							// window
	searchWindowReadOnly: false,	// if read only, there
									// isn't the add card
									// button on the window

	initComponent: function() {
		this.labelAlign = "right", this
				.callParent(arguments);
	},

	onTrigger1Click: function() {
		// business rule: if the store has more record than
		// the configuration limit
		// we want open the search window

		if (this.store.isLoading()) {
			this.store.on('load', manageTrigger, this, {
				single: true
			});
		} else {
			manageTrigger.call(this);
		}

		function manageTrigger() {
			if (this.storeIsLargerThenLimit()) {
				this.onTrigger3Click();
			} else {
				this.onTriggerClick();
			}
		};
	},

	onTrigger2Click: function() {
		if (!this.disabled) {
			reset.call(this);
		}
	},

	reset: reset,

	onTrigger3Click: function(){
		this.createSearchWindow();
	},

	storeIsLargerThenLimit: function() {
		if (this.store !== null) {
			return this.store.getTotalCount() > parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);
		}
		return false;
	},

	createSearchWindow: function() {
		if (!this.disabled) {
			var callback = Ext.Function.bind(this.buildSearchWindow, this, [this.store.baseParams], true);
			var idClass = this.store.baseParams.IdClass;
			if (!idClass) {
				var className = this.store.baseParams.ClassName;
				if (className) {
					var entryType = _CMCache.getEntryTypeByName(className);
					if (entryType) {
						idClass = entryType.get("id");
					}
				}
			}

			if (idClass) {
				CMDBuild.Management.FieldManager.loadAttributes(idClass, callback);
			}
		}
	},

	buildSearchWindow: function(attributeList, storeParams) {
		// TODO Filters should be handled differently
		var extraParams = Ext.apply({}, storeParams);
		delete extraParams.NoFilter;

		new CMDBuild.Management.ReferenceSearchWindow({
			ClassName: this.store.baseParams.ClassName,
			filterType: 'reference',
			selModel: new CMDBuild.selection.CMMultiPageSelectionModel({
				mode: "SINGLE",
				idProperty: "Id" // required to identify the records for the data and not the id of ext
			}),
			extraParams: extraParams,
			gridConfig: this.gridExtraConfig || {},
			readOnly: this.searchWindowReadOnly
		}).show().on('cmdbuild-referencewindow-selected', function(record) {
			this.addToStoreIfNotInIt(record);
			this.focus(); // to allow the "change" event that occurs on blur
			this.setValue(record.get("Id"));
			this.fireEvent('cmdbuild-reference-selected', record, this);
		}, this);
	},

	addToStoreIfNotInIt: function(record) {
		var _store = this.store,
			id = record.get("Id");

		if (_store 
				&& _store.find('Id', id) == -1 ) {

			_store.add({
				Id : id,
				Description: this.recordDescriptionFixedForCarriageReturnBugOnComboBoxes(record)
			});
		}
	},

	recordDescriptionFixedForCarriageReturnBugOnComboBoxes: function(record) {
		try {
			return record.get("Description").replace(/\n/g," ");
		} catch (e) { }
	},

	hideTrigger1 :false,
	hideTrigger2 :false,
	hideTrigger3 :false
});

	function reset() {
		this.setValue([""]); // if use clearValue the form does not send the value, so it is not possible delete the value on server side
		this.fireEvent("clear");
		this.fireEvent('change', this, this.getValue(), this.startValue);
	}

})();