(function() {
	var _FIELDS = {
		name: "name",
		description: "description",
		value: "value"
	};

	Ext.define('CMDBuild.model.CMKeyValueModel', {
		statics: {
			_FIELDS: _FIELDS
		},
		extend: 'Ext.data.Model',
		fields: [
			{name: _FIELDS.name, type: "string"},
			{name: _FIELDS.value, type: "string"}
		]
	});

	Ext.define("CMDBuild.view.administration.common.CMKeyValueGrid", {
		extend: "Ext.grid.Panel",
		frame: false,
		flex: 1,

		keyLabel: "",
		valueLabel: "",

		initComponent: function() {
			this.cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit : 1
			});

			Ext.apply(this, {
				columns: this.getCoulumnsConf(),
				store: new Ext.data.Store({
					model: "CMDBuild.model.CMKeyValueModel",
					data: []
				}),
				plugins: [this.cellEditing]
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
			var me = this;
			return {
				header: me.keyLabel || CMDBuild.Translation.administration.modClass.attributeProperties.name,
				dataIndex : CMDBuild.model.CMKeyValueModel._FIELDS.name,
				flex: 1
			};
		},

		getValueColumnConf: function() {
			var me = this;
			return {
				header: me.valueLabel || CMDBuild.Translation.administration.modClass.attributeProperties.meta.value,
				dataIndex: CMDBuild.model.CMKeyValueModel._FIELDS.value,
				editor: {
					xtype: "textfield"
				},
				flex: 1
			};
		},

		fillWithData: function(data) {
			this.store.removeAll();
			if (data) {
				var fields = CMDBuild.model.CMKeyValueModel._FIELDS;

				for (var key in data) {
					var recordConf = {};
					recordConf[fields.name] = key;
					recordConf[fields.value] = data[key] || "";

					this.store.add(new CMDBuild.model.CMKeyValueModel(recordConf));
				}
			}
		},

		count: function() {
			return this.store.count();
		},

		getData: function() {
			var records = this.store.getRange(),
				fields = CMDBuild.model.CMKeyValueModel._FIELDS,
				data = {};

			for (var i=0, l=records.length; i<l; ++i) {
				var recData = records[i].data;
				data[recData[fields.name]] = recData[fields.value];
			}

			return data;
		}
	});
})();
