(function() {
	Ext.ns("CMDBuild.administration.domain");
	/**
	 * @class CMDBuild.administration.domain.ModDomain
	 * @extend CMDBuild.ModPanel
	 * 
	 * Define the main panel of to manage the domain.
	 * It contains the form to define/modify a domain, and the button to
	 * remove them.
	 */
	CMDBuild.administration.domain.ModDomain = Ext.extend(CMDBuild.ModPanel, {
		modtype:'domain',	
		translation: CMDBuild.Translation.administration.modClass,
		initComponent : function() {
			CMDBuild.administration.domain.ModDomain.superclass.initComponent.apply(this, arguments);
		}
	});
	
})();