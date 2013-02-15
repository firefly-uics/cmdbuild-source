(function() {
	var POST = "POST";
	var GET = "GET";
	var url = _CMProxy.url.dataView;

	CMDBuild.ServiceProxy.dataView = {

		sql: {
			/**
			 * Retrieves the SQL view stored
			 * 
			 * @param {object} config
			 */
			read: function(config) {
				config.url = url.sql.create;
				config.method = GET;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Create a new SQL view
			 * 
			 * @param {object} config
			 * @param {string} config.name The name of the new View
			 * @param {string} config.description The description of the new View
			 * @param {string} config.functionName The name of the SQL function to 
			 * use as data store
			 */ 
			create: function(config) {
				config.url = url.sql.create;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Update a stored SQL view
			 * 
			 * @param config
			 * @param {string} config.name The name of the view to update
			 * @param {string} config.description The new description
			 * @param {string} config.functionName The name of the new SQL function
			 */
			update: function(config) {
				config.url = url.sql.update;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Delete a stored SQL view
			 * 
			 * @param config
			 * @param {string} config.name The name of the view to remove
			 */
			remove: function(config) {
				config.url = url.sql.remove;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			}
		},

		filter: {
			/**
			 * Retrieves the Filter view stored
			 * 
			 * @param {object} config
			 */
			read: function(config) {
				config.url = url.sql.remove;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Create a new Filter view
			 * 
			 * @param {object} config
			 * @param {string} config.name The name of the new View
			 * @param {string} config.description The description of the new View
			 * @param {string} config.className The name of the target class
			 * @param {object} config.filter The Filter configuration to
			 * use for the new view
			 */
			create: function(config) {
				config.url = url.sql.remove;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Update a Filter view
			 * 
			 * @param {object} config
			 * @param {string} config.name The name of the View to update
			 * @param {string} config.description The new description
			 * @param {string} config.className The new origin class
			 * @param {object} config.filter The new filter
			 */
			update: function(config) {
				config.url = url.sql.remove;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Delete a stored Filter view
			 * 
			 * @param config
			 * @param {string} config.name The name of the view to remove
			 */
			remove: function(config) {
				config.url = url.sql.remove;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			}
		}
	};
})();