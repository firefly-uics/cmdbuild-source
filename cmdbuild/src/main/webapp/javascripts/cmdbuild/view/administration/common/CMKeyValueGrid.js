(function() {

	Ext.define('CMDBuild.model.CMKeyValueModel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VALUE, type: 'string' }
		]
	});

	Ext.define('CMDBuild.view.administration.common.CMKeyValueGrid', {
		extend: 'Ext.grid.Panel',

		considerAsFieldToDisable: true,
		frame: false,
		flex: 1,
		keyLabel: '',
		valueLabel: '',
		keyEditorConfig: { xtype: 'textfield' },
		valueEditorConfig: { xtype: 'textfield' },

		cellEditing: Ext.create('Ext.grid.plugin.CellEditing', {
			clicksToEdit: 1
		}),

		initComponent: function() {
			Ext.apply(this, {
				columns: this.getColumnsConf(),

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.CMKeyValueModel',
					data: []
				}),

				plugins: [this.cellEditing]
			});

			this.callParent(arguments);
		},

		getColumnsConf: function() {
			return [
				this.getKeyColumnConf(),
				this.getValueColumnConf()
			];
		},

		getKeyColumnConf: function() {
			return {
				header: this.keyLabel || CMDBuild.Translation.name,
				dataIndex: CMDBuild.core.constants.Proxy.NAME,
				editor: this.keyEditorConfig,
				flex: 1
			};
		},

		getValueColumnConf: function() {
			return {
				header: this.valueLabel || CMDBuild.Translation.value,
				dataIndex: CMDBuild.core.constants.Proxy.VALUE,
				editor: this.valueEditorConfig,
				flex: 1
			};
		},

		fillWithData: function(data) {
			this.store.removeAll();

			if (data) {
				for (var key in data) {
					var recordConf = {};

					recordConf[CMDBuild.core.constants.Proxy.NAME] = key;
					recordConf[CMDBuild.core.constants.Proxy.VALUE] = data[key] || '';

					this.store.add(recordConf);
				}
			} else {
				var recordConf = {};

				recordConf[CMDBuild.core.constants.Proxy.NAME] = '';
				recordConf[CMDBuild.core.constants.Proxy.VALUE] = '';

				this.store.add(recordConf);
			}
		},

		count: function() {
			return this.store.count();
		},

		getData: function() {
			var records = this.store.getRange();
			var data = {};

			for (var i = 0, l = records.length; i < l; ++i) {
				var recData = records[i].data;

				if (recData[CMDBuild.core.constants.Proxy.NAME] != '')
					data[recData[CMDBuild.core.constants.Proxy.NAME]] = recData[CMDBuild.core.constants.Proxy.VALUE];
			}

			return data;
		}
	});

})();