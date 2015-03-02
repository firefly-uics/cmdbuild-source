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
		 * @property {Object} CMGridPanel record
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

			this.form.getForm().loadRecord(this.record);

			// Resize window, smaller than default size
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;

			this.fieldsInitialization();
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFormFields: function() {
			var itemsArray = [];
			var attributes = this.delegate.getCardAttributes();

			Ext.Array.forEach(attributes, function(attribute, index, allAttributes) {
				var item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

				if (attribute[CMDBuild.core.proxy.CMProxyConstants.FIELD_MODE] == 'read')
					item.disabled = true;

				// Setup right clientForm for templateResolver if exists
				if (!Ext.Object.isEmpty(item.templateResolver)) {
					delete item.templateResolver.getBasicForm;

					item.templateResolver.clientForm = this.delegate.clientForm;
				}

				itemsArray.push(item);
			}, this);

			return itemsArray;
		},

		/**
		 * Calls field template resolver and store load
		 */
		fieldsInitialization: function() {
			var fields = this.form.getForm().getFields().getRange();

			Ext.Array.forEach(fields, function(field, index, allFields) {
				if (!Ext.Object.isEmpty(field) && field.resolveTemplate)
					field.resolveTemplate();

				if (!Ext.Object.isEmpty(field) && !Ext.Object.isEmpty(field.store) && field.store.count() == 0)
					field.store.load();
			}, this);
		},
	});

})();