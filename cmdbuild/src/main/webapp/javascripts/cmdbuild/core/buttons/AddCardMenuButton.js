CMDBuild.AddCardMenuButton = Ext.extend(Ext.Toolbar.SplitButton, {
	translation : CMDBuild.Translation.management.moddetail,
	iconCls: 'add',
	//custom fields
	
	baseText: CMDBuild.Translation.management.modcard.add_card,
	textPrefix: CMDBuild.Translation.management.modcard.add_card,
	classId: undefined, //passed when instantiated
	cacheTreeName: CMDBuild.Constants.cachedTableType["class"], //default search in the class_tree
	eventName: "",
	
	//private
	initComponent: function() {
		this.subClasses = {};		
		Ext.apply(this, {
			text: this.baseText,
			menu : {items :[]},
			handler: this.onClick
		});
		CMDBuild.AddCardMenuButton.superclass.initComponent.apply(this, arguments);
		if (this.classId) {
			var table = CMDBuild.Cache.getTableById(this.classId);
			this.fillMenu(table);
		}
	},
	
	// FIXME: it's not a class id, but a class object! (naming)
	setClassId: function(table) {
		if (!table) {
			return;
		}
		this.classId = table.id;
		this.fillMenu(table);
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
	fillMenu: function(table) {
		if (!table) {
			return;
		}
		this.menu.removeAll();
		var classNode = this.takeClassNodeFromCache(table);
		if (classNode) {
			this.setTextSuffix(classNode.text);
			var children = classNode.childNodes;
			if (children && children.length>0) {
				//is a superClass
				this.addSubclassesToMenu(children);
			}
		}
		this.manageDropDownArrowVisibility(table);
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
	
	//private
	takeClassNodeFromCache: function(table) {
		var tableGroup = CMDBuild.Cache.getTableGroup(table);
		var classTree = CMDBuild.Cache.getTree(tableGroup);
		return CMDBuild.TreeUtility.searchNodeByAttribute({
			attribute: "id",
			value: this.classId,
			root: classTree
		});
	},
	
	//private
	addSubclassesToMenu: function(children) {		
		for (var i = 0, len = children.length; i < len; i++) {
			var childNode = children[i];			
			this.addSubclass(childNode);
		}
	},
	
	//private
	addSubclass: function(subclassNode) {
		var subclass = CMDBuild.Cache.getTableById(subclassNode.id);
		if (subclass.priv_create) {
			this.menu.addMenuItem({
				text: subclass.text,
				subclassId: subclass.id,
				subclassName: subclass.text,
				scope: this,
				handler: function(item, e){
					this.fireGivenEvent({
						classId: item.subclassId, 
						className: item.subclassName
					});
				}
			});			
		};
		var children = subclassNode.childNodes;
		if (children && children.length>0) {
			//is a superClass
			this.addSubclassesToMenu(children);
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
	fireGivenEvent: function(p) {
		this.fireEvent(this.eventName, p);
		CMDBuild.log.info('fired event', this.eventName, p);
	},
	
	//private
	resetText: function() {
		this.setText(this.baseText);
	},
	
	//private
	onClick: function() {
		//Extjs calls the handler even when disabled
		if (!this.disabled) {			
			if (this.isEmpty()) {
				this.fireGivenEvent({
					classId: this.classId,
					className: this.text
				});	
			} else {
				this.showMenu();
			}
		}
	}
});