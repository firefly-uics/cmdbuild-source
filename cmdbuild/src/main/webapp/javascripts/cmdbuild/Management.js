(function() {

	/**
	 * Management
	 */
	Ext.application({
		extend: 'Ext.app.Application',

		requires: [
			'Ext.tip.QuickTipManager', // Avoid core override
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
			Ext.WindowManager.getNextZSeed();	// To increase the default zseed. Is needed for the combo on windows probably it fix also the prev problem
			Ext.enableFx = false;
			Ext.tip.QuickTipManager.init();

			// Fix a problem of Ext 4.2 tooltips width
			// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
			delete Ext.tip.Tip.prototype.minWidth;

			Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
			Ext.create('CMDBuild.core.Data', { enableLocalized: true }); // Data connections configuration
			Ext.create('CMDBuild.core.Rest'); // Setup REST connection
			Ext.create('CMDBuild.core.cache.Cache');
			Ext.create('CMDBuild.core.navigation.Chronology'); // Navigation chronology

			CMDBuild.core.Management.init();
		}
	});

})();
