(function () {

	Ext.define('CMDBuild.view.common.field.translatable.Utils', {

		requires: [
			'CMDBuild.proxy.common.field.translatable.Translatable',
			'CMDBuild.view.common.field.translatable.Translatable'
		],

		singleton: true,

		/**
		 * Service function to save all translatable fields, to call on entity save success
		 *
		 * @param {Ext.form.Panel} form
		 *
		 * @returns {Void}
		 */
		commit: function (form) {
			if (
				Ext.isObject(form) && !Ext.Object.isEmpty(form) && Ext.isFunction(form.cascade)
				&& CMDBuild.configuration.localization.hasEnabledLanguages()
			) {
				form.cascade(function (item) {
					if (
						Ext.isObject(item) && !Ext.Object.isEmpty(item)
						&& Ext.isFunction(item.isVisible) && item.isVisible()
						&& item instanceof CMDBuild.view.common.field.translatable.Translatable
					) {
						CMDBuild.proxy.common.field.translatable.Translatable.update({ params: item.paramsGet({ includeTranslations: true }) });
					}
				});
			}
		}
	});

})();
