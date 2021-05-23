package sortgraphql.logger;

import org.apache.maven.plugin.logging.Log;

/** Facade for the internal plugin logger. Makes it easier to test logging output and isolates the sorter module from plugin internals */
public class MavenLogger implements SortingLogger {
    private final Log pluginLogger;

    public MavenLogger(Log pluginLogger) {
        this.pluginLogger = pluginLogger;
    }

    @Override
    public void warn(String content) {
        pluginLogger.warn(content);
    }

    @Override
    public void info(String content) {
        pluginLogger.info(content);
    }

    @Override
    public void error(String content) {
        pluginLogger.error(content);
    }
}
