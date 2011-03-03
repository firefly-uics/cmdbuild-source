Ext.ns("CMDBuild.Configure");

	CMDBuild.Configure.Step1 = Ext.extend( Ext.form.FormPanel, {

	initComponent: function() {
		var tr = CMDBuild.Translation.configure.step1;

		var languageStore = new Ext.data.JsonStore({
	        url: 'services/json/utils/listavailabletranslations',
	        root: "translations",
	        fields: ['name', 'value'],
	        autoLoad: true,
	        sortInfo: { field: 'value', direction: 'ASC' }
		});
		
		var cardNav = function(incr){
			var l = Ext.getCmp('configure_wizard_panel').getLayout();
			var i = l.activeItem.id.split('card-')[1];
			var next = parseInt(i) - 1 + incr;
			l.setActiveItem(next);
		};	
			
		var language = new Ext.form.ComboBox({
			name: 'language_value',
			hiddenName: 'language',
			plugins: new Ext.ux.plugins.IconCombo(),
			iconClsField: 'name',
			iconClsPrefix: 'ux-flag-',
			fieldLabel: tr.choose,
			width : 180,
			triggerAction: 'all',
			store: languageStore,
			valueField: 'name',
			displayField: 'value',
			mode: 'local',
			listeners: {
				'select' : {
					fn : function(combo, record) {
						window.location = String.format('?language={0}', record.data.name);
					}
    			}
			}
		});
		languageStore.on('load', function() {
				this.setValue(getCurrentLanguage());
			}, language);
		language.on('select', function(combo, record) {
				Ext.getCmp('configure-viewport').getEl().mask();
	            this.location = String.format('?language={0}', record.data.name);
			});

		var check = new Ext.ux.form.XCheckbox({
			name: 'language_prompt',
			fieldLabel: tr.showLangChoose
		});
		
		Ext.apply(this, {
            labelWidth: 300,
            layout: 'border',
            items: [{
				xtype: 'panel',
				layout: 'form',
				region:'center',
				items:[language, check],
				frame: true
			}],
			buttonAlign: 'right',
			buttons: [{
				id: 'card-next-step1',
				iconCls: 'arrow_right',
				text: CMDBuild.Translation.configure.next,
				handler: cardNav.createDelegate(this, [1])
			}]
			
		});

		CMDBuild.Configure.Step1.superclass.initComponent.apply(this, arguments);
	},
	
	onRender: function() {
		CMDBuild.Configure.Step1.superclass.onRender.apply(this, arguments);
	}
	
});
Ext.reg('configureStep1', CMDBuild.Configure.Step1);



