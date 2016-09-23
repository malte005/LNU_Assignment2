package assignment2.md222pv.dv606.assignment2.country;

/**
 * Created by damma on 31.08.2016.
 */
public class Country {

    private String name, year;

    public Country() {
    }

    public Country(String name, String year) {
        this.name = name;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
