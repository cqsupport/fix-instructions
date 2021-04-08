
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import groovy.transform.EqualsAndHashCode
import org.apache.commons.lang3.StringUtils
import org.apache.jackrabbit.oak.api.CommitFailedException
import org.apache.jackrabbit.oak.api.PropertyState
import org.apache.jackrabbit.oak.api.Type
import org.apache.jackrabbit.oak.plugins.nodetype.TypePredicate
import org.apache.jackrabbit.oak.spi.commit.CommitInfo
import org.apache.jackrabbit.oak.spi.commit.DefaultEditor
import org.apache.jackrabbit.oak.spi.commit.Editor
import org.apache.jackrabbit.oak.spi.commit.EditorProvider
import org.apache.jackrabbit.oak.spi.commit.EmptyHook
import org.apache.jackrabbit.oak.spi.state.ChildNodeEntry
import org.apache.jackrabbit.oak.spi.state.NodeBuilder
import org.apache.jackrabbit.oak.spi.state.NodeState
import org.apache.jackrabbit.oak.spi.state.NodeStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set

import static org.apache.jackrabbit.JcrConstants.JCR_BASEVERSION
import static org.apache.jackrabbit.JcrConstants.JCR_FROZENMIXINTYPES
import static org.apache.jackrabbit.JcrConstants.JCR_FROZENNODE
import static org.apache.jackrabbit.JcrConstants.JCR_ISCHECKEDOUT
import static org.apache.jackrabbit.JcrConstants.JCR_MIXINTYPES
import static org.apache.jackrabbit.JcrConstants.JCR_PREDECESSORS
import static org.apache.jackrabbit.JcrConstants.JCR_ROOTVERSION
import static org.apache.jackrabbit.JcrConstants.JCR_SUCCESSORS
import static org.apache.jackrabbit.JcrConstants.JCR_SYSTEM
import static org.apache.jackrabbit.JcrConstants.JCR_UUID
import static org.apache.jackrabbit.JcrConstants.JCR_VERSIONHISTORY
import static org.apache.jackrabbit.JcrConstants.JCR_VERSIONSTORAGE
import static org.apache.jackrabbit.JcrConstants.MIX_VERSIONABLE
import static org.apache.jackrabbit.JcrConstants.NT_FROZENNODE
import static org.apache.jackrabbit.JcrConstants.NT_VERSION
import static org.apache.jackrabbit.JcrConstants.NT_VERSIONHISTORY
import static org.apache.jackrabbit.oak.api.Type.NAMES
import static org.apache.jackrabbit.oak.api.Type.REFERENCE
import static org.apache.jackrabbit.oak.api.Type.REFERENCES
import static org.apache.jackrabbit.oak.plugins.memory.MultiGenericPropertyState.nameProperty

