(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslationsTable}
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
		 * @return {Mixed} returnObject
		 */
		buildColumn: function(languageObject) { // TODO
			var returnObject = null;

			if (!Ext.isEmpty(languageObject))
				returnObject = Ext.create('Ext.grid.column.Column', {
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

			return returnObject;
		}
	});

})();