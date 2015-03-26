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
			var me = this;

			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.common.field.translatable.window.Window', {
				delegate: this
			});

			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();

			_CMCache.readTranslations(
				this.translationsKeyType,
				this.translationsKeyName,
				this.translationsKeySubName,
				this.translationsKeyField,
				function(result, options, decodedResult) {
					me.form.oldValues = decodedResult.response;

					me.buildWindowItem(decodedResult.response);
				}
			);
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
		buildWindowItem: function(translationsValues) {
			var languagesWithLocalizations = CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_WITH_LOCALIZATIONS);

			Ext.Array.forEach(languagesWithLocalizations, function(language, index, allLanguages) {
				var item = Ext.create('Ext.form.field.Text', {
					name: language.get(CMDBuild.core.proxy.CMProxyConstants.TAG),
					fieldLabel: language.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION),
					labelWidth: CMDBuild.LABEL_WIDTH,
					flex: 1,
					padding: '3 5',
					labelClsExtra: 'ux-flag-' + language.get(CMDBuild.core.proxy.CMProxyConstants.TAG),
					labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
				});

				item.setValue(
					translationsValues[
						language.get(CMDBuild.core.proxy.CMProxyConstants.TAG)
					]
				);

				if (!Ext.isEmpty(this.form))
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