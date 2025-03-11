### Project

This is a Java program that generates the English rom for Dynami Tracer from the Japanese rom.

### Running the project

Pre-requisites:
- JDK 11 or newer
- maven

Download the git repository (dynami-tracer) using `git clone https://github.com/Krokodyl/dynami-tracer.git` or the [zip file](https://github.com/Krokodyl/dynami-tracer/archive/refs/heads/main.zip).


Change the rom paths in DynamiTracer.java:
```
private final static String ROM_INPUT = "D:\\git\\dynami-tracer\\roms\\input\\BS Dynami Tracer (Japan).bs";
private final static String ROM_OUTPUT = "D:\\git\\dynami-tracer\\roms\\output\\BS Dynami Tracer (English).bs";
```

Command line compilation: `mvn clean install`

Command line execution: `mvn exec:java`


### Adding letters

Small font:
- src/main/resources/images/vwf/small-font.png
- Font.java:FONT_SMALL_CHARACTERS

Normal font:
- src/main/resources/images/vwf/font.png
- Font.java:FONT_CHARACTERS

Texts:
src/main/resources/translations