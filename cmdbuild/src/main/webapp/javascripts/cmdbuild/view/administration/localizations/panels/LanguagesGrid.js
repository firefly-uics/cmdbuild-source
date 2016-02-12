(function() {

	Ext.define('CMDBuild.view.administration.localizations.panels.LanguagesGrid', {
		extend: 'Ext.container.Container',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {Array}
		 */
		languages: [],

		/**
		 * @property {Array}
		 */
		languageCheckboxes: [],

		border: false,
		frame: false,
		layout: 'column',

		initComponent: function() {
			CMDBuild.core.proxy.Localizations.getLanguages({
				scope: this,
				loadMask: true,
				success: function(result, options, decodedResult) {
					var translations = decodedResult[CMDBuild.core.proxy.CMProxyConstants.TRANSLATIONS];

					for (var i in translations) {
						var translation = translations[i];

						var item = Ext.create('Ext.form.field.Checkbox', {
							fieldLabel: translation[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
							labelWidth: CMDBuild.LABEL_WIDTH,
							name: translation[CMDBuild.core.proxy.CMProxyConstants.TAG],
							padding: '3 5',
							margin: '0 20 0 0',
							submitValue: false,
							labelClsExtra: 'ux-flag-' + translation[CMDBuild.core.proxy.CMProxyConstants.TAG],
							labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
						});

						this.languageCheckboxes.push(item);
						this.add(item);
					}
				}
			});

			this.callParent(arguments);
		},

		/**
		 * @return {Array}
		 */
		getValue: function() {
			var languageArray = [];

			Ext.Array.forEach(this.languageCheckboxes, function(languageCheckbox, i, allCheckboxes) {
				if (languageCheckbox.getValue())
					languageArray.push(languageCheckbox.getName());
			}, this);

			return languageArray;
		},

		/**
		 * @param {Array} activeLanguages
		 */
		setValue: function(activeLanguages) {
			if (Ext.isArray(activeLanguages))
				Ext.Array.forEach(this.languageCheckboxes, function(languageCheckbox, i, allCheckboxes) {
					languageCheckbox.setValue(Ext.Array.contains(activeLanguages, languageCheckbox.getName()));
				}, this);
		}
	});

})();