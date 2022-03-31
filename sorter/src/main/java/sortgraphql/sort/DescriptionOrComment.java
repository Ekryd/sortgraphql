package sortgraphql.sort;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DescriptionOrComment {
  private final String comment;
  private final String description;

  private DescriptionOrComment(String comment, String description) {
    this.comment = comment;
    this.description = description;
  }

  public static DescriptionOrComment comment(String comment) {
    return new DescriptionOrComment(comment, null);
  }

  public static DescriptionOrComment description(String description) {
    return new DescriptionOrComment(null, description);
  }
  
  public boolean isNullOrEmpty() {
    return (comment == null || comment.isEmpty()) && (description == null || description.isEmpty());
  }
  
  public boolean isDescription() {
    if (isNullOrEmpty()) {
      throw new IllegalStateException("Both comment and description is null");
    }
    return comment == null;
  }

  public String getComment() {
    return comment;
  }

  public String getDescription() {
    return description;
  }

  public static List<String> lines(Supplier<String> fn) {
    return Arrays.asList(fn.get().split("\n"));
  }
}
