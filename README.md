# KotlinTLV
Kotlin library for working with Tag-Length-Value encoded data
​
## Examples

### Declaring/Composing TLVs
To declare and compose a TLV the user must provide a tag of type Int and a value of type ByteArray:
​
```kotlin
  val tag: Int = 1
  val value: ByteArray = byteArrayOf(0x00,0x00,0x00,0x00,0x00)
```
Then construct the TLV by using the tag and the value
```kotlin
  val tlv = TLV(tag, value)   // This creates a TLV with tag 1 and value of [0x00,0x00,0x00,0x00,0x00]
```

​
### Parsing and Validating TLVs
There are two functions used for parsing TLV data:

The ```parseTlvData(data: ByteArray)``` function is used to parse a ByteArray of TLV data into a list of TLVs
```kotlin
  val rawTlvs : ByteArray = byteArrayOf(0x01,0x05,0x00,0x00,0x00,0x00,0x00,0x02,0x05,0x00,0x00,0x00,0x00,0x00)
  val tlvs : List<TLV> = parseTlvData(rawTlvs) // Creates a list of 2 tlvs with a single byte tag and length of 0
```
this is the same as creating a list of the following TLVs:
```kotlin
  val length = 5
  val value = byteArrayOf(0x00,0x00,0x00,0x00,0x00)
  val firstTag = 1
  val secondTag = 2
  var listOfTlvs : MutableList<TLV> = mutableListOf()
  listOfTlvs.add(TLV(firstTag,value))
  listOfTlvs.add(TLV(secondTag,value))
```
​
The ```(List<TLV>).writeOutTLVBinary()``` function is used to parse a list of TLVs into a ByteArray
```kotlin
  val tag1 = 1
  val tag2 = 2
  val tag3 = 3
  val value : ByteArray = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
  val tlvs : MutableList<TLV> = mutableListOf()
  tlvs.add(TLV(tag1, value))
  tlvs.add(TLV(tag2, value))
  tlvs.add(TLV(tag3, value))
  
  val byteArray: ByteArray = tlvs.toList().writeOutTLVBinary() // returns the list of TLVs as a ByteArray
```
​
### Fetching TLVs
The ```lookUpTlvInList``` function returns the TLV in the list with the corresponding tag and throws an ```UnsupportedOperationException``` if the tag isn't present in the TLV list
```kotlin
  val tag : Int = 1
  val value : ByteArray = byteArrayOf(0x00)
  val tlv = TLV(tag, value)
  var tlvList : MutableList<TLV> = mutableListOf()
  tlvList.add(tlv)
  lookUpTlvInList(tlvList, tag) // Returns tlv
  lookUpTlvInList(tlvList, tag+1) // Throws UnsupportedOperationException
```
​
The ```lookUpTlvInLisIfPresent``` function returns the TLV in the list with the corresponding tag and returns null if the tag isn't present in the TLV list
```kotlin
  val tag : Int = 1
  val value : ByteArray = byteArrayOf(0x00)
  val tlv = TLV(tag, value)
  var tlvList : MutableList<TLV> = mutableListOf()
  tlvList.add(tlv)
  lookUpTlvInList(tlvList, tag) // Returns tlv
  lookUpTlvInList(tlvList, tag+1) // Returns null
```
​
The ```fetchTlvValue``` function returns the TLV value in the list with the corresponding tag and returns an empty ByteArray if the tag isn't present in the TLV list
```kotlin
  val tag : Int = 1
  val value : ByteArray = byteArrayOf(0x00)
  val tlv = TLV(tag, value)
  var tlvList : MutableList<TLV> = mutableListOf()
  tlvList.add(tlv)
  lookUpTlvInList(tlvList, tag) // Returns tlv
  lookUpTlvInList(tlvList, tag+1) // Returns an empty ByteArray
```
​
### Full TLV specification

#### Type Field
The type value is generally a 0x01->0xFE single byte corresponding to the type of the value. 0x00 and 0xFF are special values with 0x00 corresponding to a special NULL TLV that has no length or value fields. The NULL TLV should not be used for transaction signing. If 0xFF is used, this indicates that a two byte type is in use, which will follow the 0xFF. Legal values for the two-byte TLV are 0x00FF->0xFEFF. All values from 0x0000 to 0x00FE are not legal and should be represented using the single-byte type format. 0xFF00->0xFFFF are reserved for future use in extending the protocol (RFU).

#### Length Field
The length field is generally a single byte 0x00-0xFE, corresponding to the number of bytes in the value field. Unlike the Type field, the length 0x00 does not have a special meaning and merely indicates that the value is of zero length. This is a legal situation if the mere presence of the TLV is all that is needed to transmit the information it represents. If the value is longer than 0xFE (254) bytes, a two-byte length is in use. Similar to the Type field, the length field is set to 0xFF to indicate a two-byte type followed by a value of 0x00FF->0xFEFF. Values from 0x0000 to 0x00FE are not legal and should be represented using the single-byte length format. Values from 0xFF00 to 0xFFFF are reserved for future use in extending the protocol.

#### Value Field
The value field’s format and content varies depending on the type of TLV, but can currently be anything from 0-65,279 bytes.

​
## License
​
KotlinTLV is available under the Apache 2.0 license. See the LICENSE file for more info.
