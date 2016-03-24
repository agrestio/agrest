This project contains Docbook documentation for [LinkRest](http://nhl.github.io/link-rest/).

To publish:

```
# assuming 'link-rest-pages' is a checkout of 'gh-pages' branch of LinkRest

mvn clean package
cp -r target/site/index/ ../../link-rest-pages/docs/

cd ../../link-rest-pages
git add -A
git commit -a -m "docs update"
```
