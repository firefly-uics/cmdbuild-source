package support;

import static java.lang.String.format;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerResource extends ExternalResource {

	private static final Logger logger = LoggerFactory.getLogger(ServerResource.class);

	public static class Builder implements org.cmdbuild.common.Builder<ServerResource> {

		private Class<?> serviceClass;
		private Object service;
		private int port;

		@Override
		public ServerResource build() {
			return new ServerResource(this);
		}

		public Builder withServiceClass(final Class<?> serviceClass) {
			this.serviceClass = serviceClass;
			return this;
		}

		public Builder withService(final Object service) {
			this.service = service;
			return this;
		}

		public Builder withPort(final int port) {
			this.port = port;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Class<?> serviceClass;
	private final Object service;
	private final int port;

	private Server server;

	private ServerResource(final Builder builder) {
		this.serviceClass = builder.serviceClass;
		this.service = builder.service;
		this.port = builder.port;
	}

	@Override
	protected void before() throws Throwable {
		super.before();

		final JAXRSServerFactoryBean serverFactory = new JAXRSServerFactoryBean();
		serverFactory.setResourceClasses(serviceClass);
		serverFactory.setResourceProvider(serviceClass, new SingletonResourceProvider(service));
		serverFactory.setAddress(format("http://localhost:%d/", port));
		serverFactory.setProvider(new JacksonJaxbJsonProvider());

		server = serverFactory.create();
		logger.info("server ready...");
	}

	@Override
	protected void after() {
		logger.info("server exiting...");
		server.destroy();
	}

}
