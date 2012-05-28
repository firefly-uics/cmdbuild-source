package utils.async;

import org.hamcrest.StringDescription;

public class Poller {
    private long timeoutMillis;
    private long pollDelayMillis;

    public Poller(long timeoutMillis, long pollDelayMillis) {
        this.timeoutMillis = timeoutMillis;
        this.pollDelayMillis = pollDelayMillis;
    }

    public long getTimeout() {
        return timeoutMillis;
    }

    public void setTimeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public long getPollDelay() {
        return pollDelayMillis;
    }

    public void setPollDelay(long pollDelayMillis) {
        this.pollDelayMillis = pollDelayMillis;
    }

    public void check(Probe probe) throws InterruptedException {
        Timeout timeout = new Timeout(timeoutMillis);

        while (!probe.isDone()) {
            if (timeout.hasTimedOut()) {
                break;
            }
            Thread.sleep(pollDelayMillis);
            probe.sample();
        }
        if (!probe.isSatisfied()) {
        	throw new AssertionError(describeFailureOf(probe));
        }
    }

    protected String describeFailureOf(Probe probe) {
        StringDescription description = new StringDescription();

        description.appendText("\nTried to look for:\n    ");
        probe.describeAcceptanceCriteriaTo(description);
        description.appendText("\nbut:\n    ");
        probe.describeFailureTo(description);

        return description.toString();
    }
}