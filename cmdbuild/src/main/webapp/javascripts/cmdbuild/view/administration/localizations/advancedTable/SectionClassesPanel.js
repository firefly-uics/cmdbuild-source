(function() {

	Ext.define('CMDBuild.view.administration.localizations.advancedTable.SectionClassesPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.SectionClasses}
		 */
		delegate: undefined,

		bodyCls: 'cmgraypanel',
		layout: 'fit',
		title: '@@ Classes',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('CMDBuild.view.administration.localizations.common.AdvancedTableGrid', {
						delegate: this.delegate,

						columns: this.delegate.cmfg('localizationsAdvancedTableBuildColumns'),
						store: this.delegate.cmfg('localizationsAdvancedTableClassesBuildStore')
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();