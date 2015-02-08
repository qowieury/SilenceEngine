package com.shc.silenceengine.utils;

import com.shc.silenceengine.geom2d.Polygon;
import com.shc.silenceengine.geom3d.Polyhedron;
import com.shc.silenceengine.graphics.Batcher;
import com.shc.silenceengine.graphics.Color;
import com.shc.silenceengine.graphics.opengl.Primitive;
import com.shc.silenceengine.math.Vector2;
import com.shc.silenceengine.math.Vector3;

/**
 * @author Sri Harsha Chilakapati
 */
public final class RenderUtils
{
    private static Vector2 tempVec2;
    private static Vector3 tempVec31;
    private static Vector3 tempVec32;
    private static Vector3 tempVec33;

    static
    {
        tempVec2 = new Vector2();

        tempVec31 = new Vector3();
        tempVec32 = new Vector3();
        tempVec33 = new Vector3();
    }

    private RenderUtils()
    {
    }

    public static void tracePolygon(Batcher b, Polygon polygon)
    {
        tracePolygon(b, polygon, Color.WHITE);
    }

    public static void tracePolygon(Batcher b, Polygon polygon, Color color)
    {
        tracePolygon(b, polygon, Vector2.ZERO, color);
    }

    public static void tracePolygon(Batcher b, Polygon polygon, Vector2 position, Color color)
    {
        b.begin(Primitive.LINE_LOOP);
        {
            for (Vector2 vertex : polygon.getVertices())
            {
                b.vertex(tempVec2.set(vertex).addSelf(polygon.getPosition()).addSelf(position));
                b.color(color);
            }
        }
        b.end();
    }

    public static void fillPolygon(Batcher b, Polygon polygon)
    {
        fillPolygon(b, polygon, Color.WHITE);
    }

    public static void fillPolygon(Batcher b, Polygon polygon, Color color)
    {
        fillPolygon(b, polygon, Vector2.ZERO, color);
    }

    public static void fillPolygon(Batcher b, Polygon polygon, Vector2 position, Color color)
    {
        b.begin(Primitive.TRIANGLE_FAN);
        {
            for (Vector2 vertex : polygon.getVertices())
            {
                b.vertex(tempVec2.set(vertex).addSelf(polygon.getPosition()).addSelf(position));
                b.color(color);
            }
        }
        b.end();
    }

    public static void tracePolyhedron(Batcher b, Polyhedron Polyhedron)
    {
        tracePolyhedron(b, Polyhedron, Color.WHITE);
    }

    public static void tracePolyhedron(Batcher b, Polyhedron Polyhedron, Color color)
    {
        tracePolyhedron(b, Polyhedron, Vector3.ZERO, color);
    }

    public static void tracePolyhedron(Batcher b, Polyhedron polyhedron, Vector3 position, Color color)
    {
        b.begin(Primitive.LINE_STRIP);
        {
            Vector3 v1;
            Vector3 v2;
            Vector3 v3;

            // Convert Triangle Strip vertices to Triangles
            for (int v = 0; v < polyhedron.vertexCount() - 2; v++)
            {
                if ((v & 1) != 0)
                {
                    // The Clock-Wise order
                    v1 = polyhedron.getVertex(v);
                    v2 = polyhedron.getVertex(v + 1);
                    v3 = polyhedron.getVertex(v + 2);
                }
                else
                {
                    // The Counter-Clock-Wise order
                    v1 = polyhedron.getVertex(v);
                    v2 = polyhedron.getVertex(v + 2);
                    v3 = polyhedron.getVertex(v + 1);
                }

                // Set the position of the vertices
                v1 = tempVec31.set(v1).addSelf(polyhedron.getPosition()).addSelf(position);
                v2 = tempVec32.set(v2).addSelf(polyhedron.getPosition()).addSelf(position);
                v3 = tempVec33.set(v3).addSelf(polyhedron.getPosition()).addSelf(position);

                // Draw the triangle as a line strip
                b.vertex(v1);
                b.color(color);

                b.vertex(v2);
                b.color(color);

                b.vertex(v3);
                b.color(color);
            }
        }
        b.end();
    }

    public static void fillPolyhedron(Batcher b, Polyhedron polyhedron)
    {
        fillPolyhedron(b, polyhedron, Color.WHITE);
    }

    public static void fillPolyhedron(Batcher b, Polyhedron polyhedron, Color color)
    {
        fillPolyhedron(b, polyhedron, Vector3.ZERO, color);
    }

    public static void fillPolyhedron(Batcher b, Polyhedron polyhedron, Vector3 position, Color color)
    {
        b.begin(Primitive.TRIANGLE_STRIP);
        {
            for (Vector3 vertex : polyhedron.getVertices())
            {
                b.vertex(tempVec31.set(vertex).addSelf(polyhedron.getPosition()).addSelf(position));
                b.color(color);
            }
        }
        b.end();
    }
}
