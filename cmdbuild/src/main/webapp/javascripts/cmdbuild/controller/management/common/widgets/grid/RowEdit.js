(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.grid.RowEdit', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Object}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.RowEditWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.grid.Main} configObject.parentDelegate
		 * @param {Object} configObject.record
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.management.common.widgets.grid.RowEditWindow', {
				delegate: this
			});

			this.view.form.add(this.buildFormFields());
			this.view.form.getForm().loadRecord(this.record);

			this.fieldsInitialization();

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onRowEditWindowAbortButtonClick':
					return this.onRowEditWindowAbortButtonClick();

				case 'onRowEditWindowSaveButtonClick':
					return this.onRowEditWindowSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFormFields: function() {
			var itemsArray = [];
			var attributes = this.parentDelegate.getCardAttributes();

			Ext.Array.forEach(attributes, function(attribute, index, allAttributes) {
				var item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

				if (attribute[CMDBuild.core.proxy.CMProxyConstants.FIELD_MODE] == 'read')
					item.disabled = true;

				// Setup right clientForm for templateResolver if exists
				if (!Ext.Object.isEmpty(item.templateResolver)) {
					delete item.templateResolver.getBasicForm;

					item.templateResolver.clientForm = this.parentDelegate.clientForm;
				}

				itemsArray.push(item);
			}, this);

			return itemsArray;
		},

		/**
		 * Calls field template resolver and store load
		 */
		fieldsInitialization: function() {
			var fields = this.view.form.getForm().getFields().getRange();

			Ext.Array.forEach(fields, function(field, index, allFields) {
				if (!Ext.Object.isEmpty(field) && field.resolveTemplate)
					field.resolveTemplate();

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
			var values = this.view.form.getValues();

			for (var property in values)
				this.record.set(property, values[property]);

			this.onRowEditWindowAbortButtonClick();
		}
	});

})();