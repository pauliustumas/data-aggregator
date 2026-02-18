package ai.foodscan.aggregate.db.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class TestLogAppender extends ListAppender<ILoggingEvent> {

    public void reset() {
        this.list.clear();
    }
}