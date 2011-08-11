Ext.define("CMDBuild.Administration.SetOrderWindow", {
	extend: "CMDBuild.PopupWindow",
	height: 300,
	width: 300,
	initComponent: function() {

		this.saveBtn = new CMDBuild.buttons.SaveButton({
			handler: this.onSave,
			scope: this
		});

		this.abortBtn = new CMDBuild.buttons.AbortButton({
			handler: this.onAbort,
			scope: this
		});

		this.grid = new CMDBuild.Administration.AttributeSortingGrid({
			idClass: this.idClass
		});

		Ext.apply(this, {
			items: [this.grid],
			buttonAlign: 'center',
			buttons: [this.saveBtn, this.abortBtn]
		})
	
		this.callParent(arguments);
	},
	
	onSave: function() {
		this.hide();
		var records = this.grid.getStore().getRange();
		var recToSend = {};

		for (var order = 0, i = 0, len=records.length; i<len; i++) {
			var rec = records[i];
			if (rec.data.classOrderSign == 0) {
				continue;
			}
			++order;
			recToSend[rec.data.name] = (rec.data.classOrderSign > 0 ? order : -order);
		}

		CMDBuild.Ajax.request({
			method: 'POST',
			url : 'services/json/schema/modclass/saveordercriteria',
			params: {
				records: Ext.encode(recToSend),
				idClass: this.idClass
			},
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			scope: this,
			callback: function() {
				this.onAbort();
			}
		});
	},

	onAbort: function() {
		try {
			this.close();
		} catch (e) {
			_debug(e);
		}
	}
});