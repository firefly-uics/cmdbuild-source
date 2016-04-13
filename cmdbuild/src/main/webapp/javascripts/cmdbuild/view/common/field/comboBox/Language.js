(function () {

	Ext.define('CMDBuild.view.common.field.comboBox.Language', {
		extend: 'Ext.form.field.ComboBox',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.localization.Localization'
		],

		/**
		 * @cfg {Boolean}
		 */
		enableChangeLanguage: true,

		displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
		editable: false,
		fieldCls: 'ux-icon-combo-input ux-icon-combo-item',
		forceSelection: true,
		iconClsField: 'name', // could be changed on instantiation
		iconClsField: CMDBuild.core.constants.Proxy.TAG,
		iconClsPrefix: 'ux-flag-', // could be changed on instantiation
		valueField: CMDBuild.core.constants.Proxy.TAG,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			var tpl = '<div class="x-combo-list-item ux-icon-combo-item ' + this.iconClsPrefix + '{' + this.iconClsField + '}">{' + this.displayField +'}</div>';

			Ext.apply(this, {
				listConfig: {
					getInnerTpl: function () { return tpl; }
				},
				store: CMDBuild.core.proxy.localization.Localization.getStoreLanguages(),
				queryMode: 'local'
			});

			this.callParent(arguments);

			this.setValue = Ext.Function.createInterceptor(this.setValue, function (v) {
				if (this.lastFlagCls && !Ext.isEmpty(this.inputEl)) {
					this.inputEl.removeCls(this.lastFlagCls);
				}

				this.lastFlagCls = this.iconClsPrefix + v;

				if (!Ext.isEmpty(this.inputEl))
					this.inputEl.addCls(this.lastFlagCls);
			}, this);

			this.getStore().on('load', function (store, records, successful, eOpts) {
				this.setValue(this.getCurrentLanguage());
			}, this);
		},

		listeners: {
			select: function (field, records, eOpts) {
				if (this.enableChangeLanguage)
					this.changeLanguage(records[0].get(CMDBuild.core.constants.Proxy.TAG));
			}
		},

		/**
		 * @param {String} language
		 *
		 * @private
		 */
		changeLanguage: function (language) {
			language = !Ext.isEmpty(language) && Ext.isString(language) ? language : CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE);

			window.location = '?' + CMDBuild.core.constants.Proxy.LANGUAGE + '=' + language;
		},

		/**
		 * @returns {String}
		 *
		 * @private
		 */
		getCurrentLanguage: function () {
			// Step 1: check URL
			if (!Ext.isEmpty(window.location.search))
				return Ext.Object.fromQueryString(window.location.search)[CMDBuild.core.constants.Proxy.LANGUAGE];

			// Step 2: check CMDBuild configuration (default)
			if (!Ext.isEmpty(CMDBuild) && !Ext.isEmpty(CMDBuild.configuration) && !Ext.isEmpty(CMDBuild.configuration.localization))
				return CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE);
		}
	});

})();
