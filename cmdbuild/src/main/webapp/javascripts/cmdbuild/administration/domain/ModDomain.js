(function() {
	Ext.ns("CMDBuild.administration.domain");

	CMDBuild.administration.domain.ModDomain = Ext.extend(CMDBuild.ModPanel, {
		modtype:'domain',	
		translation: CMDBuild.Translation.administration.modClass,
		constructor: function() {
			this.addButton = new Ext.Button({
				iconCls: 'add',
				text: "@add"
			});

			this.domainForm = new CMDBuild.administration.domain.CMDomainForm({
				title: "@@Proprietà"
			});
			
			this.tabPanel = new Ext.TabPanel({
				region: "center",
				frame: false,
				border: false,
				cls: CMDBuild.Constants.css.bg_gray,
				items: [this.domainForm],
				activeTab: 0
			});

			CMDBuild.administration.domain.ModDomain.superclass.constructor.apply(this, arguments);
		},

		initComponent : function() {
			this.layout = "border";
			this.title = "@@Domini";
			this.tbar = [this.addButton];
			this.items = [this.tabPanel];
			CMDBuild.administration.domain.ModDomain.superclass.initComponent.apply(this, arguments);
		}
	});

})();