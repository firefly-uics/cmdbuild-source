(function() {
	Ext.define("CMDBuild.view.management.workflow.widgets.CMManageRelation", {
		extend: "CMDBuild.view.management.classes.CMCardRelationsPanel",

		constructor: function(c) {
			this.extattrtype = "manageRelation";
			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;

			this.callParent(arguments);
		},

		initComponent: function() {
			var createAndLink = this.widgetConf.enabledFunctions.createAndLinkElement || false,
				linkElement = this.widgetConf.enabledFunctions.linkElement || false;

			this.backToActivityButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.workflow.back
			});

			Ext.apply(this, {
				cmWithAddButton: createAndLink || linkElement,
				border: false,
				frame: false,
				buttonAlign: "center",
				buttons: [this.backToActivityButton],
				cls: "x-panel-body-default-framed"
			});

			this.callParent(arguments);
		},

		cmActivate: function() {
			this.mon(this.ownerCt, "cmactive", function() {
				this.ownerCt.bringToFront(this);
			}, this, {single: true});

			this.ownerCt.cmActivate();
		},

		renderRelationActions: function(value, metadata, record) {
			if (record.get("depth") == 1) { // the domains node has no icons to render
				return "";
			}

			var tr = CMDBuild.Translation.management.modcard,
				actionsHtml = '',
				enabledFunctions = this.widgetConf.enabledFunctions,
				extAttrDef = this.widgetConf,
				isSel = (function(record) {
						var id = parseInt(record.get('CardId'));
						if(undefined === extAttrDef.currentValue){return false;}
						return extAttrDef.currentValue.indexOf(id) >= 0;
					})(record);

			if (enabledFunctions['single'] || enabledFunctions['multi']) {
				var type = 'checkbox';
				if (enabledFunctions['single']) {
					type = 'radio';
				}

				actionsHtml += '<input type="' + type + '" name="'
						+ this.widgetConf.outputName + '" value="'
						+ record.get('dst_id') + '"';

				if (isSel) {
					actionsHtml += ' checked="true"';
				}

				actionsHtml += '/>';
			}

			if (enabledFunctions['allowModify']) {
				actionsHtml += getImgTag("edit", "link_edit.png");
			}
			if (enabledFunctions['allowUnlink']) {
				actionsHtml += getImgTag("delete", "link_delete.png");
			}
			if (enabledFunctions['allowModifyCard']) {
				actionsHtml += getImgTag("editcard", "modify.png");
			}
			if (enabledFunctions['allowDelete']) {
				actionsHtml += getImgTag("deletecard", "delete.png");
			}

			return actionsHtml;
		}
	});

	function getImgTag(action, icon) {
		return '<img style="cursor:pointer" class="action-relation-'+ action +'" src="images/icons/' + icon + '"/>';
	}

})();