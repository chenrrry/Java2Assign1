import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class MovieAnalyzer {
  ArrayList<Movie> listOfMovie = new ArrayList<>();

  public MovieAnalyzer(String dataset_path) throws IOException {
    File inputfile = new File(dataset_path);
    Scanner in = new Scanner(inputfile, StandardCharsets.UTF_8);
    in.nextLine();
    while (in.hasNext()) {
      String stringA = in.nextLine();
      Movie movieA = new Movie(stringA);
      listOfMovie.add(movieA);
    }
  }

  public Map<Integer, Integer> getMovieCountByYear() {
    Map<Integer, List<Movie>> map1 = listOfMovie
            .stream().collect(Collectors.groupingBy(Movie::get_Released_Year));
    Set<Map.Entry<Integer, List<Movie>>> entries1 = map1.entrySet();
    Map<Integer, Integer> map2 = new TreeMap<>((Comparator<Object>)
            (o1, o2) -> -((Integer) o1).compareTo((Integer) o2));
    for (Map.Entry<Integer, List<Movie>> entry : entries1) {
      map2.put(entry.getKey(), entry.getValue().size());
    }
    return map2;
  }

  public Map<String, Integer> getMovieCountByGenre() {
    Map<String, List<Movie>> map1 = listOfMovie
            .stream().collect(Collectors.groupingBy(Movie::get_Genre));
    Set<Map.Entry<String, List<Movie>>> entries1 = map1.entrySet();
    Map<String, Integer> map2 = new TreeMap<>(Comparator.comparing(o -> o));
    for (Map.Entry<String, List<Movie>> entry : entries1) {
      String[] genre = entry.getKey().split(",");
      Integer number = entry.getValue().size();
      Integer count;
      for (String e : genre) {
        e = e.trim();
        if ((count = map2.get(e)) == null) {
          map2.put(e, number);
        } else {
          map2.put(e, number + count);
        }
      }
    }
    List<Map.Entry<String, Integer>> list = new ArrayList<>(map2.entrySet());
    list.sort((o1, o2) -> (o2.getValue() - o1.getValue()));
    Map<String, Integer> map3 = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> entry : list) {
      map3.put(entry.getKey(), entry.getValue());
    }
    return map3;
  }

  public Map<List<String>, Integer> getCoStarCount() {
    List<Movie> list = new ArrayList<>(listOfMovie);
    Map<List<String>, Integer> map1 = new HashMap<>();
    for (Movie movie : list) {
      List<String> stars = Arrays.asList(movie.get_stars());
      Collections.sort(stars);
      Integer count;
      for (int x = 0; x < 3; x++) {
        for (int y = x + 1; y < 4; y++) {
          List<String> a = new ArrayList<>();
          a.add(stars.get(x).trim());
          a.add(stars.get(y).trim());
          if (stars.get(y).trim().equals("") || stars.get(x).trim().equals("")) {
            continue;
          }
          if ((count = map1.get(a)) == null) {
            map1.put(a, 1);
          } else {
            map1.put(a, 1 + count);
          }
        }
      }
    }
    List<Map.Entry<List<String>, Integer>> list1 = new ArrayList<>(map1.entrySet());
    Collections.sort(list1, new Comparator<Map.Entry>() {
      public int compare(Map.Entry o1, Map.Entry o2) {
        if (o1.getValue() == o2.getValue()) {
          return (o1.getKey().toString()).compareTo(o2.getKey().toString());
        } else {
          return -(int) o1.getValue() + (int) o2.getValue();
        }
      }
    });
    Map<List<String>, Integer> map3 = new LinkedHashMap<>();
    for (Map.Entry<List<String>, Integer> entry : list1) {
      map3.put(entry.getKey(), entry.getValue());
    }
    return map3;
  }

  public List<String> getTopMovies(int top_k, String by) {
    List<Movie> list = new ArrayList<>(listOfMovie);
    if (by.equals("runtime")) {
      list = list.stream()
              .filter(l -> !l.get_runtime().equals(""))
              .collect(Collectors.toList());
      list.sort(Comparator.comparing(Movie::get_runtime_int)
              .thenComparing(Movie::get_Series_Title));
    } else {
      list = list.stream()
              .filter(l -> !l.get_runtime().equals(""))
              .collect(Collectors.toList());
      list.sort(Comparator.comparing(Movie::get_overview)
              .thenComparing(Movie::get_Series_Title));
    }
    list = list.stream()
            .limit(top_k)
            .collect(Collectors.toList());
    return list.stream().map(Movie::get_Series_Title)
            .collect(Collectors.toList());
  }


  public List<String> getTopStars(int top_k, String by) {
    List<Movie> list = new ArrayList<>(listOfMovie);
    List<String> list1 = new ArrayList<>();
    Map<String, Double> map1 = new TreeMap<>();
    Map<String, Integer> map2 = new TreeMap<>();
    if (by.equals("rating")) {
      for (Movie movie : list) {
        String[] stars = movie.get_stars();
        Double count;
        for (String e : stars) {
          if ((count = map1.get(e)) == null) {
            map1.put(e, (double) movie.get_IMDB_Rating());
            map2.put(e, 1);
            list1.add(e);
          } else {
            map1.put(e, movie.get_IMDB_Rating() + map1.get(e));
            map2.put(e, 1 + map2.get(e));
          }
        }
      }
      for (String aa : list1) {
        map1.put(aa, -map1.get(aa) / (double) map2.get(aa));
      }
      List<Map.Entry<String, Double>> list2 = new ArrayList<>(map1.entrySet());
      list2.sort(Comparator.comparing(Map.Entry::getValue));
      List<String> list3 = new ArrayList<>();
      for (Map.Entry aa : list2) {
        list3.add((String) aa.getKey());
      }
      list3 = list3.stream()
              .limit(top_k)
              .collect(Collectors.toList());
      return list3;
    } else {
      list = list.stream()
              .filter(l -> !l.get_gross().equals(""))
              .collect(Collectors.toList());
      for (Movie movie : list) {
        String[] stars = movie.get_stars();
        Double count;
        for (String e : stars) {
          if ((count = map1.get(e)) == null) {
            map1.put(e, (double) movie.get_gross_int());
            map2.put(e, 1);
            list1.add(e);
          } else {
            map1.put(e, (double) movie.get_gross_int() + map1.get(e));
            map2.put(e, 1 + map2.get(e));
          }
        }
      }
      for (String aa : list1) {
        map1.put(aa, -map1.get(aa) / (double) map2.get(aa));
      }
      List<Map.Entry<String, Double>> list2 = new ArrayList<>(map1.entrySet());
      list2.sort(Comparator.comparing(Map.Entry::getValue));
      List<String> list3 = new ArrayList<>();
      for (Map.Entry aa : list2) {
        list3.add((String) aa.getKey());
      }
      list3 = list3.stream()
              .limit(top_k)
              .collect(Collectors.toList());
      return list3;
    }
  }


  public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
    List<Movie> list = new ArrayList<>(listOfMovie);
    list = list.stream()
            .filter(l -> l.get_Genre().contains(genre))
            .filter(l -> -l.get_runtime_int() <= max_runtime)
            .filter(l -> l.get_IMDB_Rating() >= min_rating)
            .collect(Collectors.toList());
    list.sort(Comparator.comparing(Movie::get_Series_Title));
    return list.stream().map(Movie::get_Series_Title).collect(Collectors.toList());
  }
}

