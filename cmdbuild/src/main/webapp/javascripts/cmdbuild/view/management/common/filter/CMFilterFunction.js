(function() {
	 Ext.define('Functions', {
	     extend: 'Ext.data.Model',
	     fields: [
	         {name: 'name', type: 'string'}
	     ]
	 });

	 var functionsStore = Ext.create('Ext.data.Store', {
	     model: 'Functions',
	     proxy: {
	         type: 'ajax',
	         url: CMDBuild.ServiceProxy.url.functions.getFunctions,
	         reader: {
	             type: 'json',
	             root: 'functions'
	         }
	     },
	     autoLoad: true
	 });	
	 Ext.define("CMDBuild.view.management.common.filter.CMFunctions", {
		extend: "Ext.panel.Panel",
		title: "@@ Functions", //CMDBuild.Translation.management.findfilter.relations,
		bodyCls: "x-panel-body-default-framed cmbordertop",
		bodyStyle: {
			padding: "5px 5px 0 5px"
		},
		cls: "x-panel-body-default-framed",
		labelWidth: CMDBuild.LABEL_WIDTH,
		width: CMDBuild.ADM_BIG_FIELD_WIDTH,

		// configuration
		className: undefined,
		// configuration

		initComponent: function() {
			this.functionsCombo = Ext.create('Ext.form.ComboBox', {
			    fieldLabel: '@@ Function',
			    store: functionsStore,
				name: "function",
			    displayField: 'name',
			    valueField: 'name'
			});
			this.items = [this.functionsCombo];	
			this.callParent(arguments);

		},
		setData: function(data) {
			/*@@ TODO */
		},
		getData: function() {
			return this.functionsCombo.getValue();
		}
	});
})();