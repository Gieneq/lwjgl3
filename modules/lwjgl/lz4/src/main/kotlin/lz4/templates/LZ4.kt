/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package lz4.templates

import org.lwjgl.generator.*
import lz4.*

val LZ4 = "LZ4".nativeClass(Module.LZ4, prefix = "LZ4", prefixMethod = "LZ4_", library = LZ4_LIBRARY) {
    nativeDirective(
        """DISABLE_WARNINGS()
#include "lz4.h"
ENABLE_WARNINGS()""")

    documentation =
        """
        Native bindings to ${url("http://lz4.github.io/lz4/", "LZ4")}, a lossless compression algorithm, providing compression speed at 400 MB/s per core,
        scalable with multi-cores CPU. It features an extremely fast decoder, with speed in multiple GB/s per core, typically reaching RAM speed limits on
        multi-core systems.

        Speed can be tuned dynamically, selecting an "acceleration" factor which trades compression ratio for more speed up. On the other end, a high
        compression derivative, {@code LZ4_HC}, is also provided, trading CPU time for improved compression ratio. All versions feature the same decompression
        speed.

        The raw LZ4 block compression format is detailed within <a href="https://github.com/lz4/lz4/blob/dev/doc/lz4_Block_format.md">lz4_Block_format</a>.

        To compress an arbitrarily long file or data stream, multiple blocks are required. Organizing these blocks and providing a common header format to
        handle their content is the purpose of the Frame format, defined into
        <a href="https://github.com/lz4/lz4/blob/dev/doc/lz4_Frame_format.md">lz4_Frame_format</a>. Interoperable versions of LZ4 must respect this frame
        format.
        """

    IntConstant(
        "Version number part.",

        "VERSION_MAJOR".."1",
        "VERSION_MINOR".."8",
        "VERSION_RELEASE".."1"
    )

    IntConstant("Version number.", "VERSION_NUMBER".."(LZ4_VERSION_MAJOR *100*100 + LZ4_VERSION_MINOR *100 + LZ4_VERSION_RELEASE)")
    StringConstant("Version string.", "VERSION_STRING".."""LZ4_VERSION_MAJOR + "." + LZ4_VERSION_MINOR + "." + LZ4_VERSION_RELEASE""")

    int("versionNumber", "Returns the version number.")
    Nonnull..const..charASCII_p("versionString", "Returns the version string.")

    IntConstant(
        "Maximum input size.",

        "MAX_INPUT_SIZE"..0x7E000000
    )

    IntConstant(
        """
        Memory usage formula : {@code N->2^N} Bytes (examples: {@code 10 -> 1KB; 12 -> 4KB ; 16 -> 64KB; 20 -> 1MB;} etc.)

        Increasing memory usage improves compression ratio. Reduced memory usage can improve speed, due to cache effect. Default value is 14, for 16KB, which
        nicely fits into Intel x86 L1 cache.
        """,
        "MEMORY_USAGE".."14"
    )

    IntConstant("", "HASHLOG".."(LZ4_MEMORY_USAGE - 2)")
    IntConstant("", "HASHTABLESIZE".."(1 << LZ4_MEMORY_USAGE)")
    IntConstant("", "HASH_SIZE_U32".."(1 << LZ4_HASHLOG)")

    IntConstant("", "STREAMSIZE_U64".."((1 << (LZ4_MEMORY_USAGE-3)) + 4)")
    IntConstant("", "STREAMSIZE".."(LZ4_STREAMSIZE_U64 * Long.BYTES)")

    IntConstant("", "STREAMDECODESIZE_U64".."4")
    IntConstant("", "STREAMDECODESIZE".."(LZ4_STREAMDECODESIZE_U64 * Long.BYTES)")

    int(
        "compress_default",
        """
        Compresses {@code srcSize} bytes from buffer {@code src} into already allocated {@code dst} buffer of size {@code dstCapacity}.

        Compression is guaranteed to succeed if {@code dstCapacity} &ge; #compressBound(){@code (srcSize)}. It also runs faster, so it's a recommended setting.

        If the function cannot compress {@code src} into a limited {@code dst} budget, compression stops <i>immediately</i>, and the function result is
        zero. As a consequence, {@code dst} content is not valid.

        This function never writes outside {@code dst} buffer, nor read outside {@code src} buffer.
        """,

        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..int.IN("srcSize", "supported max value is #MAX_INPUT_SIZE"),
        AutoSize("dst")..int.IN("dstCapacity", "full or partial size of buffer {@code dst} (which must be already allocated)"),

        returnDoc = "the number of bytes written into buffer {@code dest} (necessarily &le; {@code maxOutputSize}) or 0 if compression fails"
    )

    int(
        "decompress_safe",
        """
        If destination buffer is not large enough, decoding will stop and output an error code (negative value).

        If the source stream is detected malformed, the function will stop decoding and return a negative result.

        This function is protected against buffer overflow exploits, including malicious data packets. It never writes outside output buffer, nor reads outside
        input buffer.
        """,

        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..int.IN("compressedSize", "is the exact complete size of the compressed block"),
        AutoSize("dst")..int.IN("dstCapacity", "is the size of destination buffer, which must be already allocated"),

        returnDoc = "the number of bytes decompressed into destination buffer (necessarily &le; {@code dstCapacity})"
    )

    macro(expression = "LZ4_MAX_INPUT_SIZE < isize ? 0 : isize + isize / 255 + 16")..int(
        "COMPRESSBOUND",
        "See #compressBound().",

        int.IN("isize", "")
    )

    int(
        "compressBound",
        """
        Provides the maximum size that LZ4 compression may output in a "worst case" scenario (input data not compressible).

        This function is primarily useful for memory allocation purposes (destination buffer size). Macro #COMPRESSBOUND() is also provided for
        compilation-time evaluation (stack memory allocation for example).

        Note that #compress_default() compress faster when dest buffer size is &ge; #compressBound(){@code (srcSize)}
        """,

        int.IN("inputSize", "max supported value is #MAX_INPUT_SIZE"),

        returnDoc = "maximum output size in a \"worst case\" scenario or 0, if input size is too large (&gt; #MAX_INPUT_SIZE)"
    )

    int(
        "compress_fast",
        """
        Same as #compress_default(), but allows to select an "acceleration" factor.

        The larger the acceleration value, the faster the algorithm, but also the lesser the compression. It's a trade-off. It can be fine tuned, with each
        successive value providing roughly +~3% to speed. An acceleration value of "1" is the same as regular #compress_default(). Values &le; 0 will be
        replaced by {@code ACCELERATION_DEFAULT} (see {@code lz4.c}), which is 1.
        """,

        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..int.IN("srcSize", ""),
        AutoSize("dst")..int.IN("dstCapacity", ""),
        int.IN("acceleration", "")
    )

    int(
        "sizeofState",
        ""
    )

    int(
        "compress_fast_extState",
        """
        Same as #compress_fast(), just using an externally allocated memory space to store compression state.

        Use #sizeofState() to know how much memory must be allocated, and allocate it on 8-bytes boundaries (using {@code malloc()} typically). Then, provide
        it as {@code void* state} to compression function.
        """,

        Unsafe..void_p.OUT("state", ""),
        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..int.IN("srcSize", ""),
        AutoSize("dst")..int.IN("dstCapacity", ""),
        int.IN("acceleration", "")
    )

    int(
        "compress_destSize",
        """
        Reverse the logic: compresses as much data as possible from {@code src} buffer into already allocated buffer {@code dst} of size
        {@code targetDstSize}.

        This function either compresses the entire {@code src} content into {@code dst} if it's large enough, or fill {@code dst} buffer completely with as
        much data as possible from {@code src}.
        """,

        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..Check(1)..int_p.INOUT(
            "srcSizePtr",
            "will be modified to indicate how many bytes where read from {@code source} to fill {@code dest}. New value is necessarily &le; old value."
        ),
        AutoSize("dst")..int.IN("targetDstSize", ""),

        returnDoc = "nb bytes written into {@code dest} (necessarily &le; {@code targetDestSize}) or 0 if compression fails"
    )

    int(
        "decompress_fast",
        """
        This function respects memory boundaries for properly formed compressed data. It is a bit faster than #decompress_safe(). However, it does not
        provide any protection against intentionally modified data stream (malicious input). Use this function in trusted environment only (data to decode
        comes from a trusted source).
        """,

        Unsafe..const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("dst")..int.IN("originalSize", "is the original uncompressed size"),

        returnDoc =
        """
        the number of bytes read from the source buffer (in other words, the compressed size). If the source stream is detected malformed, the function will
        stop decoding and return a negative result. Destination buffer must be already allocated. Its size must be &ge; {@code originalSize} bytes.
        """
    )

    int(
        "decompress_safe_partial",
        """
        This function decompress a compressed block of size {@code compressedSize} at position {@code src} into destination buffer {@code dst} of size
        {@code dstCapacity}.

        The function will decompress a minimum of {@code targetOutputSize} bytes, and stop after that. However, it's not accurate, and may write more than
        {@code targetOutputSize} (but &le; {@code dstCapacity}).

        This function never writes outside of output buffer, and never reads outside of input buffer. It is therefore protected against malicious data packets.
        """,

        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..int.IN("compressedSize", ""),
        int.IN("targetOutputSize", ""),
        AutoSize("dst")..int.IN("dstCapacity", ""),

        returnDoc =
        """
        the number of bytes decoded in the destination buffer (necessarily &le; {@code dstCapacity})

        Note: this number can be &lt; {@code targetOutputSize} should the compressed block to decode be smaller. Always control how many bytes were decoded. If
        the source stream is detected malformed, the function will stop decoding and return a negative result.
        """
    )

    LZ4_stream_t_p(
        "createStream",
        "Allocates and initializes an {@code LZ4_stream_t} structure."
    )

    int(
        "freeStream",
        "Releases memory of an {@code LZ4_stream_t} structure.",

        LZ4_stream_t_p.IN("streamPtr", "")
    )

    void(
        "resetStream",
        "An {@code LZ4_stream_t} structure can be allocated once and re-used multiple times. Use this function to start compressing a new stream.",

        LZ4_stream_t_p.IN("streamPtr", "")
    )

    int(
        "loadDict",
        """
        Use this function to load a static dictionary into {@code LZ4_stream_t}.

        Any previous data will be forgotten, only {@code dictionary} will remain in memory. Loading a size of 0 is allowed, and is the same as reset.
        """,

        LZ4_stream_t_p.IN("streamPtr", ""),
        nullable..const..char_p.IN("dictionary", ""),
        AutoSize("dictionary")..int.IN("dictSize", "")
    )

    int(
        "compress_fast_continue",
        """
        Compress content into {@code src} using data from previously compressed blocks, improving compression ratio.

        {@code dst} buffer must be already allocated. If {@code dstCapacity} &ge; #compressBound(){@code (srcSize)}, compression is guaranteed to succeed, and
        runs faster.

        Important: Up to 64KB of previously compressed data is assumed to remain present and unmodified in memory!

        Special:
        ${ol(
            "If input buffer is a double-buffer, it can have any size, including &lt; 64 KB.",
            "If input buffer is a ring-buffer, it can have any size, including &lt; 64 KB."
        )}
        """,

        LZ4_stream_t_p.IN("streamPtr", ""),
        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..int.IN("srcSize", ""),
        AutoSize("dst")..int.IN("dstCapacity", ""),
        int.IN("acceleration", ""),

        returnDoc =
        """
        size of compressed block or 0 if there is an error (typically, compressed data cannot fit into {@code dst}). After an error, the stream status is
        invalid, it can only be reset or freed.
        """
    )

    int(
        "saveDict",
        """
        If previously compressed data block is not guaranteed to remain available at its current memory location, save it into a safer place
        ({@code char* safeBuffer}).

        Note: it's not necessary to call #loadDict() after #saveDict(), dictionary is immediately usable.
        """,

        LZ4_stream_t_p.IN("streamPtr", ""),
        char_p.OUT("safeBuffer", ""),
        AutoSize("safeBuffer")..int.IN("dictSize", ""),

        returnDoc = "saved dictionary size in bytes (necessarily &le; {@code dictSize}), or 0 if error"
    )

    LZ4_streamDecode_t_p(
        "createStreamDecode",
        """
        Creates a streaming decompression tracking structure.

        A tracking structure can be re-used multiple times sequentially.
        """
    )

    int(
        "freeStreamDecode",
        "Frees a streaming decompression tracking structure.",

        LZ4_streamDecode_t_p.IN("LZ4_stream", "")
    )

    intb(
        "setStreamDecode",
        """
        An {@code LZ4_streamDecode_t} structure can be allocated once and re-used multiple times. Use this function to start decompression of a new stream of
        blocks.

        A dictionary can optionnally be set. Use #NULL or size 0 for a simple reset order.
        """,

        LZ4_streamDecode_t_p.IN("LZ4_streamDecode", ""),
        const..char_p.IN("dictionary", ""),
        AutoSize("dictionary")..int.IN("dictSize", ""),

        returnDoc = "1 if OK, 0 if error"
    )

    int(
        "decompress_safe_continue",
        """
        These decoding functions allow decompression of consecutive blocks in "streaming" mode.

        A block is an unsplittable entity, it must be presented entirely to a decompression function. Decompression functions only accept one block at a time.
        Previously decoded blocks <i>must</i> remain available at the memory position where they were decoded (up to 64 KB).

        Special: if application sets a ring buffer for decompression, it must respect one of the following conditions:
        ${ul(
            """
            Exactly same size as encoding buffer, with same update rule (block boundaries at same positions) In which case, the decoding &amp; encoding ring
            buffer can have any size, including very small ones ( &lt; 64 KB).
            """,
            """
            Larger than encoding buffer, by a minimum of {@code maxBlockSize} more bytes.

            {@code maxBlockSize} is implementation dependent. It's the maximum size of any single block. In which case, encoding and decoding buffers do not
            need to be synchronized, and encoding ring buffer can have any size, including small ones ( &lt; 64 KB).
            """,
            """
            <i>At least</i> {@code 64 KB + 8 bytes + maxBlockSize}.

            In which case, encoding and decoding buffers do not need to be synchronized, and encoding ring buffer can have any size, including larger than
            decoding buffer.
            """
        )}
        Whenever these conditions are not possible, save the last 64KB of decoded data into a safe buffer, and indicate where it is saved using
        #setStreamDecode() before decompressing next block.
        """,

        LZ4_streamDecode_t_p.IN("LZ4_streamDecode", ""),
        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..int.IN("srcSize", ""),
        AutoSize("dst")..int.IN("dstCapacity", "")
    )

    int(
        "decompress_fast_continue",
        "See #decompress_safe_continue().",

        LZ4_streamDecode_t_p.IN("LZ4_streamDecode", ""),
        Unsafe..const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("dst")..int.IN("originalSize", "")
    )

    int(
        "decompress_safe_usingDict",
        """
        These decoding functions work the same as a combination of #setStreamDecode() followed by {@code LZ4_decompress_*_continue()}. They are stand-alone,
        and don't need an {@code LZ4_streamDecode_t} structure.
        """,

        const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("src")..int.IN("srcSize", ""),
        AutoSize("dst")..int.IN("dstCapacity", ""),
        const..char_p.IN("dictStart", ""),
        AutoSize("dictStart")..int.IN("dictSize", "")
    )

    int(
        "decompress_fast_usingDict",
        "See {@code decompress_safe_usingDict}.",

        Unsafe..const..char_p.IN("src", ""),
        char_p.OUT("dst", ""),
        AutoSize("dst")..int.IN("originalSize", ""),
        const..char_p.IN("dictStart", ""),
        AutoSize("dictStart")..int.IN("dictSize", "")
    )
}