Create new project

```
$ ng new weather-app --style=scss --routing=false
```

Adding angular metarial design

```
$ cd <to-project>
$ ng add @angular/material --theme=indigo-pink --typography=true --animations=true

--theme => specific theme will be added by cli. adding a theme involves in modifying angular.json with css files. index.html will also be included with material design icon.

--typography => enables angluar material typography globally. defines how the text content should be displayed. uses Roboto Google font by default. index.html will have the link. other css classed to the body of index.html

--animations -> enables browser animations, imports BrowserAnimationsModule
```
