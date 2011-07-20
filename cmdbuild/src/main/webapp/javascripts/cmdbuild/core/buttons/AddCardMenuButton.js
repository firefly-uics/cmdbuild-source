(function() {

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
	},

	showDropDownArrow: function() {
		if (this.el) {
			this.el.child(this.arrowSelector).addClass('x-btn-split');
		}
	},
	
	hideDropDownArrow: function() {
		if (this.el) {
			this.el.child(this.arrowSelector).removeClass('x-btn-split');
		}
	},
	
	//private
	manageDropDownArrowVisibility: function(table) {
		if (!table) {
			return;
		}
		if (this.isEmpty()) {
			this.hideDropDownArrow();
			if (table.priv_create) {
				this.enable();
			} else {
				this.disable();
			}
		} else {
			this.showDropDownArrow();
			this.enable();
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
	isEmpty: function(){
		return (this.menu.items.items.length == 0 );
	},
	
	//private
	resetText: function() {
		this.setText(this.baseText);
	}
	
});

	function fillMenu(entry) {
		this.menu.removeAll();

		if (entry) {
			this.setTextSuffix(entry.data.text);
			var children = _CMUtils.getChildrenById(entry.get("id"));

			if (children && children.length>0) {
				//is a superClass
				addSubclassesToMenu.call(this, children);
			}
		}
//		this.manageDropDownArrowVisibility(table);
	}
	
	function addSubclassesToMenu(children) {		
		for (var i = 0, len = children.length; i < len; i++) {
			addSubclass.call(this, children[i]);
		}
	}
	
	function addSubclass(entry) {
		if (entry.get("priv_create")) {	
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

		var children = _CMUtils.getChildrenById(entry.get("id"));
		if (children && children.length>0) {
			//is a superClass
			addSubclassesToMenu.call(this, children);
		}
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