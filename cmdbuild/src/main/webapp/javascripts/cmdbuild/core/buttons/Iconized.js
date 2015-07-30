(function() {

	/**
	 * Fake class to be able to include this file with Extjs loader
	 */
	Ext.define('CMDBuild.core.buttons.Iconized', {});

	Ext.define('CMDBuild.core.buttons.Add', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'add',
		textDefault: CMDBuild.Translation.add
	});

	Ext.define('CMDBuild.core.buttons.Check', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'ok',
		textDefault: CMDBuild.Translation.common.buttons.check
	});

	Ext.define('CMDBuild.core.buttons.Clone', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'clone',
		textDefault: CMDBuild.Translation.common.buttons.clone
	});

	Ext.define('CMDBuild.core.buttons.Delete', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'delete',
		textDefault: CMDBuild.Translation.deleteLabel
	});

	Ext.define('CMDBuild.core.buttons.Download', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'download',
		textDefault: CMDBuild.Translation.download
	});

	Ext.define('CMDBuild.core.buttons.Import', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'import',
		textDefault: CMDBuild.Translation.common.buttons.modify
	});

	Ext.define('CMDBuild.core.buttons.Modify', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'modify',
		textDefault: CMDBuild.Translation.common.buttons.modify
	});

	Ext.define('CMDBuild.core.buttons.Password', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'password',
		textDefault: CMDBuild.Translation.common.buttons.password
	});

	Ext.define('CMDBuild.core.buttons.Reload', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'x-tbar-loading',
		textDefault: CMDBuild.Translation.common.buttons.reload
	});

	Ext.define('CMDBuild.core.buttons.Start', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'start',
		textDefault: CMDBuild.Translation.common.buttons.modify
	});

	Ext.define('CMDBuild.core.buttons.Stop', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'stop',
		textDefault: CMDBuild.Translation.common.buttons.modify
	});

})();