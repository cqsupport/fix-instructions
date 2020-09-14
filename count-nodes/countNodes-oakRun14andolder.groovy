import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger
import org.apache.jackrabbit.oak.api.Type
import org.apache.jackrabbit.oak.plugins.segment.SegmentBlob
import org.apache.jackrabbit.oak.spi.state.NodeState

def countNodes(NodeState n, deep = false, String path = "/", Integer flush = 50000, AtomicInteger count = new AtomicInteger(0), AtomicInteger binaries = new AtomicInteger(0), root = true) {
  if(root) {
    println "Counting nodes in tree ${path}"
  }

  cnt = count.incrementAndGet()
  if (cnt % flush == 0) println("  " + cnt)
  def propname = "";
  try {
    try {

        for(prop in n.getProperties()) {
          propname = prop.getName();
          if(prop.getType() == Type.BINARY || prop.getType() == Type.BINARIES) {
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
              } else if(b instanceof SegmentBlob) {
                if(!((SegmentBlob)b).isExternal()) {
                  b.length()
                }
              } else {
                b.length()
              }
            }
          } else {
            if(prop.isArray()) {
                for(sf in prop.getValue(prop.getType())) {
                    // do nothing - we just need to read all values
                }
            } else {
                prop.getValue(prop.getType());
            }

          }
        }
    } catch(ex) {
        println "warning unable to read node ${path}@" + propname + " : " + ex.getMessage()
    }
    propname = "";

    for(child in n.getChildNodeEntries()) {
      countNodes(child.getNodeState(), deep, path + child.getName() + "/", flush, count, binaries, false)
    }
  } catch(e) {
    println "warning unable to read node ${path} : " + e.getMessage()
  }

  if(root) {
    println "Total nodes in tree ${path}: ${cnt}"
    println "Total binaries in tree ${path}: ${binaries.get()}"
  }

  return cnt
}

countNodes(session.workingNode)
