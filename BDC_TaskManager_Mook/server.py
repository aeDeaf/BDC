from flask import Flask, jsonify, request

app = Flask(__name__)

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


if __name__ == '__main__':
    app.run('0.0.0.0', 8001)
