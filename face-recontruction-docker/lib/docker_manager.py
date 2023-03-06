import subprocess


def docker_build(path, image_name):
    subprocess.run(["docker", "build",
                    '-f', path,
                    '-t', get_full_image_name(image_name),
                    '--progress', 'plain',
                    '.'],
                   check=True)


def docker_run(image_name, mount_directories: list = None):
    cmd = ["docker", "run",
           '--rm',
           '--name', f'{image_name}',
           '-v', '/var/run/docker.sock:/var/run/docker.sock',
           '--gpus', 'all']
    if mount_directories is not None:
        for mount_directory in mount_directories:
            cmd.append('-v')
            cmd.append(f'{mount_directory[0]}:{mount_directory[1]}')
    cmd.append(get_full_image_name(image_name))

    print(f'Executing {cmd}')

    proc = subprocess.run(cmd, capture_output=True, text=True)
    out = str(proc.stdout) if proc.stdout is not None else None
    err = str(proc.stderr) if proc.stderr is not None else None

    print(f'Process finished ret code {proc.returncode} stdout {out} stderr {err}')

    proc.check_returncode()

    return out, err


def get_full_image_name(image_name: str):
    return f'localhost/for_kat_{image_name.lower()}:latest'
