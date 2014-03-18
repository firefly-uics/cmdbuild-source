(function() {

	Ext.define('CMDBuild.model.CMKeyValueModel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.VALUE, type: 'string' }
		]
	});

	Ext.define('CMDBuild.view.administration.common.CMKeyValueGrid', {
		extend: 'Ext.grid.Panel',

		frame: false,
		flex: 1,
		keyLabel: '',
		valueLabel: '',
		keyEditorConfig: undefined,
		cellEditing: Ext.create('Ext.grid.plugin.CellEditing', {
			clicksToEdit: 1
		}),

		initComponent: function() {
			Ext.apply(this, {
				columns: this.getCoulumnsConf(),
				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.CMKeyValueModel',
					data: []
				}),
				plugins: this.cellEditing
			});

			this.callParent(arguments);
		},

		getCoulumnsConf: function() {
			return [
				this.getKeyColumnConf(),
				this.getValueColumnConf()
			];
		},

		getKeyColumnConf: function() {
			return {
				header: this.keyLabel || CMDBuild.Translation.administration.modClass.attributeProperties.name,
				dataIndex: CMDBuild.ServiceProxy.parameter.NAME,
				editor: this.keyEditorConfig,
				flex: 1
			};
		},

		getValueColumnConf: function() {
			return {
				header: this.valueLabel || CMDBuild.Translation.administration.modClass.attributeProperties.meta.value,
				dataIndex: CMDBuild.ServiceProxy.parameter.VALUE,
				editor: {
					xtype: 'textfield'
				},
				flex: 1
			};
		},

		fillWithData: function(data) {
			this.store.removeAll();

			if (data) {
				for (var key in data) {
					var recordConf = {};
					recordConf[CMDBuild.ServiceProxy.parameter.NAME] = key;
					recordConf[CMDBuild.ServiceProxy.parameter.VALUE] = data[key] || '';

					this.store.add(recordConf);
				}
			} else {
				var recordConf = {};
				recordConf[CMDBuild.ServiceProxy.parameter.NAME] = '';
				recordConf[CMDBuild.ServiceProxy.parameter.VALUE] = '';

				this.store.add(recordConf);
			}
		},

		count: function() {
			return this.store.count();
		},

		getData: function() {
			var records = this.store.getRange(),
				data = {};

			for (var i = 0, l = records.length; i < l; ++i) {
				var recData = records[i].data;

				if (recData[CMDBuild.ServiceProxy.parameter.NAME] != '')
					data[recData[CMDBuild.ServiceProxy.parameter.NAME]] = recData[CMDBuild.ServiceProxy.parameter.VALUE];
			}

			return data;
		}
	});

})();
