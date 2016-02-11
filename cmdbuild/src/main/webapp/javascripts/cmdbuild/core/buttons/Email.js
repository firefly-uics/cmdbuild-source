(function() {

	/**
	 * Fake class to be able to include this file with Extjs loader
	 */
	Ext.define('CMDBuild.core.buttons.Email', {});

	Ext.define('CMDBuild.core.buttons.EmailEdit', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'email-edit',
		textDefault: CMDBuild.Translation.edit
	});

	Ext.define('CMDBuild.core.buttons.EmailDelete', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'email-delete',
		textDefault: CMDBuild.Translation.deleteLabel
	});

	Ext.define('CMDBuild.core.buttons.EmailRegenerate', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'email-regenerate',
		textDefault: CMDBuild.Translation.regenerateEmail
	});

	Ext.define('CMDBuild.core.buttons.EmailReply', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'email-reply',
		textDefault: CMDBuild.Translation.reply,
	});

	Ext.define('CMDBuild.core.buttons.EmailSend', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'email-send',
		textDefault: CMDBuild.Translation.send,
	});

	Ext.define('CMDBuild.core.buttons.EmailView', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'email-view',
		textDefault: CMDBuild.Translation.view
	});

})();