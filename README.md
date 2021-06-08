# KotlinTLV
Kotlin library for working with Tag-Length-Value encoded data
​
## Examples
​
### Declaring/Composing TLVs
​
```kotlin
  val tag: Int = 1
  val value: ByteArray = byteArrayOf(0x00,0x00,0x00,0x00,0x00)
  val tlv = TLV(tag, value)
  // This creates a TLV with tag 1 and value of [0x00,0x00,0x00,0x00,0x00]
```
​
### Parsing and Validating TLVs
The ```parseTlvData()``` function is used to parse a ByteArray of TLVs into a list of TLVs
```kotlin
  val byteArray: ByteArray = byteArrayOf(0x01, 0xFF, 0x03, 0xE8)
  val tlv : List<TLV> = parseTlvData(byteArray)
```
​
The ```writeOutTLVBinary()``` fucntion is used to parse a list of TLVs into a ByteArray
```kotlin
  val tlv : List<TLV>
  val byteArray: ByteArray = tlv.writeOutTLVBinary()
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
​
M.1 TLV Format
​
M.1.1 Type Field
The type value is generally a 0x01->0xFE single byte corresponding to the type of the value. 0x00 and 0xFF are special values with 0x00 corresponding to a special NULL TLV that has no length or value fields. The NULL TLV should not be used for transaction signing. If 0xFF is used, this indicates that a two byte type is in use, which will follow the 0xFF. Legal values for the two-byte TLV are 0x00FF->0xFEFF. All values from 0x0000 to 0x00FE are not legal and should be represented using the single-byte type format. 0xFF00->0xFFFF are reserved for future use in extending the protocol (RFU).
​
M.1.2 Length Field
Similarly to the Type field specified in 4.1.1, the length field is generally a single byte 0x00-0xFE, corresponding to the number of bytes in the value field. Unlike the Type field, the length 0x00 does not have a special meaning and merely indicates that the value is of zero length. This is a legal situation if the mere presence of the TLV is all that is needed to transmit the information it represents. If the value is longer than 0xFE (254) bytes, a two-byte length is in use. Similar to the Type field, the length field is set to 0xFF to indicate a two-byte type followed by a value of 0x00FF->0xFEFF. Values from 0x0000 to 0x00FE are not legal and should be represented using the single-byte length format. Values from 0xFF00 to 0xFFFF are reserved for future use in extending the protocol.
​
M.1.3 Value Field
The value field’s format and content varies depending on the type of TLV, but can currently be anything from 0-65,279 bytes.
​
​
## Author
MarkCaii, caimark93@gmail.com
​
## License
​
KotlinTLV is available under the MIT license. See the LICENSE file for more info.
