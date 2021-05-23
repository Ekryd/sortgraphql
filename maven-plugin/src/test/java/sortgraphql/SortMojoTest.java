package sortgraphql;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import refutils.ReflectionHelper;
import sortgraphql.exception.FailureException;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SortMojoTest {
    private final SorterImpl sorter = mock(SorterImpl.class);
    private final Log log = mock(Log.class);
    private SortMojo sortMojo;

    @BeforeEach
    void setup() {
        sortMojo = new SortMojo();
        ReflectionHelper mojoHelper = new ReflectionHelper(sortMojo);
        mojoHelper.setField(sorter);
    }

    @Test
    void executeShouldStartMojo() throws Exception {
        sortMojo.execute();

        verify(sorter).setup(any(SortingLogger.class), any(PluginParameters.class));
        verify(sorter).sortSchema();
        verifyNoMoreInteractions(sorter);
    }

    @Test
    void thrownExceptionShouldBeConvertedToMojoException() {
        doThrow(new FailureException("Gurka")).when(sorter).sortSchema();

        final Executable testMethod = () -> sortMojo.execute();

        final MojoFailureException thrown = assertThrows(MojoFailureException.class, testMethod);

        assertThat("Unexpected message", thrown.getMessage(), is(equalTo("Gurka")));
    }

    @Test
    void thrownExceptionShouldBeConvertedToMojoExceptionInSetup() {
        doThrow(new FailureException("Gurka")).when(sorter).setup(any(SortingLogger.class), any(PluginParameters.class));

        final Executable testMethod = () -> sortMojo.setup();

        final MojoFailureException thrown = assertThrows(MojoFailureException.class, testMethod);

        assertThat("Unexpected message", thrown.getMessage(), is(equalTo("Gurka")));
    }

    @Test
    void skipParameterShouldSkipExecution() throws Exception {
        new ReflectionHelper(sortMojo).setField("skip", true);
        new ReflectionHelper(sortMojo).setField(log);

        sortMojo.execute();

        verify(log).info("Skipping SortGraphQL");
        verifyNoMoreInteractions(sorter);
    }
}
