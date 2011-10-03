(function() {
	var FILTER = "xa:Filter",
		CLASS_ID = "xa:ClassId";

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMCalendarController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",
		cmName: "Calendar",

		mixins: {
			observable: "Ext.util.Observable"
		},

		constructor: function() {
			this.callParent(arguments);

			if (!this.widgetConf.EventStartDate ||
					!this.widgetConf.EventTitle) {

				CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
						CMDBuild.Translation.management.modworkflow.extattrs.calendar.wrong_config);

				this.skipLoading = true;
				return;
			} else {
				this.eventMapping = {
					id: "Id",
					start: this.widgetConf.EventStartDate,
					end: this.widgetConf.EventEndDate,
					title: this.widgetConf.EventTitle
				};
			}

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.view.clientForm,
				xaVars: this.view.widgetConf,
				serverVars: this.view.activity
			});

			this.mon(this.view, "eventclick", function(panel, model, el) {
				var w = new CMDBuild.view.management.common.CMCardWindow({
					cmEditMode: false,
					withButtons: false,
					classId: this.widgetConf.ClassId,
					cardId: model.get("EventId"), // id of the card destination
					title: model.get("Title")
				});
				w.show();

			}, this);

			this.mon(this.view, "viewchange", function(info) {
				if (this.filteredWithCQL) {
					return;
				} else {
					this.updatePaginationQuery();
					doRequest.call(this, this.widgetConf.ClassId);
				}
			}, this);
		},

		// override
		beforeActiveView: function() {
			this.view.clearStore();

			if (this.skipLoading) {
				return;
			}

			var me = this,
				classId = this.templateResolver.getVariable(CLASS_ID),
				cqlQuery = this.templateResolver.getVariable(FILTER);

			if (cqlQuery) {
				this.filteredWithCQL = true;
				this.templateResolver.resolveTemplates({
					attributes: [FILTER],
					scope: me.view,
					callback: function(out, ctx) {
						var filterParams = me.templateResolver.buildCQLQueryParameters(cqlQuery, ctx);
						doRequest.call(me, classId, filterParams);
					}
				});
			} else {
				this.filteredWithCQL = false;
				me.updatePaginationQuery();
				doRequest.call(me, classId);
			}
		},

		updatePaginationQuery: function() {
			var me = this,
				viewBounds = this.view.getWievBounds(),
				className = this.widgetConf.ClassName;

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
		}
	});

	// my expectation is a string in the form:
	// d/m/y or d/m/y H:i:s
	// the Date object accept a string in the format m/d/y or m/d/y H:i:s
	// so invert the d with m and return a Date object
	function buildDate(stringDate) {
		var chunks = stringDate.split(" ");
		var dateChunks = chunks[0].split("/");

		var out = dateChunks[1] + "/" + dateChunks[0] + "/" + dateChunks[2] + " ";
		if (chunks[1]) {
			out += chunks[1];
		} else {
			out += "00:00:00";
		}

		return new Date(out);
	}

	function getCMDBuildDateStringFromDateObject(d) {
		return d.getDate() + "/" + d.getMonth() + "/" + d.getFullYear();
	}

	function doRequest(idClass, filterParams) {
		var params = filterParams || {},
			me = this;

		params.IdClass = idClass;
		if (!filterParams) {
			params.CQL = this.paginationQuery;
		}

		CMDBuild.ServiceProxy.getCardList({
			params: params,
			success: function(response, operation, decodedResponse) {
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
})();