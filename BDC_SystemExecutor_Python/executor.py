import subprocess
import time

import requests

while True:
    response = requests.request('GET', 'http://localhost:9090/command')
    if response.text != '':
        commandMessage = response.json()
        command = commandMessage['command']
        print(command)
        result = subprocess.check_output(command, shell=True).decode('utf-8')
        responseMessage = {
            'initialMessageTimestamp': commandMessage['sendTimestamp'],
            'sendTimestamp': 10,
            'result': result
        }
        requests.request('POST', 'http://localhost:9090/command', json=responseMessage)
        time.sleep(5)
