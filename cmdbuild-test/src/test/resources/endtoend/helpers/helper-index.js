TestHelper.prototype = Ext.apply(TestHelper.prototype, {

	fillUsername : function(username) {
		this.getUsernameField().setValue(username);
	},

	fillPassword : function(password) {
		this.getPasswordField().setValue(password);
	},

	pressLoginButton : function() {
		this.pressButton(this.getLoginButton());
	},

	/*
	 * UI Access
	 */

	getUsernameField : function() {
		return this.getLoginPanel().user;
	},

	getPasswordField : function() {
		return this.getLoginPanel().password;
	},

	getLoginButton : function() {
		return this.getLoginPanel().loginButton;
	},

	getLoginPanel : function() {
		return Ext.getCmp("login");
	}
});
