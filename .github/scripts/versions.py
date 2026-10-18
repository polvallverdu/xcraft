"""Print the CI matrix: one entry per Minecraft version, for the main build and the legacy/ build.

Each entry has:
- dir: the Gradle build ("." or "legacy").
- version / projects: jobs run with -Pxcraft.versions=<version> and address tasks per project, so the
  always-included VCS project is not built again by every job.
- tests: "<loader>:<primary mc>:<mc>" for every Minecraft version each jar is published for (its own plus
  hotfixes); smoke_prod.py boots a real server for each.
"""
import json
import re
import tomllib
from pathlib import Path

root = Path(__file__).resolve().parents[2]
matrix = []

# Main build: Mojang-mapped Minecraft (1.16.5+)
settings = (root / "settings.gradle.kts").read_text()
props = tomllib.loads((root / "stonecutter.properties.toml").read_text())
for version, loaders in re.findall(r'^\s*match\("([^"]+)", ([^)]*)\)', settings, re.M):
    loaders = re.findall(r'"([a-z]+)"', loaders)
    tests = []
    for loader in loaders:
        extra = props.get(loader, {}).get(version, {}).get("publish", {}).get("additionalVersions", [])
        tests += [f"{loader}:{version}:{mc}" for mc in [version, *extra]]
    matrix.append({"dir": ".", "version": version,
                   "projects": " ".join(f"{version}-{l}" for l in loaders), "tests": " ".join(tests)})

# Legacy build: Legacy Fabric (1.7.10 - 1.13.2); project name == Minecraft version
legacy_settings = (root / "legacy" / "settings.gradle.kts").read_text()
legacy_props = dict(
    line.split("=", 1) for line in (root / "legacy" / "gradle.properties").read_text().splitlines()
    if "=" in line and not line.startswith("#"))
versions = re.findall(r'"(1\.\d+(?:\.\d+)?)"', re.search(r"listOf\(([^)]*)\)", legacy_settings).group(1))
for version in versions:
    hotfixes = [v for v in legacy_props.get(f"hotfixes.{version}", "").split(",") if v]
    tests = [f"legacy-fabric:{version}:{mc}" for mc in [version, *hotfixes]]
    matrix.append({"dir": "legacy", "version": version, "projects": version, "tests": " ".join(tests)})

print(json.dumps(matrix))
