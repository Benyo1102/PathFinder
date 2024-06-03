///PepsiBela,imregi.bence.zsolt@stud.u-szeged.hu

import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Cell;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;

import java.util.*;

/**
 * Egy RaceTrack jatekos agenst megvalosito osztaly.
 * Az agens a jatekban resztvevo egyik jatekos, aki kepes onalloan donteseket hozni.
 */
public class Agent extends RaceTrackPlayer {
    PriorityQueue<myCell> open;
    List<myCell> closed;
    List<myCell> vegso;

    int mutato = 0;

    /**
     * Az Agent konstruktora, amely inicializalja az agenst.
     * @param state A jatekos allapota.
     * @param random A veletlenszam-generator.
     * @param track A palya, ahol a jatek zajlik.
     * @param coins Az ermek helyzete a palyan.
     * @param color Az agens szine.
     */
    public Agent(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
        this.open = new PriorityQueue<>(Comparator.comparing(myCell::getF));
        this.closed = new ArrayList<>();
        this.vegso = new ArrayList<>();
        open.offer(new myCell(state.i, state.j, 0, 0, 0, null, null)); //beleteszi az open listbe a kezdo cellat amiről indul
        findPath();
    }

    /**
     * Visszaadja az iranyt, amelyben az agens mozogni fog.
     * @param remainingTime A hatralevo ido.
     * @return A kovetkezo lepes iranya.
     */
    @Override
    public Direction getDirection(long remainingTime) {
        return vegso.get(++mutato).elozolepes;
    }

    /**
     * Az utvonal megtalalasat vegzo fuggveny.
     * Feladata, hogy megtalalja a legjobb utvonalat a cel fele.
     */
    public void findPath() {
        // A ciklus addig fut, amig az open nem ures
        while (!open.isEmpty()) {
            // Kiveszi a legkisebb 'f' erteku cellat az 'open' listabol
            myCell legkisebb = open.poll();
            // Hozzaadja a kivalasztott cellat a 'closed' listahoz
            closed.add(legkisebb);

            // Ellenorzi, hogy a jelenlegi cella a celmezo-e (a celmezon vagyunk-e)
            if (RaceTrackGame.mask(track[legkisebb.i][legkisebb.j], RaceTrackGame.FINISH)) {
                // Ha a celmezon vagyunk, akkor visszafele halad a szulo elemeken keresztul
                while (legkisebb != null) {
                    vegso.add(legkisebb);
                    legkisebb = legkisebb.parent; // A szulo elemet veszi a kovetkezonek
                }
                Collections.reverse(vegso); // Megforditja a listat, hogy a kezdeti pontbol induljon a cel fele
                return; // Kilep a metodusbol, mivel megtalaltuk az utat
            }

            // Szomszedos cellak vizsgalata az utvonal frissitese erdekeben
            for (Direction direction : RaceTrackGame.DIRECTIONS) {
                // Letrehoz egy uj szomszedos cellat az aktualis sebesseg es irany figyelembe vetelevel
                myCell szomszed = new myCell(
                        legkisebb.i + legkisebb.vi + direction.i,
                        legkisebb.j + legkisebb.vj + direction.j,
                        0,
                        legkisebb.vi + direction.i,
                        legkisebb.vj + direction.j,
                        direction,
                        legkisebb);

                // Kiszamitja az 'f' erteket az uj cellahoz: a tavolsagot az elozo cellatol es a celhoz vezeto tavolsagot
                szomszed.f = RaceTrackGame.euclideanDistance(legkisebb, szomszed) + RaceTrackGame.euclideanDistance(szomszed, szomszed);

                // Ellenorzi, hogy a szomszedos cella fal-e vagy mar szerepel-e a 'closed' listaban
                if (!RaceTrackGame.isNeitherWall(RaceTrackGame.line8connect(legkisebb, szomszed), track) || closed.contains(szomszed)) continue;
                // Ha a szomszedos cella uj (meg nem szerepel az 'open' listaban), hozzaadja az 'open' listahoz
                if (!open.contains(szomszed)) open.add(szomszed);
            }
        }
    }


    /**
     * A myCell egy Cell tipusu segedosztaly, amely tarolja a jelenlegi cella allapotat.
     * Ez az osztaly segit az utvonaltervezesben es az allapotok nyomon koveteseben.
     */
    public static class myCell extends Cell {
        double f;
        int vi;
        int vj;

        myCell parent;

        Direction elozolepes;

        /**
         * myCell konstruktora.
         * @param i A cella i koordinataja.
         * @param j A cella j koordinataja.
         * @param f Az f ertek, amely az utvonal koltseget reprezentalja.
         * @param vi Az i iranyu sebesseg.
         * @param vj A j iranyu sebesseg.
         * @param elozolepes Az elozo lepes iranya.
         * @param parent A szulo cella.
         */
        public myCell(int i, int j, double f, int vi, int vj, Direction elozolepes, myCell parent) {
            super(i, j);
            this.f = f;
            this.vi = vi;
            this.vj = vj;
            this.elozolepes = elozolepes;
            this.parent = parent;
        }

        /**
         * Visszaadja az f erteket.
         * @return Az f ertek.
         */
        public double getF() {
            return f;
        }

        /**
         * Beallitja az f erteket.
         * @param f Az uj f ertek.
         */
        public void setF(double f) {
            this.f = f;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Cell c) {
                return (c.i == i && c.j == j);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(i, j);
        }
    }
}