(function() {

	Ext.ns("CMDBuild.administration.domain");
	
	CMDBuild.administration.domain.CMDomainAttribute = Ext.extend(Ext.Panel, {
		initComponent: function() {
			this.layout = "border";
			this.form = new CMDBuild.administration.domain.CMDomainAttributeForm({
				region: "center",
				baseCls: CMDBuild.Constants.css.top_border_gray
			});

			this.grid = new CMDBuild.administration.domain.CMDomainAttributeGrid({
				region: "north",
				height: 200,
				split: true,
				baseCls: CMDBuild.Constants.css.bottom_border_gray
			});

			this.items = [this.form, this.grid];

			CMDBuild.administration.domain.CMDomainAttribute.superclass.initComponent.apply(this, arguments);
		}
	});
})();