package br.com.ourtalk.core;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/** Routes along an offline walking graph. Returns no route for out-of-coverage or disconnected points. */
public final class WalkGraph {
    public record Point(double latitude, double longitude) {}
    public record Route(List<Point> points, double meters, double startGap, double endGap) {}
    private record Visit(int node, double meters) implements Comparable<Visit> {
        @Override public int compareTo(Visit other) { return Double.compare(meters, other.meters); }
    }

    private final double south, west, north, east;
    private final Point[] points;
    private final int[] offsets, targets;

    public WalkGraph(InputStream input) throws IOException {
        DataInputStream data = new DataInputStream(input);
        if (data.readInt() != 0x4F545731) throw new IOException("Unknown walking graph version");
        south = data.readDouble();
        west = data.readDouble();
        north = data.readDouble();
        east = data.readDouble();
        int nodes = data.readInt();
        int edges = data.readInt();
        if (!(south < north && west < east) || nodes < 2 || nodes > 200_000 || edges < 1 || edges > 1_000_000) {
            throw new IOException("Invalid graph dimensions");
        }
        points = new Point[nodes];
        offsets = new int[nodes + 1];
        targets = new int[edges];
        for (int i = 0; i < nodes; i++) {
            points[i] = new Point(data.readInt() / 1e6, data.readInt() / 1e6);
        }
        int previous = -1;
        for (int i = 0; i < edges; i++) {
            int from = data.readInt();
            int to = data.readInt();
            if (from < 0 || from < previous || from >= nodes || to < 0 || to >= nodes || from == to) {
                throw new IOException("Invalid graph edge");
            }
            offsets[from + 1]++;
            targets[i] = to;
            previous = from;
        }
        for (int i = 1; i < offsets.length; i++) offsets[i] += offsets[i - 1];
    }

    public boolean covers(Point point) {
        return point.latitude >= south && point.latitude <= north &&
            point.longitude >= west && point.longitude <= east;
    }

    public Route route(Point origin, Point destination) {
        if (!covers(origin) || !covers(destination)) return null;
        int start = nearest(origin);
        int end = nearest(destination);
        if (start < 0 || end < 0) return null;
        double[] best = new double[points.length];
        Arrays.fill(best, Double.POSITIVE_INFINITY);
        int[] previous = new int[points.length];
        Arrays.fill(previous, -1);
        PriorityQueue<Visit> queue = new PriorityQueue<>();
        best[start] = 0;
        queue.add(new Visit(start, 0));
        while (!queue.isEmpty()) {
            Visit current = queue.remove();
            if (current.meters > best[current.node]) continue;
            if (current.node == end) break;
            for (int edge = offsets[current.node]; edge < offsets[current.node + 1]; edge++) {
                int target = targets[edge];
                double cost = current.meters + distance(points[current.node], points[target]);
                if (cost >= best[target]) continue;
                best[target] = cost;
                previous[target] = current.node;
                queue.add(new Visit(target, cost));
            }
        }
        if (!Double.isFinite(best[end])) return null;
        List<Point> path = new ArrayList<>();
        for (int node = end; node >= 0; node = previous[node]) path.add(points[node]);
        Collections.reverse(path);
        return new Route(path, best[end], distance(origin, points[start]), distance(destination, points[end]));
    }

    private int nearest(Point point) {
        double limit = 120;
        int nearest = -1;
        for (int i = 0; i < points.length; i++) {
            double distance = distance(point, points[i]);
            if (distance <= limit) {
                nearest = i;
                limit = distance;
            }
        }
        return nearest;
    }

    private static double distance(Point a, Point b) {
        double dy = (b.latitude - a.latitude) * 111_195;
        double dx = (b.longitude - a.longitude) * 111_195 *
            Math.cos(Math.toRadians((a.latitude + b.latitude) / 2));
        return Math.hypot(dx, dy);
    }
}
