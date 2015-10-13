(function() {

	Ext.define('CMDBuild.view.management.accordion.Reports', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.constants.Proxy'],

		title: CMDBuild.Translation.report,

		/**
		 * @param {CMDBuild.model.report.Cache} report
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