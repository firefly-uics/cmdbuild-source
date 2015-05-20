(function() {

	/**
	 * Extends common tab grid panel to create row expander instance with custom ID to avoid multiple expander instances problems
	 */
	Ext.define('CMDBuild.view.management.classes.tabs.history.GridPanel', {
		extend: 'CMDBuild.view.management.common.tabs.history.GridPanel',

		config: {
			plugins: [
				Ext.create('CMDBuild.view.management.common.tabs.history.RowExpander', {
					id: 'classesHistoryTabRowExpander'
				})
			]
		}
	});

})();