package sortpom;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sortpom.exception.ExceptionConverter;

/**
 * Sorts the pom.xml for a Maven project.
 *
 * @author Bjorn Ekryd
 */
@Mojo(name = "sort", threadSafe = true, defaultPhase = LifecyclePhase.VALIDATE)
@SuppressWarnings({"UnusedDeclaration"})
public class SortMojo extends AbstractParentMojo {

  public void setup() throws MojoFailureException {
    new ExceptionConverter(() -> {}).executeAndConvertException();
  }

  protected void sortPom() throws MojoFailureException {
    new ExceptionConverter(() -> {}).executeAndConvertException();
  }
}
