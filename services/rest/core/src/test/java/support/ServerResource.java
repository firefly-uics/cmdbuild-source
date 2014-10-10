package support;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Random;

import org.apache.commons.lang3.Range;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerResource extends ExternalResource {

	private static final Logger logger = LoggerFactory.getLogger(ServerResource.class);

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ServerResource> {

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

	private static final Random random = new Random();

	public static final int DEFAULT_RANGE_MIN_INCLUSIVE = 1024;
	public static final int DEFAULT_RANGE_MAX_INCLUSIVE = 65535;
	public static final Range<Integer> DEFAULT_RANGE = Range.between(DEFAULT_RANGE_MIN_INCLUSIVE,
			DEFAULT_RANGE_MAX_INCLUSIVE);

	public static final JacksonJaxbJsonProvider JSON_PROVIDER = new JacksonJaxbJsonProvider();

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
		serverFactory.setAddress(address());
		serverFactory.setProvider(JSON_PROVIDER);

		server = serverFactory.create();
		logger.info(format("server ready on port %d ...", port));
	}

	@Override
	protected void after() {
		logger.info("server exiting...");
		server.destroy();
	}

	public String address() {
		return resource(EMPTY);
	}

	public String resource(final String resource) {
		return format("http://localhost:%d/%s", port, resource);
	}

	public static int randomPort() {
		return randomPort(DEFAULT_RANGE);
	}

	public static int randomPort(final Range<Integer> range) {
		return random.nextInt(range.getMaximum() - range.getMinimum()) + range.getMinimum();
	}

}
