(function () {

	Ext.define('CMDBuild.view.administration.classes.ClassesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.classes.Classes}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.administration.modClass.title,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: true,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.administration.modClass.title,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				// FIXME: legacy waiting for refactor (attribute module)
				attributesPanel: Ext.create('CMDBuild.view.administration.classes.CMAttributes', {
					title: CMDBuild.Translation.attributes,
					border: false,
					disabled: true
				}),
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.administration.modClass.add_class,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onClassesAddButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.split.Print', {
								delegate: this.delegate,
								text: CMDBuild.Translation.printSchema,
								formatList: [
									CMDBuild.core.constants.Proxy.PDF,
									CMDBuild.core.constants.Proxy.ODT
								]
							})
						]
					})
				],
				items: [
					this.tabPanel = Ext.create('Ext.tab.Panel', {
						frame: false,
						border: false,

						items: []
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
