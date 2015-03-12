(function() {

	Ext.define('CMDBuild.controller.common.field.translatable.Window', {

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeyField: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeyName: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeySubName: undefined,

		/**
		 * @cfg {String}
		 */
		translationsKeyType: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.Window}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {Mixed} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.common.field.translatable.window.Window', {
				delegate: this
			});

			this.form = this.view.form;

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
				case 'onTranslatableWindowConfirmButtonClick':
					return this.onTranslatableWindowConfirmButtonClick();

				case 'onTranslatableWindowAbortButtonClick':
					return this.onTranslatableWindowAbortButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {Object} translations
		 */
		buildWindowItem: function(translations) {
			var activeTranslations = _CMCache.getActiveTranslations();

			Ext.Array.forEach(activeTranslations, function(activeTranslation, index, allTranslations) {
				var item = Ext.create('Ext.form.field.Text', {
					name: activeTranslation[CMDBuild.core.proxy.CMProxyConstants.NAME],
					fieldLabel: activeTranslation[CMDBuild.core.proxy.CMProxyConstants.LANGUAGE],
					labelWidth: CMDBuild.LABEL_WIDTH,
					flex: 1,
					padding: '3 5',
					labelClsExtra: 'ux-flag-' + activeTranslation[CMDBuild.core.proxy.CMProxyConstants.NAME],
					labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
				});

				item.setValue(translations[activeTranslation[CMDBuild.core.proxy.CMProxyConstants.NAME]]);

				this.form.add(item);
			},this);
		},

		/**
		 * @return {CMDBuild.view.common.field.translatable.window.Window}
		 */
		getView: function() {
			return this.view;
		},

		onTranslatableWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onTranslatableWindowConfirmButtonClick: function() {
			_CMCache.createTranslations(
				this.translationsKeyType,
				this.translationsKeyName,
				this.translationsKeySubName,
				this.translationsKeyField,
				this.form.getValues(),
				this.form.getOldValues() // Control for create, delete or update values
			);

			this.onTranslatableWindowAbortButtonClick();
		}
	});

})();