---
toc-depth: 3
---

# File storage

File storage is (surprise!) intended for storing files (images, videos, audio, etc.).

```mermaid

flowchart
    subgraph File storage
        subgraph FS VLS 
            fpgs["Page files \n(many)"]
            fpg_idxs["Page indexes \n(for each page)"]
            ftoc["ToC"]
            ftoc --> fpg_idxs
            fpg_idxs --> fpgs
        end
        fls["Files \n(many)"]
        fpgs --> fls
    end

```

Example filesystem structure:

```
root
|-  vls
    |-  toc.dat
    |-  idx0.dat
    |-  page0.dat
    |-  idx1.dat
|-  files
    |-  someFileName.someExt
    |-  someFileName.someExt
    |-  someFileName.someExt
```

File storage consists of a separate VLS and a collection of files.

File storage assigns each file an id and its VLS stores mapping from ids to filenames.

## File roles & terminology

> Please, refer to [VLS specification](./variable.md).

## File structure

> Please, refer to [VLS specification](./variable.md).

## Operations

### Add file

```mermaid

flowchart
    start([Start]) --> savefile[Save file in files directory]
    savefile --> savepath[Save path in VLS]
    savepath --> e([Return new id from VLS, end])
```

### Get file

```mermaid

flowchart
    start([Start]) --> getid[Get path by id in VLS]
    getid --> ifnone>If VLS returned none]
    ifnone -->|Yes| enone([Return none, end])
    ifnone -->|No| e([Return path, end])
```

### Remove file

```mermaid

flowchart
    start([Start]) --> getid[Get path by id in VLS]
    getid --> ifnone>If VLS returned none]
    ifnone -->|Yes| enone([Return, end])
    ifnone -->|No| delvls[Delete record from VLS]
    delvls --> delfile[Delete actual file]
    delfile --> e([End])
```
