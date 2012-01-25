(function() {
	Ext.define("CMDBuild.controller.management.workflow.widgets.CMCalendarControllerWidgetReader", {
		getStartDate: function(w) { return w.EventStartDate; },
		getEndDate: function(w) { return w.EventEndDate; },
		getTitle: function(w) { return w.EventTitle; },
		getTargetName: function(w) { return w.ClassName; },
		getFilterVarName: function() { return "xa:Filter"; },
		getDefaultDate: function(w) { return w.DefaultDate; }
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMCalendarControllerWidgetReader", {
		getStartDate: function(w) { return w.startDate; },
		getEndDate: function(w) { return w.endDate; },
		getTitle: function(w) { return w.eventTitle; },
		getTargetName: function(w) { return w.targetClass; },
		getFilterVarName: function() {return "xa:filter"},
		getDefaultDate: function(w) { return w.defaultDate; }
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMCalendarController", {

		mixins: {
			observable: "Ext.util.Observable"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMCalendar.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, reader, card) {
			// this.widgetConf = c.widget;
			// this.activity = c.activity.raw || c.activity.data;

			this.WIDGET_NAME = this.self.WIDGET_NAME;

			this.view = view;
			this.ownerController = ownerController;
			this.widget = widgetDef;
			this.clientForm = clientForm;
			this.reader = reader;
			this.card = card;

			if (!this.reader.getStartDate(this.widget) ||
					!this.reader.getTitle(this.widget)) {

				CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
						CMDBuild.Translation.management.modworkflow.extattrs.calendar.wrong_config);

				this.skipLoading = true;
				return;
			} else {
				this.eventMapping = {
					id: "Id",
					start: this.reader.getStartDate(this.widget),
					end: this.reader.getEndDate(this.widget),
					title: this.reader.getTitle(this.widget)
				};
			}

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.clientForm,
				xaVars: this.widget,
				serverVars: this.card.raw || this.card.data
			});

			this.mon(this.view, "eventclick", onEventClick, this);
			this.mon(this.view, "viewchange", onViewChange, this);
		},

		// override
		beforeActiveView: function() {
			this.view.clearStore();

			openDefaultDate(this);

			if (this.skipLoading) {
				return;
			}

			var me = this,
				cqlQuery = this.templateResolver.getVariable(me.reader.getFilterVarName());

			if (cqlQuery) {
				this.filteredWithCQL = true;
				this.templateResolver.resolveTemplates({
					attributes: [me.reader.getFilterVarName()],
					scope: me.view,
					callback: function(out, ctx) {
						var filterParams = me.templateResolver.buildCQLQueryParameters(cqlQuery, ctx);
						doRequest(me, filterParams);
					}
				});
			} else {
				this.filteredWithCQL = false;
				me.updatePaginationQuery();
				doRequest(me);
			}
		},

		updatePaginationQuery: function() {
			var me = this,
				viewBounds = this.view.getWievBounds(),
				className = me.reader.getTargetName(me.widget);

			var out = "SELECT " +
				me.eventMapping.id + "," +
				me.eventMapping.title + "," +
				me.eventMapping.start + ",";

			if (me.eventMapping.end) {
				out += me.eventMapping.end;
			}

			out += " FROM " + className +
				" WHERE " + me.eventMapping.start + " >= " +
				"\"" + getCMDBuildDateStringFromDateObject(viewBounds.viewStart) + "\"" +
				" AND " + me.eventMapping.start + " <= " +
				"\"" + getCMDBuildDateStringFromDateObject(viewBounds.viewEnd) + "\"";

			this.paginationQuery = out;
		},

		destroy: function() {
			this.mun(this.view, "eventclick", onEventClick, this);
			this.mun(this.view, "viewchange", onViewChange, this);
		},

		isBusy: function() {
			return false;
		},

		isValid: function() {
			return true;
		}
	});

	function openDefaultDate(me) {
		var defaultDateAttr = me.reader.getDefaultDate(me.widget);
		if (defaultDateAttr) {
			var defaultDate = me.templateResolver.getVariable("client:" + defaultDateAttr);
			var date = buildDate(defaultDate);
			if (date) {
				me.view.setStartDate(date);
			}
		}
	}

	// my expectation is a string in the form:
	// d/m/y or d/m/y H:i:s
	// the Date object accept a string in the format m/d/y or m/d/y H:i:s
	// so invert the d with m and return a Date object
	function buildDate(stringDate) {
		if (stringDate) {
			var chunks = stringDate.split(" ");
			var dateChunks = chunks[0].split("/");
			var out = dateChunks[1] + "/" + dateChunks[0] + "/" + dateChunks[2] + " ";
			if (chunks[1]) {
				out += chunks[1];
			} else {
				out += "00:00:00";
			}
	
			return new Date(out);
		} else {
			return new Date();
		}
	}

	function getCMDBuildDateStringFromDateObject(d) {
		return d.getDate() + "/" + d.getMonth() + "/" + d.getFullYear();
	}

	function doRequest(me, filterParams) {
		var params = filterParams || {};

		if (!filterParams) {
			params.CQL = me.paginationQuery;
		}

		CMDBuild.ServiceProxy.getCardList({
			params: params,
			success: function(response, operation, decodedResponse) {
				me.view.clearStore();
				var _eventData = decodedResponse.rows || [];
				for (var i=0, l=_eventData.length; i<l; ++i) {
					var eventConf = {},
					rawEvent = _eventData[i],
					calMapping = Extensible.calendar.data.EventMappings;

					eventConf[calMapping.EventId.name] = rawEvent[me.eventMapping.id];
					eventConf[calMapping.StartDate.name] = buildDate(rawEvent[me.eventMapping.start]);
					eventConf[calMapping.Title.name] = rawEvent[me.eventMapping.title];

					if (me.eventMapping.end) {
						eventConf[calMapping.EndDate.name] = buildDate(rawEvent[me.eventMapping.end]);
					} else {
						eventConf[calMapping.EndDate.name] = buildDate(rawEvent[me.eventMapping.start]);
					}

					var event = new Extensible.calendar.data.EventModel(eventConf);
					if (event) {
						me.view.addEvent(event);
					}
				}
			}
		});
	}

	function onEventClick(panel, model, el) {
		var me = this,
			target = _CMCache.getEntryTypeByName(me.reader.getTargetName(me.widget));

		if (target) {
			var w = new CMDBuild.view.management.common.CMCardWindow({
				cmEditMode: false,
				withButtons: false,
				title: model.get("Title")
			});
	
			new CMDBuild.controller.management.common.CMCardWindowController(w, {
				entryType: target.get("id"), // classid of the destination
				card: model.get("EventId"), // id of the card destination
				cmEditMode: false
			});
			w.show();
		}
	}

	function onViewChange() {
		if (this.filteredWithCQL) {
			return;
		} else {
			this.updatePaginationQuery();
			doRequest(this);
		}
	}

})();