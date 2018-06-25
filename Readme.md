# Conditor's TEI tools

Tools for managing TEI-Condior format, an XML format derivated from [TEI-HAL](https://github.com/CCSDForge/HAL/tree/master/schema) format.

Both TEI-Conditor an TEI-HAL are a customization of the [TEI](http://www.tei-c.org/) format. They are build with a [ODD](http://www.tei-c.org/guidelines/customization/getting-started-with-p5-odds/) representation and the [TEI ROMA tools](http://www.tei-c.org/tools/roma/).

Currently 2 tools are available :

- A schema for representing/validating XML TEI-Conditor files ([ODD](https://github.com/niederlec/conditor-tei-tools/blob/master/ConditorSpecification.xml) and [XSD](https://github.com/niederlec/conditor-tei-tools/blob/master/Conditor.xsd) version)

- A [XSLT stylesheet](https://github.com/niederlec/conditor-tei-tools/blob/master/hal2Conditor.xsl) for converting TEI-HAL to TEI-Conditor format

There is also, for testing purpose, a basic java program (:warning::rotating_light:Work in progress:rotating_light::warning:) for transforming & validating files. (a saxon licence is required)




