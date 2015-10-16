(function() {

	Ext.onReady(function() {
		Ext.tip.QuickTipManager.init();

		// Fix a problem of Ext 4.2 tooltips width
		// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
		delete Ext.tip.Tip.prototype.minWidth;

		Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
		Ext.create('CMDBuild.core.Data'); // Data connections configuration

		Ext.create('CMDBuild.controller.logout.Logout');
	});

})();