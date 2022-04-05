import time
import requests

while True:
    response = requests.request('GET', 'http://localhost:9090/command')
    if response.text != '':
        commandMessage = response.json()
        print(commandMessage['command'])
        responseMessage = {
            'initialMessageTimestamp': commandMessage['sendTimestamp'],
            'sendTimestamp': 10,
            'result': 'ABOBA'
        }
        requests.request('POST', 'http://localhost:9090/command', json=responseMessage)
        time.sleep(5)
