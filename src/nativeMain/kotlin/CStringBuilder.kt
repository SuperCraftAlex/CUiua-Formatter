import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class, ExperimentalStdlibApi::class)
class CStringBuilder: AutoCloseable {

    var closed: Boolean = false
        private set

    private var allocSize = 256
    private var alloc = nativeHeap.allocArray<ByteVar>(allocSize)
    private var length = 0

    private fun ensureSpaceForExtra(extra: Int) {
        if (length + extra >= allocSize) {
            allocSize = allocSize * 2 + extra

            val newAlloc = nativeHeap.allocArray<ByteVar>(allocSize)
            for (i in 0..<length) {
                newAlloc[i] = alloc[i]
            }
            nativeHeap.free(alloc)
            alloc = newAlloc
        }
    }

    fun append(char: Byte) {
        ensureSpaceForExtra(1)
        alloc[length++] = char
    }

    fun append(str: String) {
        ensureSpaceForExtra(str.length)
        for (element in str) {
            alloc[length++] = element.code.toByte()
        }
    }

    operator fun plusAssign(char: Char) =
        append(char.code.toByte())

    override fun close() {
        if (closed) return
        nativeHeap.free(alloc)
        closed = true
    }

    override fun toString(): String {
        alloc[length] = 0
        return alloc.toKString()
    }

}