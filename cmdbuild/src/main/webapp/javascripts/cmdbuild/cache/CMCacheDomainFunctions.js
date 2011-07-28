(function() {

	var domains = {};
	var attributeStore;

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
				descr_1: d.descrdir,
				descr_2: d.descrinv,
				meta: d.meta,
				attributes: d.attributes,
				md_label: d.md_label
			});

			domainModel.isMany = function(side) {
				var c = this.get("cardinality");
				c = c.split(":");
				if (side == "_1") {
					return c[0] == "N";
				} else {
					return c[1] == "N";
				}
			}

			domains[d.idDomain] = domainModel;
			return domainModel;
		},

		getDomains: function() {
			return domains;
		},

		getDomainById: function(id) {
			return domains[id];
		},
		
		getDomainAttributesStore: function() {
			if (!attributeStore) {
			
				attributeStore = new Ext.data.Store({
					fields: [
						"index", "name", "description", "type", "isunique",
						"isbasedsp", "isnotnull","inherited", 'fieldmode',
						'isactive', "group"
					],
					autoLoad : false,
					data: [],
					sorters : [ {
						property : 'index',
						direction : "ASC"
					}],
					loadForDomainId: function(domainId) {
						this.lastDomainLoaded = domainId;
						this.removeAll();
						if (domains[domainId]) {
							var rr = domains[domainId].get("attributes") || [];
							if (rr.length > 0) {
								this.loadData(rr);
							}
						}
					},
					reloadForLastDomainId: function() {
						this.loadForDomainId(this.lastDomainLoaded);
					}
				});
			}

			return attributeStore;
		},
		
		getDirectedDomainsByEntryType: function(et) {
			if (typeof et == "object") {
				et = et.get("id");
			}

			var out = [],
				anchestorsId = _CMUtils.getAncestorsId(et);

			for (var domain in domains) {
				domain = domains[domain];
				var cid1 = domain.get("idClass1"),
					cid2 = domain.get("idClass2");

				if (Ext.Array.contains(anchestorsId, cid1)) {
					var et2 = _CMCache.getEntryTypeById(cid2);
					if (et2) {
						out.push({
							dom_id: domain.get("id"),
							description: domain.get("descr_1") + " (" + et2.get("text") + ")",
							dst_cid: domain.get("idClass2"),
							src: "_1"
						});
					}
				}

				if (Ext.Array.contains(anchestorsId, cid2)) {
					var et1 = _CMCache.getEntryTypeById(cid1)
					if (et1) {
						out.push({
							dom_id: domain.get("id"),
							description: domain.get("descr_2") + " (" + et1.get("text") + ")",
							dst_cid: domain.get("idClass1"),
							src: "_2"
						});
					}
				}
			}

			return out;
		},

		getMasterDetailsForClassId: function(id) {
			var out = []
			for (var domain in domains) {
				domain = domains[domain];
				if (domain.get("isMasterDetail")) {
					if (givenClassIdIsTheMasterForDomain(domain, id)) { 
						out.push(Ext.create("CMDBuild.cache.CMDomainModel", domain.data));
					}
				}
			}
			
			return out;
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
			var domainAttributes = domains[domainId].get("attributes") || [];
			eraseAttribute(domainAttributes, attribute); // to manage the modify of an existing attribute

			domainAttributes.push(attribute);

			if (attributeStore) {
				attributeStore.reloadForLastDomainId();
			}
			
		},
		
		onDomainAttributeDelete: function(domainId, attribute) {
			var domainAttributes = domains[domainId].get("attributes") || [];
			eraseAttribute(domainAttributes, attribute);

			if (attributeStore) {
				attributeStore.reloadForLastDomainId();
			}
		}
	});

	function givenClassIdIsTheMasterForDomain(domain, id) {
		var classtree = _CMMainViewportController.findAccordionByCMName("class"),
			cardinality = domain.get("cardinality"),
			idClass1 = domain.get("idClass1"),
			idClass2 = domain.get("idClass2"),
			ancestors = _CMUtils.getAncestorsId(id);

		if (cardinality == "1:N") {
			return Ext.Array.contains(ancestors, idClass1);
		} else if (cardinality == "N:1") {
			return Ext.Array.contains(ancestors, idClass2);
		}
	}

	function eraseAttribute(domainAttributes, attribute) {
		for (var i=0, l=domainAttributes.length; i<l; ++i) {
			if (domainAttributes[i].data.name == attribute.name) {
				Ext.Array.erase(domainAttributes, i, 1);
				break;
			}
		}
	}
	
})();