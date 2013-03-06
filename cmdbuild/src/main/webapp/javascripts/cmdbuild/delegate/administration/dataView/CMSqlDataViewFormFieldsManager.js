Ext.define("CMDBuild.delegate.administration.common.dataview.CMSqlDataViewFormFieldsManager", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager",

	/**
	 * @return {array} an array of Ext.component to use as form items
	 */
	build: function() {

		this.dataSource = new Ext.form.field.ComboBox({
			name: _CMProxy.parameter.DATASOURCE,
			fieldLabel: CMDBuild.Translation.administration.modDashboard.charts.fields.dataSource,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			valueField: "name",
			displayField: "name",
			queryMode: "local",
			editable: false,
			allowBlank: false,
			store: _CMCache.getAvailableDataSourcesStore()
		});

		var fields = this.callParent(arguments);
		fields.push(this.dataSource);

		return fields;
	},

	/**
	 * 
	 * @param {Ext.data.Model} record
	 * the record to use to fill the field values
	 */
	loadRecord: function(record) {
		this.reset();
		this.name.setValue(record.get(_CMProxy.parameter.NAME));
		this.description.setValue(record.get(_CMProxy.parameter.DESCRIPTION));
		this.dataSource.setValue(record.get(_CMProxy.parameter.DATASOURCE));
	},

	/**
	 * clear the values of his fields
	 */
	reset: function() {
		this.name.reset();
		this.description.reset();
		this.dataSource.reset();
	}
});