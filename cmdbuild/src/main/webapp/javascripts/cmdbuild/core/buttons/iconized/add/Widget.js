(function() {

	Ext.define('CMDBuild.core.buttons.iconized.add.Widget', {
		extend: 'Ext.button.Split',

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		delegate: undefined,

		iconCls: 'add',
		text: CMDBuild.Translation.addWidget,

		initComponent: function() {
			Ext.apply(this, {
				scope: this,

				menu: Ext.create('Ext.menu.Menu', {
					items: [
						{
							text: CMDBuild.Translation.createReport,
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onClassTabWidgetAddButtonClick', '.OpenReport');
							}
						},
						{
							text: CMDBuild.Translation.calendar,
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onClassTabWidgetAddButtonClick', '.Calendar');
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
							text: CMDBuild.Translation.ping,
							scope: this,

							handler: function(button, e) {
								this.delegate.cmfg('onClassTabWidgetAddButtonClick', '.Ping');
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