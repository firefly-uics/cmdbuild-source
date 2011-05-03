(function() {
	Ext.ns("CMDBuild.administration.domain");
	var ns = CMDBuild.administration.domain;
	
	ns.ModDomainController = function(panel, accordion) {
		this.panel = panel;
		this.accordion = accordion;
		this.domain = null;
		
		this.formController = new ns.CMDomainFormController({
			view: this.panel.domainForm,
			listeners: {
				onDomainDeleted: function() {
					panel.onDomainDeleted();
				}
			}
		});

		this.attributesController = new ns.CMDomainAttributeController({
			view: this.panel.domainAttributes,
			listeners: {
				onAttributeSaved: onAttributeSaved.createDelegate(this)
			}
		});

		this.panel.addButton.on("click", onAddDomainButtonClick, this);

		this.panel.on("select", function(domain) {
			if (domain) {
				this.domain = domain;
				this.formController.onDomainSelected(domain);
				this.attributesController.onDomainSelected(domain);
				this.panel.setTitleSuffix(domain.getdescription());
			}
		}, this);
		
		this.onAddDomainButtonClick = function() {
			this.accordion.silentExpand();
			this.panel.selectPanel();
			onAddDomainButtonClick.call(this);
		}
		
		this.onDomainDoubleClick = function(id) {
			this.accordion.selectNodeById(id, expandAfter=true);
		}
	};
	
	function onAttributeSaved(jsonAttribute) {
		if (this.domain != null) {
			try {
				var attributeLibary = this.domain.getAttributeLibrary();
				var newAttribute = CMDBuild.core.model.CMAttributeModel.buildFromJson(jsonAttribute);
				var oldAttribute = attributeLibary.get(newAttribute.getname());
				
				if (oldAttribute == null) {
					this.attributesController.beforeAddAttributeToLibrary(newAttribute);
					attributeLibary.add(newAttribute);
				} else {
					oldAttribute.update(newAttribute);
				}
			} catch (e) {
				_debug(e);
			}
		}
	};
	
	function onAddDomainButtonClick() {
		this.accordion.deselect();
		this.panel.selectPropertiesTab();
		this.formController.onAddButtonClick();
		this.attributesController.onAddDomainButtonClick();
	}
})();