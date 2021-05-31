package sortgraphql.logger;

public interface SortingLogger {
  /**
   * Send a message to the log in the <b>warn</b> error level.
   *
   * @param content warning message
   */
  void warn(String content);

  /**
   * Send a message to the log in the <b>info</b> error level.
   *
   * @param content info message
   */
  void info(String content);

  /**
   * Send a message to the log in the <b>error</b> error level.
   *
   * @param content error message
   */
  void error(String content);
}
