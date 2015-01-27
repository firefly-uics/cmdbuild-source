(function() {

	Ext.define('CMDBuild.view.administration.localizations.EnabledLanguagesGrid', {
		extend: 'Ext.container.Container',

		/**
		 * @cfg {Array}
		 */
		languages: [],

		languageItems: [],

		border: false,
		frame: false,
		layout: 'column',

		constructor: function() {
			var me = this;

			me.callParent(arguments);

			_CMCache.registerOnTranslations(this);

			CMDBuild.ServiceProxy.translations.readAvailableTranslations({
				success : function(response, options, decoded) {
					var column = 0;
					var arColumns = [];
					for (key in decoded.translations) {
						column++;
						var item = Ext.create('CMDBuild.view.administration.localizations.TranslationCheckbox', {
								name: decoded.translations[key].name,
								image: 'ux-flag-' + decoded.translations[key].name,
								language: decoded.translations[key].value,
								submitValue: false
							});
						me.languageItems.push(item);

						arColumns.push(item);
						if (column == 3) {
//							me.add(getLanguagesRow(arColumns));
							me.add(arColumns);
							arColumns = [];
							column = 0;
						}
					}
					if (column > 0) {
						me.add(getLanguagesRow(arColumns));
					}
				}
			});
		},
		getValues: function() {
			var languages = '';
			var first = true;
			for (key in this.languageItems) {
				var l = this.languageItems[key];
				if (l.getValue()) {
					languages += ((first) ? '' : ', ') + l.name;
					first = false;
				}
			}
			return languages;
		},

		setValues: function(activeLanguages) {
			for (key in this.languageItems) {
				var l = this.languageItems[key];
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

	function getLanguagesRow(arColumns) {
		var row = Ext.create("CMDBuild.view.administration.localizations.Row", {
			field1: arColumns[0],
			field2: arColumns[1],
			field3: arColumns[2],
		});
		return row;
	}

})();