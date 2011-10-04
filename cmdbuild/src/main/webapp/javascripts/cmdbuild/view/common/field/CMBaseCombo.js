(function() {
	var TRIGGER_LENGTH = 20;
	var PADDING = 20;

Ext.define("CMDBuild.field.CMBaseCombo", {
	alias: "cmbasecombo",
	extend: "Ext.form.field.ComboBox",

	cmGreatestItem: "",

	initComponent : function() {
		this.callParent(arguments);

		this.mon(this.store, 'load', function(store, records, successful, operation) {
			if (operation && operation.add) {
				this._growSizeFix(records || []);
			} else {
				this._growSizeFix();
			}
		}, this);

		this.mon(this.store, 'add', function(store, records) {
			this._growSizeFix(records || []);
		}, this);

		this.mon(this, "render", function() {
			this._growSizeFix();
		}, this);
	},

	_growSizeFix: function(added) {
		// compare the size of the added records with the max already
		// in the store. If no added find the max size over all the records
		var data = added || this.store.getRange();

		for (var i=0,
				l=data.length,
				rec,
				value
				; i<l; ++i) {

			rec = data[i];
			value = rec.get(this.displayField);

			this.setGreatestItem(value);
		}

		this.setSizeLookingTheGreatestItem();
	},

	setGreatestItem: function(item) {
		if (this.cmGreatestItem.length < item.length) {
			this.cmGreatestItem = item;
		}
	},

	// used by the template resolver to know if a field is a combo
	// and to take the value of multilevel lookup
	getReadableValue: function() {
		return this.getRawValue();
	},

	setSizeLookingTheGreatestItem: function() {
		if (this.cmGreatestItem && this.bodyEl) {
			var tm = new Ext.util.TextMetrics(),
				length = tm.getWidth(this.cmGreatestItem) + "px";

			this.bodyEl.dom.firstChild.style.width = length;
			this.bodyEl.dom.style.width = length;

			var fieldLength = this.bodyEl.dom.clientWidth;

			if (this.labelEl) {
				fieldLength += this.labelEl.dom.clientWidth;
			}

			var triggersLength = this.getTriggersLength();

			this.setWidth(fieldLength + triggersLength + PADDING);

			tm.destroy();
		}
	},

	getTriggersLength: function() {
		try {
			return this.triggerEl.elements.length * TRIGGER_LENGTH;
		} catch (e) {
			return 0;
		}
	}
});

})();