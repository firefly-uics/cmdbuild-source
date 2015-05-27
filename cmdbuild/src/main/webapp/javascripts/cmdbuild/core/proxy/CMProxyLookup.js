(function() {

	var LOOKUP_FIELDS = {
		Id: 'Id',
		Code: 'Code',
		Description: 'Description',
		ParentId: 'ParentId',
		Index: 'Number',
		Type: 'Type',
		ParentDescription: 'ParentDescription',
		Active: 'Active',
		Notes: 'Notes',
		TranslationUuid: 'TranslationUuid'
	};

	CMDBuild.ServiceProxy.LOOKUP_FIELDS = LOOKUP_FIELDS;

	CMDBuild.ServiceProxy.lookup = {
		/**
		 * @property {Object} parameters
		 */
		get: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: 'services/json/schema/modlookup/getlookuplist',
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		readAllTypes: function(p) {
			p.method = 'GET';
			p.url = 'services/json/schema/modlookup/tree';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		getLookupFieldStore: function(type) {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMLookupFieldStoreModel',
				proxy: {
					type: 'ajax',
					url: 'services/json/schema/modlookup/getlookuplist',
					reader: {
						type: 'json',
						root: 'rows'
					},
					extraParams: {
						type: type,
						active: true,
						'short': true
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names  not handled correctly
				},
				sorters: [
					{ property: LOOKUP_FIELDS.Index, direction: 'ASC' },
					{ property: 'Description', direction: 'ASC' }
				],

				// Disable paging
				defaultPageSize: 0,
				pageSize: 0
			});
		},

		getLookupGridStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMLookupForGrid',
				proxy: {
					type: 'ajax',
					url: 'services/json/schema/modlookup/getlookuplist',
					reader: {
						type: 'json',
						root: 'rows'
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names not handled correctly
				},
				sorters: [
					{ property: 'Number', direction: 'ASC' },
					{ property: 'Description', direction: 'ASC' }
				]
			});
		},

		setLookupDisabled: function(p, disable) {
			var url = 'services/json/schema/modlookup/enablelookup';

			if (disable)
				url = 'services/json/schema/modlookup/disablelookup';

			p.method = 'POST';
			p.url = url;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		saveLookup: function(p) {
			p.method = 'POST';
			p.url = 'services/json/schema/modlookup/savelookup';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		saveLookupType: function(p) {
			p.method = 'POST';
			p.url = 'services/json/schema/modlookup/savelookuptype';

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();