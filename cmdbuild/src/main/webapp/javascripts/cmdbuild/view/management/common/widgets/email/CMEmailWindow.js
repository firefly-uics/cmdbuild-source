(function() {

// TODO bind X button on top of window, to save update after click

var reader = CMDBuild.management.model.widget.ManageEmailConfigurationReader;
var fields = reader.FIELDS;

Ext.define("CMDBuild.view.management.common.widgets.CMEmailWindowDelegate", {
	/**
	 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow
	 * @param {Ext.form.Basic} form
	 * @param {CMDBuild.management.mail.Model} emailRecord
	 */
	onCMEmailWindowAttachFileChanged: function(emailWindow, form, emailRecord) {},

	/**
	 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow
	 * @param {CMDBuild.management.mail.Model} emailRecord
	 */
	onAddAttachmentFromDmsButtonClick: function(emailWindow, emailRecord) {},

	/**
	 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow
	 */
	onCMEmailWindowRemoveAttachmentButtonClick: function(emailWindow) {},

	/**
	 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow
	 */
	beforeCMEmailWindowDestroy: function(emailWindow) {}
});

Ext.define("CMDBuild.view.management.common.widgets.CMEmailWindowFileAttacchedPanel", {
	extend: "Ext.panel.Panel",

	// configuration
	fileName: undefined,
	referredEmail: null,
	delegate: undefined,
	// configuration

	initComponent: function() {

		this.layout = {
			type: 'hbox',
			align: 'middle'
		},

		this.frame = true;

		var me = this;

		this.margin = 5;
		this.items = [{
			bodyCls : "x-panel-body-default-framed",
			border: false,
			html: this.fileName,
			frame: false,
			flex: 1,
		}, {
			xtype: "button",
			iconCls : "delete",
			handler: function() {
				me.delegate.onCMEmailWindowRemoveAttachmentButtonClick(me);
			}
		}];

		this.callParent(arguments);
	},

	removeFromEmailWindow: function() {
		this.ownerCt.remove(this);
	}
});

Ext.define("CMDBuild.view.management.common.widgets.CMEmailWindow", {
	extend: "CMDBuild.PopupWindow",

	// configuration
	emailGrid: undefined,
	readOnly: false,
	record: undefined,
	delegate: undefined,
	// configuration

	initComponent : function() {

		var me = this;

		var body = bodyBuild(me);
		this.attachmentPanelsContainer = buildAttachmentPanelsContainer(me);
		this.attachmentButtonsContainer = buildAttachmentButtonsContainer(me);
		var formPanel = buildFormPanel(me, body);

		// to reach the basic form outside
		this.form = formPanel.getForm();

		this.title = CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose;
		this.layout = {
			type: 'vbox',
			align: 'stretch'
		};

		this.items = [formPanel, this.attachmentButtonsContainer, this.attachmentPanelsContainer];
		this.buttonAlign = 'center';
		this.buttons = buildButtons(me);
		this.delegate = this.delegate || new CMDBuild.view.management.common.widgets.CMEmailWindowDelegate();

		this.callParent(arguments);

		fixIEFocusIssue(this, body);

		var attachments = this.record.getAttachmentNames();
		for (var i=0, l=attachments.length; i<l; ++i) {
			var attachmentName = attachments[i];
			this.addAttachmentPanel(attachmentName, this.record);
		}

		this.on("beforedestroy", function () {
			if (this.save == true) {
				this.delegate.beforeCMEmailWindowDestroy(this);
			}
		}, this);

	},

	addAttachmentPanel: function(fileName, emailRecord) {
		this.attachmentPanelsContainer.add(new CMDBuild.view.management.common.widgets.CMEmailWindowFileAttacchedPanel({
			fileName: fileName,
			referredEmail: emailRecord,
			delegate: this.delegate
		}));

		this.attachmentPanelsContainer.doLayout();
	}
});

function bodyBuild(me) {
	var body;
	if (me.readOnly) {
		body = new Ext.panel.Panel({
			frame: true,
			border: true,
			html: me.record.get(fields.CONTENT),
			autoScroll: true,
			flex: 1
		});
	} else {
		body = new Ext.form.field.HtmlEditor({
			name : fields.CONTENT,
			fieldLabel : 'Content',
			hideLabel: true,
			enableLinks: false,
			enableSourceEdit: false,
			enableFont: false,
			value: me.record.get(fields.CONTENT),
			flex: 1
		});
	}
	return body;
}

function buildFormPanel(me, body) {
	return new Ext.form.FormPanel({
		frame: false,
		border: false,
		padding: '5',
		flex: 3,
		bodyCls: "x-panel-body-default-framed",
		layout: {
			type: 'vbox',
			align: 'stretch' // Child items are stretched to full width
		},
		defaults: {
			labelAlign: "right"
		},
		items : [{
				xtype: 'displayfield',
				name : fields.FROM_ADDRESS,
				fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.fromfld,
				value: me.record.get(fields.FROM_ADDRESS)
			},{
				xtype: me.readOnly ? 'displayfield' : 'textfield',
				name : fields.TO_ADDRESS,
				fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
				value: me.record.get(fields.TO_ADDRESS)
			},{
				xtype: me.readOnly ? 'displayfield' : 'textfield',
				name : fields.CC_ADDRESS,
				fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld,
				value: me.record.get(fields.CC_ADDRESS)
			},{
				xtype: me.readOnly ? 'displayfield' : 'textfield',
				name : fields.SUBJECT,
				fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjectfld,
				value: me.record.get(fields.SUBJECT)
			},body]
	});
}

function buildButtons(me) {
	var buttons;
	if (me.readOnly) {
		buttons = [
		new CMDBuild.buttons.CloseButton({
			handler: function() {
				me.destroy();
			}
		})];
	} else {
		buttons = [
			new CMDBuild.buttons.ConfirmButton({
				scope: me,
				handler: function() {
					var valueTo = me.form.getValues()[fields.TO_ADDRESS];
					var valueCC = me.form.getValues()[fields.CC_ADDRESS];

					if (controlAddresses(valueTo, valueCC)) {
						me.save = true;
						// Destroy call an event after(!) the destruction of the window
						// the event saves the values of the form. For save the values only if are correct
						// we have to put this boolean that is valdid only on the confirm button
						me.destroy();
						me.save = false;
					}
				}
			}),
			new CMDBuild.buttons.AbortButton({
				handler: function() {
					me.destroy();
				}
			})
		];
	}
	return buttons;
}

function buildAttachmentButtonsContainer(me) {
	return Ext.create('Ext.container.Container', {
		layout: {
			type: 'hbox',
			padding: '0 5'
		},
		items: [ //
			buildUploadForm(me)
			,{
				xtype: "button",
				margin: "0 0 0 5",
				text: CMDBuild.Translation.add_attachment_from_dms,
				handler: function() {
					me.delegate.onAddAttachmentFromDmsButtonClick(me, me.record);
				}
			}
		]
	});
}

function buildAttachmentPanelsContainer(me) {
	return Ext.create('Ext.container.Container', {
		autoScroll: true,
		flex: 1,
		getFileNames: function() {
			var names = [];

			this.items.each(function(i) {
				names.push(i.fileName);
			});

			return names;
		}
	});
}

function buildUploadForm(me) {
	return Ext.create('Ext.form.Panel', {
		frame : false,
		border: false,
		bodyCls: "x-panel-body-default-framed",
		items : [ {
			xtype : 'filefield',
			name : 'file',
			buttonText : CMDBuild.Translation.attachfile,
			buttonOnly: true,
			listeners: {
				change: function(field, value) {
					var form = this.up('form').getForm();
					me.delegate.onCMEmailWindowAttachFileChanged(me, form, me.record);
				}
			}
		}]
	});
}

function fixIEFocusIssue(me, body) {
	// Sometimes on IE the HtmlEditor is not able to
	// take the focus after the mouse click. With this
	// call it works. The reason is currently unknown.
	if (Ext.isIE) {
		me.mon(body, "render", function() {
			try {
				body.focus();
			} catch (e) {}
		}, me);
	}
}
function controlAddresses(valueTo, valueCC) {
	var toStr = CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld;
	var ccStr = CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld;
	var strError = CMDBuild.Translation.error;
	var errors = [];
	errors = getAddressErrors(strError + " " + toStr + ": ", valueTo, errors);
	if (Ext.String.trim(valueCC) != "") {
		errors = getAddressErrors(strError + " " + ccStr + ": ", valueCC, errors);
	}
	if (errors.length > 0) {
		var messages = htmlComposeMessage(errors);
		CMDBuild.Msg.error(null, messages , false);
		return false;
	}
	return true;
}
function getAddressErrors(str, value, errors) {
	var ar = value.split(",");
	for (var i = 0; i < ar.length; i++) {
		var s = Ext.String.trim(ar[i]);
		if (!(/^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$/.test(s))) {
			errors.push(str + ": " + s);
		}
	}
	return errors;
}
function htmlComposeMessage(errors) {
	var messages = "";
	for (var i = 0; i < errors.length; i++) {
		var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, errors[i]);
		messages += msg;
	}
	return messages;
}
})();
