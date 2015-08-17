(function() {

	Ext.define('CMDBuild.view.administration.localizations.advancedTranslationsTable.MainPanel', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTranslationsTable.Main}
		 */
		delegate: undefined,

		activeTab: 0,
		bodyCls: 'cmgraypanel-nopadding',
		border: false,
		buttonAlign: 'center',
		frame: false,
		region: 'center',

		/**
		 * @param {CMDBuild.model.Localizations.translation} languageObject
		 *
		 * @return {Ext.grid.column.Column} or null
		 */
		buildColumn: function(languageObject) {
			if (!Ext.isEmpty(languageObject))
				return Ext.create('Ext.grid.column.Column', {
					text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/'
						+ languageObject.get(CMDBuild.core.proxy.CMProxyConstants.TAG) + '.png" alt="'
						+ languageObject.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION) + ' language icon" /> '
						+ languageObject.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION),
					dataIndex: languageObject.get(CMDBuild.core.proxy.CMProxyConstants.TAG),
					width: 300,
					sortable: false,
					draggable: false,

					editor: { xtype: 'textfield' }
				});

			return null;
		}
	});

})();