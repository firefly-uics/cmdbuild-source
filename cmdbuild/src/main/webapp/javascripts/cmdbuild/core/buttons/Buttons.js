Ext.ns('CMDBuild.buttons');

CMDBuild.buttons.BaseButton = Ext.extend(Ext.button.Button, {
	withIcon: false,
	classIcon: undefined,
	initComponent: function() {
		if (this.withIcon && classIcon) {
			Ext.apply(this, {
				cls: this.classIcon
			});
		}
		this.callParent(arguments);
	}
});

CMDBuild.buttons.SaveButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: CMDBuild.Translation.common.btns.save
});

CMDBuild.buttons.ConfirmButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: CMDBuild.Translation.common.btns.confirm
});

CMDBuild.buttons.AbortButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: CMDBuild.Translation.common.btns.abort
});

CMDBuild.buttons.ImportButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: CMDBuild.Translation.common.btns.importbtn
});

CMDBuild.buttons.ExportButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: CMDBuild.Translation.common.btns.exportbtn
});

CMDBuild.buttons.UpdateButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: CMDBuild.Translation.common.btns.update
});

CMDBuild.buttons.CloseButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: CMDBuild.Translation.common.btns.close
});

CMDBuild.buttons.ApplyButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: CMDBuild.Translation.common.btns.apply
});

CMDBuild.buttons.PreviousButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: "@@ Previous"
});

CMDBuild.buttons.NextButton = Ext.extend(CMDBuild.buttons.BaseButton, {
	text: "@@ Next"
});