from flask import Flask, jsonify, request
from flask_cors import CORS, cross_origin

app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

command_queue = []


@app.route('/command', methods=['POST'])
def add_command():
    global command_queue
    command = request.json
    command_queue.append(command)
    return 'OK'


@app.route('/command', methods=['GET'])
def get_command():
    global command_queue
    if len(command_queue) > 0:
        command = command_queue[0]
        command_queue = command_queue[1:]
        return jsonify(command)
    else:
        return jsonify({})


@app.route('/status', methods=['POST'])
def show_status():
    res = request.json
    print(res)
    return jsonify({'message': 'message'})


@app.route('/frontend/containers', methods=['GET'])
@cross_origin()
def get_container_infos():
    response = {
        "dockerContainerInfos": [
            {
                "containerId": "726f5289afd9",
                "image": "git.jinr.ru:5005/nica/docker-images/bmn:latest",
                "status": "EXITED",
                "name": "bmnroot3"
            },
            {
                "containerId": "01040c7ba99a",
                "image": "git.jinr.ru:5005/nica/docker-images/bmn:latest",
                "status": "EXITED",
                "name": "bmnroot4"
            }
        ]
    }
    return jsonify(response)


if __name__ == '__main__':
    app.run('0.0.0.0', 8090)
