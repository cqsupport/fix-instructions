import org.apache.jackrabbit.oak.spi.commit.CommitInfo
import org.apache.jackrabbit.oak.spi.commit.EmptyHook
import org.apache.jackrabbit.oak.spi.state.NodeStore
import org.apache.jackrabbit.oak.commons.PathUtils
import com.google.common.collect.Lists
import java.util.List

import org.slf4j.Logger
import org.slf4j.LoggerFactory


public class VersionPropRemover {
    final Logger log = LoggerFactory.getLogger(getClass())
	def out;
  public VersionPropRemover(def out) {
     this.out = out 
  }
    public removeVersionProps(def session, def argpath, def isDryRun) {
        if (!PathUtils.isValid(argpath)) {
            doLog("Not a valid path: " + argpath);
            return;
        }
        String path;
        if (PathUtils.isAbsolute(argpath)) {
            path = argpath;
        }
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
        
        NodeStore nstore = session.getRootNode().sessionDelegate.root.store
        def rs = nstore.root
        def ns = rs
        def rnb = rs.builder()
        //def nb = rnb;
        elements.each {
          if(it.size() > 0) {
            ns = ns.getChildNode(it)
          }
        }
        removeVersionPropsRecurse(session, nstore, argpath, ns, isDryRun)        
        session.refresh(true);
    }

    private removeVersionPropsRecurse(def session, def nodeStore, def curPath, def ns, def isDryRun) {
        def entryIter = ns.getChildNodeEntries()
        def rnb = nodeStore.root.builder()
        def nb = getNodeBuilderForPath(rnb, curPath);
        removeVersionPropertiesFromNode(nodeStore, ns, nb, rnb, curPath, isDryRun)

        /*//doLog(curPath);
        entryIter.each {
            def cnb = getNodeBuilderForPath(rnb, curPath + "/" + it.getName());
            removeVersionPropertiesFromNode(nodeStore, it, cnb, rnb, curPath + "/" + it.getName(), isDryRun)
            removeVersionPropsRecurse(session, nodeStore, curPath + "/" + it.getName(), it, isDryRun)
        }*/
    }
  
    private removeVersionPropertiesFromNode(def nodeStore, def ns, def nb, def rnb, def curPath, def isDryRun) {
        def props = ns.getProperties()
        props.each { prop ->
            if("jcr:mixinTypes".equals(prop.getName())) {
                def newMixins = null;
                def firstVal = true;
                def hasMixVersionable = false;
                def mixins = prop.getValue(prop.getType())
                mixins.each{ mixin ->
                    if(!"mix:versionable".equals(mixin)) {
                        if(firstVal) {
                            newMixins = Lists.newArrayList(mixin)
                            firstVal = false
                        } else {
                            newMixins.add(mixin)
                        }
                    } else {
                        hasMixVersionable = true;
                    }
                }
                if(hasMixVersionable) {
                    doLog(((isDryRun)?"(Dry run) ":"") + "Removing mix:versionable from " + prop + " from " + curPath)
                    if(newMixins == null) {
                         if(!isDryRun) nb.setProperty(prop.getName(), Lists.newArrayList(), org.apache.jackrabbit.oak.api.Type.REFERENCES)
                    } else {
                         if(!isDryRun) nb.setProperty(prop.getName(), newMixins, org.apache.jackrabbit.oak.api.Type.REFERENCES)

                    }
                }
            }
            if("jcr:baseVersion".equals(prop.getName())) {
                doLog(((isDryRun)?"(Dry run) ":"") + "Removing " + prop + " from " + curPath)
                 if(!isDryRun) nb.removeProperty("jcr:baseVersion")
            }
            if("jcr:predecessors".equals(prop.getName())) {
                doLog(((isDryRun)?"(Dry run) ":"") + "Removing " + prop + " from " + curPath)
                 if(!isDryRun) nb.removeProperty("jcr:predecessors")
            }
            if("jcr:versionHistory".equals(prop.getName())) {
                doLog(((isDryRun)?"(Dry run) ":"") + "Removing " + prop + " from " + curPath)
                 if(!isDryRun) nb.removeProperty("jcr:versionHistory")
            }
            if(!isDryRun) nodeStore.merge(rnb, EmptyHook.INSTANCE, CommitInfo.EMPTY);           
        }
    }
    
    private getNodeBuilderForPath(def rootNodeBuilder, def argpath) {
        def nb = rootNodeBuilder
        String path;
        if (PathUtils.isAbsolute(argpath)) {
            path = argpath;
        } else {
            path = PathUtils.concat(session.getWorkingPath(), argpath);
        }
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
        return nb
    }
    
    private void doLog(def message) {
       out.println(message) 
       //log.info(message);
    }
}

def removeVersionProps(session, argpath) {
    NodeStore ns = session.getRootNode().sessionDelegate.root.store
    def rootBuilder = ns.root.builder()
    // remove versions of test.pdf
    new VersionPropRemover(out).removeVersionProps(session, argpath, false)
    null
}

def removeVersionProps(def argpath) {
 def repo = osgi.getService(org.apache.sling.jcr.api.SlingRepository)
 def session = repo.loginAdministrative(null)
 try {
  runFixer(session, argpath)
 } finally {
  session.logout()
 }
}
