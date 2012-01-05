Ext.define("CMDBuild.view.management.common.CMCardWindow", {
	extend: "CMDBuild.PopupWindow",

	cmEditMode: false, // if true, after the attributes load go in edit mode
	withButtons: false, // true to use the buttons build by the CMCardPanel

	initComponent:function() {

		this.cardPanel = new CMDBuild.view.management.classes.CMCardPanel({
			withButtons: this.withButtons,
			withToolBar: this.withToolBar,
			allowNoteFiled: true
		});

		var ee = this.cardPanel.CMEVENTS;
		this.CMEVENTS = {
			saveCardButtonClick: ee.saveCardButtonClick,
			abortButtonClick: ee.abortButtonClick,
			formFilled: ee.formFilled,
			widgetButtonClick: ee.widgetButtonClick
		};

		this.relayEvents(this.cardPanel, [
			ee.saveCardButtonClick,
			ee.abortButtonClick,
			ee.formFilled,
			ee.widgetButtonClick
		]);

		this.addEvents(ee.saveCardButtonClick);
		this.addEvents(ee.abortButtonClick);
		this.addEvents(ee.formFilled);
		this.addEvents(ee.widgetButtonClick);

		if (this.classId) {
			var privileges = _CMUtils.getClassPrivileges(this.classId);
			this.cardPanel.writePrivilege = privileges.write;
		}

		if (!this.withButtons) {
			this.closeButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.close,
				handler : this.close,
				scope : this
			});
			this.buttons = [this.closeButton];
		}

		Ext.apply(this, {
			items : this.cardPanel,
			buttonAlign : "center"
		});

		this.callParent(arguments);

		_CMUtils.forwardMethods(this, this.cardPanel, ["displayMode", "editMode", "fillForm", "loadCard", "reset", "getForm", "getWidgetButtonsPanel"]);
	}
});