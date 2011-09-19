Ext.define("CMDBuild.view.management.common.CMCardWindow", {
	extend: "CMDBuild.PopupWindow",
	classId: undefined, // setted in instantiation
	cardId: undefined,
	cmEditMode: false, // if true, after the attributes load go in edit mode
	withButtons: false, // true to use the buttons build by the CMCardPanel

	initComponent:function() {

		this.cardPanel = new CMDBuild.view.management.classes.CMCardPanel( {
			withButtons: this.withButtons,
			withToolBar: this.withToolBar,
			allowNoteFiled: true
		});

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

		this.on("show", this.loadCard, this);
		this.cardPanel.on("cmdb-close-window", this.close, this);
	},

	loadCard: function() {
		function fillForm(attributes) {
			this.cardPanel.fillForm(attributes, this.cmEditMode);
			if (this.cardId) {
				this.cardPanel.loadCard(this.cardId, this.classId);
			}
		}

		_CMCache.getAttributeList(this.classId, Ext.bind(fillForm, this));
	}
});