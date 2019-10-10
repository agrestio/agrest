This project contains Docbook documentation for [Agrest](https://agrest.io).

To publish:

```
# assuming 'agrest-io' project is checked out to the same parent directory and is on the 'gh-pages' branch

mvn clean package
cp -r target/site/index/ ../../agrest-io/docs/

cd ../../agrest-pages
git add -A
git commit -a -m "docs update"
```
