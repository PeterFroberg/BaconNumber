import java.util.ArrayList;
import java.util.LinkedList;

public class Actor { //implements Comparable<Actor>{
    private int name;
    private ArrayList<Integer> movies = new ArrayList<>();
    private int baconNumber;
    private Actor parent = null;

    public Actor(int name) {
        this.name = name;
    }

    public void setBaconNumber(int baconNumber) {
        this.baconNumber = baconNumber;
    }

    public void setParent(Actor parent) {
        this.parent = parent;
    }

    public int getName() {
        return name;
    }

    public ArrayList<Integer> getMovies() {
        return this.movies;
    }

    public void addMovie(Integer movie) {
        this.movies.add(movie);
    }

    public int getBaconNumber() {
        return this.baconNumber;
    }

    public Actor getPrevious() {
        return this.parent;
    }

    @Override
    public String toString() {
        return "Actor :" + movies.toString();
    }
}

