(function() {

Ext.ns("CMDBuild.administration.domain");
var ns = CMDBuild.administration.domain;

	ns.CMDomainAttributeController = function(conf) {
		ns.CMDomainAttributeController.superclass.constructor.call(this, conf);
		this.gridController = new ns.CMDomainAttributeGridController({
			view: this.view.grid,
			listeners: {
				onAddAttributeClick: onAddAttributeClick.createDelegate(this),
				onRowSelected: onRowSelected.createDelegate(this)
			}
		});
		
		this.formController = new ns.CMDomainAttributeFormController({
			view: this.view.form,
			listeners: {
				onAttributeSaved: onAttributeSaved.createDelegate(this)
			}
		});
		
		this.view.on("activate", function() {
			this.view.doLayout();
		}, this);
	};
	
	Ext.extend(ns.CMDomainAttributeController, CMDBuild.core.CMBaseController, {
		onAttributeSaved: Ext.emptyFn,
		onDomainSelected: function(domain) {
			this.view.enable();
			this.formController.onDomainSelected(domain);
			this.gridController.onDomainSelected(domain);
		},
		onAddDomainButtonClick: function() {
			this.view.disable();
		},
		beforeAddAttributeToLibrary: function(newDomainAttribute) {
			this.gridController.beforeAddAttributeToLibrary(newDomainAttribute);
		}
	});
	
	function onAddAttributeClick() {
		this.formController.onAddAttributeClick();
		this.gridController.clearSelection();
	}
	
	function onRowSelected(record) {
		this.formController.onRowSelected(record);
	}
	
	function onAttributeSaved(jsonAttribute) {
		this.onAttributeSaved(jsonAttribute);
	}
})();