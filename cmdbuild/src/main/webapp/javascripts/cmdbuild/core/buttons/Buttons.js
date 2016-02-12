(function() {

	/**
	 * Fake class to require all button classes
	 */
	Ext.define('CMDBuild.core.buttons.Buttons', {

		requires: [
			'CMDBuild.core.buttons.Email',
			'CMDBuild.core.buttons.FileFormats',
			'CMDBuild.core.buttons.Iconized'
		]
	});

	Ext.define('CMDBuild.core.buttons.Abort', {
		alternateClassName: 'CMDBuild.buttons.AbortButton', // Legacy class name
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.abort
	});

	Ext.define('CMDBuild.core.buttons.Apply', {
		alternateClassName: 'CMDBuild.buttons.ApplyButton', // Legacy class name
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.apply
	});

	Ext.define('CMDBuild.core.buttons.Back', {
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.workflow.back
	});

	Ext.define('CMDBuild.core.buttons.Close', {
		alternateClassName: 'CMDBuild.buttons.CloseButton', // Legacy class name
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.close
	});

	Ext.define('CMDBuild.core.buttons.Confirm', {
		alternateClassName: 'CMDBuild.buttons.ConfirmButton', // Legacy class name
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.confirm
	});

	Ext.define('CMDBuild.core.buttons.Export', {
		alternateClassName: 'CMDBuild.buttons.ExportButton', // Legacy class name
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.exportLabel
	});

	Ext.define('CMDBuild.core.buttons.Previous', {
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.previous
	});

	Ext.define('CMDBuild.core.buttons.Save', {
		alternateClassName: 'CMDBuild.buttons.SaveButton', // Legacy class name
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.save
	});

	Ext.define('CMDBuild.core.buttons.Next', {
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.next
	});

	Ext.define('CMDBuild.core.buttons.Update', {
		alternateClassName: 'CMDBuild.buttons.UpdateButton', // Legacy class name
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.common.buttons.update
	});

	Ext.define('CMDBuild.core.buttons.Upload', {
		extend: 'CMDBuild.core.buttons.Base',

		textDefault: CMDBuild.Translation.upload
	});

})();
