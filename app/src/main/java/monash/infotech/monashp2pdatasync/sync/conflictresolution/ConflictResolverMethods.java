package monash.infotech.monashp2pdatasync.sync.conflictresolution;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;
import monash.infotech.monashp2pdatasync.entities.Point;

/**
 * Created by Ali
 * This class defines different methods for resolving the conflicts
 */
public class ConflictResolverMethods {
    //add two double values
    public static String add(double x, double y) {
        return String.valueOf(x + y);
    }
    //add two int values
    public static int add(int x, int y) {
        return x + y;
    }
    //accept two polygons and merge them into a single polygon
    public static String mergePolygon(List<Point> area1,List<Point> area2)
    {
        List<Point2D> allPoints=new ArrayList<>();
        SimplePolygon2D polygon1=new SimplePolygon2D();
        for(Point p:area1) {
            allPoints.add(new Point2D(p.x,p.y));
            polygon1.addVertex(new Point2D(p.x,p.y));
        }
        SimplePolygon2D polygon2=new SimplePolygon2D();
        for(Point p:area2) {
            allPoints.add(new Point2D(p.x,p.y));
            polygon2.addVertex(new Point2D(p.x,p.y));
        }
        Polygon2D union = Polygons2D.union(polygon1, polygon2);
        String result="";
        JSONArray ja=new JSONArray();
        for (Point2D p:union.vertices())
        {
            try {
                ja.put(new JSONObject().put("x",p.x()).put("y",p.y()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ja.toString();
    }
}
