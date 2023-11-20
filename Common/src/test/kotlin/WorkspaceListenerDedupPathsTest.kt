import com.tabnineCommon.lifecycle.WorkspaceListenerService
import org.junit.Test

class WorkspaceListenerDedupPathsTest {
    @Test
    fun shouldNotDedupDisjointPaths() {
        val rootPaths = listOf("/path1", "/path2", "/path3")
        val dedupedPaths = WorkspaceListenerService.Companion.dedupRootPaths(rootPaths)
        assert(dedupedPaths == rootPaths)
    }

    @Test
    fun shouldNotDedupDifferentPaths() {
        val rootPaths = listOf("/root/path1", "/root/path2", "/root/path3")
        val dedupedPaths = WorkspaceListenerService.Companion.dedupRootPaths(rootPaths)
        assert(dedupedPaths == rootPaths)
    }

    @Test
    fun shouldDedupSubPaths() {
        val rootPaths = listOf("/root/path1", "/root/path1/path2", "/root/path3")
        val dedupedPaths = WorkspaceListenerService.Companion.dedupRootPaths(rootPaths)
        assert(dedupedPaths == listOf("/root/path1", "/root/path3"))
    }
}
