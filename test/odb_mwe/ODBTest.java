package odb_mwe;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import static org.junit.Assert.*;

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
                assertEquals("A", va.getProperty("name"));
		assertEquals("B", vb.getProperty("name"));
		assertEquals(vb, va.getProperty("other"));
		assertEquals(va, vb.getProperty("other"));
                return null;
            }
        };
    }
    ODB.DBOperation getSelectOperation()
    {
	return new ODB.DBOperation() {
	    @Override
	    public Object withDB(OrientGraph graph) {
		OrientVertex va = ODB.getNamedVertex(graph, "A");
		OrientVertex vb = ODB.getNamedVertex(graph, "B");
		assertNotNull(va);
		assertNotNull(vb);
		assertEquals("A", va.getProperty("name"));
		assertEquals("B", vb.getProperty("name"));
		assertEquals(vb, va.getProperty("other"));
		assertEquals(va, vb.getProperty("other"));
		return va;
	    }
	};
    }

    static final String memoryPath = "memory:test/mem";
    static final String plocalPath = "plocal:/tmp/odb_local";
    static final String remotePath = "remote:localhost/odb_remote";
    static ODB memorydb;
    static ODB plocaldb;
    static ODB remotedb;

    @BeforeClass
    public static void setup()
    {
	System.out.println("Setting up...");
	memorydb = new ODB(memoryPath);
	plocaldb = new ODB(plocalPath);
	remotedb = new ODB(remotePath, "root", "password");
    }

    @Test
    public void testMemoryCreate()
    {
        memorydb.withDB(getCreateOperation());
    }

    @Test 
    public void testPLocalCreate()
    {
	plocaldb.withDB(getCreateOperation());
    }

    @Test
    public void testRemoteCreate()
    {
	remotedb.withDB(getCreateOperation());
    }

    @Test
    public void testMemorySelect()
    {
        memorydb.withDB(getSelectOperation());
    }

    @Test 
    public void testPLocalSelect()
    {
	plocaldb.withDB(getSelectOperation());
    }

    @Test
    public void testRemoteSelect()
    {
	remotedb.withDB(getSelectOperation());
    }

    @AfterClass
    public static void cleanup()
    {
	System.out.println("Tearing down...");
	plocaldb.withDB(new ODB.DBOperation() {
		@Override
		public Object withDB(OrientGraph graph) {
		    return ODB.query(graph, "delete vertex V");
		}
	    });
	remotedb.withDB(new ODB.DBOperation() {
		@Override
		public Object withDB(OrientGraph graph) {
		    return ODB.query(graph, "delete vertex V");
		}
	    });
    }
}
