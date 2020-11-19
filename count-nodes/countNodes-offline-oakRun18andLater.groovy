//Adaptation of @stillalex's script from here https://gist.github.com/stillalex/06303f8cc1d3780d3eab4c72575883ae
//This version works with Oak 1.6 and later versions
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger
import org.apache.jackrabbit.oak.api.Type
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.spi.state.NodeStore

def countNodes(n, deep = false, String path = "/", Integer flush = 100000, AtomicInteger count = new AtomicInteger(0), AtomicInteger binaries = new AtomicInteger(0), root = true) {
  if(root) {
    println "Counting nodes in tree ${path}"
  }

  cnt = count.incrementAndGet()
  if (cnt % flush == 0) println("  " + cnt)

  try {
    try {
      for(prop in n.getProperties()) {
        if(prop.getType() == Type.BINARY || prop.getType() == Type.BINARIES) {
            for(b in prop.getValue(Type.BINARIES)) {
              binaries.incrementAndGet()
              if(b instanceof SegmentBlob) {
                if(!((SegmentBlob)b).isExternal()) {
                  b.length()
                }
              }
            }
        } else if(prop.getType() == Type.BINARY) {
          def b = prop.getValue(Type.BINARY);
          binaries.incrementAndGet()
          if(b instanceof SegmentBlob) {
            if(!((SegmentBlob)b).isExternal()) {
              b.length()
            }
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
      }
    } catch(e) {
      println "warning unable to read node properties ${path} : " + e.getMessage()
      org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e)
    }
    try {
      for(child in n.getChildNodeEntries()) {
        try {
          countNodes(child.getNodeState(), deep, path + child.getName() + "/", flush, count, binaries, false)
        } catch(e) {
          println "warning unable to read child node ${path} : " + e.getMessage()
	  org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e)
        }
      }
    } catch(e) {
      println "warning unable to read child entries ${path} : " + e.getMessage()
      org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e)
    }
  } catch(e) {
    println "warning unable to read node ${path} : " + e.getMessage()
    org.codehaus.groovy.runtime.StackTraceUtils.printSanitizedStackTrace(e)
  }

  if(root) {
    println "Total nodes in tree ${path}: ${cnt}"
    println "Total binaries in tree ${path}: ${binaries.get()}"
  }

  return cnt
}

countNodes(session.workingNode)
