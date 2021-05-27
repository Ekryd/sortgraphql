log = new File(basedir, 'build.log')
sorted = new File(basedir, 'src/main/resources/schema.graphqls')
expected = new File(basedir, 'expected.graphqls')
backup = new File(basedir, 'src/main/resources/schema.graphqls.bak')

assert log.exists()
assert log.text.contains('Sorting file ' + sorted.absolutePath)
assert log.text.contains('Saved backup of ' + sorted.absolutePath + ' to ' + backup.absolutePath)
assert log.text.contains('Saved sorted schema file to ' + sorted.absolutePath)
assert backup.exists()
assert expected.text.tokenize('\n').equals(sorted.text.replaceAll('\r','').tokenize('\n'))

return true
