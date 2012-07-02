(function() {
	Ext.define("CMDBuild.view.management.workflow.widgets.CMManageRelation", {
		extend: "CMDBuild.view.management.classes.CMCardRelationsPanel",

		statics: {
			WIDGET_NAME: ".ManageRelation"
		},

		constructor: function(c) {
			this.widgetConf = c.widget;
			this.callParent(arguments);
		},

		initComponent: function() {
			var reader = CMDBuild.management.model.widget.ManageRelationConfigurationReader;

			Ext.apply(this, {
				cmWithAddButton: reader.canCreateAndLinkCard(this.widgetConf) 
					|| reader.canCreateRelation(this.widgetConf),
				border: false,
				frame: false,
				cls: "x-panel-body-default-framed"
			});

			this.callParent(arguments);
		},

		renderRelationActions: function(value, metadata, record) {
			if (record.get("depth") == 1) { // the domains node has no icons to render
				return "";
			}

			var actionsHtml = '',
				reader = CMDBuild.management.model.widget.ManageRelationConfigurationReader,
				widget = this.widgetConf,
				isSel = (function(record) {
					var id = parseInt(record.get('CardId'));
					if (typeof widget.currentValue == "undefined") {
						return false;
					} else {
						return widget.currentValue.indexOf(id) >= 0;
					}
				})(record);

			if (reader.singleSelection(widget) 
					|| reader.multiSelection(widget)) {

				var type = reader.singleSelection(widget) ? 'radio' : 'checkbox';

				actionsHtml += '<input type="' + type + '" name="'
						+ reader.outputName(widget) + '" value="'
						+ record.get('dst_id') + '"';

				if (isSel) {
					actionsHtml += ' checked="true"';
				}

				actionsHtml += '/>';
			}

			if (reader.canModifyARelation(widget)) {
				actionsHtml += getImgTag("edit", "link_edit.png");
			}

			if (reader.canRemoveARelation(widget)) {
				actionsHtml += getImgTag("delete", "link_delete.png");
			}

			if (reader.canModifyALinkedCard(widget)) {
				actionsHtml += getImgTag("editcard", "modify.png");
			}

			if (reader.canRemoveARelation) {
				actionsHtml += getImgTag("deletecard", "delete.png");
			}

			return actionsHtml;
		}
	});

	function getImgTag(action, icon) {
		return '<img style="cursor:pointer" class="action-relation-'+ action +'" src="images/icons/' + icon + '"/>';
	}

})();