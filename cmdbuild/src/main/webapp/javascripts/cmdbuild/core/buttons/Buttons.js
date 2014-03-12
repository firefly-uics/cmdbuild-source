(function() {

	Ext.define('CMDBuild.buttons.BaseButton', {
		extend: 'Ext.button.Button',

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

	Ext.define('CMDBuild.buttons.SaveButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: CMDBuild.Translation.common.btns.save
	});

	Ext.define('CMDBuild.buttons.ConfirmButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: CMDBuild.Translation.common.btns.confirm
	});

	Ext.define('CMDBuild.buttons.AbortButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: CMDBuild.Translation.common.btns.abort
	});

	Ext.define('CMDBuild.buttons.ImportButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: CMDBuild.Translation.common.btns.importbtn
	});

	Ext.define('CMDBuild.buttons.ExportButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: CMDBuild.Translation.common.btns.exportbtn
	});

	Ext.define('CMDBuild.buttons.UpdateButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: CMDBuild.Translation.common.btns.update
	});

	Ext.define('CMDBuild.buttons.CloseButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: CMDBuild.Translation.common.btns.close
	});

	Ext.define('CMDBuild.buttons.ApplyButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: CMDBuild.Translation.common.btns.apply
	});

	Ext.define('CMDBuild.buttons.PreviousButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: "@@ Previous"
	});

	Ext.define('CMDBuild.buttons.NextButton', {
		extend: 'CMDBuild.buttons.BaseButton',
		text: "@@ Next"
	});

})();