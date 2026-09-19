#!/usr/bin/env python3
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
required = {
    "offline fallback": ROOT / "app/src/main/java/de/wea200675/lia/core/SafeOfflineRuntime.kt",
    "model verification": ROOT / "app/src/main/java/de/wea200675/lia/core/ModelVerifier.kt",
    "bounded recovery": ROOT / "app/src/main/java/de/wea200675/lia/core/RuntimeCoordinator.kt",
    "encrypted learning": ROOT / "app/src/main/java/de/wea200675/lia/core/LearningEvolutionStore.kt",
}
missing = [name for name, path in required.items() if not path.is_file()]
if missing:
    print("Missing safety components:", ", ".join(missing))
    sys.exit(1)
runtime = (ROOT / "app/src/main/java/de/wea200675/lia/core/ResilientLocalRuntime.kt").read_text()
if "SafeOfflineRuntime" not in runtime or "consumeNativeFailure" not in runtime:
    print("Runtime fallback/recovery boundary missing")
    sys.exit(1)
workflow = (ROOT / ".github/workflows/android.yml").read_text()
if "testDebugUnitTest" not in workflow or "assembleDebug" not in workflow or "upload-artifact" not in workflow:
    print("CI must test, build APK, and upload artifact")
    sys.exit(1)
print("Safety invariants: PASS")
