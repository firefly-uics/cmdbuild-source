(function() {

	Ext.ns("CMDBuild.administration.domain");
	var ns = CMDBuild.administration.domain;
	
	ns.CMDomainAttributeGridController = function(conf) {
		ns.CMDomainAttributeGridController.superclass.constructor.call(this, conf);

		this.view.addButton.on("click", function() {
			this.onAddAttributeClick();
		}, this);
		
		var sm = this.view.getSelectionModel();
		sm.on("rowselect", function(sm, rowIndex, record) {
			this.onRowSelected(record);
		}, this);
	};

	Ext.extend(ns.CMDomainAttributeGridController, CMDBuild.core.CMBaseController, {
		onAddAttributeClick: Ext.emptyFn,
		onRowSelected: Ext.emptyFn,
		onDomainSelected: function(domain) {
			var attributeStore = domain.getAttributeLibrary().asStore();
			this.view.changeStore(attributeStore);
			try {
				selectFirst.call(this);
			} catch (e) {
				this.view.on({
					render:{
						scope: this,
						single: true,
						fn: function() {
							selectFirst.defer(100, this);
						}
					}
				});
			}
		},
		clearSelection: function() {
			this.view.getSelectionModel().clearSelections();
		},
		beforeAddAttributeToLibrary: function(newDomainAttribute) {
			this.view.store.on({
				add:{
					scope: this.view,
					single: true,
					fn: function(store, records, index) {
						this.getSelectionModel().selectRecords(records);
					}
				}
			});
		}
	});
	// to call with this as scope
	function selectFirst() {
		this.view.getSelectionModel().selectFirstRow();
	}
})();