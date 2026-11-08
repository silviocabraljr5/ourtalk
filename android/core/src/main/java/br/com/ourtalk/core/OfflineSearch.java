package br.com.ourtalk.core;
import java.text.Normalizer;
import java.util.*;
public final class OfflineSearch {
 private static final Set<String> STOP = new HashSet<>(Arrays.asList("a","o","as","os","de","do","da","dos","das","e","em","um","uma","como","para","que"));
 private static String fold(String text){return Normalizer.normalize(text,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);}
 public static List<Guide> find(List<Guide> guides,String query){
  if(query==null||query.length()>160)throw new IllegalArgumentException("invalid_query");
  String normalized=fold(query).trim();
  List<String> terms=Arrays.stream(normalized.split("[^a-z0-9]+" )).filter(t->!t.trim().isEmpty()&&!STOP.contains(t)).distinct().collect(java.util.stream.Collectors.toList());
  if(terms.isEmpty())return Collections.emptyList();
  return guides.stream().filter(g->{String text=fold(g.title+" "+g.category+" "+g.body);return terms.stream().allMatch(text::contains);})
   .sorted(Comparator.<Guide>comparingInt(g->{String title=fold(g.title);return (title.contains(normalized)?100:0)+(int)terms.stream().filter(title::contains).count()*10;}).reversed().thenComparing(g->g.id)).collect(java.util.stream.Collectors.toList());
 }
}
