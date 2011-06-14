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
				dataIndex: 'isbasedsp'
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isunique,
				dataIndex: 'isunique'
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isnotnull,
				dataIndex: 'isnotnull'
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isactive,
				dataIndex: 'isactive'
			}), 
			{
				header: translation.field_visibility,
				dataIndex: 'fieldmode',
				renderer: renderEditingMode 
			}];
		},

		buildStore: function() {
			this.store = new Ext.data.Store({
				fields: [
					"index", "name", "description", "type", "isunique",
					"isbasedsp", "isnotnull","inherited", 'fieldmode',
					'isactive', "group"
				],
				autoLoad : false,
				sorters : [ {
					property : 'index',
					direction : "ASC"
				}]
			});
		},

		buildTBar: function() {
			this.tbar = [this.addAttributeButton];
		},
		
		onDomainSelected: function(domain) {
			this.refreshStore(domain, indexAttributeToSelectAfter = null);
		},

		refreshStore: function(domain, indexAttributeToSelectAfter) {
			var sm = this.getSelectionModel();
			var store = _CMCache.getDomainAttributesStoreForDomainId(domain.get("id"));
			
			this.reconfigure(store);
			
			this.filterInherited(this.filtering);
			if (indexAttributeToSelectAfter) {
				var r = this.store.findRecord("index", indexAttributeToSelectAfter);
				if (r) {
					sm.select(r);
				}
			} else if (this.store.count() != 0) {
				sm.select(0);
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