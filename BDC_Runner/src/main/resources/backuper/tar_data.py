import json
import os
import subprocess


def filter_files(backup_file, excluded):
    if backup_file in excluded:
        return False
    if backup_file.startswith('.') or backup_file.startswith('backup'):
        return False
    if (backup_file == 'tar_data.py') or (backup_file == 'init.sh') or (backup_file == 'backup_data.json'):
        return False
    return True


with open('backup_data.json', 'r') as f:
    backup_data = json.load(f)

index = backup_data['index']
backup_folder = backup_data['backupFolder']
excluded_files = backup_data['excludedFiles']
os.chdir(backup_folder)

with open('folderName.txt', 'w') as f:
    f.write(backup_folder)

total_files = os.listdir()
backup_files = filter(lambda i: filter_files(i, excluded_files), total_files)

tar_command = 'tar czvf /home/dockerx/backup_{0}.tar.gz'.format(index)
for file in backup_files:
    tar_command += ' ' + file

print(tar_command)

subprocess.check_output(tar_command, shell=True)
