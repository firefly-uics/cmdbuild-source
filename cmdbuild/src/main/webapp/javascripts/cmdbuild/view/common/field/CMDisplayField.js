(function() {
	var MAX_HEIGHT = 100,
		EXPAND_BUTTON_SIZE = 50;

	Ext.form.field.Display.override({
		setValue : function() {
			this.callOverridden(arguments);
			this._addTargetToLinks();
		},

		_addTargetToLinks: function() {
			var ct = this.getContentTarget();
			if (ct) {
				var links = Ext.DomQuery.select("a", ct.dom);
				if (links) {
					for (var i=0, l=links.length; i<l; ++i) {
						links[i].target = "_blank";
					}
				}
			}
		}
	});

	Ext.define("CMDBuild.view.common.field.CMDisplayField", {
		extend : "Ext.form.field.Display",

		constructor : function() {
			this.callParent(arguments);

			this.expandButtonMarkup = Ext.DomHelper.markup({tag: "div", cls: "cmdisplayfield-expandbutton"});
		},

		setValue : function() {
			this.resetSize();
			this.hideExpandButton();

			this.callParent(arguments);

			if (this.rendered) {
				var height = this.getHeight();
				if (height > MAX_HEIGHT) {
					this.setHeight(MAX_HEIGHT);
					this.showExpandButton();
				}
			}
		},

		showExpandButton: function() {
			var ct = this.getContentTarget(),
				oldWidth = ct.getWidth();

			ct.setWidth(oldWidth - EXPAND_BUTTON_SIZE);

			if (!this.expandButtonEl) {
				this.expandButtonEl = Ext.DomHelper.insertAfter(ct, this.expandButtonMarkup, returnElement = true);
				addClickListener(this, this.expandButtonEl);
			}

			this.expandButtonEl.show();
		},

		hideExpandButton: function() {
			if (this.expandButtonEl) {
				this.expandButtonEl.hide();
			}
		},

		resetSize: function() {
			this.setHeight("auto");
			var ct = this.getContentTarget();
			if (ct) {
				ct.setWidth("auto");
			}
		}
	});

	function addClickListener(field, button) {
		field.mon(button, "click", function() {
			var displayField = new Ext.form.field.Display({
					xtype: "displayfield",
					hideLabel: true,
					region: "center"
				}),
				popup = new CMDBuild.PopupWindow({
					title: field.fieldLabel,
					items: [{
						xtype: "panel",
						layout: "border",
						border: false,
						frame: false,
						padding: "5",
						autoScroll: true,
						items: [displayField]
					}],
					buttonAlign: "center",
					buttons: [{
						text: CMDBuild.Translation.common.btns.close,
						handler: function() {
							popup.destroy();
						}
					}]
				}).show();

			// set value after show to have the targetElement for the
			// _addTargetToLinks of DisplayField
			displayField.setValue(field.getValue());
		});
	}
})()