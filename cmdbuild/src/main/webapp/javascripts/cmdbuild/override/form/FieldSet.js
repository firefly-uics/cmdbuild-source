(function () {

	Ext.define('CMDBuild.override.form.FieldSet', {
		override: 'Ext.form.FieldSet',

		/**
		 * @cfg {Mixed}
		 */
		checkboxUncheckedValue: undefined,

		/**
		 * @cfg {Mixed}
		 */
		checkboxValue: undefined,

		/**
		 * Possible values checkbox or radio
		 *
		 * @cfg {String}
		 */
		controllerxType: 'checkbox',

		/**
		 * Implementation of "checkboxValue" property that allow to return different values relatively to each fieldset
		 * Implementation of controllerxType to use also radio fields - 25/08/2016
		 *
		 * @returns {Ext.Component}
		 *
		 * @override
		 */
		createCheckboxCmp: function () {
			var suffix = '-checkbox';

			this.checkboxCmp = Ext.widget({
				xtype: this.controllerxType,
				hideEmptyLabel: true,
				name: this.checkboxName || this.id + suffix,
				cls: this.baseCls + '-header' + suffix,
				id: this.id + '-legendChk',
				checked: this.collapsible && !this.collapsed,
				inputValue: Ext.isEmpty(this.checkboxValue) ? 'on' : this.checkboxValue,
				uncheckedValue: Ext.isEmpty(this.checkboxUncheckedValue) ? 'off' : this.checkboxUncheckedValue,

				listeners: {
					scope: this,
					change: this.onCheckChange
				}
			});

			return this.checkboxCmp;
		},

		/**
		 * Disable also toglecheckbox - 18/10/2016
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		disable: function() {
			this.callParent(arguments);

			if (!Ext.isEmpty(this.checkboxCmp) && Ext.isFunction(this.checkboxCmp.disable))
				this.checkboxCmp.disable();
		},

		/**
		 * Enable also toglecheckbox - 18/10/2016
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		enable: function() {
			this.callParent(arguments);

			if (!Ext.isEmpty(this.checkboxCmp) && Ext.isFunction(this.checkboxCmp.enable))
				this.checkboxCmp.enable();
		},

		/**
		 * implementation of checkchange event fire - 25/08/2016
		 *
		 * @param {Object} cmp
		 * @param {Boolean} checked
		 *
		 * @returns {Void}
		 *
		 * @override
		 * @private
		 */
		onCheckChange: function (cmp, checked) {
			this.callParent(arguments);

			this.fireEvent('checkchange', this, checked);
		},

		/**
		 * An ExtJs feature implementation to reset function for FieldSet - 08/04/2014
		 *
		 * @returns {Void}
		 */
		reset: function () {
			// Resets all items except fieldset toglecheckbox
			this.cascade(function (item) {
				if (Ext.isEmpty(item.checkboxToggle))
					item.reset();
			});
		},

		/**
		 * An ExtJs fix for CellEditing plugin within FieldSet 21/03/2014
		 *
		 * @returns {Ext.form.FieldSet}
		 */
		setExpanded: function (expanded) {
			var me = this,
				checkboxCmp = me.checkboxCmp,
				operation = expanded ? 'expand' : 'collapse';

			if (
				this.collapsible // Enable visual collapse/expand actions only if collapsible property is true
				&& (
					!me.rendered
					|| me.fireEvent('before' + operation, me) !== false
				)
			) {
				expanded = !!expanded;

				if (checkboxCmp)
					checkboxCmp.setValue(expanded);

				if (me.rendered)
					if (expanded) {
						me.removeCls(me.baseCls + '-collapsed');
					} else {
						me.addCls(me.baseCls + '-collapsed');
					}

				me.collapsed = !expanded;

				if (expanded) {
					delete me.getHierarchyState().collapsed;
				} else {
					me.getHierarchyState().collapsed = true;
				}

				if (me.rendered) {
					// say explicitly we are not root because when we have a fixed/configured height
					// our ownerLayout would say we are root and so would not have it's height
					// updated since it's not included in the layout cycle
					me.updateLayout({ isRoot: false });
					me.fireEvent(operation, me);
				}
			}

			return me;
		}
	});

})();
