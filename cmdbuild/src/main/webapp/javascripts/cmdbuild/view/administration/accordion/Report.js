(function() {

	Ext.define('CMDBuild.view.administration.accordion.Report', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		cmName: 'report',
		title: CMDBuild.Translation.administration.modreport.title,

		/**
		 * @param {CMDBuild.model.Report} report
		 *
		 * @return {Object} nodeConf
		 */
		buildNodeConf: function(report) {
			var nodeConf = report.getData();
			nodeConf['cmName'] = this.cmName;
			nodeConf['leaf'] = true;

			return nodeConf;
		},

		/**
		 * @return {Array} nodes
		 */
		buildTreeStructure: function() {
			var nodes = [];
			var reports = _CMCache.getReports();

			for (var key in reports)
				nodes.push(this.buildNodeConf(reports[key]));

			return nodes;

		}
	});

})();