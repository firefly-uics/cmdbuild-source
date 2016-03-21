(function($) {
	var NOGROUP = "_NOGROUP_";
	var GROUPOTHERS = "Others";
	var form = function() {
		this.param = undefined;
		this.backend = undefined;
		this.lookUps = [];
		this.init = function(param) {
			this.id = param.form;
			var xmlForm = $.Cmdbuild.elementsManager.getElement(this.id);
			// fields to show
			var fields = $.Cmdbuild.utilities.getFields(xmlForm);
			if (fields && fields.fields) {
				fields.form = this.id;
				$.Cmdbuild.dataModel.putFormFields(fields);
			}

			try {
				this.param = param;
				var backendFn = $.Cmdbuild.utilities.getBackend(param.backend);
				var backend = new backendFn(param, this.show, this);
				this.setBackend(backend);
			} catch (e) {
				console.log("WARNING: No data message " + e.message);
				var htmlContainer = $("#" + this.param.container)[0];
				htmlContainer.innerHTML = "<h1>NO DATA</h1>";
			}
		};
		this.flush = function(param, callback, callbackScope) {
			this.getBackend().updateData(param, callback, callbackScope);
		};
		this.refreshField = function(param) {
			param.id = param.form + "_" + param.field;
			/*
			 * ATTENTION: for now only on referenceFields 
			 * refreshField come from the cql configurator
			 */
			var selectMenu = $("#" + param.id);
			if (selectMenu[0]) {
				$.Cmdbuild.standard.referenceField.chargeLookup(param);
			}
			else {
				$.Cmdbuild.widgets.refreshCqlField(param.form, this.getBackend().widgets);
			}
		};
		this.reset = function(param) {
			for ( var key in param.data) {
				var field = $("#" + param.form + "_" + key);
				if (field) {
					field.val(param.data[key]);
				}
			}
		};
		this.getValue = function(param) {
			var val = $.Cmdbuild.utilities.getHtmlFieldValue("#" + param.form + "_" + param.field);
			if (val === null) {
				val = $.Cmdbuild.dataModel.getValue(param.form, param.field);
			}
			return val;
		};
		this.getClientDescription = function(param, callback, callbackScope) {
			var attributes = this.getBackend().getAttributes();
			var value = this.getValue(param);
			this.getDescription(param, attributes, value, function(response) {
				callback.apply(callbackScope, [response]);
			}, this);
		};
		this.getServerDescription = function(param, callback, callbackScope) {
			if (! this.getBackend().getOriginalAttributes) {
				console.log("Backend without original attributes on form: " + param.form, this.getBackend());
				callback.apply(callbackScope, [""]);
				return;
			}
			var attributes = this.getBackend().getOriginalAttributes();
			var value = $.Cmdbuild.dataModel.getValue(param.form, param.field);
			this.getDescription(param, attributes, value, function(response) {
				callback.apply(callbackScope, [response]);
			}, this);
		};
		this.getDescription = function(param, attributes, value, callback, callbackScope) {
			var attribute = undefined;
			for (var i = 0; i < attributes.length; i++) {
				if (attributes[i].name == param.field) {
					attribute = attributes[i];
					break;
				}
			}
			if (attribute && value) {
				$.Cmdbuild.utilities.getFieldDescription(attribute, value, function(response) {
					callback.apply(callbackScope, [response]);
				});
			} else {
				callback.apply(callbackScope, [value || ""]);
			}
		};
		this.change = function(param) {
			var errors = [];
			for ( var key in param.data) {
				var val = $.Cmdbuild.utilities.getHtmlFieldValue("#" + param.form + "_" + key);
				var interactivity = $.Cmdbuild.utilities.getHtmlFieldInteractivity("#" + param.form + "_" + key);
				if (val) {
					param.data[key] = val;
				} else if (interactivity != $.Cmdbuild.global.READ_WRITE_REQUIRED && val === "") {
					param.data[key] = val;
				} else if (interactivity == $.Cmdbuild.global.READ_WRITE_REQUIRED) {
					var attributes = param.formObject.getBackend().getAttributes();

					// get attribute description
					var attributeDescription = key;
					$.each(attributes, function(index, attribute) {
						if (attribute._id == key) {
							attributeDescription = attribute.description;
						}
					});

					errors.push({
						field : attributeDescription,
						errorType : $.Cmdbuild.global.NOVALUEONREQUIREDFIELD
					});
				}
			}
			return errors;
		};
		this.show = function() {
			// attributes
			this.initializationPhase = true;
			$.Cmdbuild.dataModel.evaluateXmlAttributes(this.param.form, this.getBackend().getAttributes());
			this.noGroupDefinition(this.getBackend().getAttributes());
			$.Cmdbuild.CqlManager.compile(this.param.form, this.getBackend().getAttributes());
			// generate empty data
			if (this.param.emptyForm && this.param.emptyForm == "true") {
				var newData = {};
				var attributes = this.getBackend().getAttributes();
				$.each(attributes, function(index, attribute) {
					if (attribute.defaultValue) {
						newData[attribute.name] = attribute.defaultValue;
					} else {
						newData[attribute.name] = undefined;
					}
				});
				this.setBackendData(newData)
			}

			try {
				$.Cmdbuild.dataModel.push({
					form : this.param.form,
					type : "form",
					data : this.getBackendData()
				});
				var htmlContainer = $("#" + this.param.container)[0];
				if (this.getBackend().getAttributes().length == 0) {
					htmlContainer.innerHTML = "";
					return;
				}
				var htmlStr = "";
				var tabsAttributes = {};
				this.lookUps = [];
				var attributes = this.getBackend().getAttributes();
				for (var i = 0; i < attributes.length; i++) {
					var attribute = attributes[i];
					var groupName = (this.param.noGroup == "true" || ! attribute.group) ? NOGROUP : attribute.group;
					var tab = tabsAttributes[groupName];
					if (!tab) {
						tab = [];
						tabsAttributes[groupName] = tab;
					}
					tab.push({
						form : this.param.form,
						withReturn : true,
						card : this.getBackendData(),
						attribute : attribute
					});
				}
				var nameTabbed = "tabbed" + this.param.form;
				if (Object.keys(tabsAttributes).length > 1) {
					htmlStr += this.headTabbed(nameTabbed, tabsAttributes);
					htmlStr += this.tabsTabbed(nameTabbed, tabsAttributes);
					htmlStr += this.footTabbed();
				} else {
					var group = Object.keys(tabsAttributes)[0];
					htmlStr += this.fieldsTabbed(tabsAttributes[group]);
				}
				var xmlForm = $.Cmdbuild.elementsManager.getElement(this.param.form);
				var widgets = this.getBackend().widgets;
				var customWidgets = $.Cmdbuild.utilities.getWidgetsFromXML(xmlForm);
				if (customWidgets && customWidgets.length) {
					if (widgets === undefined) {
						widgets = [];
					}
					$.each(customWidgets, function(index, widget) {
						widgets.push(widget);
					});
					this.getBackend().widgets = widgets;
				}
				if (widgets && widgets.length) {
					$.Cmdbuild.widgets.prepareFields(widgets);
					htmlStr += $.Cmdbuild.standard.widgetDiv.getWidgetDiv({
						container: this.param.container,
						form: this.param.form,
						readOnly: this.param.readonly,
						widgets: widgets
					});
					$.Cmdbuild.widgets.evaluateCqlFields(this.param.form, widgets, true);
				}
				htmlStr += this.createAllLookUps();

				// create form buttons
				htmlStr += $.Cmdbuild.utilities.getButtonsFromXML(xmlForm);

				htmlStr += "<br/>";
				$.Cmdbuild.eventsManager.deferEvents();
				htmlStr += $.Cmdbuild.elementsManager.insertChildren(xmlForm);
				var htmlContainer = $("#" + this.param.container)[0];
				htmlContainer.innerHTML = htmlStr;
				if (Object.keys(tabsAttributes).length > 1) {
					$("#" + nameTabbed).tabs();
				}
				$.Cmdbuild.elementsManager.initialize();
				$.Cmdbuild.eventsManager.unDeferEvents();

				if (this.param.onInitComplete) {
					window[this.param.onInitComplete]();
				}

				// add title to disabled input items
				$("#" + this.param.container + " input[disabled]").each(
					function(index, item) {
						$(item).attr("title", $(item).attr('value'));
					});

				if (widgets) {
					$.Cmdbuild.widgets.initialize(this.param.form, widgets);
				}
				var me = this;
				this.TM = setTimeout(function() { me.initializationPhase = false; }, 500);
//				this.initializationPhase = false;
			} catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.form.show");
				throw e;
			}
		};
		this.headTabbed = function(nameTabbed, tabsAttributes) {
			var htmlStr = "";
			htmlStr += "<div id='" + nameTabbed + "'><ul>";
			var index = 1;
			for ( var key in tabsAttributes) {
				if (key == NOGROUP) {
					continue;
				}
				var href = nameTabbed + "-tab-" + index++;
				htmlStr += "<li><a href='#" + href + "'>" + key + "</a></li>";
			}
			htmlStr += "</ul>";
			return htmlStr;
		};
		this.tabsTabbed = function(nameTabbed, tabsAttributes) {
			var htmlStr = "";
			var index = 1;
			for ( var key in tabsAttributes) {
				if (key == NOGROUP) {
					continue;
				}
				var href = nameTabbed + "-tab-" + index++;
				htmlStr += "<div id='" + href + "'>";
				htmlStr += this.fieldsTabbed(tabsAttributes[key]);
				htmlStr += "</div>";
			}
			return htmlStr;
		};
		this.fieldsTabbed = function(fields) {
			var htmlStr = "";
			for (var i = 0; i < fields.length; i++) {
				htmlStr += this.fieldToHtml(fields[i]);
			}
			htmlStr += "<div class='cmdbuildClear'></div>";
			return htmlStr;
		};
		this.footTabbed = function() {
			var htmlStr = "";
			htmlStr += "</div>";
			return htmlStr;
		};
		this.fieldToHtml = function(field) {
			var htmlStr = "";
			if (field.attribute.hidden) {
				return "";
				var name = field.attribute.name;
				var xmlElement = this.createXmlHidden(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				return htmlStr;
			}
			switch (field.attribute.type.toLowerCase()) {
			case "title":
				var name = field.attribute.name;
				var xmlElement = this.createXmlTitle(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "paragraph":
				var name = field.attribute.name;
				var xmlElement = this.createXmlParagraph(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "timestamp":
			case "time":
			case "datetime":
			case "date":
				var name = field.attribute.name;
				var xmlElement = this.createXmlDate(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
//			case "foreignkey":
//				field.attribute.referencedClassName = field.attribute.fkDestination;
			case "reference":
				var name = field.attribute.name;
				var xmlElement = this.createXmlReference(field.form + "_" + name, field.form, field.attribute,
						field.card, this.param.container);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				this.lookUps.push(field.form + "_" + name);
				break;
			case "lookup":
				var name = field.attribute.name;
				var xmlElement = this.createXmlLookup(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "select":
				var name = field.attribute.name;
				var xmlElement = this.createXmlSelect(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "decimal":
				var name = field.attribute.name;
				var xmlElement = this.createXmlDecimal(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "integer":
				var name = field.attribute.name;
				var xmlElement = this.createXmlInteger(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "string":
				var name = field.attribute.name;
				var xmlElement = this.createXmlString(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "text":
				var name = field.attribute.name;
				var xmlElement = this.createXmlText(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "boolean":
				var name = field.attribute.name;
				var xmlElement = this.createXmlBoolean(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			case "double":
				var name = field.attribute.name;
				var xmlElement = this.createXmlDouble(field.form + "_" + name, field.form, field.attribute,
						field.card);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				break;
			default:
				htmlStr += "<p><b>" + field.attribute.type + "</b> not in forms elements" + "</p>";
				break;
			}
			return htmlStr;

		};
		this.interactivityType = function(attribute) {
			var interactivity = undefined;
			if (this.param.readonly == "true") {
				return $.Cmdbuild.global.READ_ONLY;
			} 
			if (!attribute.interactivity) {
				if (attribute.mandatory) {
					attribute.interactivity = $.Cmdbuild.global.READ_WRITE_REQUIRED;
				} else {
					attribute.interactivity = $.Cmdbuild.global.READ_WRITE;
				}
			}
			return attribute.interactivity;
		};
		this.createXmlHidden = function(id, form, attribute, card) {
			var xmlStr = "<input";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " text='" + (card[attribute.name] || "") + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "</input>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlBoolean = function(id, form, attribute, card) {
			var xmlStr = "<checkbox";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " text='" + (card[attribute.name] || "") + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "</checkbox>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlSelect = function(id, form, attribute, card) {
			var xmlStr = "<select ";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
//			xmlStr += " text='" + (card[attribute.name] || "") + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "<params>";
			xmlStr += "<backend>" + attribute.backend + "</backend>";
			xmlStr += "<className>GUIForm</className>";
			xmlStr += "</params>";
			xmlStr += "<onChange>";
			xmlStr += "<command>pippo</command>";
			xmlStr += "</onChange>";
			xmlStr += "</select>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlTitle = function(id, form, attribute, card) {
			var xmlStr = "<h2";
			xmlStr += " text='" + ($.Cmdbuild.utilities.escapeHtml(card[attribute.name]) || "") + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "</h2>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlParagraph = function(id, form, attribute, card) {
			var xmlStr = "<p";
			xmlStr += " text='" + ($.Cmdbuild.utilities.escapeHtml(card[attribute.name]) || "") + "' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "</p>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlInteger = function(id, form, attribute, card) {
			var xmlStr = "<integer";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " formName='" + form + "' ";
			xmlStr += " text='" + ($.Cmdbuild.utilities.escapeHtml(card[attribute.name]) || "") + "' ";
			xmlStr += " class='alignLeft ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "</integer>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlDecimal = function(id, form, attribute, card) {
			var xmlStr = "<input";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " formName='" + form + "' ";
			xmlStr += " text='" + ($.Cmdbuild.utilities.escapeHtml(card[attribute.name]) || "") + "' ";
			xmlStr += " class='alignLeft ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "</input>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlString = function(id, form, attribute, card) {
			var xmlStr = "<input";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " formName='" + form + "' ";
			xmlStr += " text='" + ($.Cmdbuild.utilities.escapeHtml(card[attribute.name]) || "") + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all " + attribute["class"] + "' ";
			xmlStr += " withReturn='true' ";
			if (attribute.length) {
				xmlStr += " maxlength='"+ attribute.length +"' ";
			}
			xmlStr += ">";
			xmlStr += "</input>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlText = function(id, form, attribute, card) {
			var escapedText = $.Cmdbuild.utilities.escapeHtml(card[attribute.name]) || "";
			var isHTML = attribute.editorType == "HTML";

			var xmlStr = "<textarea";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " text='" + escapedText + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " formName='" + form + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all' ";
			xmlStr += " rows='6' ";
			xmlStr += " withReturn='true' ";
			if (attribute.length) {
				xmlStr += " maxlength='"+ attribute.length +"' ";
			}
			xmlStr += ">";
			xmlStr += "<params>";
			if (isHTML) {
				xmlStr += "<isHtml>true</isHtml>";
			}
			xmlStr += "<rows>4</rows>";
			xmlStr += "<rawText>" + escapedText + "</rawText>";
			xmlStr += "</params>";
			xmlStr += "</textarea>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlDate = function(id, form, attribute, card) {
			var xmlStr = "<date";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " text='" + (card[attribute.name] || "") + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " formName='" + form + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "<params><type>" + attribute.type + "</type></params>";
			xmlStr += "</date>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlLookup = function(id, form, attribute, card) {
			var xmlStr = "<lookup";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " formName='" + form + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "<params>";
			xmlStr += "<lookupName>" + attribute.lookupType + "</lookupName>";
			if (card[attribute.name]) {
				xmlStr += "<value>" + card[attribute.name] + "</value>";
			}
			xmlStr += "<backend>Lookup</backend>";
			xmlStr += "</params>";
			xmlStr += "</lookup>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlReference = function(id, form, attribute, card, container) {
			var xmlStr = "<reference";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " formName='" + form + "' ";
			xmlStr += " class='ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "<params>";
			xmlStr += "<className>" + attribute.targetClass + "</className>";
			xmlStr += "<formNRows>" + attribute.formNRows + "</formNRows>";
			if (card[attribute.name]) {
				xmlStr += "<value>" + card[attribute.name] + "</value>";
			}
			xmlStr += "<container>" + container + "</container>";
			if ($.Cmdbuild.dataModel.isAProcess(attribute.targetClass)) {
				xmlStr += "<backend>ReferenceActivityList</backend>";
				xmlStr += "<isProcess>true</isProcess>";
			} else {
				xmlStr += "<backend>CardList</backend>";
				xmlStr += "<isProcess>false</isProcess>";
			}
			xmlStr += "</params>";
			xmlStr += "</reference>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlDialog = function(id) {
			var xmlStr = "<dialog id='" + id + "_lookupDialog'></dialog>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createXmlDouble = function(id, form, attribute, card) {
			var xmlStr = "<input";
			xmlStr += " label='" + attribute.description + "' ";
			xmlStr += " interactivity='" + this.interactivityType(attribute) + "' ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " fieldName='" + attribute.name + "' ";
			xmlStr += " formName='" + form + "' ";
			xmlStr += " text='" + ($.Cmdbuild.utilities.escapeHtml(card[attribute.name]) || "") + "' ";
			xmlStr += " class='alignLeft ui-widget-content ui-corner-all' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
			xmlStr += "</input>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		};
		this.createAllLookUps = function() {
			var htmlStr = "";
			for (var i = 0; i < this.lookUps.length; i++) {
				var xmlElement = this.createXmlDialog(this.lookUps[i]);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
			}
			return htmlStr;
		};
		this.noGroupDefinition = function(attributes) {
			if (! attributes) {
				console.log("Error no attributes on form :" + this.param.form);
				return;
			}
			var thereAreGroups = false;
			for (var i = 0; i < attributes.length; i++) {
				if (attributes[i].group) {
					thereAreGroups = true;
					break;
				}
			}
			if (! thereAreGroups) {
				return;
			}
			for (var i = 0; i < attributes.length; i++) {
				if (! attributes[i].group) {
					attributes[i].group = GROUPOTHERS;
				}
			}
		};
		/*
		 * Getters and Setters
		 */
		/**
		 * Get the backend object used for this form
		 * @return {Object} The backend object
		 */
		this.getBackend = function() {
			return this.backend;
		};
		/**
		 * Set backend object for this form
		 * @param {Object} backend The backend object
		 */
		this.setBackend = function(backend) {
			this.backend = backend;
		};
		/**
		 * Return data from the backend
		 * @return {Object} Backend data
		 */
		this.getBackendData = function() {
			var backend = this.getBackend();
			if (!backend.getData) {
				console.warn("Missing getData method for backend " + this.param.backend);
				return backend.data;
			}
			return backend.getData();
		};
		/**
		 * Set data into the backend
		 * @param {Object} data New data
		 */
		this.setBackendData = function(data) {
			var backend = this.getBackend();
			if (!backend.setData) {
				console.warn("Missing setData method for backend " + this.param.backend);
				backend.data = data;
			} else {
				backend.setData(data);
			}
		};
	};
	$.Cmdbuild.standard.form = form;
})(jQuery);
