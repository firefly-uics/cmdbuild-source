(function() {

	Ext.define('CMDBuild.view.administration.localizations.advancedTranslationsTable.SectionClassesPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTranslationsTable.SectionClasses}
		 */
		delegate: undefined,

		title: '@@ Classes',

		bodyCls: 'cmgraypanel',
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('CMDBuild.view.administration.localizations.panels.AdvancedTranslationsTableGrid', {
						delegate: this.delegate,

						columns: this.delegate.cmOn('buildColumns'),
						store: this.delegate.cmOn('buildStore')
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();