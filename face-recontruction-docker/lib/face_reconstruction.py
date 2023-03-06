import os

from pathlib import Path

from typing import List

from lib.docker_manager import docker_build, docker_run
from lib.files_manager import create_temp_dir


class ImageFileResult:
    def __init__(self, file_name, old_file, new_file):
        self._file_name = file_name
        self._new_file = new_file
        self._old_file = old_file

    @property
    def file_name(self) -> str:
        return self._file_name

    @property
    def old_file(self) -> Path:
        return self._old_file

    @property
    def new_file(self) -> Path:
        return self._new_file


class FaceReconstructionResults:
    def __init__(self, files: List[ImageFileResult], out, err):
        self._files = files
        self._std_out = out
        self._std_err = err

    @property
    def files(self) -> List[ImageFileResult]:
        return self._files

    @property
    def std_out(self) -> str:
        return self._std_out

    @property
    def std_err(self) -> str:
        return self._std_err

    def have_std_err(self) -> bool:
        return self._std_err is not None and len(str(self._std_err)) > 0


class FaceReconstruction:
    def __init__(self):
        docker_build('lib/face_reconstruction.Dockerfile', 'face_reconstruction')
        dir_path = create_temp_dir()
        self.inputs = dir_path.joinpath('inputs')
        self.outputs = dir_path.joinpath('outputs')
        self.inputs.mkdir()
        self.outputs.mkdir()
        self.processed_files = []

    def copy_file_to_inputs(self, data: bytearray, file_name: str):
        file = self.inputs.joinpath(file_name)
        if file.exists():
            os.remove(file)
        file.touch()
        file.write_bytes(data)

        old_file = self.inputs.joinpath(file_name).absolute()
        new_file = self.outputs.joinpath(file_name).absolute()
        self.processed_files.append(ImageFileResult(file_name, old_file, new_file))

    def run(self):
        out, err = docker_run('face_reconstruction', [
            (self.inputs.absolute(), '/app/inputs'),
            (self.outputs.absolute(), '/app/outputs')
        ])

        from os import walk

        for (dirpath, dirnames, filenames) in walk(self.outputs.absolute()):
            print(dirpath)
            print(dirnames)
            print(filenames)

        return FaceReconstructionResults(self.processed_files, out, err)
