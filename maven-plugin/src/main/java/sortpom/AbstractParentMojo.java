package sortpom;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Common parent for both SortMojo and VerifyMojo
 */
abstract class AbstractParentMojo extends AbstractMojo {

    /**
     * Set this to 'true' to bypass sortpom plugin
     */
    @Parameter(property = "sort.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Execute plugin.
     *
     * @throws org.apache.maven.plugin.MojoFailureException exception that will be handled by plugin framework
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoFailureException {
        if (skip) {
            getLog().info("Skipping Sortpom");
        } else {
            setup();
            sortPom();
        }

    }

    protected abstract void sortPom() throws MojoFailureException;

    protected abstract void setup() throws MojoFailureException;
}
