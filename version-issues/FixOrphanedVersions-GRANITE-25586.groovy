import org.apache.jackrabbit.oak.plugins.nodetype.TypePredicate
import org.apache.jackrabbit.oak.spi.commit.CommitInfo
import org.apache.jackrabbit.oak.spi.commit.EmptyHook
import org.apache.jackrabbit.oak.spi.state.NodeBuilder
import org.apache.jackrabbit.oak.spi.state.NodeStore

import static org.apache.jackrabbit.JcrConstants.JCR_SYSTEM
import static org.apache.jackrabbit.JcrConstants.JCR_VERSIONSTORAGE
import static org.apache.jackrabbit.JcrConstants.NT_VERSIONHISTORY

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This script removes version histories with no crx.default property (referencing
 * the versionable).
 */
class NPR21684Fixer {
    final Logger log = LoggerFactory.getLogger(getClass())

    private final NodeStore ns
    
    private final NodeBuilder rootBuilder

    private final TypePredicate isVersionHistory

    private NPR21684Fixer(NodeBuilder rootBuilder, NodeStore ns) {
        this.ns = ns
        this.rootBuilder = rootBuilder
        this.isVersionHistory = new TypePredicate(rootBuilder.getNodeState(), NT_VERSIONHISTORY)
    }

    private void traverse() {
        doLog("Removing orphaned version histories")
        traverse(rootBuilder.getChildNode(JCR_SYSTEM).getChildNode(JCR_VERSIONSTORAGE))
        ns.merge(rootBuilder, EmptyHook.INSTANCE, CommitInfo.EMPTY)
        doLog("Removed orphaned version histories")
    }

    private void traverse(NodeBuilder builder) {
        for (String childName : builder.getChildNodeNames()) {
            NodeBuilder child = builder.getChildNode(childName)
            if (isVersionHistory.apply(child.getNodeState())) {
                fixVersionHistory(child)
            } else {
                traverse(child)
            }
        }
    }

    private boolean fixVersionHistory(NodeBuilder versionHistory) {
        if (!versionHistory.hasProperty("crx.default")) {
            doLog("Removing " + versionHistory)
            versionHistory.remove()
        }

    }

    private void doLog(String message) {
        log.info(message);
    }
}

def runFixer(session) {
    NodeStore ns = session.getRootNode().sessionDelegate.root.store
    def rootBuilder = ns.root.builder()
    new NPR21684Fixer(rootBuilder, ns).traverse()
    null
}

def fixVersions() {
 def repo = osgi.getService(org.apache.sling.jcr.api.SlingRepository)
 def session = repo.loginAdministrative(null)
 try {
  runFixer(session)
 } finally {
  session.logout()
 }
}

fixVersions()
