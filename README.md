# simplex-cms
Refactoring of the CMS

# Migration steps
For the first milestone, basic documentation and basic CMS stuff.

Basic rearchitecture, focus in split into differents services (CMS, ECommerce, Blog, etc)

Think about to use Git as persistence layer, to have versioning and branches out of the box. It is possible move stages into branches.

- [ ] Contents (ML1)
  - [ ] Content
  - [ ] Folder
  - [ ] Head
  - [X] Templates
- [ ] Documentation (ML1)

# No CMS stuff
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


# Notes
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
