"""Print the CI matrix from settings.gradle.kts and stonecutter.properties.toml.

One entry per Minecraft version with:
- projects: its loader projects. Jobs run with -Pxcraft.versions=<version> and address tasks per project,
  so the always-included VCS project (1.21.1-fabric) is not built again by every job.
- tests: "<loader>:<primary mc>:<mc>" for every Minecraft version each jar is published for
  (its own plus publish.additionalVersions); smoke_prod.py boots a real server for each.
"""
import json
import re
import tomllib
from pathlib import Path

root = Path(__file__).resolve().parents[2]
settings = (root / "settings.gradle.kts").read_text()
props = tomllib.loads((root / "stonecutter.properties.toml").read_text())

matrix = []
for version, loaders in re.findall(r'^\s*match\("([^"]+)", ([^)]*)\)', settings, re.M):
    loaders = re.findall(r'"([a-z]+)"', loaders)
    tests = []
    for loader in loaders:
        extra = props.get(loader, {}).get(version, {}).get("publish", {}).get("additionalVersions", [])
        tests += [f"{loader}:{version}:{mc}" for mc in [version, *extra]]
    matrix.append({
        "version": version,
        "projects": " ".join(f"{version}-{l}" for l in loaders),
        "tests": " ".join(tests),
    })
print(json.dumps(matrix))
