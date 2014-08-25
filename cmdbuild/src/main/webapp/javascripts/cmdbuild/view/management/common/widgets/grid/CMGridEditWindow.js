(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.CMGridEditWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		defaultSizeW: 0.90,
		defaultSizeH: 0.80,

		// Configurations
			delegate: undefined,
			cardAttributes: undefined,
			record: undefined,
		// END: Configurations

		buttonAlign: 'center',
		border: false,

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
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFormFields: function() {
			var itemsArray = [];
			var attributes = this.delegate.getCardAttributes();

			for (var i = 0; i < attributes.length; i++) {
				var attribute = attributes[i];
				var attributesMap = CMDBuild.Management.FieldManager.getAttributesMap();
				var item = attributesMap[attribute.type].buildField(attribute, false, false);
				var value = this.record.get(attribute.name);

				if (attribute.fieldmode == 'read')
					item.disabled = true;

				itemsArray.push(item);
				item.setValue(value);
			}

			return itemsArray;
		}
	});

})();