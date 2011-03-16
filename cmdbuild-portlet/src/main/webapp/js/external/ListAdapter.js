ListAdapter = function(o) {
	this.classForSelectedTabItem = o.classForSelectedTabItem || "";
	this.classForDefaultTabItem = o.classForDefaultTabItem || "";
	this.relationMap = {};
	this.tabList = jQuery(jQuery('#'+o.tabListId)[0]) || [];
	this.divContainer = jQuery(jQuery('#'+o.panelsContainerId)[0]) || [];
	this.selected = undefined;
	
	this.initStructure();
	this.addEventToTabList();
	this.selectFirst();
};

ListAdapter.prototype = {
	initStructure: function() {
		var items = this.tabList.children('li');
		var panels = this.divContainer.children('div');
		this.relationMap = {};
		for (var i = 0, len = items.length;  i<len; ++i) {
			this.normalizeTabItem(items[i]);
			jQuery(panels[i]).hide();
			this.relationMap[items[i].id] = jQuery(panels[i]); //fill the relationMap
		}
	},
	
	normalizeTabItem: function(item) {
		var textContent = item.textContent;
		jQuery(item).html(textContent);
		jQuery(item).css('cursor', 'pointer');
	},
	
	addEventToTabList: function() {
		var items = this.tabList.find('li');
		var scope = this;
		for (var i = 0, len = items.length;  i<len; ++i) {
			var item = items[i];
			jQuery(item).click(function(){
				scope.onClickListener(this);
			});
		}
	},
	
	onClickListener: function(listItem) {

		if (this.selected != undefined) {
			this.relationMap[this.selected.id].hide();
			jQuery(this.selected).removeClass(this.classForSelectedTabItem);
			jQuery(this.selected).addClass(this.classForDefaultTabItem);
		}
		this.selected = listItem;
                var selectedItem = this.relationMap[listItem.id];
		selectedItem.fadeIn('normal');
		jQuery(listItem).addClass(this.classForSelectedTabItem);
		jQuery(listItem).removeClass(this.classForDefaultTabItem);
	},
	
	selectFirst: function() {
		this.onClickListener(jQuery(this.tabList[0]).children()[0]);
	}
};
