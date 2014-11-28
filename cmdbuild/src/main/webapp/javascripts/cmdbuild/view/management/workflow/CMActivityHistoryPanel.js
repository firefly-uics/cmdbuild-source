(function() {

	var tr = CMDBuild.Translation.management.modcard.history_columns;

	Ext.define('CMDBuild.view.management.workflow.CMActivityHistoryTab', {
		extend: 'CMDBuild.view.management.classes.CMCardHistoryTab',

		/**
		 * @param {Object} record
		 *
		 * @return {String} body - HTML format string
		 *
		 * @override
		 */
		genHistoryBody: function(record) {
			var body = '';
			var processAttributes = _CMCache.mapOfAttributes[_CMWFState.getProcessInstance().get('IdClass')];
			var attributesDescriptionArray = [];

			// Build attributesDescriptionArray to test if display attribute
			for (var i in processAttributes)
				attributesDescriptionArray.push(processAttributes[i][CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION]);

			if (record.raw['_RelHist']) {
				body += this.historyAttribute(tr.domain, record.raw['DomainDesc'])
					+ this.historyAttribute(tr.destclass, record.raw['Class'])
					+ this.historyAttribute(tr.code, record.raw['CardCode'])
					+ this.historyAttribute(tr.description, record.raw['CardDescription']);
			}

			for (var i = 0; i < record.raw['Attr'].length; i++) {
				var attribute = record.raw['Attr'][i];

				if (Ext.Array.contains(attributesDescriptionArray, attribute.d)) {
					var label = attribute.d;
					var changed = attribute.c;
					var value = Ext.isEmpty(attribute.v) ? '' : attribute.v;

					body += this.historyAttribute(label, value, changed);
				}
			}

			return body;
		},

		/**
		 * @return {Array} columns
		 *
		 * @override
		 */
		getGridColumns: function() {
			return this.callParent(arguments).concat([
				{
					header: tr.activity_name,
					width: 40,
					sortable: false,
					dataIndex: 'Code',
					flex: 1
				},
				{
					header: tr.performer,
					sortable: false,
					dataIndex: 'Executor',
					flex: 1
				}
			]);
		},

		/**
		 * @return {Array}
		 *
		 * @override
		 */
		getStoreFields: function() {
			return this.callParent(arguments).concat([
				'Code',
				'Executor'
			]);
		},

		/**
		 * @override
		 */
		isFullVersion: function() {
			return !_CMUIConfiguration.isSimpleHistoryModeForProcess();
		}
	});

})();