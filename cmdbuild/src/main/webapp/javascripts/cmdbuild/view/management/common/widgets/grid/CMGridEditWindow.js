(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.CMGridEditWindow', {
		extend: 'CMDBuild.PopupWindow',

		defaultSizeW: 0.90,
		defaultSizeH: 0.80,

		// Configurations
			delegate: undefined,
			cardAttributes: undefined,
			record: undefined,
		// EDN: Configurations

		buttonAlign: 'center',

		initComponent: function() {
			var me = this;

			this.closeButton = Ext.create('Ext.button.Button', {
				scope: this,
				text: CMDBuild.Translation.common.buttons.close,
				handler: function() {
					this.delegate.cmOn('onEditWindowClosed');
				}
			});

			this.form = Ext.create('Ext.form.Panel', {
				cls: 'x-panel-body-default-framed',
				autoScroll: true,
				bodyCls: 'x-panel-body-default-framed',
				bodyStyle: {
					padding: '5px 5px 0px 5px'
				},

				items: me.buildFormFields()
			});

			Ext.apply(this, {
				items: [this.form],
				buttons: [this.closeButton]
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