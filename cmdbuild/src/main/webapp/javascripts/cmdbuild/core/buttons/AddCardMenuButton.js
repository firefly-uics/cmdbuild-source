(function() {

var ARROW_ELEMENT_SELECTOR = ".x-btn-split";
var ARROW_CLASS = "x-btn-split-right";

Ext.define("CMDBuild.AddCardMenuButton", {
	extend: "Ext.button.Split",
	translation : CMDBuild.Translation.management.moddetail,
	iconCls: 'add',

	//custom fields
	cmName: undefined,
	baseText: CMDBuild.Translation.management.modcard.add_card,
	textPrefix: CMDBuild.Translation.management.modcard.add_card,
	
	//private
	initComponent: function() {
		this.subClasses = {};
		Ext.apply(this, {
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

		this.classId = entry.get("id");
		fillMenu.call(this, entry);

		if (_CMUtils.isSuperclass(this.classId)) {
			this.setDisabled(this.isEmpty());
			this.showDropDownArrow();
		} else {
			var privileges = _CMUtils.getClassPrivileges(this.classId);
			this.setDisabled(!privileges.create);
			this.hideDropDownArrow();
		}
	},

	showDropDownArrow: function() {
		if (this.el) {
			this.el.child(ARROW_ELEMENT_SELECTOR).addCls(ARROW_CLASS);
		}
	},

	hideDropDownArrow: function() {
		if (this.el) {
			this.el.child(ARROW_ELEMENT_SELECTOR).removeCls(ARROW_CLASS);
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
		this.setText(this.textPrefix +" "+suffix);
	},
	
	//private
	isEmpty: function() {
		return (this.menu && this.menu.items.length == 0 );
	},
	
	//private
	resetText: function() {
		this.setText(this.baseText);
	}
	
});

	var WANNABE_DESCRIPTION = "text";

	function fillMenu(entry) {
		this.menu.removeAll();

		if (entry) {
			var id = entry.get("id"),
				descendants = _CMUtils.getDescendantsById(id);

			this.setTextSuffix(entry.data.text);

			Ext.Array.sort(descendants, function(et1, et2) {
					return et1.get(WANNABE_DESCRIPTION) >= et2.get(WANNABE_DESCRIPTION);
				});
			for (var i=0; i<descendants.length; ++i) {
				var d = descendants[i];
				addSubclass.call(this, d);
			}
		}
	}

	function addSubclass(entry) {
		var privileges = _CMUtils.getClassPrivileges(entry.get("id"));

		if (privileges.create) {	
			this.menu.add({
				text: entry.get("text"),
				subclassId: entry.get("id"),
				subclassName: entry.get("text"),
				scope: this,
				handler: function(item, e){
					this.fireEvent("cmClick", {
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
				this.fireEvent("cmClick", {
					classId: this.classId,
					className: this.text
				});
			} else {
				this.showMenu();
			}
		}
	}

})();