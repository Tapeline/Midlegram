---
toc-depth: 3
---

# Variable-length data storage

> (also referred to as "VLS")

It is intended to store data that does not have
predetermined length. Most commonly - strings.

It offers mechanism that's pretty similar to TOAST mechanism in Postgres,
albeit super-simplified.

```mermaid

flowchart
    subgraph Variable-length data storage
        p1["Page 1"]
        p2["Page 2"]
        p3["Page n"]
        pi1["Page 1 index"]
        pi2["Page 2 index"]
        pi3["Page n index"]
        toc["Table of contents"]
        pi1 --> p1
        pi2 --> p2
        pi3 --> p3
        toc --> pi1
        toc --> pi2
        toc --> pi3
    end
```

Example filesystem structure:

```
root
|-  toc.dat
|-  idx0.dat
|-  page0.dat
|-  idx1.dat
|-  page1.dat
```

## File roles

VLS has three distinct file roles:

**vls:ToC**
:   Table of contents is a file that stores mappings
    of GID ranges and availability status to page ids.

**vls:Page index**
:   Page index is a file that stores information about
    offsets, lengths and GIDs of stored data pieces in
    the page it refers to.

**vls:Page**
:   Page is a file that is a sequence of bytes of data.
    Is a continuous stream, does not have neither any 
    delimiters nor any meta information.

## Terminology

**vls:GID**
:   Global Identifier is an identifier of a string (or
    other bytes sequence) stored in VLS. Unique for a
    single VLS.

**vls:Availability**
:   A boolean value that determines whether there a page
    could be appended with some data or not. Therefore,
    it has two values:
    
    - true = available = open
    - false = full = closed

## File structure

### ToC

Filename: `toc.dat`

```
page_count (i32)
max_page_size (i64, bytes)

page_0_gid_start (i64)
page_0_gid_end (i64)
page_0_availability (bool)

page_1_gid_start (i64)
page_1_gid_end (i64)
page_1_availability (bool)

...

page_n_gid_start (i64)
page_n_gid_end (i64)
page_n_availability (bool)
```

!!! attention
    `max_page_size` should be chosen that way, so all
    `max_page_size` bytes can be safely loaded to RAM and then written back.

### Page index

Filename: `idx{{page_id}}.dat`, where `page_id: i32 >= 0` (e.g. `idx1.dat`).

Stores tuples of `(gid, offset, length, deleted)` of records stored in 
corresponding page.

```
records_count (i32)

record_0_gid (i64)
record_0_offset (i64)
record_0_length (bool)
record_0_deleted (bool)

record_1_gid (i64)
record_1_offset (i64)
record_1_length (bool)
record_1_deleted (bool)

...

record_n_gid (i64)
record_n_offset (i64)
record_n_length (bool)
record_n_deleted (bool)

```

### Page

Filename: `page{{page_id}}.dat`, where `page_id: i32 >= 0` (e.g. `page1.dat`).

Stores actual records.

```
record_0_bytes (length and offset determined by index)
record_1_bytes (length and offset determined by index)
...
record_n_bytes (length and offset determined by index)
```

## Operations

### Append

```mermaid

flowchart
    start([Start]) --> getnextid[Get next id from ToC]
    getnextid --> iflastavail>"If last page in ToC is available"]
    iflastavail -->|No| createpage[Create new page]
    iflastavail -->|Yes| ifhasenough>"If has enough space to store data \n(<= max_page_size)"]
    ifhasenough -->|No| createpage
    createpage --> createpageidx[Create new index]
    createpageidx --> appendtoc[Append new page to ToC \nand close previous]
    appendtoc --> appendpage[Append to page]
    ifhasenough -->|Yes| appendpage
    appendpage --> appendidx[Append to index]
    appendidx --> updatetoc[Update GID range in ToC]
    updatetoc --> e([End])
```

### Delete

```mermaid

flowchart
    start([Start]) --> ifintoc>"If in ToC range\n(from first page start to last page end)"]
    ifintoc -->|No| e
    ifintoc -->|Yes| locatetoc[Locate page in toc using binsearch]
    locatetoc --> locaterecord["Locate record in index using binsearch"]
    locaterecord --> mark[Mark as deleted by flipping boolean]
    mark --> e([End])
```

### Retrieve by GID

```mermaid

flowchart
    start([Start]) --> ifintoc>"If in ToC range\n(from first page start to last page end)"]
    ifintoc -->|No| enone([Return null, end])
    ifintoc -->|Yes| locatetoc[Locate page in toc using binsearch]
    locatetoc --> locaterecord["Locate record in index using binsearch"]
    locaterecord --> ifdeleted>"If record marked as deleted"]
    ifdeleted -->|Yes| enone
    ifdeleted -->|No| extract[Extract data from page by offset and len]
    extract --> e([Return, end])
```

### Update

Although not recommended, VLS can do updating. 
Updating is always `O(max_page_size)` of both memory and time.

To update faster, just delete the old record and append a new,
then refer to new GID.

```mermaid

flowchart
    start([Start]) --> ifintoc>"If in ToC range\n(from first page start to last page end)"]
    ifintoc -->|No| e([End])
    ifintoc -->|Yes| locatetoc[Locate page in toc using binsearch]
    locatetoc --> locaterecord["Locate record in index using binsearch"]
    locaterecord --> calcdelta[Calculate delta between \nold size and new size of target]
    locaterecord --> loadtail[Load tail page data from target to page end]
    loadtail --> rewritetarget["Rewrite target record"]
    rewritetarget --> updatetargetidx[Update index for target]
    updatetargetidx --> appendtail[Write tail data after target]
    appendtail --> fortail>"For each tailing record in index"]
    fortail --> incidx[Increment idx by delta]
    incidx --> fortail
    incidx --> e
```

### Defragmentation

Get rid of blanks that are left from deletion of old elements.

!!! warning
    This is a very heavy operation, as it requires a full rewrite of the VLS.

```mermaid

flowchart
    start([Start]) --> newvls[Create new temporary VLS]
    newvls --> forrecord>"For each record in current VLS"]
    forrecord --> copyrecord["Append record to temp VLS"]
    copyrecord --> forrecord
    copyrecord --> delvls[Delete current VLS directory tree]
    delvls --> movevls[Rename temp VLS so it moves to place where deleted VLS once was]
    movevls --> e([End])
```
