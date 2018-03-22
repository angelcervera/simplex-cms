# simplex-cms
Refactoring of the CMS

## Migration steps
For the first milestone, basic documentation and basic CMS stuff.

Basic rearchitecture, focus in split into different services (CMS, ECommerce, Blog, etc)

Think about to use Git as persistence layer, to have versioning and branches out of the box. It is possible move stages into branches.

### CMS stuff
- [ ] Contents (ML1)
  - [ ] Content
  - [ ] Folder
  - [ ] Head
  - [X] Templates
- [ ] Documentation (ML1)

### No CMS stuff
- [ ] Security Access (ML9999)
  - [ ] Authentication
    - [ ] onlinelogin
    - [ ] onlinelogout
    - [ ] onlineregistration
    - [ ] regeneratepassword
    - [ ] registrationpassword
    - [ ] registrationupdate
  - [ ] Authorization
    - [ ] Organiza users per groups
    - [ ] Restric access per group and user
      - [ ] Per folder
      - [ ] Per resource
  - [ ] Forms

## Layout

- Folder `data` contains:
    - Statics resources
    - Custom components from pages with the format ${page_filename}_${component_name}.html
      Because it is in the some folder structure that the resources, is possible edit html or markdown files whe edition
      tools without special server interaction.
- Folder `meta` contains metadata for static resources, folders, pages and components.
- Folder `templates` contains data and metadata information about templates.
      
Layout Examples:
```text
.
├── data
│   ├── blog
│   │   ├── 2015
│   │   │   └── 10
│   │   │       └── 04
│   │   │           └── post1
│   │   │               ├── post.html_head.html
│   │   │               ├── post.html_post-content-header.html
│   │   │               └── post.html_post-content.html
│   │   ├── index.html_post-content.html
│   │   └── rss.xml
│   ├── css
│   │   ├── application.css
│   │   ├── bootstrap.css
│   │   ├── bootstrap.min.css
│   │   ├── bootstrap-theme.css
│   │   └── bootstrap-theme.min.css
│   ├── fonts
│   │   ├── glyphicons-halflings-regular.eot
│   │   ├── glyphicons-halflings-regular.svg
│   │   ├── glyphicons-halflings-regular.ttf
│   │   ├── glyphicons-halflings-regular.woff
│   │   └── glyphicons-halflings-regular.woff2
│   ├── img
│   │   ├── avatar_qr.jpg
│   │   ├── delicious_logo.png
│   │   ├── GitHub_Logo.png
│   │   ├── se-logo.png
│   │   └── so-logo.png
│   ├── index.html
│   └── js
│       ├── bootstrap.js
│       ├── bootstrap.min.js
│       └── jquery-2.1.0.min.js
├── meta
│   ├── blog
│   │   ├── 2015
│   │   │   └── 10
│   │   │       └── 04
│   │   │           └── post1
│   │   │               ├── post.html_head.json
│   │   │               ├── post.html.json
│   │   │               ├── post.html_post-content-header.json
│   │   │               └── post.html_post-content.json
│   │   ├── index.html.json
│   │   ├── index.html_post-content.json
│   │   └── rss.xml.json
│   ├── css
│   │   ├── application.css.json
│   │   ├── bootstrap.css.json
│   │   ├── bootstrap.min.css.json
│   │   ├── bootstrap-theme.css.json
│   │   └── bootstrap-theme.min.css.json
│   ├── fonts
│   │   ├── glyphicons-halflings-regular.eot.json
│   │   ├── glyphicons-halflings-regular.svg.json
│   │   ├── glyphicons-halflings-regular.ttf.json
│   │   ├── glyphicons-halflings-regular.woff2.json
│   │   └── glyphicons-halflings-regular.woff.json
│   ├── img
│   │   ├── avatar_qr.jpg.json
│   │   ├── delicious_logo.png.json
│   │   ├── GitHub_Logo.png.json
│   │   ├── se-logo.png.json
│   │   └── so-logo.png.json
│   ├── index.html.json
│   └── js
│       ├── bootstrap.js.json
│       ├── bootstrap.min.js.json
│       └── jquery-2.1.0.min.js.json
└── templates
    └── blog
        ├── list-posts.html
        │   ├── data.html
        │   └── metadata.json
        └── post.html
            ├── data.html
            └── metadata.json

```

## Known limitations.


## Notes
Woodstox posts:
- [Configuring Woodstox XML parser: basic Stax properties / Woodstox Part 1](https://medium.com/@cowtowncoder/configuring-woodstox-xml-parser-basic-stax-properties-39bdf88c18ec)
- [Configuring Woodstox XML parser: Stax2 properties / Woodstox Part 2](https://medium.com/@cowtowncoder/configuring-woodstox-xml-parser-stax2-properties-c80ef5a32ef1) 
- [Configuring Woodstox XML parser: Woodstox-specific properties / Woodstox Part 3](https://medium.com/@cowtowncoder/configuring-woodstox-xml-parser-woodstox-specific-properties-1ce5030a5173)
- [GitHub](https://github.com/FasterXML/woodstox)
- [Official website ???](https://fasterxml.github.io/woodstox/)

Jackson:
- [Home page](https://github.com/FasterXML/jackson)
- [Scala module](https://github.com/FasterXML/jackson-module-scala)
- [https://github.com/FasterXML/jackson-modules-java8](Allow Java8 Date)
