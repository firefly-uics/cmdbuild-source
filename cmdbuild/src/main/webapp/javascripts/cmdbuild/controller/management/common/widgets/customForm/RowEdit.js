(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.RowEdit', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.layout.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onRowEditWindowAbortButtonClick',
			'onRowEditWindowSaveButtonClick'
		],

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @cfg {Ext.data.Model}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.RowEditWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.widgets.customForm.layout.Grid} configurationObject.parentDelegate
		 * @param {Object} configurationObject.record
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.RowEditWindow', {
				delegate: this
			});

			// Shorthand
			this.form = this.view.form;

			this.form.add(this.buildFields());
			this.form.loadRecord(this.record);

			this.fieldsInitialization();

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFields: function() {
			var itemsArray = [];

			if (!this.cmfg('widgetConfigurationIsAttributeEmpty',  CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.proxy.CMProxyConstants.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));

						itemsArray.push(fieldManager.buildField());
					} else { // @deprecated - Old field manager
						var attribute = attribute.getAdaptedData();
						var item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

						if (attribute[CMDBuild.core.proxy.CMProxyConstants.FIELD_MODE] == 'read')
							item.setDisabled(true);

						itemsArray.push(item);
					}
				}, this);
			}

			return itemsArray;
		},

		/**
		 * Calls field template resolver and store load
		 */
		fieldsInitialization: function() {
			Ext.Array.forEach(this.form.getForm().getFields().getRange(), function(field, i, allFields) {
				if (!Ext.Object.isEmpty(field) && !Ext.isEmpty(field.resolveTemplate))
					field.resolveTemplate();

				// Force editor fields store load (must be done because FieldManager field don't works properly)
				if (!Ext.Object.isEmpty(field) && !Ext.Object.isEmpty(field.store) && field.store.count() == 0)
					field.store.load();
			}, this);
		},

		onRowEditWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Saves data to widget's grid
		 */
		onRowEditWindowSaveButtonClick: function() {
			Ext.Object.each(this.form.getValues(), function(key, value, myself) {
				this.record.set(key, value);
			}, this);

			this.onRowEditWindowAbortButtonClick();
		}
	});

})();