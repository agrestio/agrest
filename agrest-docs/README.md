This project contains Docbook documentation for [AgREST](http://nhl.github.io/link-rest/).

To publish:

```
# assuming 'agrest-pages' is a checkout of 'gh-pages' branch of AgREST

mvn clean package
cp -r target/site/index/ ../../agrest-pages/docs/

cd ../../agrest-pages
git add -A
git commit -a -m "docs update"
```
