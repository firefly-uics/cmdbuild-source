(function() {
	// TODO 
	// manage reference: if there is a reference, update the value of the combo after card save

	Ext.define("CMDBuild.view.management.workflow.widgets.CMCreateModifyCard", {
		extend: "CMDBuild.view.management.classes.CMCardPanel",
		withButtons: false,

		constructor: function(c) {
			var widget = c.widget;
			widget.cardId = widget.id;
			delete widget.id;

			this.widgetConf = widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;
			this.noSelect = c.noSelect;
	
			this.callParent([widget]); // to apply the conf to the panel
		},

		initComponent: function() {
			this.addCardButton = new CMDBuild.AddCardMenuButton({
				classId: undefined
			});

			Ext.apply(this, {
				tbar: [this.addCardButton]
			});

			this.callParent(arguments);
		},

		// buttons that the owner panel add to itself
		getExtraButtons: function() {
			var me = this;
			return [new Ext.Button( {
				text : CMDBuild.Translation.common.btns.confirm,
				name : 'saveButton',
				handler: function() {
					me.fireEvent(me.CMEVENTS.saveCardButtonClick);
				}
			})]
		},

		initWidget: function(idClass, cardId) {
			var et = _CMCache.getEntryTypeById(idClass);

			if (et.data.superclass) {
				this.addCardButton.updateForEntry(et);
				this.addCardButton.enable();
			} else {
				this.addCardButton.disable();
			}
		}
	});
})();