import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * This script removes invalid successor and predecessor references which may be
 * result of the OAK-5193 bug (https://issues.apache.org/jira/browse/OAK-5193).
 * It should be run with the oak-run:
 *
 * <pre>
 *   java -jar oak-run-*.jar console repository/segmentstore ":load OAK-5193-fix.groovy" | tee script.log
 * </pre>
 */
class Oak5193Fixer {
    final Logger log = LoggerFactory.getLogger(getClass())

    private final NodeStore ns
  
    private final NodeBuilder rootBuilder

    private final TypePredicate isVersionHistory

    private final TypePredicate isVersion

    private Oak5193Fixer(NodeBuilder rootBuilder, NodeStore ns) {
        this.rootBuilder = rootBuilder
        this.ns = ns
        this.isVersionHistory = new TypePredicate(rootBuilder.getNodeState(), NT_VERSIONHISTORY)
        this.isVersion = new TypePredicate(rootBuilder.getNodeState(), NT_VERSION)
    }

    private void traverse() {
        traverse(rootBuilder.getChildNode(JCR_SYSTEM).getChildNode(JCR_VERSIONSTORAGE))
        ns.merge(rootBuilder, EmptyHook.INSTANCE, CommitInfo.EMPTY)
        doLog("Merged fixes")
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
        log.debug("Validating " + versionHistory)
        BiMap<String, String> uuidName = HashBiMap.create()
        for (String childName : versionHistory.getChildNodeNames()) {
            NodeBuilder child = versionHistory.getChildNode(childName)
            if (isVersion.apply(child.getNodeState())) {
                String uuid = child.getProperty(JCR_UUID).getValue(Type.STRING)
                uuidName.put(uuid, childName)
            }
        }
        for (String versionName : uuidName.values()) {
            NodeBuilder version = versionHistory.getChildNode(versionName)
            Set<String> predecessors = Sets.newLinkedHashSet((Iterable<String>) version.getProperty(JCR_PREDECESSORS).getValue(Type.REFERENCES))
            Set<String> invalidPredecessors = Sets.difference(predecessors, uuidName.keySet())

            for (String invalidPredecessorUuid : invalidPredecessors) {
                String validPredecessor = fixInvalidPredecessor(versionHistory, uuidName, invalidPredecessorUuid, versionName, version)
                doLog(versionHistory.toString() + "    (!) fixed predecessor            " + versionName + " <- " + validPredecessor)
            }

            for (String invalidSuccessorUuid : intersectReferences(version, JCR_SUCCESSORS, uuidName.keySet())) {
                doLog(versionHistory.toString() + "    (!) removed invalid successors   " + versionName + " -> " + invalidSuccessorUuid)
            }
        }

        for (String versionName : uuidName.values()) {
            NodeBuilder version = versionHistory.getChildNode(versionName)

            for (String predecessor : setMissingSuccessors(versionHistory, uuidName, version)) {
               doLog(versionHistory.toString() + "    (!) created missing successor   " + predecessor + " -> " + versionName)
            }

            for (String successor : removeRedundantSuccessors(versionHistory, uuidName, version)) {
                doLog(versionHistory.toString() + "    (!) removed redundant successor " + versionName + " -> " + successor)
            }
        }
    }

    private String fixInvalidPredecessor(NodeBuilder versionHistory, BiMap<String, String> uuidName, String invalidPredecessorUuid, String versionName, NodeBuilder version) {
        String validPredecessorName = closestVersion(versionName, uuidName.values())

        Set<String> predecessors = Sets.newLinkedHashSet(version.getProperty(JCR_PREDECESSORS).getValue(Type.REFERENCES))
        predecessors.remove(invalidPredecessorUuid)
        if (validPredecessorName != null) {
            predecessors.add(uuidName.inverse().get(validPredecessorName))
        }
        version.setProperty(JCR_PREDECESSORS, predecessors, Type.REFERENCES)

        return validPredecessorName
    }

    private Set<String> intersectReferences(NodeBuilder builder, String propertyName, Set<String> references) {
        Set<String> currentValue = Sets.newLinkedHashSet((Iterable<String>) builder.getProperty(propertyName).getValue(Type.REFERENCES))
        Set<String> intersection = Sets.intersection(currentValue, references)
        if (currentValue != intersection) {
            builder.setProperty(propertyName, intersection, Type.REFERENCES)
            return Sets.difference(currentValue, references)
        } else {
            return Collections.emptySet()
        }
    }

    private static List<String> setMissingSuccessors(NodeBuilder versionHistory, BiMap<String, String> uuidName, NodeBuilder version) {
        String uuid = version.getProperty(JCR_UUID).getValue(Type.STRING)
        Set<String> predecessors = Sets.newLinkedHashSet((Iterable<String>) version.getProperty(JCR_PREDECESSORS).getValue(Type.REFERENCES))

        List<String> fixedPredecessors = new ArrayList<>()
        for (String pUuid : predecessors) {
            String pName = uuidName.get(pUuid)
            NodeBuilder predecessor = versionHistory.getChildNode(pName)
            Set<String> successors = Sets.newLinkedHashSet((Iterable<String>) predecessor.getProperty(JCR_SUCCESSORS).getValue(Type.REFERENCES))
            if (!successors.contains(uuid)) {
                successors.add(uuid)
                predecessor.setProperty(JCR_SUCCESSORS, successors, Type.REFERENCES)
                fixedPredecessors.add(pName)
            }
        }
        return fixedPredecessors
    }

    private static List<String> removeRedundantSuccessors(NodeBuilder versionHistory, BiMap<String, String> uuidName, NodeBuilder version) {
        String uuid = version.getProperty(JCR_UUID).getValue(Type.STRING)
        Set<String> successors = Sets.newLinkedHashSet((Iterable<String>) version.getProperty(JCR_SUCCESSORS).getValue(Type.REFERENCES))

        List<String> removedSuccessors = new ArrayList<>()
        Iterator<String> successorsIt = successors.iterator()
        while (successorsIt.hasNext()) {
            String sUuid = successorsIt.next()
            String sName = uuidName.get(sUuid)
            NodeBuilder successor = versionHistory.getChildNode(sName)
            Set<String> predecessors = Sets.newLinkedHashSet((Iterable<String>) successor.getProperty(JCR_PREDECESSORS).getValue(Type.REFERENCES))
            if (!predecessors.contains(uuid)) {
                removedSuccessors.add(sName)
                successorsIt.remove()
            }
        }
        if (!removedSuccessors.isEmpty()) {
            version.setProperty(JCR_SUCCESSORS, successors, Type.REFERENCES)
        }
        return removedSuccessors
    }

    static String closestVersion(String version, Set<String> versions) {
        String v = version
        while (true) {
            v = decreaseVersion(v)
            if (v == JCR_ROOTVERSION || versions.contains(v)) {
                return v
            }
        }
    }

    static String decreaseVersion(String version) {
        if (version == JCR_ROOTVERSION) {
            return null
        } else if (version == "1.0") {
            return JCR_ROOTVERSION
        }

        String[] split = StringUtils.split(version, '.')
        String[] result
        int lastSegment = Integer.valueOf(split[split.length - 1])
        if (lastSegment == 0) {
            result = new String[split.length - 1]
            for (int i = 0; i < result.length; i++) {
                result[i] = split[i]
            }
        } else {
            result = split
            result[split.length - 1] = String.valueOf(lastSegment - 1)
        }
        return StringUtils.join(result, '.')
    }
    
    private void doLog(String message) {
        log.info(message);
    }
}

def test() {
    def decSeq = { String version ->
        List<String> seq = new ArrayList<>()
        String v = version
        while (v != null) {
            seq.add(v)
            v = Oak5193Fixer.decreaseVersion(v)
        }
        return seq
    }
    assert decSeq("1.2.1.6") == ["1.2.1.6", "1.2.1.5", "1.2.1.4", "1.2.1.3", "1.2.1.2", "1.2.1.1", "1.2.1.0", "1.2.1", "1.2.0", "1.2", "1.1", "1.0", "jcr:rootVersion"]
    assert decSeq("1.0") == ["1.0", "jcr:rootVersion"]
    assert decSeq("jcr:rootVersion") == ["jcr:rootVersion"]

    assert Oak5193Fixer.closest("1.2.1.6", ["1.3", "1.2.1.6", "1.2.1", "1.2", "jcr:rootVersion"]) == "1.2.1"
    assert Oak5193Fixer.closest("1.2.1.6", ["1.3", "1.2.1.5", "1.2.1", "1.2", "jcr:rootVersion"]) == "1.2.1.5"
    assert Oak5193Fixer.closest("1.3", ["1.3", "1.1", "jcr:rootVersion"]) == "1.1"
    assert Oak5193Fixer.closest("1.3", ["1.3", "jcr:rootVersion"]) == "jcr:rootVersion"
    assert Oak5193Fixer.closest("1.0", ["1.0", "jcr:rootVersion"]) == "jcr:rootVersion"
    assert Oak5193Fixer.closest("jcr:rootVersion", ["jcr:rootVersion"]) == null

}

def runFixer(session) {
    NodeStore ns = session.getRootNode().sessionDelegate.root.store
    def rootBuilder = ns.root.builder()
    new Oak5193Fixer(rootBuilder, ns).traverse()
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
