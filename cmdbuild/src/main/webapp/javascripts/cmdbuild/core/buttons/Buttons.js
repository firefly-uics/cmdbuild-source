(function() {

	Ext.define('CMDBuild.buttons.BaseButton', {
		extend: 'Ext.button.Button',

		withIcon: false,
		classIcon: undefined,

		minWidth: 75,

		initComponent: function() {
			if (this.withIcon && classIcon)
				Ext.apply(this, {
					cls: this.classIcon
				});

			this.callParent(arguments);
		}
	});

	Ext.define('CMDBuild.buttons.SaveButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.save
	});

	Ext.define('CMDBuild.buttons.ConfirmButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.confirm
	});

	Ext.define('CMDBuild.buttons.AbortButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.abort
	});

	Ext.define('CMDBuild.buttons.ImportButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.importLabel
	});

	Ext.define('CMDBuild.buttons.ExportButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.exportLabel
	});

	Ext.define('CMDBuild.buttons.UpdateButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.update
	});

	Ext.define('CMDBuild.buttons.CloseButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.close
	});

	Ext.define('CMDBuild.buttons.ApplyButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.apply
	});

	Ext.define('CMDBuild.buttons.PreviousButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.previous
	});

	Ext.define('CMDBuild.buttons.NextButton', {
		extend: 'CMDBuild.buttons.BaseButton',

		text: CMDBuild.Translation.common.buttons.next
	});

	Ext.define('CMDBuild.buttons.Download', {
		extend: 'CMDBuild.buttons.BaseButton',

		iconCls: 'download',
		text: CMDBuild.Translation.download
	});

})();