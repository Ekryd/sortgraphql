package sortpom;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sortpom.exception.ExceptionConverter;

/**
 * Verifies that the pom.xml is sorted. If the verification fails then the pom.xml is sorted.
 *
 * @author Bjorn Ekryd
 */
@Mojo(name = "verify", threadSafe = true, defaultPhase = LifecyclePhase.VALIDATE)
@SuppressWarnings({"UnusedDeclaration"})
public class VerifyMojo extends AbstractParentMojo {

  public void setup() throws MojoFailureException {
    new ExceptionConverter(() -> {}).executeAndConvertException();
  }

  protected void sortPom() throws MojoFailureException {
    new ExceptionConverter(() -> {}).executeAndConvertException();
  }
}
