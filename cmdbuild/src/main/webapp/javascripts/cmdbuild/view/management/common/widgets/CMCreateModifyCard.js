(function() {

	Ext.define("CMDBuild.view.management.common.widgets.CMCreateModifyCard", {
		extend: "CMDBuild.view.management.common.CMFormWithWidgetButtons",

		withButtons: false,

		initComponent: function() {
			this.addCardButton = Ext.create('CMDBuild.core.buttons.iconized.split.add.Card', {
				classId: undefined
			});

			Ext.apply(this, {
				tbar: [this.addCardButton],
				border: false,
				frame: false,
				padding: "0 0 5px 0",
				cls: "x-panel-body-default-framed"
			});

			this.callParent(arguments);
		},

		// buttons that the owner panel add to itself
		getExtraButtons: function() {
			var me = this;
			return [new Ext.Button( {
				text : CMDBuild.Translation.save,
				name : 'saveButton',
				handler: function() {
					me.fireEvent(me.CMEVENTS.saveCardButtonClick);
				}
			})];
		},

		initWidget: function(entryType, isEditable) {
			if (entryType.isSuperClass()) {
				this.addCardButton.updateForEntry(entryType);
				this.addCardButton.setDisabled(!isEditable);
			} else {
				this.addCardButton.disable();
			}
		}
	});

})();