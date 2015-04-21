(function() {

	// Class not well formatted but it's a good solution to avoid tons of little class files

	Ext.define('CMDBuild.core.buttons.Add', {
		extend: 'Ext.button.Button',

		iconCls: 'add',
		text: CMDBuild.Translation.add
	});

	Ext.define('CMDBuild.core.buttons.Clone', {
		extend: 'Ext.button.Button',

		iconCls: 'clone',
		text: CMDBuild.Translation.common.buttons.clone
	});

	Ext.define('CMDBuild.core.buttons.Delete', {
		extend: 'Ext.button.Button',

		iconCls: 'delete',
		text: CMDBuild.Translation.deleteLabel
	});

	Ext.define('CMDBuild.core.buttons.Import', {
		extend: 'Ext.button.Button',

		iconCls: 'import',
		text: CMDBuild.Translation.common.buttons.modify
	});

	Ext.define('CMDBuild.core.buttons.Modify', {
		extend: 'Ext.button.Button',

		iconCls: 'modify',
		text: CMDBuild.Translation.common.buttons.modify
	});

	Ext.define('CMDBuild.core.buttons.Reload', {
		extend: 'Ext.button.Button',

		iconCls: 'x-tbar-loading',
		text: CMDBuild.Translation.common.buttons.reload
	});

	Ext.define('CMDBuild.core.buttons.Start', {
		extend: 'Ext.button.Button',

		iconCls: 'start',
		text: CMDBuild.Translation.common.buttons.modify
	});

	Ext.define('CMDBuild.core.buttons.Stop', {
		extend: 'Ext.button.Button',

		iconCls: 'stop',
		text: CMDBuild.Translation.common.buttons.modify
	});

})();