package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

import com.jme3.app.Application;
import main.AlertState;

class Position implements Comparable<Position> {
    private static final int MAX_COLS = 12289;

    public int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int distance(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Position) {
            Position position = (Position) other;
            return x == position.x && y == position.y;
        }
        return false;
    }

    @Override
    public int hashCode() { return x * MAX_COLS + y; }

    @Override
    public int compareTo(Position other) { return hashCode() - other.hashCode(); }
}

class Status {
    private Position hero, boxes[];
    private String steps;
    private int cost;

    public Status(Position hero, Position[] boxes, String steps, int cost) {
        this.hero = hero;
        this.boxes = boxes;
        Arrays.sort(this.boxes);
        this.steps = steps;
        this.cost = cost;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Status) {
            Status status = (Status) other;
            if (!hero.equals(status.hero)) return false;
            if (boxes.length != status.boxes.length) return false;
            for (int i = 0; i < boxes.length; i++) {
                if (!boxes[i].equals(status.boxes[i])) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = hero.hashCode();
        for (Position box : boxes) result ^= box.hashCode();
        return result;
    }

    public Position getHero() { return hero; }
    public Position[] getBoxes() { return boxes; }
    public String getSteps() { return steps; }
    public int getCost() { return cost; }

    public int heuristic() {
        ArrayList<Position> unmatchedBoxes = new ArrayList<>();
        ArrayList<Position> unmatchedGoals = new ArrayList<>();
        final int total = boxes.length;
        for (int i = 0, j = 0; i < total || j < total;) {
            if (j == total || (i < total && boxes[i].compareTo(Solver.getGoals()[j]) < 0)) {
                unmatchedBoxes.add(boxes[i]);
                i++;
            } else if (i == total || boxes[i].compareTo(Solver.getGoals()[j]) > 0) {
                unmatchedGoals.add(Solver.getGoals()[j]);
                j++;
            } else {
                i++;
                j++;
            }
        }
        int result = 0;
        for (int i = 0; i < unmatchedBoxes.size(); i++) {
            result += unmatchedBoxes.get(i).distance(unmatchedGoals.get(i));
        }
        return result;
    }
}

public abstract class Solver {
    public static final int dx[] = {0, 1, 0, -1};
    public static final int dy[] = {1, 0, -1, 0};
    public static final char dir[] = {'r', 'd', 'l', 'u'};

    private static Application app;
    private static char[][] map;
    private static Position[] goals;
    private static long startTime;

    public static String solve(Application app, int rows, int cols, int heroX, int heroY, char[][] map) {
        Solver.app = app;
        Solver.map = map;
        ArrayList<Position> boxes = new ArrayList<>();
        ArrayList<Position> goals = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == 'B') {
                    boxes.add(new Position(i, j));
                    map[i][j] = ' ';
                } else if (map[i][j] == 'X') {
                    boxes.add(new Position(i, j));
                    map[i][j] = '.';
                    goals.add(new Position(i, j));
                } else if (map[i][j] == '.') {
                    goals.add(new Position(i, j));
                }
            }
        }
        Solver.goals = goals.toArray(new Position[0]);

        startTime = System.currentTimeMillis();
        return solve(new Status(new Position(heroX, heroY), boxes.toArray(new Position[0]), "", 0));
    }

    private static String solve(Status status) {
        HashMap<Status, Boolean> visited = new HashMap<>();
        PriorityQueue<Status> queue = new PriorityQueue<>((a, b) ->
                a.heuristic() + a.getCost() - b.heuristic() - b.getCost());
        queue.add(status);
        visited.put(status, true);

        while (!queue.isEmpty()) {
            Status cur = queue.poll();
            Position hero = cur.getHero();
            for (int dir = 0; dir < 4; dir++) {
                Position newHero = new Position(hero.x + dx[dir], hero.y + dy[dir]);
                if (map[newHero.x][newHero.y] == '#') continue;
                Position boxes[] = cur.getBoxes().clone();
                boolean valid = true;
                char step = Solver.dir[dir];
                for (int i = 0; i < boxes.length; i++) {
                    if (boxes[i].equals(newHero)) {
                        Position nextPos = new Position(boxes[i].x + dx[dir], boxes[i].y + dy[dir]);

                        if (map[nextPos.x][nextPos.y] == '#') {
                            valid = false;
                            break;
                        }

                        boolean isBox = false;
                        for (int j = 0; j < boxes.length; j++) if (boxes[j].equals(nextPos)) isBox = true;
                        if (isBox) {
                            valid = false;
                            break;
                        }

                        boxes[i] = nextPos;
                        step = Character.toUpperCase(step);
                        break;
                    }
                }
                if (!valid) continue;

                int cost = cur.getCost() + (Character.isUpperCase(step) ? 0 : 1);
                Status next = new Status(newHero, boxes, cur.getSteps() + step, cost);
                if (visited.containsKey(next)) continue;

                if (next.heuristic() == 0) return next.getSteps();

                queue.add(next);
                visited.put(next, true);
            }

            if (System.currentTimeMillis() - startTime > 10000) {
                app.getStateManager().attach(new AlertState(
                        "Time Out",
                        "No solution found in 10 seconds."
                ));
                return null;
            }
        }

        app.getStateManager().attach(new AlertState(
                "No Solution",
                "No solution found."
        ));
        return null;
    }

    public static Position[] getGoals() { return goals; }
}
