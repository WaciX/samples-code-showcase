from pathlib import Path
import tempfile


def create_temp_dir() -> Path:
    return Path(tempfile.mkdtemp(dir=get_root_dir()))


def get_root_dir():
    return '/app/tmp'
