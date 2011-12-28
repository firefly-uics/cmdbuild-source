package org.cmdbuild.elements.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.cmdbuild.common.utils.OS;
import org.cmdbuild.utils.template.ParameterMapEngine;
import org.cmdbuild.utils.template.TemplateResolver;

public class Ping extends Widget {

	private static final String PING_COMMAND_TEMPLATE = OS.isWindows() ? "ping -n %d %s" : "ping -c %d %s";
	private static final long TIMEOUT_MS = 30000L;

	private String address;
	private int count;

	public void setAddress(final String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	protected long getMillisTimeout() {
		return TIMEOUT_MS;
	}

	@Override
	protected WidgetAction getActionCommand(final String action, final Map<String, Object> dsVars) {
		final TemplateResolver tr = TemplateResolver.newInstanceBuilder() //
				.withEngine("ds", new ParameterMapEngine(dsVars)) //
				.build();
		final String resolvedAddress = tr.simpleEval(getAddress());
		final String command = String.format(PING_COMMAND_TEMPLATE, getCount(), resolvedAddress);
		return new WidgetAction() {

			@Override
			public String execute() throws Exception {
				final ExecutorService execService = Executors.newSingleThreadExecutor();
				final Future<String> future = execService.submit(new Callable<String>() {
					@Override
					public String call() throws Exception {
						final Process proc = Runtime.getRuntime().exec(command);
						proc.waitFor();
						return stdOutAsString(proc);
					}
				});
				try {
					return future.get(getMillisTimeout(), TimeUnit.MILLISECONDS);
				} catch (ExecutionException e) {
					throw e;
				} catch (TimeoutException e) {
					execService.shutdownNow();
					throw e;
				}
			}

			private String stdOutAsString(Process proc) throws IOException {
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				StringBuilder buffer = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					buffer.append(line).append("\n");
				}
				return buffer.toString();
			}
		};
	}

}
