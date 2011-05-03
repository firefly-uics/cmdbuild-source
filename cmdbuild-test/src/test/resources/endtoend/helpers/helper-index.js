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

	loginButtonIsDisabled : function() {
		return this.getLoginButton().disabled;
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
		return this.getLoginForm().buttons[0];
	},

	getLoginForm : function() {
		return this.getLoginPanel().form;
	},

	getLoginPanel : function() {
		return Ext.getCmp("login");
	}
});
