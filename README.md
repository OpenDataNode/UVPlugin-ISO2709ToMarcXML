<!---

This template is intended to be used for describing UnifiedViews plugins as README.md. 

- Copy this README.md to root directory for the plugin to be described.  

- Replace <<descriptive information>> with proper values.

- If no values are available, replace <<descriptive information>> with 'N/A'.

- Use <BR> tag for creation of multi-line cells (in case the length of text exceeds the width of page, it is wrapped automatically to multi-line cell).

- Enclose each configuration parameter name with ** for highlighting the text as bold. 

- Add '(optional)' to Type of input or output if it is not mandatory (all inputs and outputs are mandatory by default). 

- Delete these template comments after the completion of the document.  

-->

# T-ISO2709ToMarcXML #
----------

###General###

|                              |                                                               |
|------------------------------|---------------------------------------------------------------|
|**Name:**                     |T-ISO2709ToMarcXML                                             |
|**Description:**              |Transforms ISO 2709 (mrc) files to a XML files on output.      |
|                              |                                                               |
|**DPU class name:**           |ISO2709ToMarcXML                                               | 
|**Configuration class name:** |ISO2709ToMarcXMLConfig_V1                                      |
|**Dialogue class name:**      |ISO2709ToMarcXMLVaadinDialog                                   | 

***

###Configuration parameters###

|Parameter                        |Description                             |                                                        
|---------------------------------|----------------------------------------|
|Skip file on error (checkbox)    |Additional self-descriptive option for load. |

***

### Inputs and outputs ###

|Name                |Type       |DataUnit                         |Description                        |
|--------------------|-----------|---------------------------------|-----------------------------------|
|filesInput          |i          |FilesDataUnit                    |ISO 2709 .mrc files                |
|filesoutput         |o          |FilesDataUnit                    |MARC XML files                     |


***

### Version history ###

|Version            |Release notes                                   |
|-------------------|------------------------------------------------|
|1.0.0              |N/A                                             |                                


***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|N/A |N/A | 

