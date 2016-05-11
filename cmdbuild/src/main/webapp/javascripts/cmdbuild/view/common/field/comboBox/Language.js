(function () {

	Ext.define('CMDBuild.view.common.field.comboBox.Language', {
		extend: 'Ext.form.field.ComboBox',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.localization.Localization'
		],

		/**
		 * @cfg {Boolean}
		 */
		enableChangeLanguage: true,

		displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
		editable: false,
		fieldCls: 'ux-icon-combo-input ux-icon-combo-item',
		forceSelection: true,
		iconClsField: CMDBuild.core.constants.Proxy.TAG,
		iconClsPrefix: 'ux-flag-',
		valueField: CMDBuild.core.constants.Proxy.TAG,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				tpl: new Ext.XTemplate(
					'<tpl for=".">',
						'<div class="x-boundlist-item x-combo-list-item ux-icon-combo-item ' + this.iconClsPrefix + '{' + this.iconClsField + '}">{' + this.displayField + '}</div>',
					'</tpl>'
				),
				store: CMDBuild.proxy.localization.Localization.getStoreLanguages(),
				queryMode: 'local'
			});

			this.callParent(arguments);

			this.setValue = Ext.Function.createInterceptor(this.setValue, function (value) {
				if (this.lastFlagCls && !Ext.isEmpty(this.inputEl))
					this.inputEl.removeCls(this.lastFlagCls);

				this.lastFlagCls = this.iconClsPrefix + value;

				if (!Ext.isEmpty(this.inputEl))
					this.inputEl.addCls(this.lastFlagCls);
			}, this);

			this.getStore().on('load', function (store, records, successful, eOpts) {
				this.setValue(this.getCurrentLanguage());
			}, this);
		},

		listeners: {
			select: function (field, records, eOpts) {
				this.setValue(this.getValue()); // Fixes flag image render error

				if (this.enableChangeLanguage)
					this.changeLanguage(records[0].get(CMDBuild.core.constants.Proxy.TAG));
			}
		},

		/**
		 * @param {String} language
		 *
		 * @returns {Void}
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
