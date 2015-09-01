(function() {

	Ext.define('CMDBuild.view.common.field.translatable.Utils', {

		requires: [
			'CMDBuild.core.proxy.localization.Localization',
			'CMDBuild.view.common.field.translatable.Base'
		],

		singleton: true,

		/**
		 * Service function to save all translatable fields, to call on entity save success
		 *
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
						&& item.isVisible()
						&& item instanceof CMDBuild.view.common.field.translatable.Base
					) {
						if (!Ext.Object.isEmpty(item.configurationGet())) {
							CMDBuild.core.proxy.localization.Localization.update({
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