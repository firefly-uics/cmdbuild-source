(function() {

	// FAKE DATAS
	var workflows = Ext.create('Ext.data.Store', {
			fields: ['abbr', 'name'],
			data : [
					{"abbr":"1", "name":"Uno"},
					{"abbr":"2", "name":"Due"},
					{"abbr":"3", "name":"Tre"}
			]
	});
	// END FAKE DATAS

	Ext.define("CMDBuild.view.administration.tasks.email.CMStep3Delegate", {

		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onWorkflowChecked':
					return showComponent(this.view, 'workflowName', param.checked);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	Ext.define("CMDBuild.view.administration.tasks.email.CMStep3", {
		extend: "Ext.panel.Panel",

		title:'Phone Numbers',
		defaultType: 'textfield',
		border: false,
		bodyCls: 'cmgraypanel',
		height: "100%",

		defaults: {
			anchor: '100%'
		},

		initComponent: function() {
			var me = this;

			this.items = [
				{
					fieldLabel: '@@ Start workflow',
					name: 'workflow',
					xtype: 'checkbox',
					listeners: {
						change: function(that, newValue, oldValue, eOpts) {
							me.delegate.cmOn("onWorkflowChecked", {'checked': newValue});
						}
					}
				},
				{
					fieldLabel: '@@ Workflow name',
					name: 'workflowName',
					xtype: 'combo',
					store: workflows,
					queryMode: 'local',
					displayField: 'name',
					valueField: 'abbr',
					itemId: 'workflowName',
					hidden: true
				}
			];

			this.delegate = new CMDBuild.view.administration.tasks.email.CMStep3Delegate(this);

			this.callParent(arguments);
		}
	});

	function showComponent(view, fieldName, showing) {
		var component = view.query("#" + fieldName);
		if (showing) {
			component[0].show();
		}
		else {
			component[0].hide();
		}
	}
})();