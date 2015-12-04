(function() {

	Ext.ns('CMDBuild');

	// Global constants
	CMDBuild.LABEL_WIDTH = 150;
	CMDBuild.LABEL_WIDTH_LOGIN = 100;

	CMDBuild.BIG_FIELD_ONLY_WIDTH = 475;
	CMDBuild.MEDIUM_FIELD_ONLY_WIDTH = 150;
	CMDBuild.SMALL_FIELD_ONLY_WIDTH = 100;
	CMDBuild.BIG_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.BIG_FIELD_ONLY_WIDTH;
	CMDBuild.MEDIUM_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.MEDIUM_FIELD_ONLY_WIDTH;
	CMDBuild.SMALL_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.SMALL_FIELD_ONLY_WIDTH;

	CMDBuild.ADM_BIG_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 250;
	CMDBuild.ADM_MEDIUM_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 150;
	CMDBuild.ADM_SMALL_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 80;

	CMDBuild.CFG_LABEL_WIDTH = 300;
	CMDBuild.CFG_BIG_FIELD_WIDTH = CMDBuild.CFG_LABEL_WIDTH + 450;
	CMDBuild.CFG_MEDIUM_FIELD_WIDTH = CMDBuild.CFG_LABEL_WIDTH + 150;

	// Custom widths
	CMDBuild.HTML_EDITOR_WIDTH = CMDBuild.LABEL_WIDTH + 600;

	// Global object with runtime configuration
	CMDBuild.configuration = {};
	CMDBuild.Config = {}; // @deprecated

	Ext.WindowManager.getNextZSeed();	// To increase the default zseed. Is needed for the combo on windows probably it fix also the prev problem
	Ext.enableFx = false;

})();