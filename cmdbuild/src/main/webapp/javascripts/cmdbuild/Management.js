(function() {

	/**
	 * Management
	 */
	Ext.onReady(function() {
		Ext.tip.QuickTipManager.init();

		// Fix a problem of Ext 4.2 tooltips width
		// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
		delete Ext.tip.Tip.prototype.minWidth;

		Ext.application({
			extend: 'Ext.app.Application',

			requires: [
				'Ext.ux.Router',
				'CMDBuild.routes.management.Cards',
				'CMDBuild.routes.management.Classes',
				'CMDBuild.routes.management.Instances',
				'CMDBuild.routes.management.Processes',
				'CMDBuild.core.Management'
			],

			appFolder: './javascripts/cmdbuild',
			name: 'CMDBuild',

			routes: {
				// Classes
				'classes/:classIdentifier/cards': 'CMDBuild.routes.management.Classes#saveRoute', // Alias (wrong implementation, to delete in future)
				'classes/:classIdentifier/cards/': 'CMDBuild.routes.management.Classes#saveRoute',
				'classes/:classIdentifier/print': 'CMDBuild.routes.management.Classes#saveRoute',

				'exec/classes/:classIdentifier/cards': 'CMDBuild.routes.management.Classes#detail', // Alias (wrong implementation, to delete in future)
				'exec/classes/:classIdentifier/cards/': 'CMDBuild.routes.management.Classes#detail',
				'exec/classes/:classIdentifier/print': 'CMDBuild.routes.management.Classes#print',

				// Cards
				'classes/:classIdentifier/cards/:cardIdentifier': 'CMDBuild.routes.management.Cards#saveRoute', // Alias (wrong implementation, to delete in future)
				'classes/:classIdentifier/cards/:cardIdentifier/': 'CMDBuild.routes.management.Cards#saveRoute',
				'classes/:classIdentifier/cards/:cardIdentifier/print': 'CMDBuild.routes.management.Cards#saveRoute',

				'exec/classes/:classIdentifier/cards/:cardIdentifier': 'CMDBuild.routes.management.Cards#detail', // Alias (wrong implementation, to delete in future)
				'exec/classes/:classIdentifier/cards/:cardIdentifier/': 'CMDBuild.routes.management.Cards#detail',
				'exec/classes/:classIdentifier/cards/:cardIdentifier/print': 'CMDBuild.routes.management.Cards#print',

				// Processes
				'processes/:processIdentifier/instances/': 'CMDBuild.routes.management.Processes#saveRoute',
				'processes/:processIdentifier/print': 'CMDBuild.routes.management.Processes#saveRoute',
				'processes/': 'CMDBuild.routes.management.Processes#saveRoute',

				'exec/processes/:processIdentifier/instances/': 'CMDBuild.routes.management.Processes#detail',
				'exec/processes/:processIdentifier/print': 'CMDBuild.routes.management.Processes#print',
				'exec/processes/': 'CMDBuild.routes.management.Processes#showAll',

				// Instances
				'processes/:processIdentifier/instances/:instanceIdentifier/': 'CMDBuild.routes.management.Instances#saveRoute',

				'exec/processes/:processIdentifier/instances/:instanceIdentifier/': 'CMDBuild.routes.management.Instances#detail',
			},

			launch: function() {
				Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
				Ext.create('CMDBuild.core.Data'); // Data connections configuration
				Ext.create('CMDBuild.core.Rest'); // Setup REST connection

				CMDBuild.core.Management.init();
			}
		});
	});

})();