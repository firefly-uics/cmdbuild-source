(function() {

	var domains = {};
	var attributeStores = {};

	Ext.define("CMDBUild.cache.CMCacheDomainFunctions", {

		addDomains: function(dd) {
			for (var i=0, l=dd.length; i<l; ++i) {
				this.addDomain(dd[i]);
			}
		},

		addDomain: function(d) {
			var domainModel = Ext.create("CMDBuild.cache.CMDomainModel", {
				active: d.active,
				id: d.idDomain,
				cardinality: d.cardinality,
				nameClass1: d.class1,
				nameClass2: d.class2,
				idClass1: d.class1id,
				idClass2: d.class2id,
				classType: d.classType,
				name: d.name,
				createPrivileges: d.priv_create,
				writePrivileges: d.priv_write,
				isMasterDetail: d.md,
				description: d.description,
				directDescription: d.descrdir,
				reverseDescription: d.descrinv,
				meta: d.meta,
				attributes: d.attributes
			});

			domains[d.idDomain] = domainModel;
			return domainModel;
		},

		getDomains: function() {
			return domains;
		},

		getDomainById: function(id) {
			return domains[id];
		},
		
		getDomainAttributesStoreForDomainId: function(domainId) {
			if (!attributeStores[domainId]) {
				attributeStores[domainId] = buildAttributeStoreForDomainId(domainId);
			}
			
			return attributeStores[domainId];
		},
		
		onDomainSaved: function(domain) {
			var d = this.addDomain(domain);
			this.fireEvent("cm_domain_saved", d);

			return d;
		},

		onDomainDeleted: function(domainId) {
			domains[domainId] = undefined;
			delete domains[domainId];

			this.fireEvent("cm_domain_deleted", domainId);
		},

		onDomainAttributeSaved: function(domainId, attribute) {
			var s = this.getDomainAttributesStoreForDomainId(domainId);
			if (s) {
				var r = s.findRecord("name", attribute.name);
				if (r) {
					s.remove(r)
				}
				
				s.add(attribute);
				s.sort('index', 'ASC');
			}
		},
		
		onDomainAttributeDelete: function(domainId, attribute) {
			var s = this.getDomainAttributesStoreForDomainId(domainId);
			if (s) {
				var r = s.findRecord("name", attribute.name);
				if (r) {
					s.remove(r)
				}
			}
		}
	});
	
	function buildAttributeStoreForDomainId(domainId) {
		if (! domains[domainId] ) {
			_debug("I can not build an attribute store for domain with id " + domainId);
			return;
		}

		var s = new Ext.data.Store({
			fields: [
				"index", "name", "description", "type", "isunique",
				"isbasedsp", "isnotnull","inherited", 'fieldmode',
				'isactive', "group"
			],
			autoLoad : false,
			data: domains[domainId].get("attributes") || [],
			sorters : [ {
				property : 'index',
				direction : "ASC"
			}]
		});

		return s;
	}
	
})();