# simplex-cms
CMS reimplementing.

## Migration steps
For the first milestone, basic documentation and basic CMS stuff.

Basic rearchitecture, focus in split into different services (CMS, ECommerce, Blog, etc)

Think about to use Git as persistence layer, to have versioning and branches out of the box. It is possible move stages into branches.

### CMS stuff
- [ ] Contents (ML1)
  - [X] Content
  - [X] Folder
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

## Deploy

```
git clone https://github.com/angelcervera/simplex-cms
sbt clean assembly
docker build -t simplexportal/simplex-cms .
```

To export and import the docker images:
- https://docs.docker.com/engine/reference/commandline/save/
- https://docs.docker.com/engine/reference/commandline/load/

```$bash
docker save -o simplexportal-0.1-snapshot.tar simplexportal/simplex-cms
gzip -9 simplexportal-0.1-snapshot.tar

scp -r -P 62269 simplexportal-0.1-snapshot.tar.gz root@144.76.82.212:/root/.

docker load --input simplexportal-0.1-snapshot.tar.gz

```

Per every site, start the container on different port:
```$bash
docker run -d --cpus 1 -m 1G -p <[host_ip:]host_port>:8080 --read-only -v <host_storage_path>:/root/storage --name acervera -it simplexportal/simplex-cms
```

Start a Nginx container as reverse proxy:
```$bash
$ docker run -d --name nginx -v <local nginx.conf>:/etc/nginx/nginx.conf:ro -p 80:80 nginx
```

And the `nginx.conf` could be something like:
```$bash
worker_processes 1;

events { worker_connections 1024; }

http {

    sendfile on;

    upstream upstream-domain1 {
        server 192.168.0.38:8081;
    }

    upstream upstream-domain2 {
        server 192.168.0.38:8082;
    }

    server {
        listen 80;
        server_name  domain1.org *.domain1.org;

        location / {
            proxy_pass         http://upstream-domain1;
            proxy_redirect     off;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Host $server_name;
        }
    }

    server {
        listen 80;
        server_name  domain2.org *.domain2.org;

        location / {
            proxy_pass         http://upstream-domain2;
            proxy_redirect     off;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Host $server_name;
        }
    }

}
```



## Known limitations.


## Notes
### Woodstox posts:
- [Configuring Woodstox XML parser: basic Stax properties / Woodstox Part 1](https://medium.com/@cowtowncoder/configuring-woodstox-xml-parser-basic-stax-properties-39bdf88c18ec)
- [Configuring Woodstox XML parser: Stax2 properties / Woodstox Part 2](https://medium.com/@cowtowncoder/configuring-woodstox-xml-parser-stax2-properties-c80ef5a32ef1) 
- [Configuring Woodstox XML parser: Woodstox-specific properties / Woodstox Part 3](https://medium.com/@cowtowncoder/configuring-woodstox-xml-parser-woodstox-specific-properties-1ce5030a5173)
- [GitHub](https://github.com/FasterXML/woodstox)
- [Official website ???](https://fasterxml.github.io/woodstox/)

### Jackson:
- [Home page](https://github.com/FasterXML/jackson)
- [Scala module](https://github.com/FasterXML/jackson-module-scala)
- [https://github.com/FasterXML/jackson-modules-java8](Allow Java8 Date)

### Markup syntax:
[Markup Languages](https://en.wikipedia.org/wiki/Comparison_of_document_markup_languages)

#### Markdown:
- Doxia allow markdown
- [Atlassian Common Mark](https://github.com/atlassian/commonmark-java)
- [Flexmark](https://github.com/vsch/flexmark-java)
- [Implementations](https://www.w3.org/community/markdown/wiki/MarkdownImplementations)
- The selected one is [flexmark-java](https://github.com/vsch/flexmark-java)

### Git:
- [JGit](https://www.eclipse.org/jgit/)

# Ideas:
- Use [Doxia](http://maven.apache.org/doxia/references/index.html) to be able to use different formats out of the box.
- Use YAML instead JSON:
    - [YAML](http://yaml.org/)
    - [snakeyaml](https://bitbucket.org/asomov/snakeyaml/wiki/Home)