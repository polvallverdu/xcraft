"""Print the CI matrix from settings.gradle.kts: one entry per Minecraft version, listing its loader projects.

Each job runs with -Pxcraft.versions=<version>; tasks are addressed per project so the always-included
VCS project (1.21.1-fabric) is not built again by every job.
"""
import json
import re
from pathlib import Path

settings = (Path(__file__).resolve().parents[2] / "settings.gradle.kts").read_text()
matrix = []
for version, loaders in re.findall(r'^\s*match\("([^"]+)", ([^)]*)\)', settings, re.M):
    projects = [f"{version}-{loader}" for loader in re.findall(r'"([a-z]+)"', loaders)]
    matrix.append({"version": version, "projects": " ".join(projects)})
print(json.dumps(matrix))
