package sortgraphql.sort;

import static java.util.Optional.ofNullable;

import graphql.language.Comment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DescriptionAndComments {
  private List<String> comments = List.of();
  private String description;

  public void comments(List<Comment> commentList) {
    if (commentList == null) {
      return;
    }
    this.comments =
        commentList.stream()
            .map(Comment::getContent)
            .filter(Objects::nonNull)
            .filter(c -> !c.isBlank())
            .toList();
  }

  public void description(String description) {
    this.description = description;
  }

  public boolean isNullOrEmpty() {
    return (comments == null || comments.isEmpty())
        && (description == null || description.isEmpty());
  }

  public List<String> getComments() {
    return comments;
  }

  public Optional<String> getDescription() {
    return ofNullable(description);
  }
}
