(function() {

	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;

	Ext.define("CMDBuild.view.administration.domain.CMDomainAttributeGrid", {
		extend: "CMDBuild.view.administration.classes.CMAttributeGrid",

		buildColumnConf: function() {
			this.columns = [{
				header: translation.name,
				dataIndex: 'name',
				flex: 1
			}, {
				header: translation.description,
				dataIndex: 'description',
				flex: 1
			}, {
				header: translation.type,
				dataIndex: 'type',
				flex: 1
			},
			new Ext.ux.CheckColumn( {
				header: translation.isbasedsp,
				dataIndex: 'isbasedsp',
				cmReadOnly: true
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isunique,
				dataIndex: 'isunique',
				cmReadOnly: true
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isnotnull,
				dataIndex: 'isnotnull',
				cmReadOnly: true
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isactive,
				dataIndex: 'isactive',
				cmReadOnly: true
			}), 
			{
				header: translation.field_visibility,
				dataIndex: 'fieldmode',
				renderer: renderEditingMode 
			}];
		},

		buildStore: function() {
			this.store = _CMCache.getDomainAttributesStore();
		},

		buildTBar: function() {
			this.tbar = [this.addAttributeButton];
		},
		
		onDomainSelected: function(domain) {
			this.refreshStore(domain, indexAttributeToSelectAfter = null);
		},

		refreshStore: function(domain, indexAttributeToSelectAfter) {
			if (!domain) {
				return;
			}
			var sm = this.getSelectionModel();
			this.store.loadForDomainId(domain.get("id"));

			if (this.rendered) {
				this.selectRecordAtIndexOrTheFirst(indexAttributeToSelectAfter);
			}
		},
		
		selectAttributeByName: function(name) {
			var sm = this.getSelectionModel();
			var r = this.store.findRecord("name", name);
			if (r) {
				sm.select(r);
			} else if (this.store.count() != 0) {
				sm.select(0);
			}
		}

	});
	
	function renderEditingMode(val) {
		return translation["field_" + val];
	}

})();