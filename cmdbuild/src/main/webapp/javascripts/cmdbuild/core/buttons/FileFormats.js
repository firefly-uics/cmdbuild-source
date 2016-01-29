(function() {

	/**
	 * Fake class to be able to include this file with Extjs loader
	 */
	Ext.define('CMDBuild.core.buttons.FileFormats', {});

	Ext.define('CMDBuild.core.buttons.FileFormatsCsv', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'csv',
		textDefault: CMDBuild.Translation.csv
	});

	Ext.define('CMDBuild.core.buttons.FileFormatsOdt', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'odt',
		textDefault: CMDBuild.Translation.odt
	});

	Ext.define('CMDBuild.core.buttons.FileFormatsPdf', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'pdf',
		textDefault: CMDBuild.Translation.pdf
	});

	Ext.define('CMDBuild.core.buttons.FileFormatsRtf', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'rtf',
		textDefault: CMDBuild.Translation.rtf
	});

	Ext.define('CMDBuild.core.buttons.FileFormatsSql', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'sql',
		textDefault: CMDBuild.Translation.sql // TODO: probably to add to translations
	});

	Ext.define('CMDBuild.core.buttons.FileFormatsXml', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'xml',
		textDefault: CMDBuild.Translation.xml // TODO: probably to add to translations
	});

	Ext.define('CMDBuild.core.buttons.FileFormatsZip', {
		extend: 'CMDBuild.core.buttons.Base',

		iconCls: 'zip',
		textDefault: CMDBuild.Translation.zip // TODO: probably to add to translations
	});

})();