class Movie {

    private String Poster_Link,Series_Title,Released_Year,Certificate,Runtime,Genre,Overview,Meta_score,Director,Star1,Star2,Star3,Star4,No_of_Votes,Gross;
    float IMDB_Rating;

    public Movie(String stringA){
        int count=0;
        while (true) {
            int begin1 = stringA.indexOf("\"");
            int begin2 = stringA.indexOf(",");
            if(begin2==-1){
                Fill(stringA,count);
                break;
            }
            if(begin1<begin2&&begin1!=-1){
                begin1=stringA.indexOf("\"",1);
                int number=1;
                int num=-1;
                while (true) {
                    while (begin2<begin1){
                        begin2=stringA.indexOf(",",begin2+1);
                        if(begin2==-1){
                            break;
                        }
                    }
                    if ((begin1 + 1 == begin2&&number==1)||begin2==-1) {
                        String stringB = stringA.substring(1, begin1);
                        Fill(stringB, count);
                        count++;
                        stringA = stringA.substring(begin1 + 1);
                        if (stringA.length() > 0) {
                            stringA = stringA.substring(1);
                        }
                        break;
                    } else {
                        number=number+num;
                        num=-num;
                        begin1=stringA.indexOf("\"",begin1+1);
                        if(begin1>begin2&&number==0) {
                            String stringB = stringA.substring(0, begin2);
                            Fill(stringB, count);
                            count++;
                            stringA = stringA.substring(begin2 + 1);
                            break;
                        }
                    }
                }
            }else {
                String stringB=stringA.substring(0,begin2);
                Fill(stringB,count);
                count++;
                stringA=stringA.substring(begin2+1);
            }
            if (begin2 == -1) {
                break;
            }
        }
    }

    private void Fill(String stringB,int count){
        switch (count) {
            case 0 -> Poster_Link = stringB;
            case 1 -> Series_Title = stringB;
            case 2 -> Released_Year = stringB;
            case 3 -> Certificate = stringB;
            case 4 -> Runtime = stringB;
            case 5 -> Genre = stringB;
            case 6 -> IMDB_Rating = Float.parseFloat(stringB);
            case 7 -> Overview = stringB;
            case 8 -> Meta_score = stringB;
            case 9 -> Director = stringB;
            case 10 -> Star1 = stringB;
            case 11 -> Star2 = stringB;
            case 12 -> Star3 = stringB;
            case 13 -> Star4 = stringB;
            case 14 -> No_of_Votes = stringB;
            case 15 -> Gross = stringB;
        }
    }

    public Integer get_Released_Year(){
        return Integer.parseInt(Released_Year);
    }

    public String get_Genre(){
        return Genre;
    }

    public String get_runtime(){
        return Runtime;
    }

    public int get_runtime_int(){
        return -Integer.parseInt(Runtime.substring(0,Runtime.length()-4));
    }

    public int get_overview(){
        return -Overview.length();
    }

    public String get_Series_Title(){
        return Series_Title;
    }

    public float get_IMDB_Rating(){
        return IMDB_Rating;
    }

    public String get_gross(){
        return Gross;
    }

    public int get_gross_int(){
        return Integer.parseInt(Gross.replace(",",""));
    }

    public String[] get_stars(){
        return new String[]{Star1,Star2,Star3,Star4};
    }
}
