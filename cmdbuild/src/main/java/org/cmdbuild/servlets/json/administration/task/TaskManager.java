package org.cmdbuild.servlets.json.administration.task;

import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.ComunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.SORT;
import static org.cmdbuild.servlets.json.ComunicationConstants.START;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskManager extends JSONBaseWithSpringContext {
	private class Task {
		private long id;
		private String type;
		private Boolean active;
		private String last;//date "dd/mm/yyyy hh:mm";
		private String next;//date "dd/mm/yyyy hh:mm";
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public Boolean getActive() {
			return active;
		}
		public void setActive(Boolean active) {
			this.active = active;
		}
		public String getLast() {
			return last;
		}
		public void setLast(String last) {
			this.last = last;
		}
		public String getNext() {
			return next;
		}
		public void setNext(String next) {
			this.next = next;
		}
	}
	private Task[] all = new Task[30];
	private Task[] emails = new Task[10];
	private Task[] events = new Task[10];
	private Task[] workflow = new Task[10];

	@JSONExported
	public JsonResponse getTasksList( //
			final JSONObject serializer, //
			@Parameter(value = TYPE) final String type, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = SORT, required = false) final JSONArray sorters
	) throws JSONException, CMDBException {

		return getTaskByType(type);
	}
	public JsonResponse getTaskByType(String type) {
		String date = "08/01/2014 08:00";
		for (int i = 0; i < 10; i++) {
			Task task = new Task();
			task.setId((long)i);
			task.setType("email");
			task.setActive(true);
			task.setLast(date);
			task.setNext(date);
			emails[i] = task;
		}
		for (int i = 0; i < 10; i++) {
			Task task = new Task();
			task.setId((long)i);
			task.setType("event");
			task.setActive(true);
			task.setLast(date);
			task.setNext(date);
			events[i] = task;
		}
		for (int i = 0; i < 10; i++) {
			Task task = new Task();
			task.setId((long)i);
			task.setType("workflow");
			task.setActive(true);
			task.setLast(date);
			task.setNext(date);
			workflow[i] = task;
		}
		for (int i = 0; i < 10; i++) {
			Task task = new Task();
			task.setId((long)i);
			task.setType("email");
			task.setActive(true);
			task.setLast(date);
			task.setNext(date);
			all[i] = task;
		}
		for (int i = 10; i < 20; i++) {
			Task task = new Task();
			task.setId((long)i);
			task.setType("event");
			task.setActive(true);
			task.setLast(date);
			task.setNext(date);
			all[i] = task;
		}
		for (int i = 20; i < 30; i++) {
			Task task = new Task();
			task.setId((long)i);
			task.setType("workflow");
			task.setActive(true);
			task.setLast(date);
			task.setNext(date);
			all[i] = task;
		}
		try {
			if (type.compareTo("all") == 0) {
				return JsonResponse.success(all);
			}
			else if (type.compareTo("email") == 0) {
				return JsonResponse.success(emails);
			}
			else if (type.compareTo("event") == 0) {
				return JsonResponse.success(events);
			}
			else if (type.compareTo("workflow") == 0) {
				return JsonResponse.success(workflow);
			}
		}
		catch (Exception e) {

		}
		return null;
	}

}
