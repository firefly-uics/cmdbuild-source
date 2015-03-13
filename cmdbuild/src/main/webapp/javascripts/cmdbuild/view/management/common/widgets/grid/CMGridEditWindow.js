(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.CMGridEditWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.CMGridController}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.buttons.AbortButton}
		 */
		abortButton: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @property {Ext.data.Store.ImplicitModel} CMGridPanel record
		 */
		record: undefined,

		/**
		 * @cfg {CMDBuild.buttons.SaveButton}
		 */
		saveButton: undefined,

		buttonAlign: 'center',
		border: false,
		defaultSizeW: 0.90,
		defaultSizeH: 0.80,

		initComponent: function() {
			var me = this;

			// Buttons configuration
				this.saveButton = Ext.create('CMDBuild.buttons.SaveButton', {
					scope: this,

					handler: function() {
						this.delegate.cmOn('onEditWindowSaveButtonClick');
					}
				});

				this.abortButton = Ext.create('CMDBuild.buttons.AbortButton', {
					scope: this,

					handler: function() {
						this.delegate.cmOn('onEditWindowAbortButtonClick');
					}
				});
			// END: Buttons configuration

			this.form = Ext.create('Ext.form.Panel', {
				autoScroll: true,
				frame: true,
				border: false,

				items: me.buildFormFields()
			});

			Ext.apply(this, {
				items: [this.form],
				buttons: [this.saveButton, this.abortButton]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;

			this.callFieldTemplateResolver();
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFormFields: function() {
			var itemsArray = [];
			var attributes = this.delegate.getCardAttributes();

			for (var i = 0; i < attributes.length; i++) {
				var attribute = attributes[i];
				var item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);
				var value = this.record.get(attribute[CMDBuild.core.proxy.CMProxyConstants.NAME]);

				if (attribute[CMDBuild.core.proxy.CMProxyConstants.FIELD_MODE] == 'read')
					item.disabled = true;

				// Setup right clientForm for templateResolver if exists
				if (!Ext.Object.isEmpty(item.templateResolver)) {
					delete item.templateResolver.getBasicForm;
					item.templateResolver.clientForm = this.delegate.clientForm;
				}

				itemsArray.push(item);
				item.setValue(value);
			}

			return itemsArray;
		},

		callFieldTemplateResolver: function() {
			var fields = this.form.getForm().getFields().items;

			for (var i in fields) {
				var field = fields[i];

				if (field && field.resolveTemplate)
					field.resolveTemplate();
			}
		},
	});

})();