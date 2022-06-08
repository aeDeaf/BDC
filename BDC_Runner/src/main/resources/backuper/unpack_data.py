import os
import subprocess


def process_archive(filename):
    subprocess.check_output(['tar', '-zxvf', filename, 'folderName.txt'])
    with open('folderName.txt', 'r') as f:
        folder_name = f.read()
        subprocess.check_output(['tar', '-zxvf', filename, '-C', folder_name])
    os.remove(folder_name + '/folderName.txt')
    os.remove(filename)


os.chdir('/tmp')
archives = list(filter(lambda i: i.startswith('backup'), os.listdir()))
for archive in archives:
    print('Processing', archive)
    process_archive(archive)
