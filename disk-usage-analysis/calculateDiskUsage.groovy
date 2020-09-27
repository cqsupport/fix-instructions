//Adaptation of @stillalex's script from here https://gist.github.com/stillalex/06303f8cc1d3780d3eab4c72575883ae
//This version works with Oak 1.6 and later versions
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.jcr.*
 import org.apache.jackrabbit.oak.api.Type
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.spi.state.NodeStore
import org.apache.jackrabbit.oak.commons.PathUtils
import com.google.common.collect.Lists
import java.util.List

def String humanReadableByteCount(long bytes, boolean si) {
 try {
  int unit = si ? 1000 : 1024;
  if (bytes < unit) return bytes + " B";
  int exp = (int)(Math.log(bytes) / Math.log(unit));
  String pre = new String((si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)) + (si ? "" : "");
  return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
 } catch (Exception e) {
  out.print "humanReadableByteCount: " + e.getMessage()
 }
}

def countNodes(NodeState n, int level, String path = "/", Integer flush = 100000, AtomicInteger count = new AtomicInteger(0), AtomicInteger binaries = new AtomicInteger(0), root = true, AtomicLong runningBytes = new AtomicLong(0)) {
 if (root) {
  out.println "Counting nodes in tree ${path}"
  n = getNodeForPath(n, path);
 }

 def counts = [bytes: 0, binaryCount: 0, nodeCount: 1]
 cnt = count.incrementAndGet()
 if (cnt % flush == 0) out.println("  " + cnt)
 long bytes = 0;
 try {
  for (prop in n.getProperties()) {
   if (prop.getType() == Type.BINARY) {
    binaries.incrementAndGet();
    counts.binaryCount++;
    bytes = prop.getValue(prop.getType()).length();
    counts.bytes += bytes
    runningBytes.getAndAdd(bytes);
   } else if (prop.getType() == Type.BINARIES) {
    for (b in prop.getValue(Type.BINARIES)) {
     binaries.incrementAndGet()
     counts.binaryCount++;
     bytes = b.length();
     counts.bytes += bytes
     runningBytes.getAndAdd(bytes)
    }
   } else {
    bytes = getSizeInBytes(prop)
    //out.println "Property - " + prop.getName() + ": " + bytes + " bytes"
    counts.bytes += bytes
    runningBytes.getAndAdd(bytes)
   }
  }
  String pathSeparator = "/";
  if (path.equals("/")) pathSeparator = "";
  for (child in n.getChildNodeEntries()) {
   def childCounts = countNodes(child.getNodeState(), level + 1, path + pathSeparator + child.getName(), flush, count, binaries, false, runningBytes)
   counts.bytes += childCounts.bytes
   counts.binaryCount += childCounts.binaryCount
   counts.nodeCount += childCounts.nodeCount
  }
 } catch (e) {
  out.println "warning unable to read node ${path}: " + e.getMessage()
 }

 if (root) {
  String readableBytes = humanReadableByteCount(counts.bytes, false)
  out.println "Total nodes in tree ${path}: ${counts.nodeCount}"
  out.println "Total binaries in tree ${path}: ${counts.binaryCount}"
  out.println "Total bytes in tree ${path}: ${readableBytes}"
 } else if (level <= 3) {
  String readableBytes = humanReadableByteCount(counts.bytes, false)
  String binaryCnt = binaries.get();
  out.println "${path}, bytes: ${readableBytes}"
 }

 return counts
}

private long getSizeInBytes(def prop) {
 long runningBytes = 0
 if (prop.isArray()) {
  try {
   def vals = prop.getValue(prop.getType())
   boolean first = true;
   for (val in vals) {
     runningBytes += getSizeOfType(val);
   }
  } catch (Exception e) {
   out.println "error reading property " + e.getMessage()
  }
 } else {
   runningBytes += prop.size();
 }
 return runningBytes;
}

private getSizeOfType(def val) {
    int size = 0
      if (val instanceof Double) {
       size = 10
      } else if(val instanceof Long) {
       size = 6
      } else if(val instanceof String) {
       size = 29
      } else if(val instanceof Boolean) {
       size = 4
      } else if(val instanceof BigDecimal) {
       size = 6
      }
    return size
}

private getNodeForPath(def rootNodeBuilder, def argpath) {
 def nb = rootNodeBuilder
 String path;
 if (PathUtils.isAbsolute(argpath)) {
  path = argpath;
 } else {
  path = PathUtils.concat(session.getWorkingPath(), argpath);
 }
 List < String > elements = Lists.newArrayList();
 PathUtils.elements(path).each {
  String element ->
   if (PathUtils.denotesParent(element)) {
    if (!elements.isEmpty()) {
     elements.remove(elements.size() - 1);
    }
   } else
  if (!PathUtils.denotesCurrent(element)) {
   elements.add(element);
  }
 }

 elements.each {
  if (it.size() > 0) {
   nb = nb.getChildNode(it)
  }
 }
 return nb
}


def countNodes(session, path) {
 NodeStore nstore = session.getRootNode().sessionDelegate.root.store
 def rs = nstore.root
 def ns = rs
 out.println("Running node count and size estimate: WARNING - this isn't a perfectly accurate calculation of repository size, it is just an estimation")
 totalBytes = countNodes(ns, 0, path)
 out.println("Done: " + totalBytes)
 null
}

def countNodes(path) {
 def repo = osgi.getService(org.apache.sling.jcr.api.SlingRepository)
 def session = repo.loginAdministrative(null)
 try {
  countNodes(session, path)
 } finally {
  session.logout()
 }
}

countNodes("/")
