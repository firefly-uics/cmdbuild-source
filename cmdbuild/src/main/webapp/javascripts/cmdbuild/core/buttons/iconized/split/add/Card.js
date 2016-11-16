(function() {

	var ARROW_ELEMENT_SELECTOR = '.x-btn-split';
	var ARROW_CLASS = 'x-btn-split-right';

	Ext.require('CMDBuild.core.Utils');

	/**
	 * @link CMDBuild.core.buttons.iconized.split.add.Activity
	 */
	Ext.define('CMDBuild.core.buttons.iconized.split.add.Card', {
		extend: 'Ext.button.Split',

		translation: CMDBuild.Translation.management.moddetail,
		iconCls: 'add',

		//custom fields
		cmName: undefined,
		baseText: CMDBuild.Translation.management.modcard.add_card,
		textPrefix: CMDBuild.Translation.management.modcard.add_card,

		//private
		initComponent: function() {
			this.subClasses = {};
			Ext.applyIf(this, {
				text: this.baseText,
				menu : {items :[]},
				handler: onClick,
				scope: this
			});

			this.callParent(arguments);
		},

		updateForEntry: function(entry) {
			if (!entry) {
				return;
			}

			this.classId = entry.get('id');
			fillMenu.call(this, entry);

			var privileges = CMDBuild.core.Utils.getEntryTypePrivileges(_CMCache.getEntryTypeById(this.classId));

			var c = _CMCache.getEntryTypeById(this.classId);

			if (c && c.get('superclass')) {
				this.setDisabled(this.isEmpty() || privileges.crudDisabled.create);
				this.showDropDownArrow();
			} else {
				this.setDisabled(!privileges.create || privileges.crudDisabled.create);
				this.hideDropDownArrow();
			}
		},

		/**
		 * Enable only if is not superclass or if is superclass not empty
		 *
		 * @override
		 */
		enable: function () {
			var c = _CMCache.getEntryTypeById(this.classId);

			if (
				!Ext.isEmpty(this.classId)
				&& (
					!c && c.get('superclass')
					|| (c && c.get('superclass') && !this.isEmpty())
				)
			) {
				this.callParent(arguments);
			}
		},

		showDropDownArrow: function() {
			var arrowEl = this.getArrowEl();
			if (arrowEl) {
				arrowEl.addCls(ARROW_CLASS);
			}
		},

		hideDropDownArrow: function() {
			var arrowEl = this.getArrowEl();
			if (arrowEl) {
				arrowEl.removeCls(ARROW_CLASS);
			}
		},

		disableIfEmpty: function() {
			if (this.isEmpty()) {
				this.disable();
			} else {
				this.enable();
			}
		},

		setTextSuffix: function(suffix) {
			this.setText(this.textPrefix +' '+suffix);
		},

		getArrowEl: function() {
			try {
				var arrowEl = Ext.DomQuery.selectNode(ARROW_ELEMENT_SELECTOR, this.el.dom);
				return Ext.get(arrowEl);
			} catch (e) {
				return null;
			}
		},

		//private
		isEmpty: function() {
			return (this.menu && this.menu.items.length == 0);
		},

		//private
		resetText: function() {
			this.setText(this.baseText);
		}
	});

	var WANNABE_DESCRIPTION = 'text';

	function fillMenu(entry) {
		this.menu.removeAll();

		if (entry) {
			var entryId = entry.get('id'),
				c = _CMCache.getEntryTypeById(entryId);

			this.setTextSuffix(entry.data.text);

			if (c && c.get('superclass')) {
				var descendants = getDescendantsById(entryId);

				Ext.Array.sort(descendants, function(et1, et2) {
						return et1.get(WANNABE_DESCRIPTION) >= et2.get(WANNABE_DESCRIPTION);
					});

				for (var i=0; i<descendants.length; ++i) {
					var d = descendants[i];
					addSubclass.call(this, d);
				}
			}
		}
	}

	function getDescendantsById(entryTypeId) {
		var children = getChildrenById(entryTypeId);
		var et = _CMCache.getEntryTypeById(entryTypeId);
		var out = [et];

		for (var i = 0; i < children.length; ++i) {
			var c = children[i];
			var leaves = getDescendantsById(c.get('id'));

			out = out.concat(leaves);
		}

		return out;
	}

	function getChildrenById(entryTypeId) {
		var ett = _CMCache.getEntryTypes();
		var out = [];

		for (var et in ett) {
			et = ett[et];

			if (et.get('parent') == entryTypeId)
				out.push(et);
		}

		return out;
	}

	function addSubclass(entry) {
		var privileges = CMDBuild.core.Utils.getEntryTypePrivileges(_CMCache.getEntryTypeById(entry.get('id')));

		if (privileges.create && ! privileges.crudDisabled.create) {
			this.menu.add({
				text: entry.get('text'),
				subclassId: entry.get('id'),
				subclassName: entry.get('text'),
				scope: this,
				handler: function(item, e){
					this.fireEvent('cmClick', {
						classId: item.subclassId,
						className: item.subclassName
					});
				}
			});
		};
	}

	function onClick() {
		//Extjs calls the handler even when disabled
		if (!this.disabled) {
			if (this.isEmpty()) {
				this.fireEvent('cmClick', {
					classId: this.classId,
					className: this.text
				});
			} else {
				this.showMenu();
			}
		}
	}

})();