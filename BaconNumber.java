// Peter Fröberg, pefr7147@student.su.se


/**
 * Programmet BaconNumber räknar ut vilket Bacon Number en skådespelare har.
 * Beräkningarna görs på data från två filer som heter actors.list och actresses.list
 * Användaren uppmanas att mata in en skådespelare enligt formatet [Efternamn, Förnamn (ordningsnummer)]
 * Sedan kommer programmet att beräkna antal steg som behövs för att komma fram till Kevin Bacon (angivet som konstanten
 * GOAL_ACTOR)
 *
 * Användning
 *  Användaren uppmanas att mata in en skådespelare enligt formatet [Efternamn, Förnamn (ordningsnummer)]
 *  * Sedan kommer programmet att beräkna antal steg som behövs för att komma fram till Kevin Bacon (angivet som konstanten
 *  * GOAL_ACTOR).
 *  För att avsluta programmet anger användaren x istället för en skådespelare
 *
 * @author Peter Fröberg, pefr7147@student.su.se
 *
 * @param GOAL_ACTOR namnet på den skådespelare som vi beräknar antal steg till
 * @param movieGRaph håller reda på vilka skådespelare(Actor objekt) som har varit med i en film
 * @param actorGraph Håller en lista med Actor objekt baserat på skådespelarens nummer
 * @param actorNumberLookup Håller reda på vilket nummer en skådespelare har baserat på namn
 * @param movieNumberLookup  Håller reda på vilet nummer en film baserat på titel
 * @param currentActorName Vilken skådespelare som håller på att läsas in från fil
 * @param currentTitle Vilken filmtitel som håller på att läsas in från fil
 * @param skip sätts till true om för att markera att denna titel inte ska läggas till i graphen, som text tv shower mm.
 * @param nextMovieNumer håller reda på vilket som är nästa lediga nummer för filmer
 * @param nextActorNumer Håller reda på vilket som är nästa lediga nummer för skådespelare
 */

import java.io.IOException;
import java.util.*;

public class BaconNumber {

    private KeyboardInput input = new KeyboardInput();
    private static final String GOAL_ACTOR = "BaconKevinI";
    private Map<Integer, ArrayList<Actor>> movieGraph;
    private Map<Integer, Actor> actorGraph;
    private Map<String, Integer> actorNumberLookup;
    private Map<String, Integer> movieNumberLookup;
    private String currentActorName = "";
    private String currentTitle = "";
    private boolean startOfFile = true;
    private boolean skip = false;
    private boolean running = true;
    int nextMovieNumber = 0;
    int nextActorNumber = 0;

