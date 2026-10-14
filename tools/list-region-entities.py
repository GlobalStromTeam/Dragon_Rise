"""List the entity ids stored in a Minecraft Anvil entities region file.

Minecraft 1.21 keeps entities in a separate `entities/<x>.<z>.mca` region file whose
chunk payloads are ordinary NBT (zlib-compressed) with a top-level `Entities` list.
Reading it tells us exactly which entity types exist in the saved world, which is what
we need to identify the one whose renderer is missing (crash: EntityRenderDispatcher
returns a null renderer -> NPE in shouldRender).
"""

import gzip
import struct
import sys
import zlib
from pathlib import Path


def read_nbt_string(buf, i):
    """Return (string, next_index) for a length-prefixed modified-UTF8 string."""
    (n,) = struct.unpack_from(">H", buf, i)
    i += 2
    return buf[i:i + n].decode("utf-8", "replace"), i + n


def walk(buf, i, out, depth=0):
    """Recursively collect every string that follows a named-tag marker.

    A full NBT parser is overkill here; we only need compound keys and string
    values that could name an entity id.
    """
    if i >= len(buf) or depth > 32:
        return i
    tag = buf[i]
    i += 1
    if tag == 0:
        return i
    name, i = read_nbt_string(buf, i)
    if tag == 1:      # byte
        i += 1
    elif tag == 2:    # short
        i += 2
    elif tag == 3:    # int
        i += 4
    elif tag == 4:    # long
        i += 8
    elif tag == 5:    # float
        i += 4
    elif tag == 6:    # double
        i += 8
    elif tag == 7:    # byte array
        (n,) = struct.unpack_from(">i", buf, i)
        i += 4 + n
    elif tag == 8:    # string
        val, i = read_nbt_string(buf, i)
        if name in ("id", "Id", "identifier"):
            out.append(val)
    elif tag == 9:    # list
        (sub,) = struct.unpack_from(">b", buf, i)
        (n,) = struct.unpack_from(">i", buf, i + 1)
        i += 5
        if sub == 0:
            return i
        for _ in range(n):
            i = walk_payload(buf, i, sub, out, depth + 1)
    elif tag == 10:   # compound
        while True:
            if buf[i] == 0:
                i += 1
                break
            i = walk(buf, i, out, depth + 1)
    elif tag == 11:   # int array
        (n,) = struct.unpack_from(">i", buf, i)
        i += 4 + 4 * n
    elif tag == 12:   # long array
        (n,) = struct.unpack_from(">i", buf, i)
        i += 4 + 8 * n
    return i


def walk_payload(buf, i, tag, out, depth=0):
    """Like walk() but for a list element, which has no name."""
    if tag == 8:
        val, i = read_nbt_string(buf, i)
        out.append(val)
        return i
    if tag == 10:
        while True:
            if buf[i] == 0:
                i += 1
                break
            i = walk(buf, i, out, depth + 1)
        return i
    # other element types are not interesting for our purpose
    if tag == 1:
        return i + 1
    if tag == 2:
        return i + 2
    if tag == 3 or tag == 5:
        return i + 4
    if tag == 4 or tag == 6:
        return i + 8
    return i


def main(path):
    data = Path(path).read_bytes()
    found = []
    for idx in range(1024):
        off = int.from_bytes(data[idx * 4:idx * 4 + 3], "big")
        cnt = data[idx * 4 + 3]
        if off == 0 or cnt == 0:
            continue
        start = off * 4096
        (length,) = struct.unpack_from(">i", data, start)
        (comp,) = struct.unpack_from(">B", data, start + 4)
        raw = data[start + 5:start + 4 + length]
        try:
            if comp == 1:
                payload = gzip.decompress(raw)
            elif comp == 2:
                payload = zlib.decompress(raw)
            else:
                payload = raw
        except Exception as exc:                     # noqa: BLE001
            print(f"  chunk {idx}: decompress failed: {exc}")
            continue
        out = []
        try:
            walk(payload, 0, out)
        except Exception as exc:                     # noqa: BLE001
            print(f"  chunk {idx}: parse stopped: {exc}")
        for v in out:
            if ":" in v:
                found.append(v)

    print(f"region file: {path}")
    if not found:
        print("  (no namespaced ids found)")
    for v in sorted(set(found)):
        print(f"  {v}")


if __name__ == "__main__":
    main(sys.argv[1])
