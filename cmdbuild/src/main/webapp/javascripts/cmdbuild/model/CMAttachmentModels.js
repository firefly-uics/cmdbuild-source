(function() {

	/*
	 * CMDBuild.model.CMAttachmentCategoryModel describes
	 * an attachment category.
	 * Each category contains a name, a description and an
	 * array of CMDBuild.model.CMMetadataGroup.
	 * 
	 * CMDBuild.model.CMMetadataGroup contains a name
	 * and an array of metadata definitions. These definitions
	 * describes how to render the widget to edit the referred
	 * metadata (an attribute of the attachment)
	 * 
	 * So, we have:
	 * {
	 * 		name: ...,
	 * 		description: ...,
	 * 		metadataGroups: [{
	 * 			name: ...,
	 * 			metadataDefinitions: [...]
	 * 		},
	 * 		...
	 * 		]
	 * }
	 */

	var NAME = "name",
		DESCRIPTION = "description",
		METADATA_GROUPS = "metadataGroups",
		METADATA_DEFINITIONS = "metadata";

	Ext.define("CMDBuild.model.CMAttachmentCategoryModel", {
		extend: "Ext.data.Model",

		fields: [
			{name: NAME, type: 'string'},
			{name: DESCRIPTION, type: 'string'},
			{name: METADATA_GROUPS, type: 'auto'}
		],

		statics: {
			buildFromJson: function(data) {
				var groups = data[METADATA_GROUPS];
				data[METADATA_GROUPS] = [];
				var category = new CMDBuild.model.CMAttachmentCategoryModel(data);

				for (var i=0, g=null; i<groups.length; ++i) {
					g = groups[i];
					category.addMetadataGroup(new CMDBuild.model.CMMetadataGroup(g));
				}

				return category;
			}
		},

		constructor: function(data) {
			var d = data || {};

			if (typeof d[METADATA_GROUPS] == "undefined"
					|| !Ext.isArray(d[METADATA_GROUPS])) {

				d[METADATA_GROUPS] = [];
			}

			this.callParent([d]);
		},

		getName: function() {
			return this.get(NAME);
		},

		setName: function(name) {
			this.set(NAME, name);
		},

		getDescription: function() {
			return this.get(DESCRIPTION);
		},

		setDescription: function(description) {
			this.set(DESCRIPTION, description);
		},

		getMetadataGroups: function() {
			return this.get(METADATA_GROUPS);
		},

		addMetadataGroup: function(g) {
			var groups = this.getMetadataGroups();
			groups.push(g);
		}
	});

	Ext.define("CMDBuild.model.CMMetadataGroup", {
		extend: "Ext.data.Model",

		fields: [
			{name: NAME, type: 'string'},
			{name: METADATA_DEFINITIONS, type: 'auto'}
		],

		constructor: function(data) {
			var d = data || {};
			if (typeof d[METADATA_DEFINITIONS] == "undefined"
				|| !Ext.isArray(d[METADATA_DEFINITIONS])) {

				d[METADATA_DEFINITIONS] = [];
			}

			this.callParent([d]);
		},

		getName: function() {
			return this.get(NAME);
		},

		setName: function(name) {
			this.set(NAME, name);
		},

		getMetadataDefinitions: function() {
			return this.get(METADATA_DEFINITIONS);
		}
	});
})();