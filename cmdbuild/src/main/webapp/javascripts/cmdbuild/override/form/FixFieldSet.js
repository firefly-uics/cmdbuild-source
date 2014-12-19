(function() {

	Ext.define('CMDBuild.override.form.FixFieldSet', {
		override: 'Ext.form.FieldSet',

		/**
		 * An ExtJs fix for CellEditing plugin within FieldSet 21/03/2014
		 */
		setExpanded: function(expanded) {
			var me = this;
			var checkboxCmp = me.checkboxCmp;
			var operation = expanded ? 'expand' : 'collapse';

			if (!me.rendered || me.fireEvent('before' + operation, me) !== false) {
				expanded = !!expanded;

				if (checkboxCmp)
					checkboxCmp.setValue(expanded);

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
		},

		/**
		 * An ExtJs fix to have a correct fields label and field width in FieldSet - 08/04/2014
		 */
		fieldWidthsFix: function() {
			this.cascade(function(item) {
				if (Ext.isEmpty(item.checkboxToggle) && item instanceof Ext.form.Field) {
					item.labelWidth = item.labelWidth - 10;
					item.width = item.width - 10;
				}
			});
		},

		/**
		 * An ExtJs feature implementation to reset function for FieldSet - 08/04/2014
		 */
		reset: function() { // Resets all items except fieldset toglecheckbox
			this.cascade(function(item) {
				if (Ext.isEmpty(item.checkboxToggle))
					item.reset();
			});
		}
	});

})();