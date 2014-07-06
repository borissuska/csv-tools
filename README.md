CSV Tools
=========

Tools for splitting and encoding CSV files.

Requirements
------------

CSV Tools is written in Java 8 using Java FX 8 technology. Then you need at least JRE8 installed. CSV Tools is
available as executable JAR file (Java Archive), just double click the jar file.

CSV Standards and recommendations
---------------------------------

CSV files are poorly standardized, recommendations are:

* use `,` sign as separator
* use `"` sign for escaping (in case you need use `,` or `"` in your field value)
* `"` escape by `""` sequence
* to handle all characters for almost all languages use `UTF-8` charset.
* enclose all fields values into escaping characters, including numbers
* number format is not specified, use it by your locale standards


Split CSV
---------

You can split CSV file by defining number of lines for every file. You can define if header will be repeated for
every file. File mask defines template for new files name, `{{index}}` sequence stands for index number for new file
name.

> **Important**: Split tool requires input escaping character to be defined.

Encode CSV
----------

Using the CSV Tools you can change Charset to another one, recommended Charsets are `UTF-*` family.

Change separator and escaping character
---------------------------------------

Using the CSV Tools you can change separator and escaping characters too. You have to define which one is used in your
file and then you can choose which will be used after conversion. Recommended character for separators is `,` and as 
an escaping character is recommended to use `"` sign.

License
=======

Tool is distributed under MIT License. You can use it anyhow for free.




