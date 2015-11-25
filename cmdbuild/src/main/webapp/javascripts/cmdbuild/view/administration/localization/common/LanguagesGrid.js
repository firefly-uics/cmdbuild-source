(function() {

	Ext.define('CMDBuild.view.administration.localization.common.LanguagesGrid', {
		extend: 'Ext.container.Container',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.localization.Localization'
		],

		border: false,
		frame: false,
		layout: 'column',

		initComponent: function() {
			CMDBuild.core.proxy.localization.Localization.getLanguages({
				scope: this,
				loadMask: true,
				success: function(result, options, decodedResult) {
					var translations = decodedResult[CMDBuild.core.constants.Proxy.TRANSLATIONS];

					// Sort languages columns with alphabetical sort order
					CMDBuild.core.Utils.objectArraySort(translations, CMDBuild.core.constants.Proxy.DESCRIPTION);

					Ext.Array.forEach(translations, function(translation, i, allTranslations) {
						var item = Ext.create('Ext.form.field.Checkbox', {
							fieldLabel: translation[CMDBuild.core.constants.Proxy.DESCRIPTION],
							labelWidth: CMDBuild.LABEL_WIDTH,
							name: translation[CMDBuild.core.constants.Proxy.TAG],
							padding: '3 5',
							margin: '0 20 0 0',
							submitValue: false,
							labelClsExtra: 'ux-flag-' + translation[CMDBuild.core.constants.Proxy.TAG],
							labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
						});

						this.add(item);
					}, this);
				}
			});

			this.callParent(arguments);
		},

		/**
		 * @return {Array}
		 */
		getValue: function() {
			var languageArray = [];

			Ext.Array.forEach(this.getItems(), function(languageCheckbox, i, allCheckboxes) {
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
				Ext.Array.forEach(this.getItems(), function(languageCheckbox, i, allCheckboxes) {
					languageCheckbox.setValue(Ext.Array.contains(activeLanguages, languageCheckbox.getName()));
				}, this);
		},

		/**
		 * Service function to get all items
		 *
		 * @returns {Array}
		 */
		getItems: function() {
			return this.items.items;
		}
	});

})();