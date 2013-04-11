package org.cmdbuild.services.soap.operation;

import java.util.Map;

import org.cmdbuild.elements.report.ReportFactory;

public interface ReportFactoryBuilder<T extends ReportFactory> {

	ReportFactoryBuilder<T> withExtension(String extension);

	ReportFactoryBuilder<T> withProperties(Map<String, String> properties);

	T build();

}
