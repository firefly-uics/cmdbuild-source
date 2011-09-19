(function() {

	Ext.define("CMDBuild.DummyModel", {
		extend: "Ext.data.Model",
		fields:[]
	});

	Ext.define("CMDBuild.cache.CMLookupTypeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "text",type: 'string'},
			{name: "parent",type: 'string'},
			{name: "type",type: 'string'}
		]
	});

	Ext.define("CMDBuild.cache.CMEntryTypeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "text",type: 'string'},
			{name: "superclass",type: 'boolean'},
			{name: "active",type: 'boolean'},
			{name: "parent",type: 'string'},
			{name: "tableType",type: 'string'},
			{name: "type",type: 'string'},
			{name: "name",type: 'string'},
			{name: "priv_create",type: 'boolean'},
			{name: "priv_write",type: 'boolean'},
		],

		deleteGeoAttr: function(a) {
			var attrs = this.getGeoAttrs(),
				next;

			for (var i=0, l=attrs.length; i<l; ++i) {
				next = attrs[i];
				if (next && next.name == a.name && next.masterTableName == a.masterTableName) {
					delete attrs[i];
					return true;
				}
			}

			return false;
		},

		createOrUpdateGeoAttr: function(a) {
			if (!this.updateGeoAttr(a)) {
				this.data.meta.geoAttributes.push(a);
			}
		},

		updateGeoAttr: function(a) {
			var attrs = this.getGeoAttrs(),
				next;

			for (var i=0, l=attrs.length; i<l; ++i) {
				next = attrs[i];
				if (next && next.name == a.name && next.masterTableName == a.masterTableName) {
					this.data.meta.geoAttributes[i] = a;
					return true;
				}
			}

			return false;
		},

		getGeoAttrs: function() {
			var a = [];
			try {
				a = this.data.meta.geoAttributes;
			} catch (e) {
				CMDBuild.log.Error("Something went wrong: CMDBuild.cache.CMEntryTypeModel " + this.data.text + " getGeoAttrs");
			}
			return a;
		},

		getMyGeoAttrs: function() {
			var attrs = this.getGeoAttrs(),
				anchestors = _CMUtils.getAncestorsId(this.data.id),
				out = [];

			for (var a in attrs) {
				a = attrs[a];
				if (Ext.Array.contains(anchestors, a.masterTableId+"")) { // TODO fix the need to cast as string, the model save it as string
					out.push(a);
				}
			}

			return out;
		},

		getVisibleGeoAttrs: function() {
			var attrs = this.getGeoAttrs(),
			out = [];

			for (var a in attrs) {
				a = attrs[a];
				if (a.isvisible) {
					out.push(a);
				}
			}

			return out;
		}
	});
	
	Ext.define("CMDBuild.cache.CMDomainModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "active", type: "boolean"},
			{name: "cardinality", type: "string"},
			{name: "nameClass1", type: "string"},
			{name: "nameClass2", type: "string"},
			{name: "idClass1", type: "string"},
			{name: "idClass2", type: "string"},
			{name: "classType", type: "string"},
			{name: "name", type: "string"},
			{name: "createPrivileges", type: "boolean"},
			{name: "writePrivileges", type: "boolean"},
			{name: "isMasterDetail", type: "boolean"},
			{name: "description", type: "stirng"},
			{name: "descr_1", type: "stirng"},
			{name: "descr_2", type: "stirng"},
			{name: "md_label", type: "string"}
		],

		getAttributes: function() {
			var a;
			if (this.raw) {
				a = this.raw.attributes;
			}

			return a || this.data.attributes || [];
		},

		getDetailSide: function() {
			var c = this.get("cardinality");
			if (c == "1:N") {
				return "_2";
			} else if (c == "N:1") {
				return "_1";
			} else {
				return undefined;
			}
		},

		hasCreatePrivileges: function() {
			if (this.raw) {
				return this.raw.createPrivileges;
			} else {
				return this.data.createPrivileges;
			}
		}
	});

	Ext.define("CMDBuild.cache.CMReportModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "active", type: "boolean"},
			{name: "text", type: "string"},
			{name: "type", type: "string"},
			{name: "group", type: "string"}
		]
	});
	
	Ext.define("CMDBuild.cache.CMReporModelForGrid", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "type", type: "string"},
			{name: "groups", type: "string"},
			{name: "query", type: "string"},
			{name: "description", type: "string"},
			{name: "title", type: "string"}
		]
	});

   	Ext.define("CMDBuild.cache.CMReferenceStoreModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "Id", type: 'int'},
			{name: "Description",type: 'string'}
		]
	});

})();