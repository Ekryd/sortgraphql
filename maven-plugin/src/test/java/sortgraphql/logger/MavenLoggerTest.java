package sortgraphql.logger;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class MavenLoggerTest {
    private final Log logMock = mock(Log.class);
    private MavenLogger mavenLogger;

    @BeforeEach
    void setUp() {
        mavenLogger = new MavenLogger(logMock);
    }

    @Test
    void warnShouldOutputWarnLevel() {
        mavenLogger.warn("Gurka");

        verify(logMock).warn("Gurka");
        verifyNoMoreInteractions(logMock);
    }

    @Test
    void infoShouldOutputInfoLevel() {
        mavenLogger.info("Gurka");

        verify(logMock).info("Gurka");
        verifyNoMoreInteractions(logMock);
    }

    @Test
    void errorShouldOutputErrorLevel() {
        mavenLogger.error("Gurka");

        verify(logMock).error("Gurka");
        verifyNoMoreInteractions(logMock);
    }
}
