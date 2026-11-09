"""Check Android translation coverage and formatting placeholders without dependencies."""
import re
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1] / 'android/app/src/main/res'
def read(folder):
    nodes = list(ET.parse(root / folder / 'strings.xml').getroot())
    values = {node.attrib['name']: ''.join(node.itertext()) for node in nodes}
    assert len(nodes) == len(values), f'Duplicate resource in {folder}'
    return values

en, pl = read('values'), read('values-pl')
assert en.keys() == pl.keys(), f'Missing keys: {en.keys() ^ pl.keys()}'
for key in en:
    assert re.findall(r'%\d+\$[ds]', en[key]) == re.findall(r'%\d+\$[ds]', pl[key]), key
    assert en[key].strip() and pl[key].strip(), key
print(f'OK: {len(en)} matching PL/EN resources and format arguments.')
