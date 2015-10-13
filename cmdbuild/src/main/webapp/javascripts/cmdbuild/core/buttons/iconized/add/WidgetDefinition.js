(function() {

	Ext.define('CMDBuild.core.buttons.iconized.add.WidgetDefinition', {
		extend: 'Ext.button.Split',

		iconCls: 'add',

		text: CMDBuild.Translation.common.buttons.add,

		initComponent: function() {
			Ext.apply(this, {
				scope: this,

				menu: Ext.create('Ext.menu.Menu', {
					items: [
						{
							text: CMDBuild.Translation.administration.modClass.widgets['.OpenReport'].title,
							WIDGET_NAME: '.OpenReport',
							scope: this,

							handler: function(button, e) {
								this.fireEvent('cm-add', '.OpenReport');
							}
						},
						{
							text: CMDBuild.Translation.administration.modClass.widgets['.Calendar'].title,
							WIDGET_NAME: '.Calendar',
							scope: this,

							handler: function(button, e) {
								this.fireEvent('cm-add', '.Calendar');
							}
						},
						{
							text: CMDBuild.Translation.administration.modClass.widgets['.Workflow'].title,
							WIDGET_NAME: '.Workflow',
							scope: this,

							handler: function(button, e) {
								this.fireEvent('cm-add', '.Workflow');
							}
						},
						{
							text: CMDBuild.Translation.administration.modClass.widgets['.Ping'].title,
							WIDGET_NAME: '.Ping',
							scope: this,

							handler: function(button, e) {
								this.fireEvent('cm-add', '.Ping');
							}
						},
						{
							text: CMDBuild.Translation.administration.modClass.widgets['.CreateModifyCard'].title,
							WIDGET_NAME: '.CreateModifyCard',
							scope: this,

							handler: function(button, e) {
								this.fireEvent('cm-add', '.CreateModifyCard');
							}
						}
					]
				}),

				handler: function(button, e) {
					this.showMenu();
				},
			});

			this.callParent(arguments);
		}
	});

})();