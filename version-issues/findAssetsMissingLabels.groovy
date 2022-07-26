// findAssetsMissingLabels.groovy
//
// Find assets which have labels which are missing versions.

import com.day.cq.commons.jcr.JcrConstants

def DEBUG = false
def REPAIR = true

// For each asset under a folder, call function to find if any labels in version history are invalid (don't have an associated UUID).
def startFolder = "/content/dam/broadcom/techdocs/us/en/dita/ca-enterprise-software/it-operations-management/performance-management/capm_consolidated/content"
def testAssetPath = "/content/dam/broadcom/techdocs/us/en/dita/ca-enterprise-software/it-operations-management/performance-management/capm_consolidated/content/automate-tenant-configuration-with-rest-web-services.dita"

println "Assets containing labels without versions, starting with folder $startFolder:"
def count = 0
def query = createAssetQuery( startFolder )
if (DEBUG) println "query = ${query.statement}"
def result = query.execute()
def rows = result.rows
if (DEBUG) println "found ${rows.size} result(s)"
rows.each 
{ row ->
    def assetPath = row.path
    // if (DEBUG) println assetPath
    if (true || assetPath == testAssetPath)
    {
        if (DEBUG) println "assetPath is '$assetPath'"
        def valid = labelIsValidForAsset( assetPath, DEBUG, REPAIR )
        if (DEBUG) println "valid is '$valid'"
        if (! valid)
        {
            count++
            println "$count $assetPath"
        }
    }
}

println "Total of $count assets found."


// Given an asset path return false if any label does not have a valid UUID.
def labelIsValidForAsset( assetPath, DEBUG, REPAIR )
{
    def exists = true
    def assetNode = getNode( assetPath )
    if (assetNode)
    {
        def isVersionable = hasVersionableMixin( assetNode )
        if (isVersionable)
        {
            def assetUuid = assetNode.get(JcrConstants.JCR_UUID)
            if (DEBUG) println "assetUuid is '$assetUuid'"
            def versionLabelsPath = createVersionLabelsPathForUUID( assetUuid )         
            if (DEBUG) println "versionLabelsPath is '$versionLabelsPath'"
            def versionLabelsNode = getNode( versionLabelsPath )
            if (versionLabelsNode)
            {
                // Get all properties in node (excluding jcr:primaryType)
                def properties = versionLabelsNode.properties
                properties.each
                { property -> 
    
                    if (REPAIR || exists)
                    {
                        def label = property.name
                        if (DEBUG) println "label is '$label'"                
                        if ("jcr:primaryType" != label)
                        {
                            def versionUuid = property.string
                            if (DEBUG) println "versionUuid is '$versionUuid'"
                            if (! uuidExists( versionUuid ))
                            {
                                exists = false
                                if (DEBUG) println "Version UUID $versionUuid does not exist for label $label in asset $assetPath"
                                if (REPAIR)
                                {
                                    removeLabel( assetPath, label )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    exists
}

// Found at com.adobe.fmdita.versioncontrol.VersionUtils.java:
//   javax.jcr.version.VersionHistory versionHistory = session.getWorkspace().getVersionManager().getVersionHistory(path);
//   versionHistory.removeVersionLabel(label);
def removeLabel( path, label )
{
	def versionHistory = session.getWorkspace().getVersionManager().getVersionHistory( path )
	versionHistory.removeVersionLabel( label )
}

def hasVersionableMixin( assetNode )
{
    def hasVersionable = false
    def nodeTypes = assetNode.getMixinNodeTypes()
    nodeTypes.each
    { nodeType ->
    
        if (! hasVersionable)
        {
            if (nodeType.isNodeType( JcrConstants.MIX_VERSIONABLE ))
                hasVersionable = true
        }
    }
    hasVersionable
}

def createVersionLabelsPathForUUID( uuid )
{
    def pair1 = uuid.substring(0,2)
    def pair2 = uuid.substring(2,4)
    def pair3 = uuid.substring(4,6)
    def path = "/jcr:system/jcr:versionStorage/$pair1/$pair2/$pair3/$uuid/jcr:versionLabels"
    path
}

def uuidExists( UUID )
{
    def query = createUUIDQuery(UUID)
    def result = query.execute()
    def rows = result.rows
    1 == rows.size
}

def createUUIDQuery( UUID ) 
{
    def queryManager = session.workspace.queryManager
    def statement = "//*[@jcr:uuid = '$UUID']"
    def query = queryManager.createQuery(statement, "xpath")
    query
}

def createAssetQuery( assetFolder )
{
    def queryManager = session.workspace.queryManager
    def statement = "/jcr:root$assetFolder//element(*,dam:Asset)"
    def query = queryManager.createQuery(statement, "xpath")
    query
}