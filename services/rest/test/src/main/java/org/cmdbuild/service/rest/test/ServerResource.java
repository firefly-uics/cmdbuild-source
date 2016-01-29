package org.cmdbuild.service.rest.test;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

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

		private static final int DEFAULT_RETRIES = 3;

		private Class<?> serviceClass;
		private Object service;
		private Range<Integer> portRange;
		private Integer retries;

		@Override
		public ServerResource build() {
			validate();
			return new ServerResource(this);
		}

		private void validate() {
			retries = defaultIfNull(retries, DEFAULT_RETRIES);
		}

		public Builder withServiceClass(final Class<?> serviceClass) {
			this.serviceClass = serviceClass;
			return this;
		}

		public Builder withService(final Object service) {
			this.service = service;
			return this;
		}

		/**
		 * @deprecated use {@link withPortRange(Range)} instead.
		 */
		@Deprecated
		public Builder withPort(final Range<Integer> range) {
			return withPortRange(range);
		}

		public Builder withPortRange(final Range<Integer> range) {
			this.portRange = range;
			return this;
		}

		public Builder setRetries(final int retries) {
			this.retries = retries;
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
	private final Range<Integer> portRange;
	private final int retries;

	private String address;
	private Server server;

	private ServerResource(final Builder builder) {
		this.serviceClass = builder.serviceClass;
		this.service = builder.service;
		this.portRange = builder.portRange;
		this.retries = builder.retries;
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		boolean started = false;
		for (int count = 0; (count < retries) && !started; count++) {
			try {
				logger.info("server starting...");
				final Integer _port = random.nextInt(portRange.getMaximum() - portRange.getMinimum())
						+ portRange.getMinimum();
				address = format("http://localhost:%d", _port);
				final JAXRSServerFactoryBean serverFactory = new JAXRSServerFactoryBean();
				serverFactory.setResourceClasses(serviceClass);
				serverFactory.setResourceProvider(serviceClass, new SingletonResourceProvider(service));
				serverFactory.setAddress(address);
				serverFactory.setProvider(JSON_PROVIDER);

				server = serverFactory.create();
				logger.info(format("server ready at ", address));
				started = true;
			} catch (final Exception e) {
				logger.warn("error starting server", e);
			}
		}
		if (!started) {
			throw new RuntimeException("server cannot be started");
		}
	}

	@Override
	protected void after() {
		logger.info("server exiting...");
		server.destroy();
	}

	public String resource(final String resource) {
		return format("%s/%s", address, resource);
	}

	public static Range<Integer> randomPort() {
		return randomPort(DEFAULT_RANGE);
	}

	public static Range<Integer> randomPort(final Range<Integer> range) {
		return range;
	}

}
