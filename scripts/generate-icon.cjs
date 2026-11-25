const fs = require('fs')
const path = require('path')
const zlib = require('zlib')

function crc32(buffer) {
  let crc = 0xffffffff
  for (let i = 0; i < buffer.length; i++) {
    crc ^= buffer[i]
    for (let j = 0; j < 8; j++) crc = (crc >>> 1) ^ (0xedb88320 & -(crc & 1))
  }
  return (crc ^ 0xffffffff) >>> 0
}
function chunk(type, data) {
  const name = Buffer.from(type)
  const size = Buffer.alloc(4)
  size.writeUInt32BE(data.length, 0)
  const checksum = Buffer.alloc(4)
  checksum.writeUInt32BE(crc32(Buffer.concat([name, data])), 0)
  return Buffer.concat([size, name, data, checksum])
}
const size = 192
const rows = []
for (let y = 0; y < size; y++) {
  const row = Buffer.alloc(1 + size * 4)
  row[0] = 0
  for (let x = 0; x < size; x++) {
    const offset = 1 + x * 4
    const inside = (x > 34 && x < 158 && y > 28 && y < 164)
    const accent = (x > 54 && x < 138 && y > 48 && y < 144)
    row[offset] = accent ? 196 : (inside ? 69 : 24)
    row[offset + 1] = accent ? 201 : (inside ? 76 : 29)
    row[offset + 2] = accent ? 255 : (inside ? 112 : 43)
    row[offset + 3] = 255
  }
  rows.push(row)
}
const ihdr = Buffer.alloc(13)
ihdr.writeUInt32BE(size, 0)
ihdr.writeUInt32BE(size, 4)
ihdr[8] = 8
ihdr[9] = 6
const png = Buffer.concat([
  Buffer.from([137,80,78,71,13,10,26,10]),
  chunk('IHDR', ihdr),
  chunk('IDAT', zlib.deflateSync(Buffer.concat(rows))),
  chunk('IEND', Buffer.alloc(0))
])
const target = path.join(__dirname, '..', 'src', 'common', 'icon.png')
fs.mkdirSync(path.dirname(target), { recursive: true })
fs.writeFileSync(target, png)
console.log('Generated Gallery Mini icon:', target)
