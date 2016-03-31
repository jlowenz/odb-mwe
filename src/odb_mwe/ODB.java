package odb_mwe;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by jlowens on 3/22/16.
 */
public class ODB {
    private final OrientGraphFactory factory;
    private final int maxRetries;
    private static Logger log = Logger.getLogger("ODB");

    public ODB(String connectionPath) {
        this(connectionPath, 3);
    }

    public ODB(String connectionPath, String user, String pass) {
        this(connectionPath, user, pass, 3);
    }

    public ODB(String connectionPath, int maxRetries) {
        this.factory = new OrientGraphFactory(connectionPath);
        this.maxRetries = maxRetries;
    }

    public ODB(String connectionPath, String user, String pass, int maxRetries) {
        this.factory = new OrientGraphFactory(connectionPath, user, pass);
        this.factory.setupPool(4, 16);
        this.maxRetries = maxRetries;
    }

    public void disconnect() {
        this.factory.close();
    }

    public interface DBOperation {
        Object withDB(OrientGraph graph);
    }

    private class WithDB implements BiFunction<OrientGraph, DBOperation, Object> {
        @Override
        public Object apply(OrientGraph g, DBOperation f) {
            for (int i = 0; i < maxRetries; i++) {
                try {
                    return f.withDB(g);
                } catch (ONeedRetryException e) {
                    if (i == (maxRetries - 1)) {
                        throw e;
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
	    return null;
        }
    }

    public Object withDB(DBOperation f) {
        OrientGraph g = this.factory.getTx();
        try {
            Object ret = new WithDB().apply(g,f);
            g.commit();
            return ret;
        } catch (Throwable t) {
            g.rollback();
            //t.printStackTrace();
	    throw t;
        } finally {
            g.shutdown();
        }
    }

    public static String asClass(String name)
    {
        return "class:" + name;
    }

    public static OrientVertex addVertex(OrientGraph g, String type, Object ... props)
    {
        return g.addVertex(asClass(type),props);
    }

    public static OrientVertex[] createVertices(OrientGraph g)
    {
        OrientVertex v1 = addVertex(g, "V", "name", "A");
        OrientVertex v2 = addVertex(g, "V", "name", "B", "other", v1);
        v1.setProperty("other", v2);
	v1.save();
	v2.save();
        return new OrientVertex[] {v1, v2};
    }

    public static OrientVertex getNamedVertex(OrientGraph g, String name)
    {
        String sql = "select from V where name='" + name + "'";
        Iterable<OrientVertex> results = g.command(new OCommandSQL(sql).setFetchPlan("*:-1")).execute();
        System.out.println("Got " + results);
	Iterator<OrientVertex> i = results.iterator();
	if (i.hasNext()) return i.next();
	else return null;
        //return results.iterator().next();
    }
    
    public static Object query(OrientGraph g, String sql)
    {
	return g.command(new OCommandSQL(sql).setFetchPlan("*:-1")).execute();
    }
}
