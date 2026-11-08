package br.com.ourtalk.core;
import java.util.Objects;
public final class Guide {
 public final String id,title,category,body,source,sourceUrl,reviewedAt;
 public Guide(String id,String title,String category,String body,String source,String sourceUrl,String reviewedAt){
  this.id=Objects.requireNonNull(id);this.title=Objects.requireNonNull(title);this.category=Objects.requireNonNull(category);this.body=Objects.requireNonNull(body);this.source=Objects.requireNonNull(source);this.sourceUrl=Objects.requireNonNull(sourceUrl);this.reviewedAt=Objects.requireNonNull(reviewedAt);
  if(id.trim().isEmpty()||title.trim().isEmpty()||body.trim().isEmpty())throw new IllegalArgumentException("empty_guide");
 }
}
