(function() {

	// Class not well formatted but it's a good solution to avoid tons of little class files

	Ext.define('CMDBuild.core.buttons.Delete', {
		extend: 'Ext.button.Button',

		iconCls: 'delete',
		text: CMDBuild.Translation.deleteLabel
	});

	Ext.define('CMDBuild.core.buttons.Modify', {
		extend: 'Ext.button.Button',

		iconCls: 'modify',
		text: CMDBuild.Translation.common.buttons.modify
	});

})();