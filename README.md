# docstore2
The code represents a simple Spring-Boot REST application in order to demonstrate MySQL's XDevAPI for Document Store. 

The code was developed in Eclipse 2020 03 and the database version is MySQL 8.0.19.

The data for the application comes from the restaurants.json file. This will need to be imported into a MySQL Document Store.
To do this use MySQL Shell (change user connection string as appropriate):

```shell
os-prompt% mysqlsh
mysqlsh-js> \c root@localhost
mysqlsh-js> var schema = session.createSchema('nycfood')
mysqlsh-js> var collection = schema.createCollection('outlets')
mysqlsh-js> util.jsonImport('<path-to-restaurants.json>',{schema: 'nycfood', collection: 'outlets'})
mysqlsh-js> collection.find()   // should display 3772 documents
```

For details of how to use MySQL Document Store refer to https://downloads.mysql.com/docs/x-devapi-userguide-en.a4.pdf and for the Java API itself go to https://dev.mysql.com/doc/dev/connector-j/8.0/?com/mysql/cj/xdevapi/package-summary.html

