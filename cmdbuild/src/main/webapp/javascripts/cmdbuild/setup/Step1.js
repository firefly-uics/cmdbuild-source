(function() {
	var tr = CMDBuild.Translation.configure.step1;
	
	Ext.define("CMDBuild.setup.Step1", {
		extend: "Ext.form.Panel",
		constructor: function() {
			this.title = CMDBuild.Translation.configure.title;

 			this.languageStore = CMDBuild.ServiceProxy.setup.getLanguageStore();

			this.languageCombo = new CMDBuild.field.CMIconCombo({
				name: 'language',
				iconClsField: 'name',
				iconClsPrefix: 'ux-flag-',
				fieldLabel: tr.choose,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: this.languageStore,
				valueField: 'name',
				displayField: 'value',
				queryMode: 'local',
				listeners: {
					'select' : {
						fn : function(combo, record) {
							window.location = Ext.String.format('?language={0}', record[0].get("name"));
						}
					}
				}
			});

			this.check = new Ext.ux.form.XCheckbox({
 				name: 'language_prompt',
 				fieldLabel: tr.showLangChoose,
 				labelWidth: CMDBuild.LABEL_WIDTH
 			});

			this.callParent(arguments);
		},

		initComponent: function() {
			this.items = [this.languageCombo, this.check];

			this.languageStore.on('load', function() {
				this.setValue(getCurrentLanguage());
			}, this.languageCombo);

			this.frame = true;
 			this.callParent(arguments);
 		}
});
	
})();