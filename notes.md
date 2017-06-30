Suggestions from Andy:

In concurrent-futures there is another solution without cats - you could use `Future.sequence`:
```Future.sequence(List("apple", "banana", "cherry").map(findFruit)).map(_.foreach(println)))```


