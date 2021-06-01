log = new File(basedir, 'build.log')
sorted1 = new File(basedir, 'src/main/resources/wolfMain.graphqls')
expected1 = new File(basedir, 'wolfMain_expected.graphqls')
backup1 = new File(basedir, 'src/main/resources/wolfMain.graphqls.bak')

sorted2 = new File(basedir, 'src/main/resources/wolfAdd.graphqls')
expected2 = new File(basedir, 'wolfAdd_expected.graphqls')
backup2 = new File(basedir, 'src/main/resources/wolfAdd.graphqls.bak')

assert log.exists()
assert log.text.contains('Sorting file ' + sorted1.absolutePath)
assert log.text.contains('Saved backup of ' + sorted1.absolutePath + ' to ' + backup1.absolutePath)
assert log.text.contains('Saved sorted schema file to ' + sorted1.absolutePath)
assert backup1.exists()
assert expected1.text.tokenize('\n').equals(sorted1.text.replaceAll('\r','').tokenize('\n'))

assert log.text.contains('Sorting file ' + sorted2.absolutePath)
assert log.text.contains('Saved backup of ' + sorted2.absolutePath + ' to ' + backup2.absolutePath)
assert log.text.contains('Saved sorted schema file to ' + sorted2.absolutePath)
assert backup2.exists()
assert expected2.text.tokenize('\n').equals(sorted2.text.replaceAll('\r','').tokenize('\n'))

return true
