## MapReduce
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