    public BaconNumber() {
        movieGraph = new HashMap<>();
        actorGraph = new HashMap<>();
        movieNumberLookup = new HashMap<>();
        actorNumberLookup = new HashMap<>();
        try {
            System.out.println("Started to load files....");
            Long startTime = System.currentTimeMillis();
            readFile("actresses.list");
            Long time = (System.currentTimeMillis() - startTime) / 1000;
            startTime = System.currentTimeMillis();
            System.out.println("ReadTime Actresses: " + time);
            readFile("actors.list");
            time = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("ReadTime Actors: " + time);
            startTime = System.currentTimeMillis();
            movieNumberLookup.clear();
            time = (System.currentTimeMillis() - startTime);
            System.out.println("Bacon Time: " + time);

            while (running) {
                String findBaconNumberFor = input.inputString("Ange skådespelaren som du vill ha Bacon nummret för (x to quit): ", true);
                if (findBaconNumberFor.equalsIgnoreCase("x")) {
                    running = false;
                }
                startTime = System.currentTimeMillis();
                int baconNumber = findBaconNumber(findBaconNumberFor);
                System.out.println("Bacon nummret för " + findBaconNumberFor + " är: " + baconNumber);
                time = (System.currentTimeMillis() - startTime);// / 1000;
                System.out.println("Det tog " + time + " millesekunder att räkna fram bacon nummret för " + findBaconNumberFor);
                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean generateMovieGraph(String str, BaconReader.PartType type) {
        /**
         * Filtrerar ut orelevant data från filerna som hämtats med BaconReader.java
         * och skapar grapherna som används vid beräkning av antal steg till GOAL_ACTOR
         */

        if (type.equals(BaconReader.PartType.INFO) && (str.equals("V") || str.equals("TV")) || str.equals("archivefootage") || str.contains("Himself")) { //) || str.contains("Herself")){
            skip = true;
        }

        if ((type.equals(BaconReader.PartType.NAME) && (!currentActorName.equals(str)))) {

            if (!startOfFile && !currentTitle.equals("")) {
                if (!skip) {
                    addActor(currentActorName, currentTitle);
                }
            }
            skip = false;
            currentActorName = str;
            currentTitle = "";

        } else if (type.equals(BaconReader.PartType.TITLE)) {

            if (!str.equals(currentTitle) && (!startOfFile) && ((!currentTitle.equals("")) || !str.equals("SUSPENDED"))) {
                if (!skip) {
                    addActor(currentActorName, currentTitle);
                }
                skip = false;
            }
            currentTitle = str;
            startOfFile = false;
        }
        return false;
    }

    private void readFile(String fileName) throws IOException {
        /**
         * Läser in filer från disk
         */
        BaconReader baconReader = new BaconReader(fileName);
        BaconReader.Part bPart;
        //int i = 0;
        bPart = baconReader.getNextPart();

        do {
            String stringToSend = bPart.text;
            BaconReader.PartType bPartToSend = bPart.type;

            stringToSend = stringToSend.replaceAll("[^a-zA-Z0-9_-]", "");

            generateMovieGraph(stringToSend, bPartToSend);

            bPart = baconReader.getNextPart();
            //i++;
            //}while (i<100);
            //}while (i<50000000);
        } while (bPart != null);
        baconReader.close();
    }

    private void addActor(String actor, String movie) {
        /**
         * Denna funktion lägger till skådespelare och filmer i datastrukturerna.
         * För att minska minnes användningen och öka prestandan så byter funktionen ut text strängarna
         * till Integer i datastrukturerna. Samtidigt skapas två hashmappar som lookup mellan namn och nummer.
         * HashMappen "movieNumberLookup" används endast för att bygga datastrukturerna som används. Denna hashMap
         * töms efter att laddningen är klar.
         */

        if (actor.equals("") || movie.equals("")) {
            return;
        }

        int movieNumber = nextMovieNumber;
        int actorNumber = nextActorNumber;

        Actor actorNode;
        if (movieNumberLookup.containsKey(movie)) {
            movieNumber = movieNumberLookup.get(movie);

        } else {
            movieNumberLookup.put(movie, movieNumber);
            nextMovieNumber++;
        }

        if (actorNumberLookup.containsKey(actor)) {
            actorNumber = actorNumberLookup.get(actor);
            actorNode = actorGraph.get(actorNumber);

            actorNode.addMovie(movieNumber);
        } else {
            //actorNumber = nextActorNumber;
            actorNumberLookup.put(actor, actorNumber);

            actorNode = new Actor(actorNumber);
            actorGraph.put(actorNumber, actorNode);
            actorNode.addMovie(movieNumber);
            nextActorNumber++;
        }

        if (movieGraph.containsKey(movieNumber)) {
            movieGraph.get(movieNumber).add(actorNode);
        } else {
            movieGraph.put(movieNumber, new ArrayList<>());
            movieGraph.get(movieNumber).add(actorNode);
        }
        startOfFile = false;
    }

    private int findBaconNumber(String actorName) throws IllegalArgumentException {
        /**
         * Funktionen beräknar antalet steg mellan den av användaren angivna skådespelaren
         * och GOAL_ACTOR. Genom att söka igenom graphen med "Breadth First" metoden.
         * @param queue - kö för vilka skådespelare som ska kontrolleras
         * @param visited - Håller reda på vilka skådrespelare som redan har kontrollerats
         */

        actorName = actorName.replaceAll("[^a-zA-Z0-9_-]", "");

        if (actorName.equals(GOAL_ACTOR)) {
            return 0;
        }

        Queue<Actor> bfsQueue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        Actor startActor = actorGraph.get(actorNumberLookup.get(actorName));

        if (startActor == null) {
            System.out.println("Skådespelaren finns inte i databasen");
            return -1;
        }
        int goalActorNumber = actorNumberLookup.get(GOAL_ACTOR);

        startActor.setBaconNumber(0);
        bfsQueue.add(startActor);
        visited.add(actorNumberLookup.get(actorName));

        /**
         * Loopar igenom kön (queue) tills den antingen är tom och ingen koppling mellan skådespelarna
         * hittades, eller att man har hittat en väg mellan skådespelarna och då returnerar då antal
         * steg mellan skådespelarna i form av baconNumber
         */
        while (!bfsQueue.isEmpty()) {
            Actor currentActor = bfsQueue.remove();
            List<Integer> movieList = currentActor.getMovies();
            for (Integer movie : movieList) {
                for (Actor a : movieGraph.get(movie)) {
                    if (a.getName() == (goalActorNumber)) {
                        int baconNumber = currentActor.getBaconNumber() + 1;
                        return baconNumber;
                    } else {
                        if (!visited.contains(a.getName())) {
                            bfsQueue.add(a);
                            visited.add(a.getName());
                            a.setBaconNumber(currentActor.getBaconNumber() + 1);
                            //a.setParent(currentActor);
                        }
                    }
                }
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        new BaconNumber();
    }
}
