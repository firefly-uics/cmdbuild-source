(function() {

	Ext.define('CMDBuild.view.administration.localizations.panels.LanguagesGrid', {
		extend: 'Ext.container.Container',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
//			'CMDBuild.ServiceProxy.translations' // TODO
		],

		/**
		 * @cfg {Array}
		 */
		languages: [],

		languageCheckboxes: [],

		border: false,
		frame: false,
		layout: 'column',

		initComponent: function() {
			_CMCache.registerOnTranslations(this);

			CMDBuild.LoadMask.instance.show();
			CMDBuild.ServiceProxy.translations.readAvailableTranslations({
				scope: this,
				success: function(result, options, decodedResult) {
					var translations = decodedResult[CMDBuild.core.proxy.CMProxyConstants.TRANSLATIONS];
_debug('decodedResult', decodedResult);
					for (var i in translations) {
						var translation = translations[i];

						var item = Ext.create('Ext.form.field.Checkbox', {
							fieldLabel: translation[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
							labelWidth: CMDBuild.LABEL_WIDTH,
							name: translation[CMDBuild.core.proxy.CMProxyConstants.TAG],
							padding: '3 5',
							submitValue: false,
							labelClsExtra: 'ux-flag-' + translation[CMDBuild.core.proxy.CMProxyConstants.TAG],
							labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
						});

						this.languageCheckboxes.push(item);
						this.add(item);
					}
				},
				callback: function() {
					CMDBuild.LoadMask.instance.hide();
				}
			});

			this.callParent(arguments);
		},

		/**
		 * @return {Array}
		 */
		getValue: function() {
			var languageArray = {};

			for (var i in this.languageCheckboxes) {
				var languageCheckbox = this.languageCheckboxes[i];

				if (languageCheckbox.getValue())
					languageArray.push(languageCheckbox.getName());
			}

			return languageArray;
		},

		/**
		 * @param {Array} activeLanguages
		 */
		setValue: function(activeLanguages) {
			for (var i in this.languageCheckboxes) {
				var languageCheckbox = this.languageCheckboxes[i];

				languageCheckbox.setValue(Ext.Array.contains(activeLanguages, languageCheckbox.getName()));
			}
		},

		// TODO: this method is used????
		resetLanguages: function() {
			this.setValue(_CMCache.getActiveTranslations());
		}
	});

})();