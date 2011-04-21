(function() {

	Ext.ns("CMDBuild.administration.domain");
	
	CMDBuild.administration.domain.CMDomainAttribute = Ext.extend(Ext.Panel, {
		initComponent: function() {
//			this.attributeForm = new CMDBuild.administration.form.AttributeFormTemplate();
			this.items = [
//			              this.attributeForm
	              ];
			
			CMDBuild.administration.domain.CMDomainAttribute.superclass.initComponent.apply(this, arguments);
		}
	});
})();