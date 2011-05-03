(function() {
	Ext.ns("CMDBuild.administration.domain");
	var translationDomainProp = CMDBuild.Translation.administration.modClass.domainProperties;
	var translationModClass = CMDBuild.Translation.administration.modClass;
	
	var baseTitle = translationModClass.tabs.domains;
	
	CMDBuild.administration.domain.ModDomain = Ext.extend(CMDBuild.ModPanel, {
		modtype:'domain',	
		translation: CMDBuild.Translation.administration.modClass,
		NAME: "CMModDomain",
		constructor: function() {
			this.addButton = new Ext.Button({
				iconCls: 'add',
				text: translationDomainProp.add_domain
			});

			this.domainForm = new CMDBuild.administration.domain.CMDomainForm({
				title: translationModClass.tabs.properties
			});

			this.domainAttributes = new CMDBuild.administration.domain.CMDomainAttribute({
				title: translationModClass.tabs.attributes
			});

			this.tabPanel = new Ext.TabPanel({
				region: "center",
				frame: false,
				border: false,
				cls: CMDBuild.Constants.css.bg_gray,
				items: [this.domainForm, this.domainAttributes],
				activeTab: 0
			});

			CMDBuild.administration.domain.ModDomain.superclass.constructor.apply(this, arguments);
		},

		initComponent : function() {
			this.layout = "border";
			this.title = baseTitle;
			this.tbar = [this.addButton];
			this.items = [this.tabPanel];
			CMDBuild.administration.domain.ModDomain.superclass.initComponent.apply(this, arguments);
		},

		selectPropertiesTab: function() {
			this.tabPanel.activate(this.domainForm);
		},

		onDomainDeleted: function() {
			this.domainAttributes.disable();
			this.domainForm.disableModify();
			this.domainForm.disableToolBar();
		},

		setTitleSuffix: function(domainDescription) {
			if (typeof domainDescription != "undefined") {
				this.setTitle(baseTitle + " - " + domainDescription);
			} else {
				this.setTitle(baseTitle);
			}
		} 
	});

})();