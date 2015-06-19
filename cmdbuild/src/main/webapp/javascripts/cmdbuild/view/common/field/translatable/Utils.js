(function() {

	Ext.define('CMDBuild.view.common.field.translatable.Utils', {

		requires: [
			'CMDBuild.core.proxy.localizations.Localizations',
			'CMDBuild.view.common.field.translatable.Base'
		],

		singleton: true,

		/**
		 * @returns {Ext.form.Panel}
		 */
		commit: function(form) {
			if (
				!Ext.isEmpty(form)
				&& form instanceof Ext.form.Panel
				&& CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION].hasEnabledLanguages()
			) {
				form.cascade(function(item) {
					if (
						!Ext.isEmpty(item)
						&& item instanceof CMDBuild.view.common.field.translatable.Base
					) {
						if (!Ext.Object.isEmpty(item.configurationGet())) {
							CMDBuild.core.proxy.localizations.Localizations.update({
								params: item.configurationGet(true, true),
								success: function(response, options, decodedResponse) {
									CMDBuild.core.Message.success();
								}
							});
						}
					}
				});
			}
		}
	});

})();