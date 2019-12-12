package lab5;

public class UrlTest implements Comparable<UrlTest>{
    private final String url;
    private final Integer count;

    public UrlTest(String url, Integer count){
        this.url = url;
        this.count = count;
    }

    public String getUrl(){
        return url;
    }

    public int getCount(){
        return count;
    }

    @Override
    public int compareTo(UrlTest b){
        return this.url.compareTo(b.url) != 0 ?
                this.url.compareTo(b.url) : this.count.compareTo(b.count);

    }
}
