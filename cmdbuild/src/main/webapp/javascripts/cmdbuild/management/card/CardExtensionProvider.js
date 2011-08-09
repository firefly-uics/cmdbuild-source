/**
 * This class provide an interface for an object
 * that can be used to add attributes to a card
 * after the submit of the card.
 */
CMDBuild.Management.CardExtensionProvider = function() {
	this.extensionName = undefined;
};
CMDBuild.Management.CardExtensionProvider.prototype = {
	getValues: function() {
		throw new Error("CardExtensionProvider - getDate: " +
				"This method must be implemented in a subclass.");
	},
	getExtensionName: function() {
		if (this.extensionName) {
			return this.extensionName;
		} else {
			throw new Error("CardExtensionProvider - getExtensionName: extensionName is undefined");
		}
	}
};