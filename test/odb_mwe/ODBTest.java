package odb_mwe;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jlowens on 3/22/16.
 */

public class ODBTest  {
    ODB.DBOperation getCreateOperation()
    {
        return new ODB.DBOperation() {
            @Override
            public Object withDB(OrientGraph graph) {
                OrientVertex[] verts = ODB.createVertices(graph);
                assertEquals(verts.length, 2);
                OrientVertex va = verts[0];
                OrientVertex vb = verts[1];
                assertEquals(va.getProperty("name"), "A");
                return null;
            }
        };
    }

    @Test
    void testMemoryCreate(String connectionString)
    {
        ODB odb = new ODB(connectionString);
        odb.withDB(getCreateOperation());
    }
}
