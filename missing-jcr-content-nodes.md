# How to find pages and assets missing the jcr:content node
1. Go to /crx/de/index.jsp
2. Login as admin
3. Go to `Tools` => `Query`
4. Select `SQL2`
5. In the `Query` box, paste one of the queries below:
Find assets missing the `jcr:content/metadata` node:
```
select * from [dam:Asset] as s where s.[jcr:content/metadata/jcr:primaryType] is null and ISDESCENDANTNODE(s,'/content/dam')
```

Find pages missing the `jcr:content` node:
```
select * from [cq:Page] as s where s.[jcr:content/jcr:primaryType] is null and ISDESCENDANTNODE(s,'/content')
```
6. Click `Execute`
