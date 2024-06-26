 # Serial Part (SDE Maven module)
---
## Description

This module use for SerialPart submodel specification and descriptors. It's contain the codes related to SerialPart to validate, parse and transfer data for DigitalTwins and EDC to create aspect twins and data offer.

---
#### Version: 1.0.0
#### SerialPart Aspect Model URN: urn:bamm:io.catenax.serial_part:1.0.0#SerialPart
#### Semantic Id: urn:bamm:io.catenax.serial_part:1.0.0

#### Version: 3.0.0
#### SerialPart Aspect Model URN: urn:bamm:io.catenax.serial_part:3.0.0#SerialPart
#### Semantic Id: urn:bamm:io.catenax.serial_part:3.0.0
---

### Schema

Please find below links for schema details:

- [serial-part-v1.0.0 schema](src/main/resources/serial-part-v1.0.0.json)
- [serial-part-v3.0.0 schema](src/main/resources/serial-part-v3.0.0.json)


### SerialPart CSV file headers

| Headers Name       	       		| Mandatory                     | Position 	|
|-------------------------------	----|-----------------------------	|--------	|
| uuid		                   		| No		     		        		|    1     	|
| part_instance_id			   		| Yes					      	|    2    	|
| manufacturing_date    				| Yes 							| 	 3	   	|
| manufacturing_country  	    		| No                           	| 	 4	  	|
| manufacturer_part_id 		      	| Yes                           | 	 5	  	|
| customer_part_id		    			| No                     		| 	 6	 	|
| classification		 				| Yes                           |    7 	 	|
| name_at_manufacturer	 			| Yes                           |    8 	 	|
| name_at_customer	 				| No                           	|    9 	 	|
| optional_identifier_key	 		| No                           	|    10	 	|
| optional_identifier_value			| No                           	|    11	 	|


#### [CSV Sample File Link]

#### Example for submodel SerialPart


### Work Flow 

 - CSV to POJO
 - CSV column validation and mandatory field validation
 - POJO TO DTO
 - UUID generate v4
 - DigitalTwins API's calls 
 - EDC API's calls
 - BPNDiscovery API's calls
 - DB Store
 
### External Services Call

 - DigitalTwins
 - EDC Connectors
 - BPN Discovery

