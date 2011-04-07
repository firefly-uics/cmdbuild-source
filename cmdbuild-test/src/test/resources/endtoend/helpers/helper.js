TestHelper = function() {
};

TestHelper.prototype = {

	pressButton : function(button) {
		button.btnEl.dom.click();
	},

	errorPopupIsDisplayed : function() {
		return this.popupIsDisplayed(Ext.Msg.ERROR);
	},

	popupIsDisplayed : function(iconClass) {
		return Ext.Msg.isVisible() && this.elementHasAtLeastAChildWithClass(Ext.Msg.getDialog().getEl(), iconClass);
	},

	elementHasAtLeastAChildWithClass : function(el, subelClass) {
		return el.query("." + subelClass).length > 0;
	}
};
