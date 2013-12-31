package com.tinkerpop.gremlin.computer.gremlin;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.blueprints.Vertex;

import java.io.Serializable;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GremlinMessage implements Serializable {

    public enum Destination {

        VERTEX, EDGE, PROPERTY;

        public static Destination of(Object object) {
            if (object instanceof Vertex)
                return VERTEX;
            else if (object instanceof Edge)
                return EDGE;
            else if (object instanceof Property)
                return PROPERTY;
            else
                throw new IllegalArgumentException("Unknown type: " + object.getClass());
        }
    }

    public final Object elementId;
    public final Destination destination;
    public final String propertyKey;

    private GremlinMessage(final Destination destination, final Object elementId, final String propertyKey) {
        this.destination = destination;
        this.elementId = elementId;
        this.propertyKey = propertyKey;

    }

    public static GremlinMessage of(final Object object) {
        Destination blueprints = Destination.of(object);
        if (blueprints == Destination.VERTEX)
            return new GremlinMessage(blueprints, ((Vertex) object).getId(), null);
        else if (blueprints == Destination.EDGE)
            return new GremlinMessage(blueprints, ((Edge) object).getId(), null);
        else
            return new GremlinMessage(blueprints, ((Property) object).getElement().getId(), ((Property) object).getKey());
    }
}