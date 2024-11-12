import os
import signal


class ProcessAlreadyRunningError(Exception):
    def __init__(self, message, pid=None):
        self.message = message
        self.pid = pid

    def __str__(self):
        return f'{self.message} PID: {self.pid}'


class InvalidPidFileException(Exception):
    def __init__(self, message, pid_file_path=None):
        self.message = message
        self.pid_file_path = pid_file_path

    def __str__(self):
        return f'{self.message} File path: {self.pid_file_path}'


class PidFile(object):

    def __init__(self, path, logger):
        self.path = path
        self.logger = logger
        self.file_exists = os.path.exists(self.path)
        self.pid = None
        if self.file_exists:
            with open(self.path, 'r') as f:
                try:
                    self.pid = int(f.read())
                except Exception:
                    raise InvalidPidFileException("Failed to read PID from file!", self.path)

    def __enter__(self):
        if self.file_exists:
            raise ProcessAlreadyRunningError(f'Existing PID-file with indicates process is already running!',
                                             pid=self.pid)
        self.pid = os.getpid()
        with open(self.path, 'w+') as f:
            f.write(f'{self.pid}\n')

        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self._remove()

    def _remove(self):
        if os.path.exists(self.path):
            os.remove(self.path)

    def kill(self):
        if self.pid is not None and self.pid != "":
            try:
                os.kill(self.pid, signal.SIGTERM)  # or SIGKILL
            except ProcessLookupError:
                self.logger.warn(f'Process {self.pid} of PID-file {self.path} is not running and cannot be killed!')
        self._remove()
        return self.pid


def kill_process(pid_file_path, logger):
    pid_file = PidFile(pid_file_path, logger)
    return pid_file.kill()
