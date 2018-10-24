This project contains Docbook documentation for [Agrest](https://agrest.io).

To publish:

```
# assuming 'agrest-pages' is a checkout of 'gh-pages' branch of Agrest

mvn clean package
cp -r target/site/index/ ../../agrest-pages/docs/

cd ../../agrest-pages
git add -A
git commit -a -m "docs update"
```
