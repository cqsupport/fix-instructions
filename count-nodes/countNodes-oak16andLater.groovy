//Adaptation of @stillalex's script from here https://gist.github.com/stillalex/06303f8cc1d3780d3eab4c72575883ae
//This version works with Oak 1.6 and later versions
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger
import org.apache.jackrabbit.oak.api.Type
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.spi.state.NodeStore
import org.apache.jackrabbit.oak.commons.PathUtils
import com.google.common.collect.Lists
import java.util.List

org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("countNodes.groovy");

def countNodes(NodeState n, deep = false, String path = "/", Integer flush = 1000, AtomicInteger count = new AtomicInteger(0), AtomicInteger binaries = new AtomicInteger(0), root = true) {
  org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("countNodes.groovy");

  if(root) {
    log.info("Counting nodes in tree ${path}");
  }
 
  cnt = count.incrementAndGet()
  if (cnt % flush == 0) log.info("  " + cnt)

  try {
      for(prop in n.getProperties()) {
        try {
          if(prop.getType() == Type.BINARIES) {
              for(b in prop.getValue(Type.BINARIES)) {
                binaries.incrementAndGet()
                if(deep) {
                    InputStream s = b.getNewStream();
                    try {
                      byte[] buffer = new byte[1024];
                      int l = s.read(buffer, 0, buffer.length);
                    } finally {
                      s.close();
                    }
                } else {
                    b.length();
                }
                binaries.incrementAndGet();
              }
        } else if(prop.getType() == Type.BINARY) {
            def b = prop.getValue(Type.BINARY);
            if(deep) {
                InputStream s = b.getNewStream();
                try {
                  byte[] buffer = new byte[1024];
                  int l = s.read(buffer, 0, buffer.length);
                } finally {
                  s.close();
                }
            } else {
                b.length();
            }
            binaries.incrementAndGet();
        } else {
            // Check regular properties for missing segments 
            if(prop.isArray()) {
                for(sf in prop.getValue(prop.getType())) {
                    // do nothing - we just need to read all values
                }
            } else {
                prop.getValue(prop.getType());
            }
        }
      } catch(e) {
          log.error("warning unable to read node properties ${path} ${prop.name}: " + e.getMessage())
        //org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e, out)
      }
    }

    try {
      for(child in n.getChildNodeEntries()) {
        try {
          countNodes(child.getNodeState(), deep, path + "/" + child.getName(), flush, count, binaries, false)
        } catch(e) {
          log.error("warning unable to read child node ${path} : " + e.getMessage())
	      //org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e, out)
        }
      }
    } catch(e) {
      log.error("warning unable to read child entries ${path} : " + e.getMessage())
      //org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e, out)
    }
  } catch(e) {
    log.error("warning unable to read node ${path} : " + e.getMessage())
    //org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e, out)
  }

  if(root) {
    log.info("Total nodes in tree ${path}: ${cnt}");
    log.info("Total binaries in tree ${path}: ${binaries.get()}");
  }

  return cnt
}

def countNodes(session, path, deep) {
    NodeStore nstore = session.getRootNode().sessionDelegate.root.store
    def rs = nstore.root
    def rnb = rs.builder()
    def nb = rnb;
    List<String> elements = Lists.newArrayList();
    PathUtils.elements(path).each{String element ->
        if (PathUtils.denotesParent(element)) {
            if (!elements.isEmpty()) {
                elements.remove(elements.size() - 1);
            }
        } else if (!PathUtils.denotesCurrent(element)) {
            elements.add(element);
        }
    }

    elements.each {
      if(it.size() > 0) {
        nb = nb.getChildNode(it)
      }
    }
    countNodes(nb.getNodeState(), deep, path)
}

def countNodes(path) {
	def repo = osgi.getService(org.apache.sling.jcr.api.SlingRepository)
	def session = repo.loginAdministrative(null)
	try {
	    countNodes(session, path, true)
	} finally {
	    session.logout()
	}
}

log.info("Running node counter");
// Or only check the async oak indexes
t1 = Thread.start("countNodes /oak:index",{countNodes("/oak:index")})
t2 = Thread.start("countNodes /content", {countNodes("/content")})
t3 = Thread.start("countNodes /var", {countNodes("/var")})
t4 = Thread.start("countNodes /etc", {countNodes("/etc")})
t5 = Thread.start("countNodes /tmp", {countNodes("/tmp")})
t6 = Thread.start("countNodes /conf", {countNodes("/conf")})
t7 = Thread.start("countNodes /jcr:system", {countNodes("/jcr:system")})
t8 = Thread.start("countNodes /apps", {countNodes("/apps")})
t9 = Thread.start("countNodes /libs", {countNodes("/libs")})
t10 = Thread.start("countNodes /home", {countNodes("/home")})

log.info("Done starting countNodes threads");

t1.join();
t2.join();
t3.join();
t4.join();
t5.join();
t6.join();
t7.join();
t8.join();
t9.join();
t10.join();

log.info("Done running countNodes");

null
