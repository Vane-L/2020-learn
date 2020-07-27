## MapReduce

### 特性
- **parallelization 并行化** 
- **fault-tolerance 容错**
- **locality optimization 数据本地化**
- **load balancing 负载均衡**

### 执行
1. 首先将输入文件切分为M个，然后将代码复制到集群的每个机器
2. 其中有个work是*Master*，它负责指派map task和reduce task给空闲的worker
3. map task读取输入分片，并通过map函数输出key-value pairs到内存中
4. 定期将内存中的key-value pairs写到本地磁盘，然后通知*Master*机器
5. 当*Master*通知reduce worker中间结果文件的location，通过RPC读取数据 
`The sorting is needed because typically many different keys map to the same reduce task.`
6. reduce函数的输出被追加到所属分区的输出文件
7. 当所有的task完成后，*Master*会唤醒用户程序

### Master数据
- task state
- worker machine

### Fault Tolerance容错
- Worker Failure
    - master周期性地ping每个worker。如果在一个约定的时间范围内没有收到worker返回的信息，master将把这个worker标记为失效。
    - 当worker故障时，由于已经完成的Map任务的输出存储在机器上，Map任务的输出已不可访问了，因此必须重新执行，而已经完成的Reduce任务的输出存储在文件系统上，因此不需要再次执行。
    - MapReduce可以处理大规模worker失效的情况。_只需要简单的再次执行那些不可访问的worker完成的工作，之后继续执行未完成的任务，直到最终完成这个MapReduce操作_
- Master Failure
    - 将master的数据周期性的checkpoint
- 处理机制
    - 通过对map和reduce任务的输出是原子提交，每个task都输出到一个private temporary files
    - 当Reduce任务完成时，Reduce worker进程以原子的方式把临时文件重命名为最终的输出文件。

#### 伪代码
``` 
Map(k,v)
    split v into word
    foreach word w:
        emit(w,1)

Reduce(k,v)
    emit(len(v))
```
#### WordCount代码
```
public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
  StringTokenizer itr = new StringTokenizer(value.toString());
  while (itr.hasMoreTokens()) {
    word.set(itr.nextToken());
    context.write(word, one);
  }
}
public void reduce(Text key, Iterable<IntWritable> values,Context context) throws IOException, InterruptedException {
  int sum = 0;
  for (IntWritable val : values) {
    sum += val.get();
  }
  result.set(sum);
  context.write(key, result);
}
```