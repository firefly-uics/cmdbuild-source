(function() {

	Ext.define('CMDBuild.view.administration.localizations.EnabledLanguagesGrid', {
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

		constructor: function() {
			this.callParent(arguments);

			_CMCache.registerOnTranslations(this);

			CMDBuild.LoadMask.instance.show();
			CMDBuild.ServiceProxy.translations.readAvailableTranslations({
				scope: this,
				success: function(result, options, decodedResult) {
					var translations = decodedResult[CMDBuild.core.proxy.CMProxyConstants.TRANSLATIONS];
_debug('decodedResult', decodedResult);
					for (var i in translations) {
						var item = Ext.create('CMDBuild.view.administration.localizations.TranslationCheckbox', {
							name: translations[i][CMDBuild.core.proxy.CMProxyConstants.NAME],
							image: 'ux-flag-' + translations[i][CMDBuild.core.proxy.CMProxyConstants.NAME],
							language: translations[i][CMDBuild.core.proxy.CMProxyConstants.VALUE],
							submitValue: false
						});

						this.languageCheckboxes.push(item);
						this.add(item);
					}
				},
				callback: function() {
					CMDBuild.LoadMask.instance.hide();
				}
			});
		},

		getValues: function() {
			var languages = '';
			var first = true;
			for (key in this.languageCheckboxes) {
				var l = this.languageCheckboxes[key];
				if (l.getValue()) {
					languages += ((first) ? '' : ', ') + l.name;
					first = false;
				}
			}
			return languages;
		},

		setValues: function(activeLanguages) {
			for (key in this.languageCheckboxes) {
				var l = this.languageCheckboxes[key];
				l.setValue(inActiveLanguages(l, activeLanguages));
			}
		},

		resetLanguages: function() {
			var activeLanguages = _CMCache.getActiveTranslations();
			this.setValues(activeLanguages);
		}
	});

	function inActiveLanguages(language, activeLanguages) {
		for (var i = 0; i < activeLanguages.length; i++) {
			if (language.name == activeLanguages[i].name) {
				return true;
			}
		}
		return false;
	}

})();