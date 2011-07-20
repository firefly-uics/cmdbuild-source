(function() {
	
	var tr_attributes = CMDBuild.Translation.administration.modClass.attributeProperties;
	
	Ext.define("CMDBuild.view.administration.classes.CMGeoAttributesGrid", {
		extend: "CMDBuild.view.administration.classes.CMAttributeGrid",
		
		initComponent: function() {
			this.callParent(arguments);

			this.on("render", 
				Ext.Function.createDelayed(function() {
					if (this.danglingStore) {
						this.reconfigure(this.danglingStore, this.columns);
					}
				}, 500),this, {single: true}
			);
		},
		
		buildColumnConf: function() {
			this.columns = [{
				header: tr_attributes.type,
				sortable: true,
				dataIndex: 'type',
				flex: 1
			},{
				header: tr_attributes.name,
				sortable: true,
				dataIndex: 'name',
				flex: 1
			},{
				header: tr_attributes.description,
				sortable: true,
				dataIndex: 'description',
				flex: 1
			}]
		},

		buildStore: function() {
			this.store = new Ext.data.SimpleStore( {
				model: "GISLayerModel"
			});
		},

		buildTBar: function() {
			this.tbar = [this.addAttributeButton]
		},

		refreshStore: function(idClass, indexAttributeToSelectAfter) {
			var store = _CMCache.getGeoAttributesStoreForClass(idClass);
			if (this.rendered) {
				this.reconfigure(store, this.columns);
			} else {
				this.danglingStore = store;
			}
		},

		selectAttribute: function(geoAttribute) {
			var sm = this.getSelectionModel();
			if (geoAttribute.name) {
				var r = this.store.findRecord("name", geoAttribute.name);
				if (r) {
					sm.select(r);
				}
			} else if (this.store.count() != 0) {
				sm.select(0);
			}
		}

	});
})()