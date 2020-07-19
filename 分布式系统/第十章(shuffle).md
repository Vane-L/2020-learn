## shuffle计算

#### 程序执行的步骤
1. map 阶段负责读取和解析数据，
2. shuffle 阶段负责把对应的数据分发给相应的 reducer
3. 而 reduce 阶段则做汇总属于自己的数据并做最终业务逻辑处理。

#### shuffle阶段
- shuffle write 是指 mapper 把处理好的数据写到本地磁盘，一般会以 reduce 好处理的形式组织。
- shuffle read 是指 reducer 把分散在各个 mapper 的数据读取到本地并合并。

- Hash Shuffle
    - 每个 mapper 都为每个 reducer 生成一个文件，所以在本地硬盘上存在 M*R 个文件，其中 M 是 Mapper 的数量，R 是 Reducer 的数量。
    - Consolidated Hash Shuffle
        - 让 Executor 的 map 共用文件，而下游是以分区为单位拉取和处理数据的。
        - 本地硬盘上的文件数量就减少为 P*R，其中 P 是并发任务数。
        - 优化效果：把最初的 M*R 中的 M 降为了计算并行度，使得文件数和 map 数不再挂钩。
- Sort Shuffle
    - 需要一个排序操作，以 partition 为 key，把所有数据按 partitionId 排序后写到唯一的文件中。
    - map 阶段输出的结果文件数就变成 2M 了，M 个数据文件和 M 个索引文件。
    - 由于涉及排序，没法直接 append 到文件，所以会需要把装不下的数据分批 sort 然后 spill 到硬盘，最后又需要 merge 和 sort 成一个文件。
    - Consolidated Sort Shuffle
        - 文件数就只和并行度有关，同时摆脱了 map 和 reduce 数的束缚，文件数进一步降低为 2P
- 当分区数大于一定数量时，才使用 sort shuffle，否则退化到 hash shuffle，只是会在最后做一次不排序的合并，保证只输出一个数据文件及其索引文件。
- Tungsten Sort Shuffle
    - Tungsten 实现了新的内存管理方式，利用堆外内存（off-heap）来存储数据。
      1. 直接以**二进制**的形式保存数据，大幅节省内存。
      2. 堆内只保存指针，数据都保存在堆外，大幅减少堆内对象数，减轻了 GC 压力。
    - Tungsten Sort Shuffle 和 Sort Shuffle 整体流程类似
      1. 直接在**序列化数据**上排序，省去反序列化带来的开销。
      2. 对只有 8 bytes 长的指针数组的排序，可以更好利用 CPU 多级 cache， 比随机访问内存更高效。    
    - 对于内存管理，类似操作系统的两级架构管理内存，先定位 page，再通过 offset 找到具体的数据。
    - 限制
      - 为了不用反序列化数据就能排序，把 partitionId 放在了指针里，由 24 bit 组成，所以支持的最大分区数是 16,777,216。
      - offset 占 27 bit，所以序列化后的单个数据，最大只能是 2**27 = 128MB。
      - page 占 13 bit，所以单个 task 管理的最大数据量是 2**13 * 128MB = 1TB。
      - 为了避免反序列化数据，**只支持对 partition 排序**，不支持对数据 key 排序。
      - 为了避免反序列化数据，**不支持 aggregation**。
      - 为了避免反序列化数据，spill 文件想要自动 merge，就需要序列化协议支持，如 KryoSerializer 和 Spark 内置的 Serializer。
- 从 ShuffleWrite 的角度看，选择顺序变成了 `BypassMergeSortShuffleWriter > UnsafeShuffleWriter > SortShuffleWriter`，每一种都有自己适用的场景，当满足一定条件时，就会自动选择。  